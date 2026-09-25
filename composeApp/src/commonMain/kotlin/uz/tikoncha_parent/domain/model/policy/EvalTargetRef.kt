package uz.tikoncha_parent.domain.model.policy

import uz.tikoncha_parent.domain.model.PolicyType
import kotlin.time.Instant

/** "Hozir X yopiqmi?" so'rovi uchun nishon. */
data class EvalTargetRef(val type: TargetType, val key: String)

/** `POST /v2/policies/evaluate` javobi. */
data class EvalResult(
    val decision: EvalDecision,
    val reason: EvalReason,
    val policyId: String?,
    val policyName: String?,
    val scope: PolicyType?,
    val action: PolicyAction?,
    val dueToLimit: Boolean,
    val causes: List<String>,
    val limitsEvaluated: Boolean,
    val asOf: Instant,
)