package uz.tikoncha_parent.domain.use_case.policy

import kotlinx.coroutines.flow.Flow
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class ObservePoliciesUseCase(
    private val repository: PolicyRepository,
) {
    operator fun invoke(childId: String): Flow<List<Policy>> =
        repository.observePolicies(childId)
}