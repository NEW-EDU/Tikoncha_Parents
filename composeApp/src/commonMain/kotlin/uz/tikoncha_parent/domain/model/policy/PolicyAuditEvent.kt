package uz.tikoncha_parent.domain.model.policy

import kotlin.time.Instant

/** O'zgarishlar tarixidagi bitta yozuv: kim, qachon, nima qilgan. */
data class PolicyAuditEvent(
    val id: String,
    val policyId: String,
    val childUserId: String?,
    val actorUserId: String?,
    val event: PolicyEventType,
    val diff: Map<String, Pair<String?, String?>>,
    val createdAt: Instant,
)

data class PolicyAuditPage(
    val items: List<PolicyAuditEvent>,
    val total: Int,
    val limit: Int,
    val offset: Int,
)