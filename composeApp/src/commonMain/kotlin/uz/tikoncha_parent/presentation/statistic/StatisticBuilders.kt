package uz.tikoncha_parent.presentation.statistic

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.app_usage.AppTotal
import uz.tikoncha_parent.domain.model.app_usage.UsageHistory

/*
 * Domen hisobini ([UsageHistory]) ekran modellariga o'giradi. Bu yerda yig'indi yoki
 * o'rtacha HISOBLANMAYDI — hammasi UsageHistory'da, u yerda test qilingan.
 */

internal fun LocalDate.startOfWeek(): LocalDate = minus(dayOfWeek.ordinal, DateTimeUnit.DAY)   // MONDAY = 0

private fun dayLabelFor(date: LocalDate, today: LocalDate): DayLabel = when (date) {
    today -> DayLabel.TODAY
    today.minus(1, DateTimeUnit.DAY) -> DayLabel.YESTERDAY
    else -> DayLabel.NONE
}

/* ============ PAGES ============ */

fun buildWeeklyPages(history: UsageHistory, today: LocalDate): List<PagePeriod> {
    val anchor = (history.firstDay ?: today).startOfWeek()
    return generateSequence(anchor) { it.plus(7, DateTimeUnit.DAY).takeIf { next -> next <= today } }
        .map { start ->
            val end = start.plus(6, DateTimeUnit.DAY).coerceAtMost(today)
            val total = history.total(start, end)
            PagePeriod(
                title = PageTitle.Week(
                    startDay = start.day,
                    startMonthIndex = start.month.number - 1,
                    endDay = end.day,
                    endMonthIndex = end.month.number - 1,
                ),
                subtitle = HourMinute.fromMillis(total),
                chartSubtitle = ChartSubtitle.WeeklyAverage(HourMinute.fromMillis(history.averagePerDay(start, end))),
                startDate = start,
                endDateInclusive = end,
                totalMillis = total,
            )
        }
        .toList()
}

fun buildDailyPages(history: UsageHistory, today: LocalDate): List<PagePeriod> {
    val first = history.firstDay?.coerceAtMost(today) ?: today
    return generateSequence(first) { it.plus(1, DateTimeUnit.DAY).takeIf { next -> next <= today } }
        .map { d ->
            val total = history.totalOn(d)
            PagePeriod(
                title = PageTitle.Day(dayLabel = dayLabelFor(d, today), day = d.day, monthIndex = d.month.number - 1),
                subtitle = HourMinute.fromMillis(total),
                chartSubtitle = null,
                startDate = d,
                endDateInclusive = d,
                totalMillis = total,
            )
        }
        .toList()
}

/* ============ BARS ============ */

fun emptyBars(mode: DateSelectionType): List<ChartBarUi> = when (mode) {
    DateSelectionType.WEEK -> (0..6).map { ChartBarUi(it, 0.0, 0L) }
    DateSelectionType.DAY -> (0 until DAY_SLOTS).map { ChartBarUi(it, 0.0, 0L) }
}

fun buildWeeklyBars(history: UsageHistory, page: PagePeriod): List<ChartBarUi> =
    (0..6).map { i ->
        val day = page.startDate.plus(i, DateTimeUnit.DAY)
        val ms = if (day in page.startDate..page.endDateInclusive) history.totalOn(day) else 0L
        ChartBarUi(slotIndex = i, valueMinutes = ms / 60_000.0, totalMillis = ms)
    }

fun buildDailyBars(history: UsageHistory, page: PagePeriod): List<ChartBarUi> =
    history.slots(page.startDate, HOURS_PER_SLOT).mapIndexed { slot, ms ->
        ChartBarUi(slotIndex = slot, valueMinutes = ms / 60_000.0, totalMillis = ms)
    }

/* ============ APPS ============ */

/** Ulush eng ko'p ilovaga nisbatan — shu yerda bir marta. */
fun buildStatApps(history: UsageHistory, page: PagePeriod): List<StatAppUi> {
    val apps = history.appTotals(page.startDate, page.endDateInclusive)
    val max = apps.firstOrNull()?.millis ?: 0L
    return apps.map { it.toStatApp(share = if (max > 0L) it.millis.toFloat() / max else 0f) }
}

private fun AppTotal.toStatApp(share: Float) = StatAppUi(
    packageName = packageName,
    name = name,
    iconUrl = iconUrl,
    millis = millis,
    perDayMillis = perDayMillis,
    share = share,
)

/* ============ DIALOG ============ */

fun buildWeeklyBarDetails(history: UsageHistory, page: PagePeriod, bar: ChartBarUi): UsageDetailsUi {
    val day = page.startDate.plus(bar.slotIndex, DateTimeUnit.DAY)
    return UsageDetailsUi(
        title = UsageDetailsTitle.WeekdayDate(weekdayIndex = bar.slotIndex, day = day.day, monthIndex = day.month.number - 1),
        total = HourMinute.fromMillis(bar.totalMillis),
        items = history.appTotals(day, day).map { it.toDetail() },
    )
}

fun buildDailyBarDetails(history: UsageHistory, page: PagePeriod, bar: ChartBarUi): UsageDetailsUi {
    val day = page.startDate
    val hourFrom = bar.slotIndex * HOURS_PER_SLOT
    return UsageDetailsUi(
        title = UsageDetailsTitle.HourRange(
            day = day.day,
            monthIndex = day.month.number - 1,
            hourFrom = hourFrom,
            hourToExclusive = hourFrom + HOURS_PER_SLOT,
        ),
        total = HourMinute.fromMillis(bar.totalMillis),
        items = history.appTotalsAt(day, hourFrom, hourFrom + HOURS_PER_SLOT).map { it.toDetail() },
    )
}

private fun AppTotal.toDetail() = UsageDetailItem(packageName = packageName, name = name, iconUrl = iconUrl, usageMillis = millis)

private const val HOURS_PER_SLOT = 2
private const val DAY_SLOTS = 24 / HOURS_PER_SLOT
