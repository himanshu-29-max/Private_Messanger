package com.example.security

import java.security.MessageDigest
import kotlin.random.Random

object CryptoEngine {

    /**
     * Compute SHA-256 hash of a string to simulate safe PIN hashing of standard Argon2.
     */
    fun sha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Fallback safe hash
            input.hashCode().toString(16)
        }
    }

    /**
     * Simulate E2E message encryption using a simple printable Cipher model.
     * In absolute local storage, the database saves this cipher block.
     */
    fun encrypt(plainText: String, secretKey: String = "FORTRESS_DEFAULT_SEED_KEY"): String {
        if (plainText.isEmpty()) return ""
        return try {
            val salt = secretKey.hashCode() % 12
            val bytes = plainText.toByteArray(Charsets.UTF_8)
            val shifted = bytes.map { (it.toInt() + salt).toByte() }.toByteArray()
            android.util.Base64.encodeToString(shifted, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            "CIPHER_BLK[${plainText.hashCode()}]"
        }
    }

    /**
     * Simulate E2E message decryption.
     */
    fun decrypt(cipherText: String, secretKey: String = "FORTRESS_DEFAULT_SEED_KEY"): String {
        if (cipherText.isEmpty()) return ""
        if (!cipherText.endsWith("=") && cipherText.length % 4 != 0) {
            return cipherText // Not dynamic cipher
        }
        return try {
            val salt = secretKey.hashCode() % 12
            val decoded = android.util.Base64.decode(cipherText, android.util.Base64.NO_WRAP)
            val unshifted = decoded.map { (it.toInt() - salt).toByte() }.toByteArray()
            String(unshifted, Charsets.UTF_8)
        } catch (e: Exception) {
            "[" + cipherText.take(12) + "... ENCRYPTED]"
        }
    }

    /**
     * Generate the Security Verification Fingerprint (derived from display IDs).
     * Used for E2E Call verification matching. e.g., "ED92 - F3BB - A19C - 881F"
     */
    fun generateFingerprint(id1: String, id2: String): String {
        val combined = if (id1 < id2) id1 + id2 else id2 + id1
        val hash = sha256(combined + "E2EE_CALL_FINGERPRINT_SALT")
        if (hash.length >= 16) {
            val block1 = hash.substring(0, 4).uppercase()
            val block2 = hash.substring(4, 8).uppercase()
            val block3 = hash.substring(8, 12).uppercase()
            val block4 = hash.substring(12, 16).uppercase()
            return "$block1 • $block2 • $block3 • $block4"
        }
        return "9E31 • 4D8F • AA12 • FF83"
    }

    /**
     * Generate a random Security Verification Fingeprint for a solo/private session.
     */
    fun generateRandomFingerprint(): String {
        val symbols = listOf("🔒", "🛡️", "🔑", "🦊", "🦅", "🔋", "💾", "💻", "🛰️", "🤖")
        val randomIdx1 = Random.nextInt(symbols.size)
        val randomIdx2 = Random.nextInt(symbols.size)
        val randomIdx3 = Random.nextInt(symbols.size)
        
        val hexSymbols = (1..4).map { 
            Random.nextInt(0x1000, 0xFFFF).toString(16).uppercase()
        }
        
        return "${symbols[randomIdx1]} ${hexSymbols[0]} • ${hexSymbols[1]} ${symbols[randomIdx2]} • ${hexSymbols[2]} • ${hexSymbols[3]} ${symbols[randomIdx3]}"
    }

    /**
     * Simulates WebRTC SDP Session fingerprints.
     */
    fun generateSdpFingerprint(): String {
        val digest = "SHA-256"
        val chars = "0123456789ABCDEF"
        val builder = java.lang.StringBuilder()
        builder.append("$digest ")
        for (i in 0..11) {
            builder.append(chars[Random.nextInt(16)])
            builder.append(chars[Random.nextInt(16)])
            if (i < 11) builder.append(":")
        }
        return builder.toString()
    }
}
