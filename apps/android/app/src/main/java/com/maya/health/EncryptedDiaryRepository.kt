package com.maya.health

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Minimal encrypted at-rest H1 diary, AndroidKeyStore-backed AES-256/GCM.
 * No network permission, automatic cloud backup disabled, file in noBackupFilesDir.
 * Corrupted/inaccessible encrypted data fails closed: do not silently overwrite.
 */
class EncryptedDiaryRepository(context: Context) {
    private val storageFile = File(context.noBackupFilesDir, "food_diary_v1.enc")
    private val atomic = AtomicFile(storageFile)

    fun load(): List<FoodEntry> {
        if (!storageFile.exists() && !File(storageFile.path + ".bak").exists()) return emptyList()
        val encrypted = atomic.openRead().use { it.readBytes() }
        if (encrypted.size > MAX_FILE_SIZE) throw IOException("Encrypted diary too large")
        val payload = DataInputStream(encrypted.inputStream()).use { input ->
            val version = input.readInt()
            if (version != FORMAT_VERSION) throw IOException("Unsupported diary version")
            val ivLen = input.readInt()
            if (ivLen != 12) throw IOException("Invalid encryption metadata")
            val iv = ByteArray(ivLen)
            input.readFully(iv)
            val remaining = input.available()
            if (remaining < 16 || remaining > MAX_FILE_SIZE) throw IOException("Invalid ciphertext")
            val cipherBytes = ByteArray(remaining)
            input.readFully(cipherBytes)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            cipher.doFinal(cipherBytes)
        }
        val root = JSONObject(String(payload, StandardCharsets.UTF_8))
        if (root.getInt("schemaVersion") != 1) throw IOException("Unsupported data schema")
        val arr = root.getJSONArray("entries")
        if (arr.length() > 10000) throw IOException("Diary limit exceeded")
        val entries = mutableListOf<FoodEntry>()
        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            val grams = if (item.isNull("portionGrams")) null else item.getDouble("portionGrams")
            val entry = FoodEntry(
                id = item.getString("id"),
                foodName = item.getString("foodName"),
                energyKcal = item.getDouble("energyKcal"),
                portionGrams = grams,
                loggedAtEpochMs = item.getLong("loggedAtEpochMs"),
                source = item.getString("source")
            )
            if (entry.id.isBlank() || !FoodMath.isValid(
                entry.foodName, entry.energyKcal, entry.portionGrams,
                entry.portionGrams?.toString() ?: ""
            ) || entry.source != "USER_ENTERED") throw IOException("Invalid diary entry")
            entries.add(entry)
        }
        return entries
    }

    fun save(entries: List<FoodEntry>) {
        if (entries.size > 10000) throw IOException("Diary limit exceeded")
        val arr = JSONArray()
        entries.forEach { entry ->
            require(entry.id.isNotBlank() && FoodMath.isValid(
                entry.foodName, entry.energyKcal, entry.portionGrams,
                entry.portionGrams?.toString() ?: ""
            )) { "Invalid diary entry" }
            arr.put(JSONObject().apply {
                put("id", entry.id)
                put("foodName", entry.foodName)
                put("energyKcal", entry.energyKcal)
                put("portionGrams", entry.portionGrams ?: JSONObject.NULL)
                put("loggedAtEpochMs", entry.loggedAtEpochMs)
                put("source", "USER_ENTERED")
            })
        }
        val clear = JSONObject().put("schemaVersion", 1).put("entries", arr)
            .toString().toByteArray(StandardCharsets.UTF_8)
        if (clear.size > MAX_FILE_SIZE - 64) throw IOException("Diary too large")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val ciphertext = cipher.doFinal(clear)
        val output = ByteArrayOutputStream()
        DataOutputStream(output).use {
            it.writeInt(FORMAT_VERSION)
            it.writeInt(cipher.iv.size)
            it.write(cipher.iv)
            it.write(ciphertext)
        }
        var stream: java.io.FileOutputStream? = null
        try {
            stream = atomic.startWrite()
            stream.write(output.toByteArray())
            atomic.finishWrite(stream)
        } catch (failure: Exception) {
            if (stream != null) atomic.failWrite(stream)
            throw failure
        }
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val found = keyStore.getKey(KEY_ALIAS, null)
        if (found is SecretKey) return found
        // If a ciphertext exists but the key is lost, never create a replacement key.
        if (storageFile.exists() || File(storageFile.path + ".bak").exists()) {
            throw IOException("Encrypted diary key missing: preserve the file for recovery")
        }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        private const val KEY_ALIAS = "maya_health_diary_v1"
        private const val FORMAT_VERSION = 1
        private const val MAX_FILE_SIZE = 5_000_000
    }
}
