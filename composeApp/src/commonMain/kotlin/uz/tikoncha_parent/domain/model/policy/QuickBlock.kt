package uz.tikoncha_parent.domain.model.policy

import uz.tikoncha_parent.domain.model.PolicyType
import kotlin.time.Instant

data class QuickBlockTarget(val type: TargetType, val key: String) {
    companion object {
        fun app(pkg: String) = QuickBlockTarget(TargetType.APP, pkg.trim())
        fun site(domain: String) = QuickBlockTarget(TargetType.SITE, domain.trim().lowercase())
        fun feature(code: String) = QuickBlockTarget(TargetType.FEATURE, code.trim().lowercase())
    }
}

enum class QuickBlockResult {
    ADDED, EXISTS, REMOVED, ABSENT,
    /** O'chiq blok qayta yoqildi (ilova allaqachon ro'yxatda edi) — serverdan kelmaydi. */
    ENABLED;

    companion object {
        fun from(raw: String?): QuickBlockResult =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: EXISTS
    }
}

/**
 * Bitta shaxsning (bola yoki ota-ona) tezkor bloklari to'plami.
 *
 * O'chirilgan ([isActive] = false) yoki pauzadagi blok ro'yxatini saqlaydi, lekin hech
 * narsani yopmaydi — ekranda "yopiq" deb ko'rsatilmaydi.
 */
data class QuickBlockEntry(
    val policyId: String,
    val scope: PolicyType,
    val actorUserId: String?,
    val targets: PolicyTargets,
    val updatedAt: Instant,
    val isActive: Boolean = true,
    val pausedUntil: Instant? = null,
) {
    fun isMine(myUserId: String): Boolean =
        scope == PolicyType.PARENT_CHILD && actorUserId == myUserId

    val isChildOwner: Boolean get() = scope == PolicyType.STUDENT

    fun isPaused(now: Instant): Boolean = pausedUntil != null && pausedUntil > now

    /** Hozir haqiqatan yopyaptimi. */
    fun isEnforced(now: Instant): Boolean = isActive && !isPaused(now)
}
