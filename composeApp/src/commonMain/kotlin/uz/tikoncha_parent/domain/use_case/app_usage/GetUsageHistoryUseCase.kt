package uz.tikoncha_parent.domain.use_case.app_usage

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.map
import uz.tikoncha_parent.domain.model.app_usage.UsageHistory
import uz.tikoncha_parent.domain.repository.ChildRepository

/**
 * Statistika ekrani uchun oxirgi [DAYS] kun (bugun ham). Server obuna bo'yicha
 * ruxsat etilmagan kunlarni o'zi qirqadi.
 */
class GetUsageHistoryUseCase(
    private val repository: ChildRepository,
) {
    suspend operator fun invoke(childId: String, today: LocalDate, days: Int = DAYS): Outcome<UsageHistory> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)
        return repository.appUsages(childId, from = today.minus(days - 1, DateTimeUnit.DAY), to = today)
            .map { UsageHistory(it) }
    }

    companion object { const val DAYS = 16 }
}
