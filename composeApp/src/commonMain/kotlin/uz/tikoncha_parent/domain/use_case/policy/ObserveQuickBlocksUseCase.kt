package uz.tikoncha_parent.domain.use_case.policy

import kotlinx.coroutines.flow.Flow
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.repository.policy.QuickBlockRepository

class ObserveQuickBlocksUseCase(
    private val repository: QuickBlockRepository,
) {
    operator fun invoke(childId: String): Flow<List<QuickBlockEntry>> =
        repository.observeQuickBlocks(childId)
}