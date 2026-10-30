package com.example.data

import android.util.Base64
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {
    private const val DEFAULT_PASSPHRASE = "EAS_SECURE_OFFLINE_JOB_CARD_BACKUP_2026_MASTER"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12
    private const val SALT_LENGTH = 16

    fun encrypt(
        plaintext: String,
        customPassword: String? = null,
        metadata: Map<String, Any> = emptyMap()
    ): String {
        val passphrase = if (!customPassword.isNullOrBlank()) customPassword else DEFAULT_PASSPHRASE
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)

        val iv = ByteArray(IV_LENGTH)
        random.nextBytes(iv)

        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(keyFactory.generateSecret(keySpec).encoded, "AES")
        keySpec.clearPassword()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        val json = JSONObject().apply {
            put("type", "EAS_ENCRYPTED_BACKUP")
            put("version", 1)
            put("algorithm", "AES-256-GCM")
            put("kdf", "PBKDF2WithHmacSHA256")
            put("iterations", ITERATIONS)
            put("timestamp", System.currentTimeMillis())
            put("hasCustomPassword", !customPassword.isNullOrBlank())
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("ciphertext", Base64.encodeToString(ciphertext, Base64.NO_WRAP))

            val metaObj = JSONObject()
            metadata.forEach { (k, v) -> metaObj.put(k, v) }
            put("metadata", metaObj)
        }

        return json.toString(2)
    }

    fun isEncryptedBackup(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            root.optString("type") == "EAS_ENCRYPTED_BACKUP" && root.has("ciphertext")
        } catch (e: Exception) {
            false
        }
    }

    fun getEncryptedMetadata(jsonString: String): Map<String, Any>? {
        return try {
            val root = JSONObject(jsonString)
            if (root.optString("type") != "EAS_ENCRYPTED_BACKUP") return null
            val meta = root.optJSONObject("metadata") ?: return emptyMap()
            val map = mutableMapOf<String, Any>()
            val keys = meta.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = meta.get(key)
            }
            map
        } catch (e: Exception) {
            null
        }
    }

    fun decrypt(jsonString: String, customPassword: String? = null): String {
        val root = JSONObject(jsonString)
        val hasCustomPassword = root.optBoolean("hasCustomPassword", false)
        val passphrase = if (hasCustomPassword) {
            if (customPassword.isNullOrBlank()) {
                throw IllegalArgumentException("This backup is encrypted with a custom password. Please enter the password to restore.")
            }
            customPassword
        } else {
            if (!customPassword.isNullOrBlank()) customPassword else DEFAULT_PASSPHRASE
        }

        val salt = Base64.decode(root.getString("salt"), Base64.DEFAULT)
        val iv = Base64.decode(root.getString("iv"), Base64.DEFAULT)
        val ciphertext = Base64.decode(root.getString("ciphertext"), Base64.DEFAULT)
        val iterations = root.optInt("iterations", ITERATIONS).coerceIn(1000, 250_000)

        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, iterations, KEY_LENGTH)
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(keyFactory.generateSecret(keySpec).encoded, "AES")
        keySpec.clearPassword()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        val decryptedBytes = cipher.doFinal(ciphertext)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
