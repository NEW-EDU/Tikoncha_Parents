package uz.tikoncha_parent.presentation.statistic

import kotlinx.datetime.LocalDate
import uz.tikoncha_parent.domain.model.HourMinute

enum class DayLabel { TODAY, YESTERDAY, NONE }

sealed class PageTitle {
    data class Day(
        val dayLabel: DayLabel,
        val day: Int,
        val monthIndex: Int          // 0..11
    ) : PageTitle()

    data class Week(
        val startDay: Int,
        val startMonthIndex: Int,
        val endDay: Int,
        val endMonthIndex: Int
    ) : PageTitle()
}

sealed class ChartSubtitle {
    data class WeeklyAverage(val avgPerDay: HourMinute) : ChartSubtitle()
}

data class PagePeriod(
    val title: PageTitle,
    val subtitle: HourMinute,           // umumiy
    val chartSubtitle: ChartSubtitle?,  // weekly: avg/kun, daily: null
    val startDate: LocalDate,
    val endDateInclusive: LocalDate,
    val totalMillis: Long
)

data class ChartBarUi(
    val slotIndex: Int,                 // weekly: 0..6, daily: 0..11
    val valueMinutes: Double,
    val totalMillis: Long
)

/**
 * Ro'yxat qatori. [share] — eng ko'p ishlatilgan ilovaga nisbatan (0..1), qator
 * chizilishida hisoblanmasin deb bir marta tayyorlanadi. [perDayMillis] — haftalikda
 * "kuniga ~" (oraliqdagi ma'lumotli kunlarga bo'lingan).
 */
data class StatAppUi(
    val packageName: String,
    val name: String,
    val iconUrl: String?,
    val millis: Long,
    val perDayMillis: Long,
    val share: Float,
)

sealed class UsageDetailsTitle {
    /** Haftalikda bar bosilsa: "Du, 12 - May" */
    data class WeekdayDate(
        val weekdayIndex: Int,
        val day: Int,
        val monthIndex: Int
    ) : UsageDetailsTitle()

    /** Kunlikda bar bosilsa: "19 - May, 14:00 - 16:00" */
    data class HourRange(
        val day: Int,
        val monthIndex: Int,
        val hourFrom: Int,
        val hourToExclusive: Int
    ) : UsageDetailsTitle()
}

data class UsageDetailItem(
    val packageName: String,
    val name: String,
    val iconUrl: String?,
    val usageMillis: Long,
)

data class UsageDetailsUi(
    val title: UsageDetailsTitle,
    val total: HourMinute,
    val items: List<UsageDetailItem>
)