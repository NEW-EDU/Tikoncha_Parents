package uz.tikoncha_parent.data.policy

import kotlinx.serialization.json.Json

/** TikonchaClient bilan bir xil sozlama — testlar haqiqiy dekoderni tekshirsin. */
internal val wireJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

/**
 * Backend `PolicyOut` / `QuickBlockOut` / `QuickBlockListOut` sxemalaridan
 * (`model_dump_json`) olingan — prod qaytaradigan shaklning o'zi (v2.1, `602c089`).
 */
internal object Wire {
    const val SCHOOL_POLICY = """{"id":"33333333-3333-3333-3333-333333333333","schema_version":2,"name":"Dars vaqti","kind":"STANDARD","preset":"SCHOOL","scope_type":"PARENT_CHILD","scope_id":"11111111-1111-1111-1111-111111111111","actor_user_id":"22222222-2222-2222-2222-222222222222","created_by":"22222222-2222-2222-2222-222222222222","action":"DENY","priority":100,"is_active":true,"paused_until":null,"expires_at":null,"pack_code":null,"targets":{"packages":["*"],"categories":[],"sites":[],"features":[],"ios_selection_ids":[],"packs":[],"exclude_packages":["com.android.dialer"]},"conditions":{"time":{"days":[1,2,3,4,5],"start_min":480,"end_min":780,"include":true},"location":{"type":"CIRCLE","center":{"lat":41.3,"lng":69.2},"radius_m":300,"include":true},"wifi":[]},"limits":{"usage":{"days":[6,7],"window":"HOUR","minutes":20},"launch":null},"created_at":"2026-09-25T10:00:00Z","updated_at":"2026-09-25T10:00:00Z","deleted_at":null,"effective_active":true}"""

    const val QUICK_BLOCK_ADDED = """{"policy_id":"44444444-4444-4444-4444-444444444444","result":"added","targets":{"packages":["com.instagram.android"],"categories":[],"sites":[],"features":[],"ios_selection_ids":[],"packs":[],"exclude_packages":[]},"policy":{"id":"44444444-4444-4444-4444-444444444444","schema_version":2,"name":"Tezkor blok","kind":"QUICK_BLOCK","preset":null,"scope_type":"PARENT_CHILD","scope_id":"11111111-1111-1111-1111-111111111111","actor_user_id":"22222222-2222-2222-2222-222222222222","created_by":"22222222-2222-2222-2222-222222222222","action":"DENY","priority":100,"is_active":true,"paused_until":null,"expires_at":null,"pack_code":null,"targets":{"packages":["com.instagram.android"],"categories":[],"sites":[],"features":[],"ios_selection_ids":[],"packs":[],"exclude_packages":[]},"conditions":{"time":null,"location":null,"wifi":[]},"limits":{"usage":null,"launch":null},"created_at":"2026-09-25T10:00:00Z","updated_at":"2026-09-25T10:00:00Z","deleted_at":null,"effective_active":true}}"""

    const val QUICK_BLOCK_LIST = """{"child_id":"11111111-1111-1111-1111-111111111111","items":[{"policy_id":"44444444-4444-4444-4444-444444444444","scope_type":"PARENT_CHILD","actor_user_id":"22222222-2222-2222-2222-222222222222","targets":{"packages":["com.instagram.android"],"categories":[],"sites":[],"features":[],"ios_selection_ids":[],"packs":[],"exclude_packages":[]},"updated_at":"2026-09-25T10:00:00Z","is_active":false,"paused_until":null},{"policy_id":"55555555-5555-5555-5555-555555555555","scope_type":"STUDENT","actor_user_id":null,"targets":{"packages":["com.whatsapp"],"categories":[],"sites":[],"features":[],"ios_selection_ids":[],"packs":[],"exclude_packages":[]},"updated_at":"2026-09-25T10:00:00Z","is_active":true,"paused_until":"2026-09-25T12:00:00Z"}]}"""

    /** v2.0 shakli (ro'yxatlar) — prod endi buni qaytarmaydi. */
    const val V20_CONDITIONS = """{"time":[{"days":[1],"start_min":0,"end_min":60,"include":true}],"location":[],"wifi":[]}"""

    const val CHILD_ID = "11111111-1111-1111-1111-111111111111"
    const val PARENT_ID = "22222222-2222-2222-2222-222222222222"

    fun envelope(data: String, success: Boolean = true, code: Int = 200, error: String? = null): String =
        """{"success":$success,"data":$data,"error":${error?.let { "\"$it\"" } ?: "null"},"code":$code}"""
}
