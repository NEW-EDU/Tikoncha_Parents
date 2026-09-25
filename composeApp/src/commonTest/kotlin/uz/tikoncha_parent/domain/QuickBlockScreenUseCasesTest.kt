package uz.tikoncha_parent.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.PaidFeature
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.domain.model.policy.Patch
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyListSnapshot
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.model.policy.QuickBlockResult
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.model.policy.QuickOwner
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository
import uz.tikoncha_parent.domain.repository.policy.QuickBlockRepository
import uz.tikoncha_parent.domain.use_case.policy.AddQuickBlockAppsUseCase
import uz.tikoncha_parent.domain.use_case.policy.AddQuickBlockUseCase
import uz.tikoncha_parent.domain.use_case.policy.GetBlockableChildAppsUseCase
import uz.tikoncha_parent.domain.use_case.policy.UpdateOwnQuickBlockUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

private class ScriptedQuickRepo(private val failOn: String? = null) : QuickBlockRepository {
    val added = mutableListOf<String>()
    val patches = mutableListOf<PolicyPatch>()
    override fun observeQuickBlocks(childId: String): Flow<List<QuickBlockEntry>> = emptyFlow()
    override suspend fun refresh(childId: String): Outcome<List<QuickBlockEntry>> = Outcome.Success(emptyList())
    override suspend fun add(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult> =
        if (target.key == failOn) Outcome.Failure(ErrorCause.PremiumRequired(PaidFeature.QUICK_BLOCK))
        else { added += target.key; Outcome.Success(QuickBlockResult.ADDED) }
    override suspend fun remove(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult> =
        Outcome.Success(QuickBlockResult.REMOVED)
    override suspend fun update(childId: String, policyId: String, patch: PolicyPatch): Outcome<QuickBlockEntry> {
        patches += patch; return Outcome.Success(entry(QuickOwner.ME, id = policyId))
    }
}

private class AppsRepo(private val apps: List<InstalledApp>) : PolicyRepository {
    override suspend fun childApps(userId: String): Outcome<List<InstalledApp>> = Outcome.Success(apps)
    override fun observePolicies(childId: String): Flow<List<Policy>> = emptyFlow()
    override fun cachedPolicies(childId: String): List<Policy> = emptyList()
    override suspend fun refreshPolicies(childId: String, force: Boolean): Outcome<PolicyListSnapshot> = error("unused")
    override suspend fun createPolicy(childId: String, draft: PolicyDraft): Outcome<Policy> = error("unused")
    override suspend fun patchPolicy(policyId: String, patch: PolicyPatch): Outcome<Policy> = error("unused")
    override suspend fun deletePolicy(policyId: String): Outcome<Unit> = error("unused")
}

class QuickBlockScreenUseCasesTest {

    @Test
    fun addsOneByOneAndSkipsProtected() = runTest {
        val repo = ScriptedQuickRepo()
        val res = AddQuickBlockAppsUseCase(AddQuickBlockUseCase(repo))("child", listOf("a.b", "c.d"))
        assertEquals(Outcome.Success(2), res)
        assertEquals(listOf("a.b", "c.d"), repo.added)
    }

    /** Obuna yo'q — birinchisi o'tmasa xato qaytadi; keyingilari yuborilmaydi. */
    @Test
    fun firstFailureStops() = runTest {
        val repo = ScriptedQuickRepo(failOn = "a.b")
        val res = AddQuickBlockAppsUseCase(AddQuickBlockUseCase(repo))("child", listOf("a.b", "c.d"))
        assertEquals(ErrorCause.PremiumRequired(PaidFeature.QUICK_BLOCK), (res as Outcome.Failure).cause)
        assertEquals(emptyList(), repo.added)
    }

    @Test
    fun partialSuccessReportsCount() = runTest {
        val repo = ScriptedQuickRepo(failOn = "c.d")
        val res = AddQuickBlockAppsUseCase(AddQuickBlockUseCase(repo))("child", listOf("a.b", "c.d", "e.f"))
        assertEquals(Outcome.Success(1), res)
    }

    @Test
    fun pauseAndResumeSendOnlyPausedUntil() = runTest {
        val repo = ScriptedQuickRepo()
        val own = entry(QuickOwner.ME, "a.b", id = "qb")
        val until = Instant.parse("2026-09-25T13:00:00Z")
        UpdateOwnQuickBlockUseCase(repo).pause("child", own, until)
        UpdateOwnQuickBlockUseCase(repo).pause("child", own, null)
        assertEquals(listOf<Patch<Instant?>>(Patch.Value(until), Patch.Value(null)), repo.patches.map { it.pausedUntil })
        assertEquals(listOf(true, true), repo.patches.map { it.isActive is Patch.Unset && it.targets is Patch.Unset })
    }

    @Test
    fun blockableAppsHideTikonchaAndProtectedSortedByName() = runTest {
        val apps = listOf(
            InstalledApp("com.youtube", "YouTube", null, null, 1),
            InstalledApp("uz.tikoncha.student", "Tikoncha", null, null, 2),
            InstalledApp("com.android.settings", "Sozlamalar", null, null, 3),
            InstalledApp("com.instagram.android", "instagram", null, null, 4),
        )
        val res = GetBlockableChildAppsUseCase(AppsRepo(apps))("child")
        assertEquals(listOf("com.instagram.android", "com.youtube"), (res as Outcome.Success).data.map { it.packageName })
    }
}
