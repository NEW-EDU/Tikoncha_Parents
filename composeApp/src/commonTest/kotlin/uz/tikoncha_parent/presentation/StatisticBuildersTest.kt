package uz.tikoncha_parent.presentation

import kotlinx.datetime.LocalDate
import uz.tikoncha_parent.domain.model.app_usage.AppUsage
import uz.tikoncha_parent.domain.model.app_usage.UsageHistory
import uz.tikoncha_parent.presentation.statistic.ChartSubtitle
import uz.tikoncha_parent.presentation.statistic.buildDailyBarDetails
import uz.tikoncha_parent.presentation.statistic.buildDailyBars
import uz.tikoncha_parent.presentation.statistic.buildDailyPages
import uz.tikoncha_parent.presentation.statistic.buildStatApps
import uz.tikoncha_parent.presentation.statistic.buildWeeklyPages
import kotlin.test.Test
import kotlin.test.assertEquals

class StatisticBuildersTest {

    private val min = 60_000L
    private val thu = LocalDate(2026, 9, 24)          // bugun — payshanba
    private val mon = LocalDate(2026, 9, 21)
    private val prevFri = LocalDate(2026, 9, 18)

    private val history = UsageHistory(
        listOf(
            AppUsage("com.youtube", "YouTube", null, mapOf(
                prevFri to mapOf(20 to 30 * min),
                mon to mapOf(10 to 60 * min),
                thu to mapOf(9 to 20 * min, 21 to 10 * min),
            )),
            AppUsage("com.whatsapp", "WhatsApp", null, mapOf(mon to mapOf(10 to 15 * min))),
        )
    )

    @Test
    fun weeksStartOnMondayAndEndToday() {
        val pages = buildWeeklyPages(history, today = thu)
        assertEquals(listOf(LocalDate(2026, 9, 14), mon), pages.map { it.startDate })
        assertEquals(thu, pages.last().endDateInclusive)
    }

    /** Joriy hafta: 105 daq, ma'lumotli kunlar — dushanba va payshanba → kuniga 52 daq 30 s. */
    @Test
    fun weeklyAverageUsesDaysWithData() {
        val week = buildWeeklyPages(history, today = thu).last()
        assertEquals(105 * min, week.totalMillis)
        val avg = (week.chartSubtitle as ChartSubtitle.WeeklyAverage).avgPerDay
        assertEquals(0 to 52, avg.hour to avg.minute)
    }

    @Test
    fun appsCarryShareAndPerDay() {
        val week = buildWeeklyPages(history, today = thu).last()
        val apps = buildStatApps(history, week)
        assertEquals(listOf("com.youtube", "com.whatsapp"), apps.map { it.packageName })
        assertEquals(1f, apps[0].share)
        assertEquals(15f / 90f, apps[1].share)
        assertEquals(45 * min, apps[0].perDayMillis)                  // 90 daq / 2 kun
    }

    @Test
    fun dailyPagesFromFirstDataDayToToday() {
        val pages = buildDailyPages(history, today = thu)
        assertEquals(prevFri, pages.first().startDate)
        assertEquals(7, pages.size)
        assertEquals(0L, pages.first { it.startDate == LocalDate(2026, 9, 19) }.totalMillis)
    }

    @Test
    fun dayBarsAndDetails() {
        val today = buildDailyPages(history, today = thu).last()
        val bars = buildDailyBars(history, today)
        assertEquals(12, bars.size)
        assertEquals(20 * min, bars[4].totalMillis)          // 08:00–10:00
        val details = buildDailyBarDetails(history, today, bars[10])  // 20:00–22:00
        assertEquals(listOf("com.youtube" to 10 * min), details.items.map { it.packageName to it.usageMillis })
    }

    @Test
    fun emptyHistoryShowsTodayOnly() {
        val pages = buildDailyPages(UsageHistory(emptyList()), today = thu)
        assertEquals(listOf(thu), pages.map { it.startDate })
    }
}
