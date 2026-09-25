package uz.tikoncha_parent.presentation.policy.list

import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.policy.ProtectionPackStatus
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import uz.tikoncha_parent.domain.policy.TimeRuleMatcher
import uz.tikoncha_parent.domain.policy.isActiveNow
import uz.tikoncha_parent.presentation.policy.model.ContentProtectionUi
import uz.tikoncha_parent.presentation.policy.model.Ownership
import uz.tikoncha_parent.presentation.policy.model.PolicyCardUi
import uz.tikoncha_parent.presentation.policy.model.PolicyListInfo
import uz.tikoncha_parent.presentation.policy.model.PolicySummary
import uz.tikoncha_parent.presentation.policy.model.PolicyTab
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.presentation.policy.model.PresetPolicyUi
import uz.tikoncha_parent.presentation.policy.model.QuickBlockUi
import kotlin.time.Instant

/** Ro'yxatni qurish uchun hamma narsa bitta joyda — reducer sof, test qilinadi. */
data class PolicyListInput(
    val policies: List<Policy>,
    val quick: QuickBlockSnapshot,
    val packs: List<ProtectionPackStatus>,
    val myUserId: String,
    val paid: Boolean?,
    val weekDay: Int,
    val minuteOfDay: Int,
    val now: Instant,
    val lastLat: Double? = null,
    val lastLng: Double? = null,
)

fun Policy.toSummary(): PolicySummary = PolicySummary(
    startMin = conditions.time?.startMin,
    endMin = conditions.time?.endMin,
    days = conditions.time?.days?.map { it.num }?.toSet().orEmpty(),
    limitWindow = limits.usage?.window,
    limitMinutes = limits.usage?.minutes,
    appCount = targets.packages.count { it != PolicyTargets.ALL_APPS } + targets.features.size,
    categoryCount = targets.categories.size,
    siteCount = targets.sites.size,
    hasLocation = conditions.location != null,
    allApps = targets.isAllApps,
)

private fun Policy.ownership(myUserId: String): Ownership = when (scope) {
    PolicyType.SCHOOL, PolicyType.ALL -> Ownership.SCHOOL
    PolicyType.STUDENT -> Ownership.CHILD
    PolicyType.PARENT_CHILD -> if (actorUserId == myUserId) Ownership.MINE else Ownership.COPARENT
}

private val PolicyType.rank: Int
    get() = when (this) {
        PolicyType.SCHOOL -> 4
        PolicyType.PARENT_CHILD -> 3
        PolicyType.STUDENT -> 2
        PolicyType.ALL -> 1
    }

/**
 * Domain → ekran holati (Student `PolicyListInput.reduce` bilan bir xil tuzilish).
 * `busy`, `error`, `payWall`, `tab` va farzand ma'lumoti avvalgi holatdan qoladi.
 */
fun PolicyListInput.reduce(previous: PolicyListState): PolicyListState {
    fun Policy.active() = isActiveNow(weekDay, minuteOfDay, now, paid, lastLat, lastLng)
    fun Policy.card() = PolicyCardUi(
        policyId = id,
        title = name,
        ownership = ownership(myUserId),
        isEnabled = isActive,
        isActive = active(),
        isPaused = pausedUntil?.let { it > now } == true,
        isQuickBlock = isQuickBlock,
        summary = toSummary(),
    )
    fun QuickBlockEntry.card(owner: Ownership) = PolicyCardUi(
        policyId = policyId,
        title = "",                       // UI "Tezkor blok" deb yozadi
        ownership = owner,
        isEnabled = isActive,
        isActive = isEnforced(now) && paid != false,
        isPaused = isPaused(now),
        isQuickBlock = true,
        summary = PolicySummary(appCount = targets.packages.size, siteCount = targets.sites.size),
    )

    // Himoya paketlari va tezkor bloklar oddiy karta emas
    val standard = policies.filter { it.isStandard && it.deletedAt == null }
    val mineAll = standard.filter { it.ownership(myUserId) == Ownership.MINE }

    // ── Tayyor jadvallar: faqat o'zimning preset'li jadvalimga bog'lanadi ──
    val presets = PresetKind.entries.map { kind ->
        val p = mineAll.firstOrNull { it.preset == kind.preset }
        if (p == null) PresetPolicyUi(kind = kind, summary = kind.defaultSummary())
        else PresetPolicyUi(
            kind = kind,
            policyId = p.id,
            isEnabled = p.isActive,
            isActive = p.active(),
            isPaused = p.pausedUntil?.let { it > now } == true,
            summary = p.toSummary(),
        )
    }
    val presetIds = presets.mapNotNull { it.policyId }.toSet()
    val mine = mineAll.filter { it.id !in presetIds }.map { it.card() }

    // ── Tezkor blok: mening blokim shablon, boshqalarniki o'z tabida ──
    val own = quick.mine
    val quickBlock = own?.let {
        QuickBlockUi(
            policyId = it.policyId,
            isEnabled = it.isActive,
            isActive = it.isEnforced(now) && paid != false,
            isPaused = it.isPaused(now),
            appCount = it.targets.packages.size,
            pausedUntil = it.pausedUntil,
        )
    } ?: QuickBlockUi()
    // Har egada tezkor blok doim bor (server oldindan yaratadi: o'chiq, bo'sh) — bo'shi ham ko'rinadi
    val otherQuick = quick.entries.filter { !it.isMine(myUserId) }

    val child = standard.filter { it.ownership(myUserId) == Ownership.CHILD }.map { it.card() }
    val childQuick = otherQuick.filter { it.isChildOwner }.map { it.card(Ownership.CHILD) }
    val coParent = standard.filter { it.ownership(myUserId) == Ownership.COPARENT }.map { it.card() }
    val coParentQuick = otherQuick.filter { !it.isChildOwner }.map { it.card(Ownership.COPARENT) }
    val school = standard.filter { it.ownership(myUserId) == Ownership.SCHOOL }.map { it.card() }

    // ── Kontent himoya: bitta switch = barcha paketlar ──
    val protection = ContentProtectionUi(
        isEnabled = packs.any { it.enabledByMe },
        byOthers = packs.any { it.enabledByChild || it.enabledByCoParent },
        isAvailable = packs.isNotEmpty(),
    )

    val tabs = buildList {
        add(PolicyTab.TEMPLATES)
        add(PolicyTab.MINE)
        if (child.isNotEmpty() || childQuick.isNotEmpty()) add(PolicyTab.CHILD)
        if (coParent.isNotEmpty() || coParentQuick.isNotEmpty()) add(PolicyTab.COPARENT)
        if (school.isNotEmpty()) add(PolicyTab.SCHOOL)
    }

    // ── Izoh: faollar (kuchlisi birinchi), ustunlik, tezkor blok, pauza ──
    val activeAll = standard.filter { it.active() }
        .sortedWith(compareByDescending<Policy> { it.scope.rank }.thenByDescending { it.priority })
    val info = PolicyListInfo(
        activeTitles = activeAll.map { it.name }.distinct(),
        activeUntilMin = activeAll.firstOrNull()?.conditions?.time
            ?.takeIf { TimeRuleMatcher.matches(weekDay, minuteOfDay, it) }?.endMin,
        quickActive = paid != false && quick.entries.any { it.targets.packages.isNotEmpty() && it.isEnforced(now) },
        hasChildPolicies = child.isNotEmpty(),
        hasSchoolPolicies = school.isNotEmpty(),
        pausedCount = mineAll.count { it.isActive && it.pausedUntil?.let { p -> p > now } == true },
    )

    val enabledCount = (if (protection.isEnabled) 1 else 0) +
        presets.count { it.isEnabled } +
        (if (quickBlock.isOn) 1 else 0) +
        mine.count { it.isEnabled }

    return previous.copy(
        tab = if (previous.tab in tabs) previous.tab else PolicyTab.TEMPLATES,
        tabs = tabs,
        presets = presets,
        protection = protection,
        quickBlock = quickBlock,
        mine = mine,
        child = child,
        childQuick = childQuick,
        coParent = coParent,
        coParentQuick = coParentQuick,
        school = school,
        info = info,
        enabledCount = enabledCount,
        paid = paid,
        loaded = true,
    )
}

/** Bazada hali yo'q tayyor jadval kartasi — PresetDefaults qiymatlari ko'rsatiladi. */
fun PresetKind.defaultSummary(): PolicySummary {
    val d = uz.tikoncha_parent.domain.policy.PresetDefaults.of(preset, title = "") ?: return PolicySummary()
    return PolicySummary(
        startMin = d.conditions.time?.startMin,
        endMin = d.conditions.time?.endMin,
        days = d.conditions.time?.days?.map { it.num }?.toSet().orEmpty(),
        limitWindow = d.limits.usage?.window,
        limitMinutes = d.limits.usage?.minutes,
        categoryCount = d.targets.categories.size,
        allApps = d.targets.isAllApps,
    )
}
