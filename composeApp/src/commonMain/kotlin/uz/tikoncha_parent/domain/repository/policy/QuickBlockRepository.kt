package uz.tikoncha_parent.domain.repository.policy

import kotlinx.coroutines.flow.Flow
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.model.policy.QuickBlockResult
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget

interface QuickBlockRepository {
    fun observeQuickBlocks(childId: String): Flow<List<QuickBlockEntry>>
    fun cachedQuickBlocks(childId: String): List<QuickBlockEntry>
    suspend fun refresh(childId: String): Outcome<List<QuickBlockEntry>>
    suspend fun add(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult>
    suspend fun remove(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult>
}