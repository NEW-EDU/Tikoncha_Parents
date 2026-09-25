package uz.tikoncha_parent.domain.use_case.app_usage

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.map
import uz.tikoncha_parent.domain.model.app_usage.AppTotal
import uz.tikoncha_parent.domain.model.app_usage.TodayUsage
import uz.tikoncha_parent.domain.model.app_usage.TopApp
import uz.tikoncha_parent.domain.model.app_usage.UsageHistory
import uz.tikoncha_parent.domain.repository.ChildRepository
import kotlin.time.Clock

/** Bosh ekran kartasi: bugungi jami va eng ko'p ishlatilgan ilovalar. */
class TodayUsageUseCase(
    private val repository: ChildRepository
) {
    suspend operator fun invoke(
        userId: String,
        today: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    ): Outcome<TodayUsage> {
        // Bitta so'rov: bugun + oxirgi kunlar (bugun bo'sh bo'lsa, odatdagi ilovalarni ko'rsatish uchun)
        val from = today.minus(RECENT_DAYS - 1, DateTimeUnit.DAY)
        return repository.appUsages(userId, from = from, to = today).map { apps ->
            val history = UsageHistory(apps)
            TodayUsage(
                total = HourMinute.fromMillis(history.totalOn(today)),
                topApps = history.appTotals(today, today).top(),
                recentTopApps = history.appTotals(from, today).top(),
            )
        }
    }

    private fun List<AppTotal>.top(): List<TopApp> =
        take(TOP_APPS_COUNT).map { TopApp(packageName = it.packageName, name = it.name, iconUrl = it.iconUrl, millis = it.millis) }

    private companion object {
        const val TOP_APPS_COUNT = 3
        const val RECENT_DAYS = 7
    }
}
