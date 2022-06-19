package com.lokile.encrypter.encrypters

import android.util.Base64
import java.nio.ByteBuffer
import javax.crypto.Cipher

data class EncryptedData(val data: ByteArray, val iv: ByteArray) {
    fun toByteArray(): ByteArray {
        return ByteBuffer.allocate(iv.size + data.size + 1)
            .apply {
                put(iv.size.toByte())
                put(iv)
                put(data)
            }.array()
    }

    fun toStringData() = Base64.encodeToString(toByteArray(), Base64.DEFAULT)
}

interface IEncrypter {
    fun encrypt(data: ByteArray, useRandomizeIv: Boolean = true): EncryptedData?
    fun encrypt(data: String, useRandomizeIv: Boolean = true): String?

    fun decrypt(data: EncryptedData): ByteArray?
    fun decrypt(data: ByteArray): ByteArray?
    fun decrypt(data: String): String?

    fun getEncryptCipher(useRandomizeIv: Boolean = true): Cipher
    fun getDecryptCipher(iv: ByteArray): Cipher
    fun resetKeys(): Boolean
}