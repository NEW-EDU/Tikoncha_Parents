package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.repository.policy.QuickBlockRepository

class RefreshQuickBlocksUseCase(
    private val repository: QuickBlockRepository,
) {
    suspend operator fun invoke(childId: String): Outcome<List<QuickBlockEntry>> =
        repository.refresh(childId)
}