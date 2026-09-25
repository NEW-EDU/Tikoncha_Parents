package uz.tikoncha_parent.presentation

import uz.tikoncha_parent.domain.ME
import uz.tikoncha_parent.domain.NOW
import uz.tikoncha_parent.domain.OTHER
import uz.tikoncha_parent.domain.entry
import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.WeekDay
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyConditions
import uz.tikoncha_parent.domain.model.policy.PolicyKind
import uz.tikoncha_parent.domain.model.policy.PolicyLimits
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.policy.ProtectionPack
import uz.tikoncha_parent.domain.model.policy.ProtectionPackStatus
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import uz.tikoncha_parent.domain.model.policy.QuickOwner
import uz.tikoncha_parent.domain.model.policy.TimeCondition
import uz.tikoncha_parent.presentation.policy.list.PolicyListInput
import uz.tikoncha_parent.presentation.policy.list.PolicyListState
import uz.tikoncha_parent.presentation.policy.list.reduce
import uz.tikoncha_parent.presentation.policy.model.Ownership
import uz.tikoncha_parent.presentation.policy.model.PolicyTab
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class PolicyListReducerTest {

    private fun p(
        id: String,
        scope: PolicyType = PolicyType.PARENT_CHILD,
        actor: String? = ME,
        preset: PolicyPreset? = null,
        active: Boolean = true,
        time: TimeCondition? = null,
        pausedUntil: Instant? = null,
        name: String = id,
        packCode: String? = null,
    ) = Policy(
        id = id, name = name, kind = PolicyKind.STANDARD, preset = preset, scope = scope, scopeId = "child",
        actorUserId = actor, createdBy = actor, action = PolicyAction.DENY, priority = 100, isActive = active,
        pausedUntil = pausedUntil, expiresAt = null, packCode = packCode, targets = PolicyTargets(packages = listOf("a.b")),
        conditions = PolicyConditions(time = time), limits = PolicyLimits(), createdAt = NOW, updatedAt = NOW,
    )

    private fun input(
        policies: List<Policy>,
        quick: QuickBlockSnapshot = QuickBlockSnapshot(myUserId = ME),
        packs: List<ProtectionPackStatus> = emptyList(),
        weekDay: Int = 1,
        minute: Int = 9 * 60,
    ) = PolicyListInput(policies, quick, packs, ME, paid = true, weekDay = weekDay, minuteOfDay = minute, now = NOW)

    private val morning = TimeCondition(days = setOf(WeekDay.MON), startMin = 8 * 60, endMin = 14 * 60)

    @Test
    fun onlyTemplatesAndMineWithoutOthers() {
        val s = input(listOf(p("mine"))).reduce(PolicyListState())
        assertEquals(listOf(PolicyTab.TEMPLATES, PolicyTab.MINE), s.tabs)
        assertEquals(listOf("mine"), s.mine.map { it.policyId })
    }

    @Test
    fun ownerTabsAppearOnlyWithContent() {
        val s = input(
            listOf(p("c", scope = PolicyType.STUDENT, actor = null), p("o", actor = OTHER), p("s", scope = PolicyType.SCHOOL, actor = null)),
        ).reduce(PolicyListState())
        assertEquals(listOf(PolicyTab.TEMPLATES, PolicyTab.MINE, PolicyTab.CHILD, PolicyTab.COPARENT, PolicyTab.SCHOOL), s.tabs)
        assertEquals(Ownership.CHILD, s.child.single().ownership)
        assertFalse(s.coParent.single().canControl)
    }

    @Test
    fun childQuickBlockOpensChildTab() {
        val quick = QuickBlockSnapshot(listOf(entry(QuickOwner.CHILD, "com.tiktok")), ME, paid = true)
        val s = input(emptyList(), quick = quick).reduce(PolicyListState())
        assertTrue(PolicyTab.CHILD in s.tabs)
        assertEquals(1, s.childQuick.single().summary.appCount)
    }

    @Test
    fun myPresetPolicyBindsToTemplateAndLeavesMine() {
        val s = input(listOf(p("sleep", preset = PolicyPreset.SLEEP), p("x"))).reduce(PolicyListState())
        val sleep = s.presets.first { it.kind == PresetKind.SLEEP }
        assertEquals("sleep", sleep.policyId)
        assertTrue(sleep.isEnabled)
        assertEquals(listOf("x"), s.mine.map { it.policyId })
        assertNull(s.presets.first { it.kind == PresetKind.SCHOOL }.policyId)      // hali yaratilmagan
        assertEquals(8 * 60, s.presets.first { it.kind == PresetKind.SCHOOL }.summary.startMin)  // standart qiymat
    }

    /** Farzandning Uyqu shabloni mening shablonimga bog'lanmaydi. */
    @Test
    fun childPresetIsNotMyTemplate() {
        val s = input(listOf(p("childSleep", scope = PolicyType.STUDENT, actor = null, preset = PolicyPreset.SLEEP))).reduce(PolicyListState())
        assertNull(s.presets.first { it.kind == PresetKind.SLEEP }.policyId)
        assertEquals(listOf("childSleep"), s.child.map { it.policyId })
    }

    @Test
    fun activeNowStrongestScopeFirstWithUntil() {
        val s = input(
            listOf(p("Mine", time = morning, name = "O‘yinlar"), p("School", scope = PolicyType.SCHOOL, actor = null, time = morning, name = "Dars")),
        ).reduce(PolicyListState())
        assertEquals(listOf("Dars", "O‘yinlar"), s.info.activeTitles)
        assertEquals(14 * 60, s.info.activeUntilMin)
        assertTrue(s.info.hasSchoolPolicies)
    }

    @Test
    fun pausedAndDisabledAreNotActive() {
        val later = Instant.parse("2026-09-25T12:00:00Z")
        val s = input(listOf(p("a", time = morning, pausedUntil = later), p("b", time = morning, active = false))).reduce(PolicyListState())
        assertTrue(s.info.activeTitles.isEmpty())
        assertEquals(1, s.info.pausedCount)
        assertTrue(s.mine.first { it.policyId == "a" }.isPaused)
    }

    @Test
    fun protectionAndCounts() {
        val pack = ProtectionPack("ADULT", mapOf("uz" to "Kattalar"), null, 0, 10, 0, 0, true, 1)
        val packs = listOf(ProtectionPackStatus(pack, myPolicy = null, enabledByCoParent = false, enabledByChild = true))
        val quick = QuickBlockSnapshot(listOf(entry(QuickOwner.ME, "a.b")), ME, paid = true)
        val s = input(listOf(p("sleep", preset = PolicyPreset.SLEEP), p("x", active = false)), quick = quick, packs = packs).reduce(PolicyListState())
        assertFalse(s.protection.isEnabled)
        assertTrue(s.protection.byOthers)
        assertTrue(s.quickBlock.isOn)
        assertEquals(2, s.enabledCount)                     // Uyqu + Tezkor blok
        assertTrue(s.info.quickActive)
    }

    @Test
    fun selectedTabFallsBackWhenItDisappears() {
        val s = input(listOf(p("mine"))).reduce(PolicyListState(tab = PolicyTab.SCHOOL))
        assertEquals(PolicyTab.TEMPLATES, s.tab)
    }
}
