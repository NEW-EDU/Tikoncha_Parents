package uz.tikoncha_parent.domain.use_case.policy

import kotlin.time.Instant
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class PausePolicyUseCase(
    private val repository: PolicyRepository,
) {
    /** [until] `null` — pauzani bekor qilish ("Davom ettirish"). */
    suspend operator fun invoke(policyId: String, until: Instant?): Outcome<Policy> {
        val patch = if (until == null) PolicyPatch.resume() else PolicyPatch.pause(until)
        return repository.patchPolicy(policyId, patch)
    }
}