package uz.tikoncha_parent.data.local

import com.russhwolf.settings.Settings
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/** Encrypted key-value store: Android Keystore (AES-GCM) or the iOS Keychain. */
interface SecureStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

expect fun createSecureStore(): SecureStore

/**
 * Secrets (auth tokens, the live-signing private key) kept in [secure] instead of the plain
 * settings file.
 *
 * Builds before this one kept them in [plain]. On start-up each plain value is copied across,
 * read back, and only then deleted, so signed-in users stay signed in after the update. If the
 * encrypted store fails on a device, the value stays in [plain] and reads fall back to it, so a
 * broken Keystore never signs anyone out; the next launch or write tries again.
 */
@OptIn(ExperimentalAtomicApi::class)
class SecretVault(
    private val plain: Settings,
    private val secure: SecureStore,
    keys: List<String>,
) {
    private class Cached(val value: String)

    // Only non-null values are cached: a failed or locked read must not stick for the process.
    private val cache = keys.associateWith { AtomicReference<Cached?>(null) }

    init {
        migrate(keys)
    }

    fun get(key: String): String? {
        val slot = slot(key)
        slot.load()?.let { return it.value }
        val value = runCatching { secure.getString(key) }.getOrNull() ?: plain.getStringOrNull(key)
        if (value != null) slot.compareAndSet(null, Cached(value))
        return value
    }

    fun put(key: String, value: String?) {
        if (value.isNullOrEmpty()) {
            remove(key)
            return
        }
        if (runCatching { secure.putString(key, value) }.isSuccess) {
            plain.remove(key)
        } else {
            runCatching { secure.remove(key) }
            plain.putString(key, value)
        }
        slot(key).store(Cached(value))
    }

    fun remove(key: String) {
        runCatching { secure.remove(key) }
        plain.remove(key)
        slot(key).store(null)
    }

    private fun slot(key: String) = cache[key] ?: error("Not a secret key: $key")

    private fun migrate(keys: List<String>) {
        val firstRun = plain.getBooleanOrNull(MIGRATED_FLAG) != true
        for (key in keys) {
            val old = plain.getStringOrNull(key)
            if (old == null) {
                // A fresh install has no flag yet. The iOS Keychain outlives an uninstall, so
                // anything already there belongs to an earlier install and must not sign in.
                if (firstRun) runCatching { secure.remove(key) }
                continue
            }
            val moved = runCatching {
                secure.putString(key, old)
                secure.getString(key) == old
            }.getOrDefault(false)
            if (moved) plain.remove(key)
        }
        if (firstRun) plain.putBoolean(MIGRATED_FLAG, true)
    }

    companion object {
        const val MIGRATED_FLAG = "secretsInSecureStoreV1"
    }
}

/** The app's one vault. Created on first use, which runs the migration. */
object Secrets {
    const val ACCESS_TOKEN = "accessToken"
    const val REFRESH_TOKEN = "refreshToken"
    const val LIVE_PRIVATE_KEY = "live_priv_b64"

    private val vaultLazy = lazy {
        SecretVault(Settings(), createSecureStore(), listOf(ACCESS_TOKEN, REFRESH_TOKEN, LIVE_PRIVATE_KEY))
    }
    private val vault by vaultLazy

    /** Moves plaintext secrets left by older builds now, not on first use. */
    fun ensureMigrated() {
        vaultLazy.value
    }

    fun get(key: String): String? = vault.get(key)
    fun put(key: String, value: String?) = vault.put(key, value)
    fun remove(key: String) = vault.remove(key)
}
