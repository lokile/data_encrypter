package com.lokile.encrypter.encrypters.imp

import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.core.content.edit
import com.lokile.encrypter.encrypters.EncryptedData
import com.lokile.encrypter.encrypters.IEncrypter
import com.lokile.encrypter.secretKeyProviders.ISecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.imp.RSASecretKeyProvider
import java.nio.ByteBuffer
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

abstract class BaseEncrypter : IEncrypter {
    protected lateinit var algorithm: String
    protected val cipher by lazy { Cipher.getInstance(algorithm) }
    protected var keyProvider: ISecretKeyProvider
    protected var app: Context
    protected var fixedIv: ByteArray? = null
    protected lateinit var secretKey: Key
    private val prefName = "xfhw9LYsjwSaR4cfAakQhVFn1"
    private val savedIvKeyWord = "352VZrcCFprRHWZ8cg9DvytRb"

    constructor(context: Context, keyProvider: ISecretKeyProvider, algorithm: String) {
        app = context.applicationContext
        this.keyProvider = keyProvider
        this.algorithm = algorithm
        loadKey()
    }

    constructor(context: Context, alias: String, algorithm: String) {
        app = context.applicationContext
        this.keyProvider = loadDefaultKeyProvider(alias)
        this.algorithm = algorithm
        loadKey()
    }

    protected fun initCipher(mode: Int, iv: ByteArray? = null) {
        if (iv != null) {
            cipher.init(
                mode, secretKey, IvParameterSpec(iv)
            )
        } else {
            cipher.init(
                mode, secretKey
            )
        }
    }

    protected fun loadDefaultKeyProvider(alias: String): ISecretKeyProvider {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AESSecretKeyProvider(alias)
        } else {
            RSASecretKeyProvider(app, alias)
        }
    }

    protected fun loadKey() {
        secretKey =
            keyProvider.getSecretKey() ?: throw Exception("Error when loading the secretKey")
    }

    protected fun saveFixedIv(iv: ByteArray, useRandomizeIv: Boolean) {
        if (this.keyProvider.getIv() != null || useRandomizeIv || this.fixedIv != null) {
            return
        }
        if (keyProvider.getAlias().isEmpty()) {
            throw Exception("Alias is empty")
        }
        this.fixedIv = iv
        initCipher(Cipher.ENCRYPT_MODE)
        val ivData = cipher.doFinal(iv)
        val ivIv = cipher.iv
        app.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            .edit {
                putString(
                    savedIvKeyWord + keyProvider.getAlias(),
                    Base64.encodeToString(
                        ByteBuffer.allocate(ivIv.size + ivData.size + 1)
                            .apply {
                                put(ivIv.size.toByte())
                                put(ivIv)
                                put(ivData)
                            }.array(), Base64.DEFAULT
                    )
                )
            }
    }

    protected fun loadFixedIv(useRandomizeIv: Boolean): ByteArray? {
        val encryptIv = this.keyProvider.getIv()
        if (encryptIv != null || useRandomizeIv) {
            return encryptIv
        }
        if (this.fixedIv != null) {
            return this.fixedIv
        }

        if (keyProvider.getAlias().isEmpty()) {
            throw Exception("Alias is empty")
        }

        val pref = app.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val encryptedIv = pref.getString(savedIvKeyWord + keyProvider.getAlias(), null)
        if (encryptedIv != null) {
            return decrypt(Base64.decode(encryptedIv, Base64.DEFAULT)).apply {
                if (this == null) {
                    pref.edit {
                        remove(savedIvKeyWord + keyProvider.getAlias())
                    }
                }
            }
        } else {
            return null
        }
    }

    override fun encrypt(data: ByteArray, useRandomizeIv: Boolean): EncryptedData? {
        synchronized(this) {
            try {
                initCipher(Cipher.ENCRYPT_MODE, loadFixedIv(useRandomizeIv))

                val output = cipher.doFinal(data)
                val iv = cipher.iv

                saveFixedIv(iv, useRandomizeIv)
                return EncryptedData(output, iv)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }

    override fun decrypt(data: EncryptedData): ByteArray? {
        synchronized(this) {
            try {
                initCipher(Cipher.DECRYPT_MODE, data.iv)
                return cipher.doFinal(data.data)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }

    override fun getEncryptCipher(useRandomizeIv: Boolean): Cipher {
        initCipher(Cipher.ENCRYPT_MODE, loadFixedIv(useRandomizeIv))
        return cipher
    }

    override fun getDecryptCipher(iv: ByteArray): Cipher {
        initCipher(Cipher.DECRYPT_MODE, iv)
        return cipher
    }

    override fun resetKeys(): Boolean {
        synchronized(this) {
            try {
                app.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit {
                    remove(savedIvKeyWord + keyProvider.getAlias())
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    app.deleteSharedPreferences(prefName)
                }
                return keyProvider.removeSecretKey()
            } finally {
                loadKey()
            }
        }
    }
}