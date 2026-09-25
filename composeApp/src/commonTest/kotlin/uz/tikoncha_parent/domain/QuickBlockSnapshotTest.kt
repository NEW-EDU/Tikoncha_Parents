package uz.tikoncha_parent.domain

import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.policy.QuickBadge
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import uz.tikoncha_parent.domain.model.policy.QuickLock
import uz.tikoncha_parent.domain.model.policy.QuickOwner
import uz.tikoncha_parent.domain.model.policy.QuickStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

internal const val ME = "parent-me"
internal const val OTHER = "parent-other"
internal val NOW = Instant.parse("2026-09-25T10:00:00Z")

internal fun entry(
    owner: QuickOwner,
    vararg pkgs: String,
    active: Boolean = true,
    pausedUntil: Instant? = null,
    id: String = owner.name,
) = QuickBlockEntry(
    policyId = id,
    scope = if (owner == QuickOwner.CHILD) PolicyType.STUDENT else PolicyType.PARENT_CHILD,
    actorUserId = when (owner) { QuickOwner.ME -> ME; QuickOwner.CO_PARENT -> OTHER; QuickOwner.CHILD -> null },
    targets = PolicyTargets(packages = pkgs.toList()),
    updatedAt = NOW,
    isActive = active,
    pausedUntil = pausedUntil,
)

class QuickBlockSnapshotTest {

    private fun snap(vararg e: QuickBlockEntry, paid: Boolean? = true) =
        QuickBlockSnapshot(entries = e.toList(), myUserId = ME, paid = paid)

    @Test
    fun mineActiveIsGreenLock() {
        val s = snap(entry(QuickOwner.ME, "com.instagram.android"))
        assertEquals(QuickLock.ON, s.lock("com.instagram.android", NOW))
        assertEquals(QuickBadge(QuickOwner.ME, QuickStatus.ACTIVE), s.badge("com.instagram.android", NOW))
        assertEquals(QuickLock.OFF, s.lock("com.whatsapp", NOW))
        assertNull(s.badge("com.whatsapp", NOW))
    }

    @Test
    fun serverComparesPackagesIgnoringCase() {
        val s = snap(entry(QuickOwner.ME, "com.Instagram.Android"))
        assertTrue(s.isBlockedByMe("com.instagram.android"))
    }

    @Test
    fun pausedUntilFutureIsInactiveThenBackToGreen() {
        val until = Instant.parse("2026-09-25T11:00:00Z")
        val s = snap(entry(QuickOwner.ME, "a.b", pausedUntil = until))
        assertEquals(QuickLock.INACTIVE, s.lock("a.b", NOW))
        assertEquals(QuickStatus.PAUSED, s.badge("a.b", NOW)!!.status)
        assertEquals(QuickLock.ON, s.lock("a.b", Instant.parse("2026-09-25T11:00:01Z")))
    }

    @Test
    fun unpaidChildBlocksNothingButRemovalIsFree() {
        val s = snap(entry(QuickOwner.ME, "a.b"), paid = false)
        assertEquals(QuickLock.INACTIVE, s.lock("a.b", NOW))
        assertEquals(QuickStatus.UNPAID, s.badge("a.b", NOW)!!.status)
        assertFalse(s.needsPaywall("a.b"))           // olib tashlash
        assertTrue(s.needsPaywall("c.d"))            // qo'shish
        assertFalse(snap(paid = null).needsPaywall("c.d"))   // noma'lum — server hal qiladi
    }

    @Test
    fun disabledOwnBlockIsNotMineAndWarnsAboutReenable() {
        val s = snap(entry(QuickOwner.ME, "a.b", "c.d", "e.f", active = false))
        assertFalse(s.isBlockedByMe("a.b"))
        assertTrue(s.isInMyList("a.b"))
        assertEquals(QuickLock.OFF, s.lock("a.b", NOW))
        assertNull(s.badge("a.b", NOW))
        assertEquals(2, s.reenableCount("a.b"))       // qolgan ikkitasi ham yopiladi
        assertEquals(3, s.reenableCount("x.y"))
        assertEquals(0, snap(entry(QuickOwner.ME, "a.b")).reenableCount("x.y"))
    }

    @Test
    fun othersShowBadgeButMyLockStaysMine() {
        val s = snap(entry(QuickOwner.CHILD, "a.b"), entry(QuickOwner.CO_PARENT, "c.d"))
        assertEquals(QuickBadge(QuickOwner.CHILD, QuickStatus.ACTIVE), s.badge("a.b", NOW))
        assertEquals(QuickBadge(QuickOwner.CO_PARENT, QuickStatus.ACTIVE), s.badge("c.d", NOW))
        // Bola o'zi yoki ikkinchi ota-ona istalgan payt olib tashlashi mumkin — men o'zimnikiga qo'sha olaman
        assertEquals(QuickLock.OFF, s.lock("a.b", NOW))
    }

    @Test
    fun strongestForeignBadgeWins() {
        val paused = Instant.parse("2026-09-25T12:00:00Z")
        val s = snap(
            entry(QuickOwner.CHILD, "a.b"),
            entry(QuickOwner.CO_PARENT, "a.b", pausedUntil = paused),
        )
        assertEquals(QuickBadge(QuickOwner.CHILD, QuickStatus.ACTIVE), s.badge("a.b", NOW))
        val both = snap(entry(QuickOwner.CHILD, "a.b"), entry(QuickOwner.CO_PARENT, "a.b"))
        assertEquals(QuickOwner.CO_PARENT, both.badge("a.b", NOW)!!.owner)
    }

    @Test
    fun disabledForeignBlockIsIgnored() {
        val s = snap(entry(QuickOwner.CHILD, "a.b", active = false))
        assertNull(s.badge("a.b", NOW))
    }

    @Test
    fun mineBeatsForeign() {
        val s = snap(entry(QuickOwner.CHILD, "a.b"), entry(QuickOwner.ME, "a.b"))
        assertEquals(QuickOwner.ME, s.badge("a.b", NOW)!!.owner)
    }

    @Test
    fun protectedAppsHaveNoLock() {
        val s = snap()
        assertNull(s.lock("uz.tikoncha.student", NOW))
        assertNull(s.lock("com.android.settings", NOW))
    }
}
