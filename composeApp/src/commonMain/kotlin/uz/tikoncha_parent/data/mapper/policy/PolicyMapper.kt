package uz.tikoncha_parent.data.mapper.policy

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import uz.tikoncha_parent.data.remote.model.policy.ConditionsDto
import uz.tikoncha_parent.data.remote.model.policy.EvaluateInDto
import uz.tikoncha_parent.data.remote.model.policy.EvaluateOutDto
import uz.tikoncha_parent.data.remote.model.policy.LatLngDto
import uz.tikoncha_parent.data.remote.model.policy.LaunchLimitDto
import uz.tikoncha_parent.data.remote.model.policy.LimitsDto
import uz.tikoncha_parent.data.remote.model.policy.LocationConditionDto
import uz.tikoncha_parent.data.remote.model.policy.PackOutDto
import uz.tikoncha_parent.data.remote.model.policy.PacksListOutDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyCreateDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyEventListOutDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyEventOutDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyListOutDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyOutDto
import uz.tikoncha_parent.data.remote.model.policy.QuickBlockEntryDto
import uz.tikoncha_parent.data.remote.model.policy.QuickBlockInDto
import uz.tikoncha_parent.data.remote.model.policy.TargetsDto
import uz.tikoncha_parent.data.remote.model.policy.TimeConditionDto
import uz.tikoncha_parent.data.remote.model.policy.UsageLimitDto
import uz.tikoncha_parent.data.remote.model.policy.WifiConditionDto
import uz.tikoncha_parent.domain.model.GeoType
import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.LocationData
import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.WeekDay
import uz.tikoncha_parent.domain.model.policy.EvalDecision
import uz.tikoncha_parent.domain.model.policy.EvalReason
import uz.tikoncha_parent.domain.model.policy.EvalResult
import uz.tikoncha_parent.domain.model.policy.EvalTargetRef
import uz.tikoncha_parent.domain.model.policy.LaunchLimit
import uz.tikoncha_parent.domain.model.policy.PackCatalog
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyAuditEvent
import uz.tikoncha_parent.domain.model.policy.PolicyAuditPage
import uz.tikoncha_parent.domain.model.policy.PolicyConditions
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyEventType
import uz.tikoncha_parent.domain.model.policy.PolicyKind
import uz.tikoncha_parent.domain.model.policy.PolicyLimits
import uz.tikoncha_parent.domain.model.policy.PolicyListSnapshot
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.policy.ProtectionPack
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.model.policy.TargetType
import uz.tikoncha_parent.domain.model.policy.TimeCondition
import uz.tikoncha_parent.domain.model.policy.UsageLimit
import uz.tikoncha_parent.domain.model.policy.WifiCondition
import uz.tikoncha_parent.platform.Logger
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

private const val TAG = "PolicyMapper"

// ── O'QISH: DTO → domain ──────────────────────────────────────

fun PolicyOutDto.toDomain(): Policy = Policy(
    id = id,
    name = name,
    kind = PolicyKind.from(kind),
    preset = PolicyPreset.from(preset),
    scope = PolicyType.from(scope_type),
    scopeId = scope_id,
    actorUserId = actor_user_id,
    createdBy = created_by,
    action = PolicyAction.valueToPolicyAction(action),
    priority = priority,
    isActive = is_active,
    pausedUntil = paused_until?.toInstantOrNull(),
    expiresAt = expires_at?.toInstantOrNull(),
    packCode = pack_code,
    targets = targets.toDomain(),
    conditions = conditions.toDomain(),
    limits = limits.toDomain(),
    createdAt = created_at.toInstantOrNow(),
    updatedAt = updated_at.toInstantOrNow(),
    deletedAt = deleted_at?.toInstantOrNull(),
)

fun PolicyListOutDto.toDomain(): PolicyListSnapshot = PolicyListSnapshot(
    childId = child_id,
    items = items.map { it.toDomain() },
    asOf = as_of.toInstantOrNow(),
    packsVersion = packs_version,
)

fun TargetsDto.toDomain(): PolicyTargets = PolicyTargets(
    packages = packages,
    categories = categories,
    sites = sites,
    features = features,
    iosSelectionIds = ios_selection_ids,
    packs = packs,
)

fun ConditionsDto.toDomain(): PolicyConditions = PolicyConditions(
    time = time.mapNotNull { it.toDomain() },
    location = location.mapNotNull { it.toDomain() },
    wifi = wifi.map { WifiCondition(ssid = it.ssid, include = it.include) },
)

/** M1: kunlar bo'sh bo'lsa shart tashlanadi — server bunday shartni 422 bilan rad etadi. */
fun TimeConditionDto.toDomain(): TimeCondition? {
    val week = days.toWeekDays()
    if (week.isEmpty()) return null
    return TimeCondition(days = week, startMin = start_min, endMin = end_min, include = include)
}

/** M3: koordinatalar almashtirilmaydi. Noma'lum `type` yoki yetishmagan maydon → tashlanadi. */
fun LocationConditionDto.toDomain(): LocationRule? = when (type.uppercase()) {
    "CIRCLE" -> center?.let { c ->
        LocationRule(
            geoType = GeoType.CIRCLE,
            centerLat = c.lat,
            centerLng = c.lng,
            radiusMeters = radius_m,
            polygon = null,
            reverse = !include,
        )
    }

    "POLYGON" -> polygon?.takeIf { it.size >= 3 }?.let { points ->
        LocationRule(
            geoType = GeoType.POLYGON,
            centerLat = null,
            centerLng = null,
            radiusMeters = null,
            polygon = points.map { LocationData(lat = it.lat, lng = it.lng) },
            reverse = !include,
        )
    }

    else -> null
}

fun LimitsDto.toDomain(): PolicyLimits = PolicyLimits(
    usage = usage.mapNotNull { dto ->
        val week = dto.days.toWeekDays()
        if (week.isEmpty()) null
        else UsageLimit(days = week, window = LimitWindow.from(dto.window), minutes = dto.minutes)
    },
    launch = launch.mapNotNull { dto ->
        val week = dto.days.toWeekDays()
        if (week.isEmpty()) null else LaunchLimit(days = week, maxLaunches = dto.max_launches)
    },
)

fun QuickBlockEntryDto.toDomain(): QuickBlockEntry = QuickBlockEntry(
    policyId = policy_id,
    scope = PolicyType.from(scope_type),
    actorUserId = actor_user_id,
    targets = targets.toDomain(),
    updatedAt = updated_at.toInstantOrNow(),
)

fun PackOutDto.toDomain(): ProtectionPack = ProtectionPack(
    code = code,
    name = name,
    description = description,
    packageCount = packages.size,
    siteCount = sites.size,
    categoryCount = categories.size,
    featureCount = features.size,
    isActive = is_active,
    version = version,
)

fun PacksListOutDto.toDomain(): PackCatalog = PackCatalog(
    packs = items.map { it.toDomain() },
    version = version,
    asOf = as_of.toInstantOrNow(),
    categoriesMap = categories_map,
)

fun EvaluateOutDto.toDomain(): EvalResult = EvalResult(
    decision = EvalDecision.from(decision),
    reason = EvalReason.from(reason),
    policyId = policy_id,
    policyName = policy_name,
    scope = scope_type?.let { PolicyType.from(it) },
    action = action?.let { PolicyAction.valueToPolicyAction(it) },
    dueToLimit = due_to_limit,
    causes = causes,
    limitsEvaluated = limits_evaluated,
    asOf = as_of.toInstantOrNow(),
)

fun PolicyEventOutDto.toDomain(): PolicyAuditEvent = PolicyAuditEvent(
    id = id,
    policyId = policy_id,
    childUserId = child_user_id,
    actorUserId = actor_user_id,
    event = PolicyEventType.from(event),
    diff = diff.toDiffMap(),
    createdAt = created_at.toInstantOrNow(),
)

fun PolicyEventListOutDto.toDomain(): PolicyAuditPage = PolicyAuditPage(
    items = items.map { it.toDomain() },
    total = total,
    limit = limit,
    offset = offset,
)

// ── YOZISH: domain → DTO ──────────────────────────────────────

fun PolicyDraft.toCreateDto(childId: String): PolicyCreateDto = PolicyCreateDto(
    name = name.trim(),
    scope_type = PolicyType.PARENT_CHILD.name,
    scope_id = childId,
    action = action.name,
    priority = priority,
    is_active = isActive,
    preset = preset?.name,
    paused_until = pausedUntil?.toString(),
    expires_at = expiresAt?.toString(),
    targets = targets.toDto(),
    conditions = conditions.toDto(),
    limits = limits.toDto(),
)

/** M5: server saytni lower, kategoriyani UPPER kutadi — normallashtirib yuboramiz. */
fun PolicyTargets.toDto(): TargetsDto = TargetsDto(
    packages = packages.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
    categories = categories.map { it.trim().uppercase() }.filter { it.isNotEmpty() }.distinct(),
    sites = sites.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct(),
    features = features.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct(),
    ios_selection_ids = iosSelectionIds,
    packs = packs,
)

fun PolicyConditions.toDto(): ConditionsDto = ConditionsDto(
    time = time.mapNotNull { it.toDto() },
    location = location.mapNotNull { it.toDto() },
    wifi = wifi.map { WifiConditionDto(ssid = it.ssid, include = it.include) },
)

/** M2: `startMin == endMin` — server 422 beradi, shuning uchun element tashlanadi. */
fun TimeCondition.toDto(): TimeConditionDto? {
    if (days.isEmpty() || startMin == endMin) return null
    return TimeConditionDto(
        days = days.map { it.num }.sorted(),
        start_min = startMin,
        end_min = endMin,
        include = include,
    )
}

fun LocationRule.toDto(): LocationConditionDto? = when (geoType) {
    GeoType.CIRCLE -> {
        val lat = centerLat
        val lng = centerLng
        val radius = radiusMeters
        if (lat == null || lng == null || radius == null) null
        else LocationConditionDto(
            type = "CIRCLE",
            center = LatLngDto(lat = lat, lng = lng),
            radius_m = radius.coerceIn(MIN_RADIUS_M, MAX_RADIUS_M),
            include = !reverse,
        )
    }

    GeoType.POLYGON -> polygon?.takeIf { it.size >= 3 }?.let { points ->
        LocationConditionDto(
            type = "POLYGON",
            polygon = points.map { LatLngDto(lat = it.lat, lng = it.lng) },
            include = !reverse,
        )
    }
}

/** M4: HOUR oynasida 60 daqiqadan oshmaydi, DAY da 1440 dan. */
fun PolicyLimits.toDto(): LimitsDto = LimitsDto(
    usage = usage.mapNotNull { limit ->
        if (limit.days.isEmpty() || limit.minutes < 1) null
        else UsageLimitDto(
            days = limit.days.map { it.num }.sorted(),
            window = limit.window.name,
            minutes = when (limit.window) {
                LimitWindow.HOUR -> limit.minutes.coerceAtMost(MINUTES_PER_HOUR)
                LimitWindow.DAY -> limit.minutes.coerceAtMost(MINUTES_PER_DAY)
            },
        )
    },
    launch = launch.mapNotNull { limit ->
        if (limit.days.isEmpty() || limit.maxLaunches < 1) null
        else LaunchLimitDto(days = limit.days.map { it.num }.sorted(), max_launches = limit.maxLaunches)
    },
)

fun QuickBlockTarget.toDto(childId: String): QuickBlockInDto = when (type) {
    TargetType.APP -> QuickBlockInDto(child_id = childId, `package` = key)
    TargetType.SITE -> QuickBlockInDto(child_id = childId, site = key)
    TargetType.FEATURE -> QuickBlockInDto(child_id = childId, feature = key)
}

fun EvalTargetRef.toDto(
    childId: String,
    at: String? = null,
    lat: Double? = null,
    lng: Double? = null,
    wifiSsid: String? = null,
): EvaluateInDto = EvaluateInDto(
    child_id = childId,
    target_type = type.name,
    key = key,
    at = at,
    lat = lat,
    lng = lng,
    wifi_ssid = wifiSsid,
)

// ── Yordamchilar ──────────────────────────────────────────────

private const val MIN_RADIUS_M = 10
private const val MAX_RADIUS_M = 50_000
private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 1440

/** M1: 1..7 dan tashqaridagi qiymatlar jimgina tashlanadi. */
internal fun List<Int>.toWeekDays(): Set<WeekDay> =
    mapNotNull { n -> WeekDay.entries.firstOrNull { it.num == n } }.toSet()

/**
 * Server `updated_at`/`as_of` ni UTC (`…Z`) da, `created_at` ni esa mintaqa belgisisiz
 * mahalliy vaqtda qaytaradi. Ikkinchisi uchun server mintaqasi bo'yicha talqin qilamiz.
 */
internal fun String.toInstantOrNull(): Instant? =
    runCatching { Instant.parse(this) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(this).toInstant(SERVER_TIME_ZONE) }.getOrNull()

/** created_at / updated_at hech qachon null bo'lmaydi — parse yiqilsa hozirgi vaqt. */
internal fun String.toInstantOrNow(): Instant = toInstantOrNull() ?: run {
    Logger.e(TAG, "Instant parse xato: $this")
    Clock.System.now()
}

/** M9: {"is_active": {"from": true, "to": false}} → {"is_active" to ("true" to "false")}.
 * from/to obyekti bo'lmagan qiymat ({"key": "com.app"}, {"pack": "GAMBLING_APPS"}) — `to` ga tushadi.*/
internal fun JsonObject?.toDiffMap(): Map<String, Pair<String?, String?>> {
    if (this == null) return emptyMap()
    return mapValues { (_, value) ->
        val obj = value as? JsonObject
        if (obj != null && ("from" in obj || "to" in obj)) {
            obj["from"].asDiffText() to obj["to"].asDiffText()
        } else {
            null to value.asDiffText()
        }
    }
}

private fun JsonElement?.asDiffText(): String? = when (this) {
    null -> null
    is JsonPrimitive -> contentOrNull
    else -> toString()
}

private val SERVER_TIME_ZONE = TimeZone.of("Asia/Tashkent")