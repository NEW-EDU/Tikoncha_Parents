@file:OptIn(ExperimentalSerializationApi::class)

package uz.tikoncha_parent.data.remote.model.policy

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * v2 javob envelope'i — barcha /v2 endpointlari uchun bitta generik sinf.
 * HTTP doim 200; haqiqiy status [code] da (200/201/403/422...).
 */
@Serializable
data class ApiEnvelope<T>(
    val success: Boolean = false,
    val data: T? = null,
    val error: String? = null,
    val code: Int? = null,
)

// ── targets / conditions / limits ─────────────────────────────

@Serializable
data class TargetsDto(
    val packages: List<String> = emptyList(),
    val categories: List<String> = emptyList(),   // server UPPER qiladi
    val sites: List<String> = emptyList(),        // server lower qiladi, subdomen mos keladi
    val features: List<String> = emptyList(),     // server lower qiladi ("youtube_shorts")
    val ios_selection_ids: List<String> = emptyList(),
    val packs: List<String> = emptyList(),        // ProtectionPack.code — havola, nusxa emas
)

@Serializable
data class TimeConditionDto(
    val days: List<Int>,        // ISO 1..7, bo'sh emas
    val start_min: Int,         // 0..1439
    val end_min: Int,           // 0..1440; 1439 ≡ 1440; start >= end → tunni kesib o'tadi
    val include: Boolean = true,
)

@Serializable
data class LatLngDto(val lat: Double, val lng: Double)

/**
 * CIRCLE va POLYGON — bitta yassi DTO, [type] diskriminator.
 *
 * ⚠️ Ishlatilmagan maydonlar @EncodeDefault(NEVER) bilan belgilangan:
 * global encodeDefaults = true bo'lsa ham `null` qiymatlar JSON'ga YOZILMAYDI.
 * Server bu yerda diskriminatorli birlashma kutadi — ortiqcha `null` yubormaslik kerak.
 */
@Serializable
data class LocationConditionDto(
    val type: String,                                     // "CIRCLE" | "POLYGON"
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val center: LatLngDto? = null,                        // CIRCLE
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val radius_m: Int? = null,                            // CIRCLE, 10..50_000
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val polygon: List<LatLngDto>? = null,                 // POLYGON, 3..500 nuqta
    val include: Boolean = true,
)

@Serializable
data class WifiConditionDto(val ssid: String, val include: Boolean = true)

@Serializable
data class ConditionsDto(
    val time: List<TimeConditionDto> = emptyList(),
    val location: List<LocationConditionDto> = emptyList(),
    val wifi: List<WifiConditionDto> = emptyList(),
)

@Serializable
data class UsageLimitDto(
    val days: List<Int>,
    val window: String = "DAY",   // "DAY" | "HOUR"; HOUR → minutes <= 60
    val minutes: Int,             // 1..1440
)

/** Parents yaratmaydi — faqat passthrough (tahrirda yo'qolmasligi uchun). */
@Serializable
data class LaunchLimitDto(val days: List<Int>, val max_launches: Int)

@Serializable
data class LimitsDto(
    val usage: List<UsageLimitDto> = emptyList(),
    val launch: List<LaunchLimitDto> = emptyList(),
)

// ── policy ────────────────────────────────────────────────────

@Serializable
data class PolicyCreateDto(
    val name: String,                       // 1..120, bo'sh emas
    val scope_type: String,                 // Parents: doim "PARENT_CHILD"
    val scope_id: String,                   // bola user id
    val action: String,                     // "ALLOW" | "DENY"
    val priority: Int = 100,                // 0..100000, katta = kuchli
    val is_active: Boolean = true,
    val preset: String? = null,             // "SLEEP" | "APP_LIMIT" | "CONTENT" | "PROTECTION"
    val paused_until: String? = null,       // ISO-8601 UTC: "2026-09-10T05:00:00Z"
    val expires_at: String? = null,         // kelajakda bo'lishi shart
    val targets: TargetsDto,                // bo'sh bo'lmasin (422 policy_targets_required)
    val conditions: ConditionsDto = ConditionsDto(),
    val limits: LimitsDto = LimitsDto(),
)

/**
 * PATCH tanasi DTO EMAS — JsonObject (PolicyPatchEncoder, 1D-bo'lak).
 * Ruxsat etilgan maydonlar: name, action, priority, is_active, preset,
 * paused_until, expires_at, targets, conditions, limits.
 */

@Serializable
data class PolicyOutDto(
    val id: String,
    val schema_version: Int = 2,
    val name: String,
    val kind: String = "STANDARD",          // "STANDARD" | "QUICK_BLOCK"
    val preset: String? = null,
    val scope_type: String,                 // "SCHOOL" | "STUDENT" | "PARENT_CHILD" | "ALL"
    val scope_id: String,
    val actor_user_id: String? = null,      // PARENT_CHILD: ota-ona user id
    val created_by: String? = null,
    val action: String,
    val priority: Int = 100,
    val is_active: Boolean = true,
    val paused_until: String? = null,
    val expires_at: String? = null,
    val pack_code: String? = null,          // preset = PROTECTION
    val targets: TargetsDto = TargetsDto(),
    val conditions: ConditionsDto = ConditionsDto(),
    val limits: LimitsDto = LimitsDto(),
    val created_at: String,
    val updated_at: String,
    val deleted_at: String? = null,         // faqat ?since= javobida bo'lishi mumkin
    val effective_active: Boolean = true,   // server hisoblagan
)

@Serializable
data class PolicyListOutDto(
    val items: List<PolicyOutDto> = emptyList(),
    val as_of: String,                      // keyingi `since` uchun
    val child_id: String,
    val packs_version: Int = 0,
)

@Serializable
data class PolicyDeleteOutDto(val deleted: Boolean = false, val policy_id: String? = null)

// ── quick block ───────────────────────────────────────────────

@Serializable
data class QuickBlockInDto(
    val child_id: String,
    val `package`: String? = null,
    val site: String? = null,
    val feature: String? = null,            // aynan bittasi to'ldiriladi
)

@Serializable
data class QuickBlockOutDto(
    val policy_id: String,
    val result: String,                     // "added" | "exists" | "removed" | "absent"
    val targets: TargetsDto = TargetsDto(),
)

@Serializable
data class QuickBlockEntryDto(
    val policy_id: String,
    val scope_type: String,                 // "STUDENT" (bola o'zi) | "PARENT_CHILD"
    val actor_user_id: String? = null,      // PARENT_CHILD: qaysi ota-ona
    val targets: TargetsDto = TargetsDto(),
    val updated_at: String,
)

@Serializable
data class QuickBlockListOutDto(
    val child_id: String,
    val items: List<QuickBlockEntryDto> = emptyList(),
)

// ── evaluate ──────────────────────────────────────────────────

@Serializable
data class EvaluateInDto(
    val child_id: String,
    val target_type: String,                // "APP" | "SITE" | "FEATURE"
    val key: String,                        // paket | domen | feature kodi
    val at: String? = null,                 // bolaning MAHALLIY vaqti: "2026-09-10T14:30:00"
    val lat: Double? = null,
    val lng: Double? = null,
    val wifi_ssid: String? = null,
)

@Serializable
data class EvaluateOutDto(
    val decision: String,                   // "ALLOW" | "BLOCK"
    val reason: String,
    val policy_id: String? = null,
    val policy_name: String? = null,
    val scope_type: String? = null,
    val action: String? = null,
    val due_to_limit: Boolean = false,
    val causes: List<String> = emptyList(), // TIME | LOCATION | WIFI | LIMIT | ALLOWLIST
    val limits_evaluated: Boolean = false,
    val as_of: String,
)

// ── packs ─────────────────────────────────────────────────────

@Serializable
data class PackOutDto(
    val code: String,                                     // "GAMBLING_APPS" | "ADULT_SITES" | …
    val name: Map<String, String> = emptyMap(),           // {"uz","ru","en"}
    val description: Map<String, String>? = null,
    val packages: List<String> = emptyList(),
    val sites: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val features: List<String> = emptyList(),
    val is_active: Boolean = true,
    val version: Int = 0,
    val updated_at: String,
)

@Serializable
data class PacksListOutDto(
    val items: List<PackOutDto> = emptyList(),
    val as_of: String,
    val version: Int = 0,
    /** META kategoriya (GAMES, SOCIAL…) → Play Store leaf'lari. */
    val categories_map: Map<String, List<String>> = emptyMap(),
)

// ── events ────────────────────────────────────────────────────

@Serializable
data class PolicyEventOutDto(
    val id: String,
    val policy_id: String,
    val child_user_id: String? = null,
    val actor_user_id: String? = null,
    val event: String,                      // CREATED | UPDATED | ENABLED | …
    val diff: JsonObject? = null,           // {"is_active": {"from": true, "to": false}}
    val created_at: String,
)

@Serializable
data class PolicyEventListOutDto(
    val items: List<PolicyEventOutDto> = emptyList(),
    val total: Int = 0,
    val limit: Int = 50,
    val offset: Int = 0,
)