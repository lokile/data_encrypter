# LokileCrypt-Android



## Overview
This library provides a simple way to encrypt and decrypt data using the AES 256-bit encryption algorithm. The library store the secret key in the Android Keystore which makes it more difficult to extract from the device. This library also allows you to provide your secret key to encrypt/decrypt so that you can transfer/received the encrypted data to/from outside of the app.

## Requirements
- Android API 18 or higher
- Android Studio 4.1+
- Java 1.8+

## Installation
Add the following code to to your root build.gradle at the end of repositories:
```
  allprojects {
      repositories {
        ...
        maven { url 'https://jitpack.io' }
      }
  }
```
Then, add the dependency to your app build.gradle file, the latest version is: [![](https://jitpack.io/v/lokile/LokileCrypt.svg)](https://jitpack.io/#lokile/LokileCrypt)
```
  dependencies {
    implementation 'com.github.lokile:LokileCrypt:latest_version'
  }
```

## Usage:
### Create the object:
- Just create the Encrypter object and use it to encrypt/decrypt your data, it will generate a new secret key for the new alias and store in Android KeyStore system.
```
import com.lokile.encrypter.encrypters.imp.Encrypter
......
var encrypter: Encrypter
try {
  encrypter = Encrypter(
    context=context,
    alias="your_alias", // it's a keyword to save/load the secret key in keystore,
    //algorithm="your_encryption_algorithm" //default value is "AES/CBC/PKCS7PADDING"
  )
} catch(e: Exception) {
  Log.e(TAG, "Failed to create the Encrypter", e)
}
```
- You can provide your secret key to encrypt/decrypt your data:
```
try {
    encrypter = Encrypter(
        context = appContext,
        keyProvider = object : SecretKeyProvider() {
            override fun getAlias() = "Your alias"
            
            override fun getSecretKey(): Key? {
                TODO("Not yet implemented")
            }

            override fun getIv(): ByteArray? {
              //return null if you want to use randomize iv
              return null
            }
        },
        //algorithm="your_encryption_algorithm" //default value is "AES/CBC/PKCS7PADDING"
    )
} catch (e: Exception) {
    Log.e(TAG, "Failed to create the Encrypter", e)
}
```
### Encryption
- The following code will encrypt the data, the library will generate a new randomized IV key when performing encryption, 
so the encrypted results are not the same for the same input
```
val toBeEncrypted="Hello World!"
val result1:String? = encrypter.encrypt(toBeEncrypted)
val result2:String? = encrypter.encrypt(toBeEncrypted) // result1 != result2
```
- If you expected that the encrypted data should be the same in the above case. You can update the code as the following:
```
val toBeEncrypted="Hello World!"
val result1:String? = encrypter.encrypt(toBeEncrypted, useRandomizeIv=false)
val result2:String? = encrypter.encrypt(toBeEncrypted, useRandomizeIv=false) // result1 == result2
```
- The above function merges the IV key and the encrypted data into single output String, if you want to separate these data, you can update the code as the following:
```
val result1:EncryptedData? = encrypter.encrypt(toBeEncrypted.toByteArray())
//result1.data
//result1.iv
//result1.toStringData() //to merge the data and iv to a single String
//result1.toByteArray()  //to merge the data and iv to a single ByteArray
```

### Decryption

```
//encrypted1 is a ByteArray or EncryptedData
val decrypted1:ByteArray? = encrypter.decrypt(encrypted1)
// if the original data is string, you can convert the decrypted1 to string:
// val decrypted1Str = String(decrypted1) 

//encrypted2 is a String
val decrypted2:String? = encrypter.decrypt(encrypted2)
```
