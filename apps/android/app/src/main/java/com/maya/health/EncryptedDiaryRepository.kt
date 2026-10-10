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
        return DiaryJsonCodec.decode(payload)
    }

    fun save(entries: List<FoodEntry>) {
        val clear = DiaryJsonCodec.encode(entries)
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

    /**
     * Purge local journal and Keystore key. Backups exported by SAF are external:
     * the user must separately delete copies from their chosen storage.
     */
    fun deleteAll() {
        atomic.delete()
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
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
