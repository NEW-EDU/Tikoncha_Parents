package uz.tikoncha_parent.data.mapper.policy

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import uz.tikoncha_parent.data.remote.model.policy.ConditionsDto
import uz.tikoncha_parent.data.remote.model.policy.LimitsDto
import uz.tikoncha_parent.data.remote.model.policy.TargetsDto
import uz.tikoncha_parent.domain.model.policy.Patch
import uz.tikoncha_parent.domain.model.policy.PolicyPatch

private val patchJson = Json { encodeDefaults = true }

/**
 * PATCH tanasi DTO emas, JsonObject.
 *
 * Sabab: TikonchaClient Json'ida `explicitNulls` sukut (true) + `encodeDefaults = true`,
 * ya'ni nullable DTO yuborilsa BARCHA null maydonlar tanaga tushadi va server
 * `exclude_unset` semantikasi bo'yicha ularni "o'zgartirish kerak" deb tushunadi.
 * JsonObject'da esa faqat biz qo'ygan kalitlar bo'ladi.
 */
fun PolicyPatch.toJsonObject(): JsonObject = buildJsonObject {
    name.ifSet { put("name", it.trim()) }
    action.ifSet { put("action", it.name) }
    priority.ifSet { put("priority", it) }
    isActive.ifSet { put("is_active", it) }
    preset.ifSet { put("preset", it?.name) }
    pausedUntil.ifSet { put("paused_until", it?.toString()) }
    expiresAt.ifSet { put("expires_at", it?.toString()) }
    targets.ifSet {
        put("targets", patchJson.encodeToJsonElement(TargetsDto.serializer(), it.toDto()))
    }
    conditions.ifSet {
        put("conditions", patchJson.encodeToJsonElement(ConditionsDto.serializer(), it.toDto()))
    }
    limits.ifSet {
        put("limits", patchJson.encodeToJsonElement(LimitsDto.serializer(), it.toDto()))
    }
}

private inline fun <T> Patch<T>.ifSet(block: (T) -> Unit) {
    if (this is Patch.Value) block(value)
}