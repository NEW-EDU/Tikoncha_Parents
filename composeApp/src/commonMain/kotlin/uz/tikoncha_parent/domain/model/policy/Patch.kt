package uz.tikoncha_parent.domain.model.policy

import kotlin.time.Instant

/**
 * Qisman yangilash uchun maydon holati.
 *
 * [Unset] — maydon PATCH tanasiga umuman kirmaydi.
 * [Value] — kiradi; `value == null` bo'lsa JSON `null` yoziladi (maydon tozalanadi).
 */
sealed interface Patch<out T> {
    data object Unset : Patch<Nothing>
    data class Value<T>(val value: T) : Patch<T>
}

/**
 * `PATCH /v2/policies/{id}` tanasi.
 *
 * Server `exclude_unset` ishlatadi: tanada bor maydon qo'llanadi, `null` ham "bor" hisoblanadi.
 * Shu sababli faqat [preset], [pausedUntil], [expiresAt] null'lanadi — qolganlari non-null tip.
 */
data class PolicyPatch(
    val name: Patch<String> = Patch.Unset,
    val action: Patch<PolicyAction> = Patch.Unset,
    val priority: Patch<Int> = Patch.Unset,
    val isActive: Patch<Boolean> = Patch.Unset,
    val preset: Patch<PolicyPreset?> = Patch.Unset,
    val pausedUntil: Patch<Instant?> = Patch.Unset,
    val expiresAt: Patch<Instant?> = Patch.Unset,
    val targets: Patch<PolicyTargets> = Patch.Unset,
    val conditions: Patch<PolicyConditions> = Patch.Unset,
    val limits: Patch<PolicyLimits> = Patch.Unset,
) {
    val isEmpty: Boolean
        get() = listOf(
            name, action, priority, isActive, preset,
            pausedUntil, expiresAt, targets, conditions, limits,
        ).all { it is Patch.Unset }

    companion object {
        fun toggle(enabled: Boolean) = PolicyPatch(isActive = Patch.Value(enabled))
        fun pause(until: Instant) = PolicyPatch(pausedUntil = Patch.Value(until))
        /** Pauzani bekor qiladi — tanada `{"paused_until": null}`. */
        fun resume() = PolicyPatch(pausedUntil = Patch.Value(null))
    }
}