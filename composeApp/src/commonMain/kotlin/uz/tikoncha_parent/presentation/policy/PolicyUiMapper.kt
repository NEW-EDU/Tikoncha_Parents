package uz.tikoncha_parent.presentation.policy

import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import uz.tikoncha_parent.domain.model.DayHour
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.policy.Patch
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyConditions
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyLimits
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.policy.TimeCondition
import uz.tikoncha_parent.domain.model.policy.UsageLimit
import uz.tikoncha_parent.presentation.policy.common.toMinutes
import uz.tikoncha_parent.presentation.policy.limit_rule.LimitRuleUi
import uz.tikoncha_parent.presentation.policy.policy_list.PolicyItemUi
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedState
import uz.tikoncha_parent.presentation.policy.time_rule.TimeRuleUi
import kotlin.time.Instant
import kotlin.time.Clock

fun Policy.toItemUi(myUserId: String, now: Instant): PolicyItemUi = PolicyItemUi(
    policyId = id,
    policyName = name,
    action = action,
    kind = kind,
    preset = preset,
    packCode = packCode,
    policyType = scope,
    actorUserId = actorUserId,
    isMine = isMine(myUserId),
    canEdit = canEdit(myUserId),
    isActive = isActive,
    effectiveState = effectiveState(now),
    pausedUntil = pausedUntil,
    expiresAt = expiresAt,
    targets = targets,
    timeRule = conditions.time.mapIndexed { index, condition -> condition.toUi(index + 1) },
    limitRule = limits.usage.mapIndexed { index, limit -> limit.toUi(index + 1) },
    locationRule = conditions.location.firstOrNull(),
    extraLocations = conditions.location.drop(1),
    wifiRule = conditions.wifi,
    launchLimits = limits.launch,
)

fun TimeCondition.toUi(id: Int): TimeRuleUi = TimeRuleUi(
    id = id,
    startTime = startMin.toLocalTimeClamped(),
    endTime = endMin.toLocalTimeClamped(),
    reverse = !include,
    allDay = isAllDay,
    weekDays = days,
)

fun TimeRuleUi.toCondition(): TimeCondition = TimeCondition(
    days = weekDays,
    startMin = if (allDay) 0 else startTime.toMinutes(),
    endMin = if (allDay) TimeCondition.MINUTES_PER_DAY else endTime.toMinutes(),
    include = !reverse,
)

fun UsageLimit.toUi(id: Int): LimitRuleUi = LimitRuleUi(
    id = id,
    time = HourMinute.fromMinutes(minutes),
    weekDays = days,
    limitType = if (window == LimitWindow.HOUR) DayHour.HOUR else DayHour.DAY,
)

fun LimitRuleUi.toLimit(): UsageLimit = UsageLimit(
    days = weekDays,
    window = if (limitType == DayHour.HOUR) LimitWindow.HOUR else LimitWindow.DAY,
    minutes = time.toMinutes(),
)

/** 1439 va 1440 ikkalasi ham kun oxiri — ikkalasi 23:59 ga tushadi. */
private fun Int.toLocalTimeClamped(): LocalTime =
    if (this >= TimeCondition.END_OF_DAY_SENTINEL) LocalTime(23, 59)
    else LocalTime(this / 60, this % 60)


// ── Presentation → domain: saqlash uchun ──────────────────────
/**
 * [now] — tayyor muddat varianti shu paytdan hisoblanadi (saqlash tugmasi bosilgan payt).
 */
fun PolicySharedState.toDraft(
    now: Instant = Clock.System.now(),
    zone: TimeZone = TimeZone.currentSystemDefault(),
): PolicyDraft = PolicyDraft(
    name = policyTitle.trim(),
    action = policyAction,
    preset = preset,
    isActive = isActive,
    pausedUntil = pausedUntil,
    expiresAt = expiryOption?.until(now, zone) ?: expiresAt,
    targets = PolicyTargets(
        packages = selectedPkgs.toList(),
        categories = selectedCategories.toList(),
        sites = selectedSites.toList(),
        features = selectedFeatures.toList(),
        iosSelectionIds = iosSelectionIds,
        packs = packs,
    ),
    conditions = PolicyConditions(
        time = timeList.map { it.toCondition() },
        location = listOfNotNull(locationRule) + extraLocations,
        wifi = wifiList,
    ),
    limits = PolicyLimits(
        usage = limitList.map { it.toLimit() },
        launch = launchLimits,
    ),
)

/**
 * Tahrir: faqat o'zgargan bo'limlar PATCH ga kiradi.
 *
 * Server `targets`/`conditions`/`limits` ni BUTUNLAY almashtiradi, shuning uchun
 * o'zgarmagan bo'limni yubormaslik — passthrough maydonlarni saqlashning eng ishonchli yo'li.
 */
fun PolicySharedState.toPatch(): PolicyPatch {
    val draft = toDraft()
    val initial = initialDraftSnapshot

    val targetsChanged = initial == null ||
            initial.packages.toSet() != selectedPkgs ||
            initial.categories.toSet() != selectedCategories ||
            initial.sites.toSet() != selectedSites ||
            initial.features.toSet() != selectedFeatures

    val conditionsChanged = initial == null ||
            initial.timeList != timeList ||
            initial.locationRule != locationRule

    val limitsChanged = initial == null || initial.limitList != limitList

    // Muddat faqat ota-ona uni o'zgartirganda yuboriladi. Aks holda muddati o'tgan jadvalni
    // boshqa sabab bilan tahrirlaganda eski (o'tgan) vaqt qayta ketib, server 422 qaytarardi.
    val expiryChanged = expiryOption != null ||
            (initial != null && initial.expiresAt != expiresAt)

    return PolicyPatch(
        name = if (initial == null || initial.title != draft.name) Patch.Value(draft.name) else Patch.Unset,
        action = if (initial == null || initial.action != draft.action) Patch.Value(draft.action) else Patch.Unset,
        expiresAt = if (expiryChanged) Patch.Value(draft.expiresAt) else Patch.Unset,
        targets = if (targetsChanged) Patch.Value(draft.targets) else Patch.Unset,
        conditions = if (conditionsChanged) Patch.Value(draft.conditions) else Patch.Unset,
        limits = if (limitsChanged) Patch.Value(draft.limits) else Patch.Unset,
    )
}