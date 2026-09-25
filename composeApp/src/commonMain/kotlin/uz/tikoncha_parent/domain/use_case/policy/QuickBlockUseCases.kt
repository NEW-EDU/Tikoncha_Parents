package uz.tikoncha_parent.domain.use_case.policy

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.tikoncha_parent.domain.model.SubscriptionType
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.map
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.model.policy.QuickBlockResult
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.policy.ProtectedPackages
import uz.tikoncha_parent.domain.repository.PaymentRepository
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository
import uz.tikoncha_parent.domain.repository.policy.QuickBlockRepository
import kotlin.time.Instant

/**
 * Bolaning barcha tezkor bloklari bitta holat sifatida. Tarif ([QuickBlockSnapshot.paid])
 * bu oqimda yo'q — u [ChildPaidStatusUseCase] dan olinib `copy(paid = …)` bilan qo'yiladi.
 */
class ObserveQuickBlockSnapshotUseCase(
    private val repository: QuickBlockRepository,
) {
    operator fun invoke(childId: String, myUserId: String): Flow<QuickBlockSnapshot> =
        repository.observeQuickBlocks(childId).map { QuickBlockSnapshot(entries = it, myUserId = myUserId) }
}

/**
 * Statistikadagi qulf bosildi. Qaror [snapshot] bo'yicha:
 *  · mening blokimda (yoqilgan) → olib tashlash (doim bepul);
 *  · mening O'CHIQ blokimda allaqachon bor → blokni qayta yoqish (server `add` bunda
 *    `exists` qaytarib hech narsani o'zgartirmaydi);
 *  · aks holda → qo'shish (o'chiq blok ham qayta yoqiladi — UI oldin so'raydi, `reenableCount`).
 */
class ToggleQuickBlockUseCase(
    private val repository: QuickBlockRepository,
) {
    suspend operator fun invoke(
        childId: String,
        packageName: String,
        snapshot: QuickBlockSnapshot,
    ): Outcome<QuickBlockResult> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)
        if (!ProtectedPackages.canBlock(packageName)) return Outcome.Failure(ErrorCause.Validation)

        val target = QuickBlockTarget.app(packageName)
        val mine = snapshot.mine
        return when {
            snapshot.isBlockedByMe(packageName) -> repository.remove(childId, target)
            mine != null && !mine.isActive && snapshot.isInMyList(packageName) ->
                repository.update(childId, mine.policyId, PolicyPatch.toggle(true)).map { QuickBlockResult.ENABLED }
            else -> repository.add(childId, target)
        }
    }
}

/**
 * Bola pullik tarifdami — tezkor blok va himoya paketlari shunga bog'liq (server `_paid`).
 * `null` — hali ma'lum emas: UI oldindan to'smaydi, server javobi hal qiladi.
 */
class ChildPaidStatusUseCase(
    private val repository: PaymentRepository,
) {
    operator fun invoke(childId: String): Boolean? =
        repository.cachedSubscriptionLimit(childId)?.let { it.subscriptionType != SubscriptionType.FREE }

    /** Serverdan yangilab o'qiydi — ekran qayta ko'ringanda (sotib olgandan keyin darhol ishlasin). */
    suspend fun refresh(childId: String): Boolean? {
        repository.syncSubscriptionLimits()
        return invoke(childId)
    }
}

/** Mening tezkor blokim: yoqish/o'chirish va to'xtatish. Ro'yxat o'zgarmaydi. */
class UpdateOwnQuickBlockUseCase(
    private val repository: QuickBlockRepository,
) {
    suspend fun setEnabled(childId: String, own: QuickBlockEntry, enabled: Boolean): Outcome<QuickBlockEntry> =
        repository.update(childId, own.policyId, PolicyPatch.toggle(enabled))

    /** [until] `null` — pauzani bekor qilish. */
    suspend fun pause(childId: String, own: QuickBlockEntry, until: Instant?): Outcome<QuickBlockEntry> =
        repository.update(childId, own.policyId, if (until == null) PolicyPatch.resume() else PolicyPatch.pause(until))
}

/**
 * Bir nechta ilovani qo'shish. Server bitta so'rovda bittasini qabul qiladi — birin-ketin,
 * birinchi xatoda to'xtaydi (masalan, obuna yo'q: qolganlari ham o'tmaydi).
 * Natija — qo'shilganlar soni.
 */
class AddQuickBlockAppsUseCase(
    private val add: AddQuickBlockUseCase,
) {
    suspend operator fun invoke(childId: String, packages: Collection<String>): Outcome<Int> {
        var added = 0
        for (pkg in packages) {
            when (val res = add(childId, QuickBlockTarget.app(pkg))) {
                is Outcome.Failure -> return if (added == 0) res else Outcome.Success(added)
                is Outcome.Success -> added++
            }
        }
        return Outcome.Success(added)
    }
}

/** Bola qurilmasidagi, bloklash mumkin bo'lgan ilovalar (Tikoncha va himoyalanganlar yo'q), nom bo'yicha. */
class GetBlockableChildAppsUseCase(
    private val repository: PolicyRepository,
) {
    suspend operator fun invoke(childId: String): Outcome<List<InstalledApp>> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)
        return repository.childApps(childId).map { apps ->
            apps.filter { ProtectedPackages.canBlock(it.packageName) }.sortedBy { it.name.lowercase() }
        }
    }
}
