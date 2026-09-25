package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class DeletePolicyUseCase(
    private val repository: PolicyRepository,
) {
    suspend operator fun invoke(policyId: String): Outcome<Unit> {
        if (policyId.isBlank()) return Outcome.Failure(ErrorCause.NotFound)
        return repository.deletePolicy(policyId)
    }
}