package uz.tikoncha_parent.presentation

import uz.tikoncha_parent.domain.ME
import uz.tikoncha_parent.domain.NOW
import uz.tikoncha_parent.domain.entry
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import uz.tikoncha_parent.domain.model.policy.QuickOwner
import uz.tikoncha_parent.presentation.policy.quick.QuickBlockState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class QuickBlockStateTest {

    private fun state(vararg e: uz.tikoncha_parent.domain.model.policy.QuickBlockEntry) = QuickBlockState(
        childId = "child",
        loaded = true,
        quick = QuickBlockSnapshot(e.toList(), ME, paid = true),
        now = NOW,
        childApps = listOf(
            InstalledApp("com.youtube", "YouTube", null, "https://icon", 1),
            InstalledApp("com.whatsapp", "WhatsApp", null, null, 2),
        ),
    )

    @Test
    fun ownListAndOthersAreSeparated() {
        val s = state(
            entry(QuickOwner.ME, "com.youtube"),
            entry(QuickOwner.CHILD, "com.whatsapp"),
            entry(QuickOwner.CO_PARENT, "com.tiktok", active = false),   // o'chiq — ko'rsatilmaydi
        )
        assertEquals(listOf("com.youtube"), s.ownPackages)
        assertEquals(listOf("com.whatsapp"), s.childPackages)
        assertTrue(s.coParentPackages.isEmpty())
        assertEquals(listOf("com.whatsapp"), s.addableApps.map { it.packageName })   // o'zimdagisi yo'q
        assertEquals("YouTube", s.label("com.youtube"))
        assertEquals("com.tiktok", s.label("com.tiktok"))                          // nom kelmagan — paket
    }

    @Test
    fun switchNeedsANonEmptyOwnBlock() {
        assertFalse(state().canToggle)
        assertFalse(state(entry(QuickOwner.ME)).canToggle)
        assertTrue(state(entry(QuickOwner.ME, "com.youtube")).canToggle)
        assertFalse(state(entry(QuickOwner.ME, "com.youtube")).copy(pendingEnabled = false).canToggle)
    }

    @Test
    fun pauseInThePastIsNotShown() {
        val past = Instant.parse("2026-09-25T09:00:00Z")
        val future = Instant.parse("2026-09-25T11:00:00Z")
        assertNull(state(entry(QuickOwner.ME, "a.b", pausedUntil = past)).pausedUntil)
        assertEquals(future, state(entry(QuickOwner.ME, "a.b", pausedUntil = future)).pausedUntil)
    }
}
