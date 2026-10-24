package uz.tikoncha_parent.data.mapper

import uz.tikoncha_parent.common.DateTimeUtil
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import uz.tikoncha_parent.data.remote.model.SubscriptionStatusDto
import uz.tikoncha_parent.domain.model.subscription.PlanDuration
import uz.tikoncha_parent.domain.model.subscription.PlanType
import uz.tikoncha_parent.domain.model.subscription.SubscriptionStatus
import kotlin.time.Instant

fun SubscriptionStatusDto.toDomain(): SubscriptionStatus = SubscriptionStatus(
    planType = PlanType.fromString(planType),
    planName = planName,
    planDuration = PlanDuration.fromString(planDuration),
    amount = amount,
    isExpired = isExpired,
    remainingDays = remainingDays,
    createdAt = createdAt.toAppDate(),
    expiresAt = expiresAt.toAppDate()
)

private fun String?.toAppDate(): String? = runCatching {
    this?.takeIf { it.isNotBlank() }?.let { raw ->
        val instant = Instant.parse(raw)
        val ld = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        // Oy nomi bilan ("11 January 2027" / "11-yanvar 2027"), raqamli "11.01.2027" emas
        DateTimeUtil.formatLongDate(ld)
    }
}.getOrNull()