package com.maya.health

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * A password-protected, portable diary export.
 * Format: ASCII MHBU (4) | schema version (1) | salt (16) | iv (12) | AES-256/GCM ciphertext+tag.
 * The exported key is derived from a user password, never from device Keystore.
 * Requires a strong user passphrase. The code intentionally does not store or log it.
 */
object PortableDiaryBackup {
    private const val VERSION: Byte = 1
    private val MAGIC = "MHBU".toByteArray(StandardCharsets.US_ASCII)
    private val AAD = "maya-health-backup-v1".toByteArray(StandardCharsets.US_ASCII)
    private const val SALT_LENGTH = 16
    private const val NONCE_LENGTH = 12
    private const val ITERATIONS = 210_000
    const val MAX_BACKUP_BYTES = 6_000_000
    private const val MAX_PLAIN_BYTES = 5_000_000
    private const val HEADER_LENGTH = 4 + 1 + SALT_LENGTH + NONCE_LENGTH

    fun encrypt(plain: ByteArray, password: CharArray): ByteArray {
        require(password.size in 12..256) { "Passphrase must be 12–256 characters" }
        require(plain.size <= MAX_PLAIN_BYTES) { "Backup too large" }
        val rng = SecureRandom()
        val salt = ByteArray(SALT_LENGTH).also { rng.nextBytes(it) }
        val nonce = ByteArray(NONCE_LENGTH).also { rng.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(128, nonce))
        cipher.updateAAD(AAD)
        val encrypted = cipher.doFinal(plain)
        return ByteBuffer.allocate(HEADER_LENGTH + encrypted.size).apply {
            put(MAGIC); put(VERSION); put(salt); put(nonce); put(encrypted)
        }.array()
    }

    fun decrypt(input: ByteArray, password: CharArray): ByteArray {
        require(password.isNotEmpty()) { "Password required" }
        if (input.size <= HEADER_LENGTH + 16 || input.size > MAX_BACKUP_BYTES) {
            throw IOException("Invalid encrypted archive size")
        }
        val buffer = ByteBuffer.wrap(input)
        val magic = ByteArray(4).also { buffer.get(it) }
        if (!magic.contentEquals(MAGIC) || buffer.get() != VERSION) {
            throw IOException("Unsupported archive")
        }
        val salt = ByteArray(SALT_LENGTH).also { buffer.get(it) }
        val nonce = ByteArray(NONCE_LENGTH).also { buffer.get(it) }
        val encrypted = ByteArray(buffer.remaining()).also { buffer.get(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(128, nonce))
        cipher.updateAAD(AAD)
        val plain = try {
            cipher.doFinal(encrypted)
        } catch (failure: AEADBadTagException) {
            throw IOException("Incorrect passphrase or modified archive", failure)
        }
        if (plain.size > MAX_PLAIN_BYTES) throw IOException("Archive payload too large")
        return plain
    }

    fun readLimited(stream: InputStream): ByteArray {
        val buffer = ByteArray(8192)
        val out = ByteArrayOutputStream()
        while (true) {
            val n = stream.read(buffer)
            if (n < 0) break
            if (out.size() + n > MAX_BACKUP_BYTES) throw IOException("Archive too large")
            out.write(buffer, 0, n)
        }
        return out.toByteArray()
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, 256)
        return try {
            val raw = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
            try {
                SecretKeySpec(raw, "AES")
            } finally {
                raw.fill(0)
            }
        } finally {
            spec.clearPassword()
        }
    }
}
