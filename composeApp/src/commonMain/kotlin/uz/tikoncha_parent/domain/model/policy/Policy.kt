package uz.tikoncha_parent.domain.model.policy

import kotlin.time.Instant
import uz.tikoncha_parent.domain.model.PolicyType

/** Serverdagi bitta mustaqil jadval. */
data class Policy(
    val id: String,
    val name: String,
    val kind: PolicyKind,
    val preset: PolicyPreset?,
    val scope: PolicyType,
    /** PARENT_CHILD / STUDENT da — bola user id. */
    val scopeId: String,
    /** PARENT_CHILD da — jadvalni yaratgan ota-ona user id. */
    val actorUserId: String?,
    val createdBy: String?,
    val action: PolicyAction,
    val priority: Int,
    val isActive: Boolean,
    val pausedUntil: Instant?,
    val expiresAt: Instant?,
    val packCode: String?,
    val targets: PolicyTargets,
    val conditions: PolicyConditions,
    val limits: PolicyLimits,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
) {
    val isQuickBlock: Boolean get() = kind == PolicyKind.QUICK_BLOCK
    val isProtection: Boolean get() = preset == PolicyPreset.PROTECTION

    /** Ro'yxatning "Jadvallar" bo'limiga kiradigan oddiy jadval. */
    val isStandard: Boolean get() = kind == PolicyKind.STANDARD && !isProtection

    /** Ota-ona faqat o'zi yaratgan PARENT_CHILD jadvalni tahrirlaydi (server `_authorize_edit`). */
    fun isMine(myUserId: String): Boolean =
        scope == PolicyType.PARENT_CHILD && actorUserId == myUserId

    fun canEdit(myUserId: String): Boolean = isMine(myUserId)

    /** Server `effective_active` bilan bir xil mantiq, lekin qurilma vaqtida hisoblanadi. */
    fun effectiveState(now: Instant): PolicyEffectiveState = when {
        deletedAt != null || !isActive -> PolicyEffectiveState.OFF
        pausedUntil != null && pausedUntil > now -> PolicyEffectiveState.PAUSED
        expiresAt != null && expiresAt <= now -> PolicyEffectiveState.EXPIRED
        else -> PolicyEffectiveState.ACTIVE
    }

    /** Server `count_standard_active` bilan bir xil: bepul tarif limitini yeydigan jadval. */
    fun countsTowardsPolicyLimit(): Boolean =
        isStandard && isActive &&
                (scope == PolicyType.PARENT_CHILD || scope == PolicyType.STUDENT)
}

/** `GET /v2/policies` natijasi — delta sinxronizatsiya uchun [asOf] bilan. */
data class PolicyListSnapshot(
    val childId: String,
    val items: List<Policy>,
    val asOf: Instant,
    val packsVersion: Int,
)