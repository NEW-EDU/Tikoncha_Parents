package uz.tikoncha_parent.data.local

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.CFBridgingRetain
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrService

actual fun createSecureStore(): SecureStore = KeychainSecureStore

/**
 * Keychain items, readable after the first unlock since boot (so background work such as a push
 * can still authenticate). Unlike UserDefaults they are encrypted at rest and leave the phone only
 * inside an encrypted backup.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)
private object KeychainSecureStore : SecureStore {
    private val keychain = KeychainSettings(
        kSecAttrService to CFBridgingRetain("uz.tikoncha_parent.secure"),
        kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlock,
    )

    override fun getString(key: String): String? = keychain.getStringOrNull(key)
    override fun putString(key: String, value: String) = keychain.putString(key, value)
    override fun remove(key: String) = keychain.remove(key)
}
