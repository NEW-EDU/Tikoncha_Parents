package uz.tikoncha_parent.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.Patch
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.model.policy.QuickBlockResult
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.model.policy.QuickOwner
import uz.tikoncha_parent.domain.repository.policy.QuickBlockRepository
import uz.tikoncha_parent.domain.use_case.policy.ToggleQuickBlockUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeQuickBlockRepository : QuickBlockRepository {
    val calls = mutableListOf<String>()
    override fun observeQuickBlocks(childId: String): Flow<List<QuickBlockEntry>> = emptyFlow()
    override suspend fun refresh(childId: String): Outcome<List<QuickBlockEntry>> = Outcome.Success(emptyList())
    override suspend fun add(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult> {
        calls += "add:${target.key}"; return Outcome.Success(QuickBlockResult.ADDED)
    }
    override suspend fun remove(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult> {
        calls += "remove:${target.key}"; return Outcome.Success(QuickBlockResult.REMOVED)
    }
    override suspend fun update(childId: String, policyId: String, patch: PolicyPatch): Outcome<QuickBlockEntry> {
        val enabled = (patch.isActive as? Patch.Value)?.value
        val paused = (patch.pausedUntil as? Patch.Value)?.let { "pause:${it.value}" }
        calls += paused?.let { "$it:$policyId" } ?: "enable:$policyId:$enabled"
        return Outcome.Success(entry(QuickOwner.ME, id = policyId))
    }
}

class ToggleQuickBlockUseCaseTest {

    private val repo = FakeQuickBlockRepository()
    private val toggle = ToggleQuickBlockUseCase(repo)
    private fun snap(vararg e: QuickBlockEntry) = QuickBlockSnapshot(e.toList(), ME, paid = true)

    @Test
    fun blockedByMeIsRemoved() = runTest {
        assertEquals(Outcome.Success(QuickBlockResult.REMOVED), toggle("child", "a.b", snap(entry(QuickOwner.ME, "a.b"))))
        assertEquals(listOf("remove:a.b"), repo.calls)
    }

    /** Server `add` o'chiq blokdagi mavjud ilovaga `exists` deydi va hech narsa o'zgarmaydi. */
    @Test
    fun appInMyDisabledBlockReenablesTheBlock() = runTest {
        val res = toggle("child", "a.b", snap(entry(QuickOwner.ME, "a.b", active = false, id = "qb-1")))
        assertEquals(Outcome.Success(QuickBlockResult.ENABLED), res)
        assertEquals(listOf("enable:qb-1:true"), repo.calls)
    }

    @Test
    fun otherwiseAdds() = runTest {
        toggle("child", "c.d", snap(entry(QuickOwner.ME, "a.b", active = false)))   // o'chiq blokka yangi ilova
        toggle("child", "a.b", snap(entry(QuickOwner.CHILD, "a.b")))                 // bolaniki — o'zimnikiga qo'shaman
        assertEquals(listOf("add:c.d", "add:a.b"), repo.calls)
    }

    @Test
    fun protectedAppIsRejectedWithoutRequest() = runTest {
        val res = toggle("child", "uz.tikoncha.parent", snap())
        assertEquals(ErrorCause.Validation, (res as Outcome.Failure).cause)
        assertTrue(repo.calls.isEmpty())
    }

    @Test
    fun noChildNoRequest() = runTest {
        assertEquals(ErrorCause.ChildNotSelected, (toggle("", "a.b", snap()) as Outcome.Failure).cause)
        assertTrue(repo.calls.isEmpty())
    }
}
