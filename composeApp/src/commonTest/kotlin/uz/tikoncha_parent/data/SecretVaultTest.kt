package uz.tikoncha_parent.data

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import uz.tikoncha_parent.data.local.SecretVault
import uz.tikoncha_parent.data.local.SecureStore

class SecretVaultTest {

    private class FakeSecureStore(
        val values: MutableMap<String, String> = mutableMapOf(),
        var failWrites: Boolean = false,
        var failReads: Boolean = false,
        var corruptReads: Boolean = false,
    ) : SecureStore {
        override fun getString(key: String): String? {
            if (failReads) error("keystore unavailable")
            return values[key]?.let { if (corruptReads) "$it-garbled" else it }
        }
        override fun putString(key: String, value: String) {
            if (failWrites) error("keystore unavailable")
            values[key] = value
        }
        override fun remove(key: String) {
            values.remove(key)
        }
    }

    private val keys = listOf("accessToken", "refreshToken", "live_priv_b64")

    @Test
    fun upgradeMovesSignedInUsersTokensAndKeepsThemSignedIn() {
        val plain = MapSettings("accessToken" to "A1", "refreshToken" to "R1", "userId" to "u1")
        val secure = FakeSecureStore()

        val vault = SecretVault(plain, secure, keys)

        assertEquals("A1", vault.get("accessToken"))
        assertEquals("R1", vault.get("refreshToken"))
        assertEquals(mapOf("accessToken" to "A1", "refreshToken" to "R1"), secure.values)
        assertNull(plain.getStringOrNull("accessToken"))
        assertNull(plain.getStringOrNull("refreshToken"))
        assertEquals("u1", plain.getStringOrNull("userId")) // non-secret keys stay where they were
        assertTrue(plain.getBooleanOrNull(SecretVault.MIGRATED_FLAG) == true)
    }

    @Test
    fun upgradeOfSignedOutUserJustSetsTheFlag() {
        val plain = MapSettings()
        val vault = SecretVault(plain, FakeSecureStore(), keys)

        assertNull(vault.get("accessToken"))
        assertTrue(plain.getBooleanOrNull(SecretVault.MIGRATED_FLAG) == true)
    }

    @Test
    fun freshInstallDropsKeychainItemsLeftByAnEarlierInstall() {
        val plain = MapSettings() // UserDefaults are wiped on uninstall, the Keychain is not
        val secure = FakeSecureStore(mutableMapOf("accessToken" to "OLD", "refreshToken" to "OLD_R"))

        val vault = SecretVault(plain, secure, keys)

        assertNull(vault.get("accessToken"))
        assertNull(vault.get("refreshToken"))
        assertTrue(secure.values.isEmpty())
    }

    @Test
    fun laterLaunchesKeepTokensInTheSecureStore() {
        val plain = MapSettings(SecretVault.MIGRATED_FLAG to true)
        val secure = FakeSecureStore(mutableMapOf("accessToken" to "A2"))

        val vault = SecretVault(plain, secure, keys)

        assertEquals("A2", vault.get("accessToken"))
        assertEquals("A2", secure.values["accessToken"])
    }

    @Test
    fun brokenSecureStoreDuringUpgradeKeepsUserSignedInAndRetriesNextLaunch() {
        val plain = MapSettings("accessToken" to "A1", "refreshToken" to "R1")
        val secure = FakeSecureStore(failWrites = true)

        val first = SecretVault(plain, secure, keys)
        assertEquals("A1", first.get("accessToken"))
        assertEquals("A1", plain.getStringOrNull("accessToken"))

        secure.failWrites = false
        val second = SecretVault(plain, secure, keys)
        assertEquals("A1", second.get("accessToken"))
        assertEquals("R1", second.get("refreshToken"))
        assertNull(plain.getStringOrNull("accessToken"))
        assertEquals("A1", secure.values["accessToken"])
    }

    @Test
    fun valueThatDoesNotReadBackIsNotDeletedFromPlainSettings() {
        val plain = MapSettings("accessToken" to "A1")
        SecretVault(plain, FakeSecureStore(corruptReads = true), keys)

        assertEquals("A1", plain.getStringOrNull("accessToken"))
    }

    @Test
    fun failedWriteFallsBackToPlainAndDropsTheOlderSecureCopy() {
        val plain = MapSettings(SecretVault.MIGRATED_FLAG to true)
        val secure = FakeSecureStore(mutableMapOf("accessToken" to "OLD"))
        val vault = SecretVault(plain, secure, keys)

        secure.failWrites = true
        vault.put("accessToken", "NEW")

        assertEquals("NEW", vault.get("accessToken"))
        assertNull(secure.values["accessToken"])
        // A fresh process (no cache) still finds the new value
        secure.failWrites = false
        assertEquals("NEW", SecretVault(plain, secure, keys).get("accessToken"))
        assertNull(plain.getStringOrNull("accessToken"))
    }

    @Test
    fun unreadableSecureStoreFallsBackWithoutCachingAMiss() {
        val plain = MapSettings(SecretVault.MIGRATED_FLAG to true)
        val secure = FakeSecureStore(mutableMapOf("accessToken" to "A1"), failReads = true)
        val vault = SecretVault(plain, secure, keys)

        assertNull(vault.get("accessToken")) // e.g. iOS before first unlock
        secure.failReads = false
        assertEquals("A1", vault.get("accessToken"))
    }

    @Test
    fun putEmptyAndRemoveClearBothStores() {
        val plain = MapSettings(SecretVault.MIGRATED_FLAG to true, "refreshToken" to "R_PLAIN")
        val secure = FakeSecureStore(mutableMapOf("accessToken" to "A1"))
        val vault = SecretVault(plain, secure, keys)

        vault.put("accessToken", "")
        vault.remove("refreshToken")

        assertNull(vault.get("accessToken"))
        assertNull(vault.get("refreshToken"))
        assertTrue(secure.values.isEmpty())
        assertNull(plain.getStringOrNull("refreshToken"))
    }
}
