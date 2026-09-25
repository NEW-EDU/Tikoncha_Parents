package uz.tikoncha_parent.domain.repository.policy

import kotlinx.coroutines.flow.Flow
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.model.policy.QuickBlockResult
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget

interface QuickBlockRepository {
    fun observeQuickBlocks(childId: String): Flow<List<QuickBlockEntry>>
    suspend fun refresh(childId: String): Outcome<List<QuickBlockEntry>>
    suspend fun add(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult>
    suspend fun remove(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult>

    /**
     * Mening tezkor blokimni yoqish/o'chirish yoki to'xtatish — ro'yxat saqlanadi.
     * Ro'yxatning o'zi faqat [add]/[remove] bilan o'zgaradi (server `PATCH targets` ni rad etadi).
     */
    suspend fun update(childId: String, policyId: String, patch: PolicyPatch): Outcome<QuickBlockEntry>
}