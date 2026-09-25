package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class UpdatePolicyUseCase(
    private val repository: PolicyRepository,
) {
    suspend operator fun invoke(policyId: String, patch: PolicyPatch): Outcome<Policy> {
        if (policyId.isBlank()) return Outcome.Failure(ErrorCause.NotFound)
        // Bo'sh PATCH — server `{}` ni qabul qiladi, lekin bu dasturiy xato.
        if (patch.isEmpty) return Outcome.Failure(ErrorCause.Validation)
        return repository.patchPolicy(policyId, patch)
    }
}