package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PolicyListSnapshot
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository
import uz.tikoncha_parent.domain.repository.policy.ProtectionPackRepository

class RefreshPoliciesUseCase(
    private val repository: PolicyRepository,
    private val packRepository: ProtectionPackRepository,
) {
    suspend operator fun invoke(childId: String, force: Boolean = false): Outcome<PolicyListSnapshot> {
        val result = repository.refreshPolicies(childId, force)

        // Server paket katalogi yangilanganini shu raqam bilan bildiradi.
        if (result is Outcome.Success) {
            val cachedVersion = packRepository.cachedCatalog()?.version
            if (cachedVersion != null && result.data.packsVersion > cachedVersion) {
                packRepository.catalog(force = true)
            }
        }
        return result
    }
}