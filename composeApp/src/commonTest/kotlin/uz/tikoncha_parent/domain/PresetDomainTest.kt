package uz.tikoncha_parent.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import uz.tikoncha_parent.domain.model.GeoType
import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.WeekDay
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.domain.model.policy.Patch
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyConditions
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyKind
import uz.tikoncha_parent.domain.model.policy.PolicyLimits
import uz.tikoncha_parent.domain.model.policy.PolicyListSnapshot
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.policy.TimeCondition
import uz.tikoncha_parent.domain.policy.PresetDefaults
import uz.tikoncha_parent.domain.policy.TimeRuleMatcher
import uz.tikoncha_parent.domain.policy.contains
import uz.tikoncha_parent.domain.policy.isActiveNow
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository
import uz.tikoncha_parent.domain.use_case.policy.CreatePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.SavePolicyDraftUseCase
import uz.tikoncha_parent.domain.use_case.policy.TogglePresetUseCase
import uz.tikoncha_parent.domain.use_case.policy.UpdatePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.toDraft
import uz.tikoncha_parent.domain.use_case.policy.withSharedDays
import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.policy.UsageLimit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

internal fun policy(
    conditions: PolicyConditions = PolicyConditions(),
    limits: PolicyLimits = PolicyLimits(),
    active: Boolean = true,
    pausedUntil: Instant? = null,
    kind: PolicyKind = PolicyKind.STANDARD,
    preset: PolicyPreset? = null,
    id: String = "p1",
) = Policy(
    id = id, name = "Jadval", kind = kind, preset = preset, scope = PolicyType.PARENT_CHILD, scopeId = "child",
    actorUserId = ME, createdBy = ME, action = PolicyAction.DENY, priority = 100, isActive = active,
    pausedUntil = pausedUntil, expiresAt = null, packCode = null, targets = PolicyTargets(packages = listOf("*")),
    conditions = conditions, limits = limits, createdAt = NOW, updatedAt = NOW,
)

class PresetDomainTest {

    private val sleep = TimeCondition(days = setOf(WeekDay.FRI), startMin = 22 * 60, endMin = 6 * 60)

    @Test
    fun overnightWindowBelongsToStartDay() {
        assertTrue(TimeRuleMatcher.matches(weekDay = 5, minuteOfDay = 23 * 60, rule = sleep))   // juma kechasi
        assertTrue(TimeRuleMatcher.matches(weekDay = 6, minuteOfDay = 5 * 60, rule = sleep))    // shanba tongi
        assertFalse(TimeRuleMatcher.matches(weekDay = 6, minuteOfDay = 23 * 60, rule = sleep))  // shanba kechasi — yo'q
        assertFalse(TimeRuleMatcher.matches(weekDay = 5, minuteOfDay = 5 * 60, rule = sleep))   // juma tongi — payshanbaniki
    }

    @Test
    fun outsideWindowAndEndOfDay() {
        val outside = TimeCondition(days = setOf(WeekDay.MON), startMin = 8 * 60, endMin = 14 * 60, include = false)
        assertTrue(TimeRuleMatcher.matches(1, 15 * 60, outside))
        assertFalse(TimeRuleMatcher.matches(1, 9 * 60, outside))
        assertFalse(TimeRuleMatcher.matches(2, 15 * 60, outside))                              // ro'yxatda yo'q kun
        val allDay = TimeCondition(days = setOf(WeekDay.MON), startMin = 0, endMin = 1439)
        assertTrue(TimeRuleMatcher.matches(1, 1439, allDay))                                   // 23:59 uzilmaydi
    }

    @Test
    fun activeNowRespectsLiveTimeAndPlace() {
        val school = PolicyConditions(
            time = TimeCondition(days = setOf(WeekDay.MON), startMin = 8 * 60, endMin = 14 * 60),
            location = LocationRule(GeoType.CIRCLE, centerLat = 41.3, centerLng = 69.2, radiusMeters = 300, reverse = false),
        )
        val p = policy(conditions = school)
        assertTrue(p.isActiveNow(1, 9 * 60, NOW, lastLat = 41.3005, lastLng = 69.2))
        assertFalse(p.isActiveNow(1, 9 * 60, NOW, lastLat = 41.4, lastLng = 69.2))     // hududdan tashqarida
        assertFalse(p.isActiveNow(1, 9 * 60, NOW))                                     // joy noma'lum
        assertFalse(policy(active = false).isActiveNow(1, 9 * 60, NOW))
        assertFalse(policy(pausedUntil = Instant.parse("2026-09-25T11:00:00Z")).isActiveNow(1, 9 * 60, NOW))
        assertFalse(policy(kind = PolicyKind.QUICK_BLOCK).isActiveNow(1, 9 * 60, NOW, paid = false))
        assertTrue(policy(kind = PolicyKind.QUICK_BLOCK).isActiveNow(1, 9 * 60, NOW, paid = null))
    }

    @Test
    fun polygonContains() {
        val square = LocationRule(
            GeoType.POLYGON, polygon = listOf(
                uz.tikoncha_parent.domain.model.LocationData(0.0, 0.0), uz.tikoncha_parent.domain.model.LocationData(0.0, 1.0),
                uz.tikoncha_parent.domain.model.LocationData(1.0, 1.0), uz.tikoncha_parent.domain.model.LocationData(1.0, 0.0),
            ), reverse = false,
        )
        assertTrue(square.contains(0.5, 0.5))
        assertFalse(square.contains(1.5, 0.5))
    }

    /** Uyqu vaqti ALLOW emas (ota-onada ALLOW pullik) — DENY "*" + istisnolar. */
    @Test
    fun sleepDefaultsAreFreeDenyAll() {
        val d = PresetDefaults.sleep("Uyqu vaqti", openPackages = listOf("com.sec.android.app.clockpackage"))
        assertEquals(PolicyAction.DENY, d.action)
        assertTrue(d.targets.isAllApps)
        assertEquals(listOf("com.sec.android.app.clockpackage"), d.targets.excludePackages)
        assertEquals(22 * 60 to 7 * 60, d.conditions.time!!.startMin to d.conditions.time!!.endMin)
        assertEquals(listOf("GAMES", "SOCIAL"), PresetDefaults.school("Dars vaqti").targets.categories)
        assertEquals(60, PresetDefaults.appLimit("Vaqt limiti").limits.usage!!.minutes)
    }

    private class Repo : PolicyRepository {
        val calls = mutableListOf<String>()
        override suspend fun childApps(userId: String): Outcome<List<InstalledApp>> = Outcome.Success(emptyList())
        override fun observePolicies(childId: String): Flow<List<Policy>> = emptyFlow()
        override fun cachedPolicies(childId: String): List<Policy> = emptyList()
        override suspend fun refreshPolicies(childId: String, force: Boolean): Outcome<PolicyListSnapshot> = error("unused")
        override suspend fun createPolicy(childId: String, draft: PolicyDraft): Outcome<Policy> {
            calls += "create:${draft.preset}"; return Outcome.Success(policy())
        }
        override suspend fun patchPolicy(policyId: String, patch: PolicyPatch): Outcome<Policy> {
            calls += "patch:$policyId:${(patch.isActive as? Patch.Value)?.value}"; return Outcome.Success(policy())
        }
        override suspend fun deletePolicy(policyId: String): Outcome<Unit> = error("unused")
    }

    @Test
    fun presetToggleCreatesOnceThenPatches() = runTest {
        val repo = Repo()
        val toggle = TogglePresetUseCase(repo)
        toggle("child", PolicyPreset.SLEEP, existing = null, enabled = true, title = "Uyqu vaqti")
        toggle("child", PolicyPreset.SLEEP, existing = policy(id = "s1"), enabled = false, title = "Uyqu vaqti")
        val noop = toggle("child", PolicyPreset.SCHOOL, existing = null, enabled = false, title = "Dars vaqti")
        assertIs<Outcome.Success<Unit>>(noop)
        assertEquals(listOf("create:SLEEP", "patch:s1:false"), repo.calls)
    }

    /**
     * Tahrirda boshlanish = tugash tanlansa, mapper vaqt shartini tashlab yuborardi va
     * PATCH jadvalni 24/7 blokka aylantirardi. Endi saqlash rad etiladi, serverga hech narsa ketmaydi.
     */
    @Test
    fun editWithZeroLengthWindowIsRejectedNotTurnedIntoAlwaysOn() = runTest {
        val repo = Repo()
        val save = SavePolicyDraftUseCase(CreatePolicyUseCase(repo), UpdatePolicyUseCase(repo))
        val saved = policy(conditions = PolicyConditions(time = sleep), id = "s1").toDraft()
        val broken = saved.copy(conditions = PolicyConditions(time = sleep.copy(endMin = sleep.startMin)))

        assertIs<Outcome.Failure>(save("child", "s1", broken, saved))
        assertIs<Outcome.Failure>(UpdatePolicyUseCase(repo)("s1", PolicyPatch(conditions = Patch.Value(broken.conditions))))
        assertEquals(emptyList(), repo.calls)

        val fine = saved.copy(conditions = PolicyConditions(time = sleep.copy(endMin = 7 * 60)))
        assertIs<Outcome.Success<Policy>>(save("child", "s1", fine, saved))
        assertEquals(listOf("patch:s1:null"), repo.calls)
    }

    /** "Du 22–06 dan tashqari" seshanba kunduzi amal qilmaydi — seshanba ro'yxatda yo'q (qurilma va server bilan bir xil). */
    @Test
    fun outsideOvernightWindowDoesNotSpillIntoNextDay() {
        val outside = TimeCondition(days = setOf(WeekDay.MON), startMin = 22 * 60, endMin = 6 * 60, include = false)
        assertTrue(TimeRuleMatcher.matches(1, 12 * 60, outside))                     // Du 12:00
        assertFalse(TimeRuleMatcher.matches(1, 23 * 60, outside))                    // Du 23:00 — oyna ichida
        assertFalse(TimeRuleMatcher.matches(2, 3 * 60, outside))                     // Se 03:00 — oyna davomi
        assertFalse(TimeRuleMatcher.matches(2, 10 * 60, outside))                    // Se 10:00 — ro'yxatda yo'q kun
    }

    /** Tungi oyna limiti oyna boshlangan kunga tegishli: "Juma 22–06, juma limiti" shanba 01:00 da ham faol. */
    @Test
    fun overnightLimitBelongsToStartDay() {
        assertEquals(1, TimeRuleMatcher.occurrenceDayOffset(6, 60, sleep))
        assertEquals(0, TimeRuleMatcher.occurrenceDayOffset(5, 23 * 60, sleep))
        val p = policy(
            conditions = PolicyConditions(time = sleep),
            limits = PolicyLimits(usage = UsageLimit(days = setOf(WeekDay.FRI), window = LimitWindow.DAY, minutes = 60)),
        )
        assertTrue(p.isActiveNow(6, 60, NOW))                                        // Shanba 01:00 — juma kechasi
        assertFalse(p.isActiveNow(6, 23 * 60, NOW))                                  // Shanba 23:00 — shanba ro'yxatda yo'q
    }

    /** Limit kunlari — jadval kunlari: vaqt kunida, lekin limitsiz kunda jadval faol emas (qurilma ham qo'llamaydi). */
    @Test
    fun limitDaysAreThePolicysDays() {
        val school = TimeCondition(days = setOf(WeekDay.MON, WeekDay.THU), startMin = 8 * 60, endMin = 14 * 60)
        val p = policy(
            conditions = PolicyConditions(time = school),
            limits = PolicyLimits(usage = UsageLimit(days = setOf(WeekDay.MON), window = LimitWindow.DAY, minutes = 30)),
        )
        assertTrue(p.isActiveNow(1, 10 * 60, NOW))
        assertFalse(p.isActiveNow(4, 10 * 60, NOW))
        // Saqlashda kunlar bittaga keltiriladi — limit kunlari vaqtnikiga teng
        val shared = p.toDraft().withSharedDays()
        assertEquals(school.days, shared.limits.usage!!.days)
    }
}
