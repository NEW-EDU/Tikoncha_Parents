package uz.tikoncha_parent.domain

import uz.tikoncha_parent.domain.policy.NeverBlock
import uz.tikoncha_parent.domain.policy.ProtectedPackages
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BlockRulesTest {

    @Test
    fun tikonchaAppsAreNeverBlocked() {
        assertTrue(NeverBlock.isNeverBlocked("uz.tikoncha.student"))
        assertTrue(NeverBlock.isNeverBlocked(" UZ.Tikoncha.Parent "))
        assertFalse(ProtectedPackages.canBlock("uz.tikoncha.parent"))
        assertFalse(ProtectedPackages.canBlock("uz.tikoncha.student"))
    }

    @Test
    fun serverProtectedPackagesCannotBeBlocked() {
        listOf("com.android.settings", "com.samsung.android.dialer", "com.google.android.inputmethod.latin", "com.android.systemui")
            .forEach { assertFalse(ProtectedPackages.canBlock(it), it) }
    }

    @Test
    fun ordinaryAppsCanBeBlocked() {
        assertTrue(ProtectedPackages.canBlock("com.instagram.android"))
        // "Doim ochiq" vositalar oq ro'yxatda, lekin qo'lda tezkor blokka qo'shsa bo'ladi
        assertTrue(ProtectedPackages.canBlock("com.sec.android.app.clockpackage"))
        assertFalse(ProtectedPackages.canBlock(""))
        assertFalse(ProtectedPackages.canBlock("*"))
    }

    /** Backend `protected.py` → PROTECTED_PACKAGES: 2 own + 20 comm + 8 settings. */
    @Test
    fun sameSizeAsServerList() {
        assertEquals(30, ProtectedPackages.PACKAGES.size)
    }
}
