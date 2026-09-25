package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class TogglePolicyUseCase(
    private val repository: PolicyRepository,
) {
    suspend operator fun invoke(policyId: String, enabled: Boolean): Outcome<Policy> =
        repository.patchPolicy(policyId, PolicyPatch.toggle(enabled))
}