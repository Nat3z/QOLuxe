package com.nat3z.qoluxe.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


object ChatUtils {
    var localKeys: String? = null
    var otherKeys: Map<Long, String> = mapOf()

    fun createKeys(): String {
        // Choose the algorithm for key generation, such as "AES"
        // Choose the algorithm for key generation, such as "AES"
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance("AES")

        keyGenerator.init(256)
        val secretKey: SecretKey = keyGenerator.generateKey()
        val encodedKey = secretKey.encoded
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    fun rawStringToKey(rawString: String?): ByteArray? {
        return Base64.getDecoder().decode(rawString)
    }


    @Throws(Exception::class)
    fun encrypt(plaintext: ByteArray?, key: ByteArray?): ByteArray? {
        val secretKeySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        return cipher.doFinal(plaintext)
    }

    fun sendEncryptedMessage(message: String, key: String) {
        val encryptedMessage = encryptMessage(message, key)
        if (encryptedMessage.length > 256) {
            MinecraftClient.getInstance().player!!.sendMessage(Text.of(Formatting.RED.toString() + "The message you sent was too long to be encrypted and was sent as raw text."))
            MinecraftClient.getInstance().networkHandler!!.sendChatMessage(message)
        }
        MinecraftClient.getInstance().networkHandler!!.sendChatMessage(".$encryptedMessage")
    }

    @Throws(Exception::class)
    fun decrypt(ciphertext: ByteArray?, key: ByteArray?): ByteArray? {
        val secretKeySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        return cipher.doFinal(ciphertext)
    }
    fun decryptMessage(encryptedMessage: String, key: String): String {
        // turn the string key into a byte array and pass it to the decrypt method to turn it into a string
        return String(decrypt(Base64.getDecoder().decode(encryptedMessage), rawStringToKey(key)!!)!!, Charsets.UTF_8)
    }

    fun encryptMessage(message: String, key: String): String {
        return Base64.getEncoder().encodeToString(encrypt(message.toByteArray(), rawStringToKey(key)!!))
    }
}
