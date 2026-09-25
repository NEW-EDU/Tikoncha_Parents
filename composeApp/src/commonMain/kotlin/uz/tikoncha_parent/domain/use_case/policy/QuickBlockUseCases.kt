package uz.tikoncha_parent.domain.use_case.policy

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.tikoncha_parent.domain.model.SubscriptionType
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.map
import uz.tikoncha_parent.domain.model.policy.QuickBlockResult
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.policy.ProtectedPackages
import uz.tikoncha_parent.domain.repository.PaymentRepository
import uz.tikoncha_parent.domain.repository.policy.QuickBlockRepository

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
                repository.setEnabled(childId, mine.policyId, enabled = true).map { QuickBlockResult.ENABLED }
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
