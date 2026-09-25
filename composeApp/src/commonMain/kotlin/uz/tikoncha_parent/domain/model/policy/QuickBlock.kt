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
    ADDED, EXISTS, REMOVED, ABSENT;

    companion object {
        fun from(raw: String?): QuickBlockResult =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: EXISTS
    }
}

/** Bitta shaxsning (bola yoki ota-ona) tezkor bloklari to'plami. */
data class QuickBlockEntry(
    val policyId: String,
    val scope: PolicyType,
    val actorUserId: String?,
    val targets: PolicyTargets,
    val updatedAt: Instant
) {
    fun isMine(myUserId: String): Boolean =
        scope == PolicyType.PARENT_CHILD && actorUserId == myUserId

    val isChildOwner: Boolean get() = scope == PolicyType.STUDENT
}
