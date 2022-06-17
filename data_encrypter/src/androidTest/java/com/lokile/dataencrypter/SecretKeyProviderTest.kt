package com.lokile.dataencrypter

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lokile.dataencrypter.secretKeyProviders.ISecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.PasswordSecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.RSASecretKeyProvider
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SecretKeyProviderTest {
    lateinit var provider: ISecretKeyProvider

    @After
    fun tearDown() {
        provider.removeSecretKey()
    }

    @Test
    fun testAesKeyProvider() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            provider = AESSecretKeyProvider("testAesKeyProvider")
            assertNotNull(provider.getSecretKey())
        }
    }

    @Test
    fun testRsaKeyProvider() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            provider = RSASecretKeyProvider(appContext, "testRsaKeyProvider")
            assertNotNull(provider.getSecretKey())
        }
    }

    @Test
    fun testPasswordKeyProvider() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        provider = PasswordSecretKeyProvider(appContext, "testPasswordKeyProvider", "testPasswordKeyProvider")
        assertNotNull(provider.getSecretKey())
    }
}