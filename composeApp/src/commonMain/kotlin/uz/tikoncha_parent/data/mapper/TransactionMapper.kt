package uz.tikoncha_parent.data.mapper

import kotlinx.datetime.LocalDateTime
import uz.tikoncha_parent.data.remote.model.transaction.TransactionDto
import uz.tikoncha_parent.data.remote.model.transaction.TransactionHistoryData
import uz.tikoncha_parent.domain.model.transaction.PlanDuration
import uz.tikoncha_parent.domain.model.transaction.PurchaseType
import uz.tikoncha_parent.domain.model.transaction.Transaction
import uz.tikoncha_parent.domain.model.transaction.TransactionPage
import uz.tikoncha_parent.domain.model.transaction.TransactionStatus

internal fun TransactionDto.toDomain(): Transaction = Transaction(
    id = id,
    userId = userId,
    childUserId = childUserId,
    userFirstName = userFirstName.orEmpty(),
    userLastName = userLastName.orEmpty(),
    userPhone = userPhone.orEmpty(),
    isUserRegistered = isUserRegistered,
    merchantTransId = merchantTransId,
    amount = amount,
    originalAmount = originalAmount,
    coins = coins,
    status = TransactionStatus.from(status),
    purchaseType = PurchaseType.from(purchaseType),
    planName = planName,
    planDuration = planDuration?.let(PlanDuration::from),
    expiredAt = expiredAt?.let(::parseDateTimeOrNull),
    createdAt = createdAt?.let(::parseDateTimeOrNull),
    isAutopay = provider.equals("PAYLOV", ignoreCase = true),
    cardMask = cardMask
)

internal fun TransactionHistoryData.toDomain(): TransactionPage = TransactionPage(
    items = items.map { it.toDomain() },
    total = total,
    limit = limit,
    offset = offset,
    hasNext = hasNext,
    hasPrevious = hasPrevious,
)

private fun parseDateTimeOrNull(raw: String): LocalDateTime? = try {
    // Server formati: "2026-05-14T10:37:32.710826"
    // kotlinx-datetime ISO LocalDateTime'ni qabul qiladi
    LocalDateTime.parse(raw)
} catch (e: Exception) {
    null
}