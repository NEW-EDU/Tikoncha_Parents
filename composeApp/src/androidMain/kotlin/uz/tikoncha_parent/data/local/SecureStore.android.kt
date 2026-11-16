package uz.tikoncha_parent.data.local

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import uz.tikoncha_parent.platform.appContext

actual fun createSecureStore(): SecureStore = KeystoreSecureStore

/**
 * Values are encrypted with AES-256-GCM under a key that never leaves the Android Keystore, and
 * kept in their own preferences file (excluded from backup and device transfer, since the key
 * does not travel). The entry name is bound in as associated data, so values cannot be swapped.
 */
private object KeystoreSecureStore : SecureStore {
    private const val FILE = "tikoncha_secure"
    private const val KEY_ALIAS = "tikoncha_secure_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    private val prefs: SharedPreferences by lazy {
        val context = appContext ?: error("Application context is not set yet")
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    }

    @Volatile private var cachedKey: SecretKey? = null

    @Synchronized
    private fun key(): SecretKey {
        cachedKey?.let { return it }
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val key = keyStore.getKey(KEY_ALIAS, null) as? SecretKey ?: KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
            }
            .generateKey()
        cachedKey = key
        return key
    }

    override fun getString(key: String): String? {
        val stored = prefs.getString(key, null) ?: return null
        return try {
            val bytes = Base64.decode(stored, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, bytes, 0, IV_BYTES))
            cipher.updateAAD(key.encodeToByteArray())
            cipher.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES).decodeToString()
        } catch (e: Exception) {
            // Wrong or replaced key, tampered or corrupt value: it can never be read, so drop it.
            // Anything else (Keystore briefly unavailable) is thrown and the caller keeps its copy.
            if (e !is AEADBadTagException && e !is KeyPermanentlyInvalidatedException && e !is IllegalArgumentException) throw e
            if (e is KeyPermanentlyInvalidatedException) cachedKey = null
            prefs.edit().remove(key).commit()
            null
        }
    }

    override fun putString(key: String, value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        cipher.updateAAD(key.encodeToByteArray())
        val iv = cipher.iv
        check(iv.size == IV_BYTES) { "Unexpected IV length ${iv.size}" }
        val encoded = Base64.encodeToString(iv + cipher.doFinal(value.encodeToByteArray()), Base64.NO_WRAP)
        check(prefs.edit().putString(key, encoded).commit()) { "Could not write $FILE" }
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).commit()
    }
}
