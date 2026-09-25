package uz.tikoncha_parent.domain.model.policy

import kotlin.time.Instant

/** `POST /v2/policies` uchun loyiha. Scope'ni repository qo'yadi (doim PARENT_CHILD). */
data class PolicyDraft(
    val name: String,
    val action: PolicyAction,
    val targets: PolicyTargets,
    val conditions: PolicyConditions = PolicyConditions(),
    val limits: PolicyLimits = PolicyLimits(),
    val preset: PolicyPreset? = null,
    val priority: Int = 100,
    val isActive: Boolean = true,
    val expiresAt: Instant? = null,
    val pausedUntil: Instant? = null,
)