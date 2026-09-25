package uz.tikoncha_parent.domain.repository.policy

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import uz.tikoncha_parent.data.remote.model.AppDto
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.EvalResult
import uz.tikoncha_parent.domain.model.policy.EvalTargetRef
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyListSnapshot
import uz.tikoncha_parent.domain.model.policy.PolicyPatch

interface PolicyRepository {

    /** Bola qurilmasidagi ilovalar. */
    suspend fun childApps(userId: String): Outcome<List<AppDto>>

    /** Keshdagi ro'yxatni kuzatish — ekranlar shuni yig'adi. */
    fun observePolicies(childId: String): Flow<List<Policy>>

    /** Keshning hozirgi holati (sinxron o'qish kerak bo'lgan joylar uchun). */
    fun cachedPolicies(childId: String): List<Policy>

    /** Serverdan yangilash. Kesh bo'lsa `since` bilan delta, [force] bo'lsa to'liq. */
    suspend fun refreshPolicies(childId: String, force: Boolean = false): Outcome<PolicyListSnapshot>

    suspend fun getPolicy(policyId: String): Outcome<Policy>
    suspend fun createPolicy(childId: String, draft: PolicyDraft): Outcome<Policy>
    suspend fun patchPolicy(policyId: String, patch: PolicyPatch): Outcome<Policy>
    suspend fun deletePolicy(policyId: String): Outcome<Unit>

    suspend fun evaluate(
        childId: String,
        target: EvalTargetRef,
        at: LocalDateTime? = null,
    ): Outcome<EvalResult>
}