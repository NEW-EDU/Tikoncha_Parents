package uz.tikoncha_parent.domain.use_case.app_usage

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_usage.AppUsage
import uz.tikoncha_parent.domain.model.app_usage.TodayUsage
import uz.tikoncha_parent.domain.model.app_usage.TopApp
import uz.tikoncha_parent.domain.repository.ChildRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class TodayUsageUseCase(
    private val repository: ChildRepository
) {

    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(userId: String): Outcome<TodayUsage> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        // Bitta so'rov: bugun + oxirgi kunlar (bugun bo'sh bo'lsa, odatdagi ilovalarni ko'rsatish uchun)
        val from = today.minus(RECENT_DAYS - 1, DateTimeUnit.DAY)

        return when (val res = repository.appUsages(userId, from = from, to = today)) {
            is Outcome.Failure -> res
            is Outcome.Success -> {
                val todayApps = res.data.map { app -> app.toTopApp(app.usage[today]?.values?.sum() ?: 0L) }
                val recentApps = res.data.map { app -> app.toTopApp(app.usage.values.sumOf { it.values.sum() }) }
                Outcome.Success(
                    TodayUsage(
                        total = HourMinute.fromMillis(todayApps.sumOf { it.millis }),
                        topApps = todayApps.top(),
                        recentTopApps = recentApps.top(),
                    )
                )
            }
        }
    }

    private fun AppUsage.toTopApp(millis: Long) = TopApp(
        packageName = packageName,
        name = name,
        iconUrl = iconUrl,
        millis = millis,
    )

    private fun List<TopApp>.top(): List<TopApp> =
        filter { it.millis > 0L }
            .sortedByDescending { it.millis }
            .take(TOP_APPS_COUNT)

    private companion object {
        const val TOP_APPS_COUNT = 3
        const val RECENT_DAYS = 7
    }
}