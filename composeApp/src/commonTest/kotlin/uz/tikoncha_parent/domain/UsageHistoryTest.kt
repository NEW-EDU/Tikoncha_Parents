package uz.tikoncha_parent.domain

import kotlinx.datetime.LocalDate
import uz.tikoncha_parent.domain.model.app_usage.AppUsage
import uz.tikoncha_parent.domain.model.app_usage.UsageHistory
import kotlin.test.Test
import kotlin.test.assertEquals

class UsageHistoryTest {

    private val mon = LocalDate(2026, 9, 21)
    private val tue = LocalDate(2026, 9, 22)
    private val wed = LocalDate(2026, 9, 23)
    private val thu = LocalDate(2026, 9, 24)
    private val min = 60_000L

    private fun app(pkg: String, vararg days: Pair<LocalDate, Map<Int, Long>>) =
        AppUsage(packageName = pkg, name = pkg, iconUrl = null, usage = days.toMap())

    /**
     * 4 kunlik hafta, telefon 4 kunning hammasida ishlatilgan; WhatsApp faqat 2 kunida.
     * Kuniga o'rtacha = jami / 4 (ma'lumotli kunlar), 2 ga emas.
     */
    private val history = UsageHistory(
        listOf(
            app("com.whatsapp", mon to mapOf(9 to 30 * min), wed to mapOf(20 to 54 * min)),
            app("com.youtube", mon to mapOf(10 to 10 * min), tue to mapOf(10 to 20 * min),
                wed to mapOf(10 to 30 * min), thu to mapOf(0 to 40 * min, 1 to 5 * min)),
        )
    )

    @Test
    fun perAppAverageDividesByDaysWithData() {
        val wa = history.appTotals(mon, thu).first { it.packageName == "com.whatsapp" }
        assertEquals(84 * min, wa.millis)
        assertEquals(21 * min, wa.perDayMillis)
    }

    @Test
    fun pageAverageUsesTheSameDivisor() {
        assertEquals(4, history.daysWithData(mon, thu))
        assertEquals((84 + 105) * min / 4, history.averagePerDay(mon, thu))
    }

    @Test
    fun sortedDescendingAndUnusedDropped() {
        assertEquals(listOf("com.youtube", "com.whatsapp"), history.appTotals(mon, thu).map { it.packageName })
        assertEquals(listOf("com.youtube"), history.appTotals(tue, tue).map { it.packageName })
    }

    @Test
    fun emptyDayIsNotADayWithData() {
        val h = UsageHistory(listOf(app("a", mon to mapOf(1 to 0L), tue to mapOf(1 to 5 * min))))
        assertEquals(mon, h.firstDay)                     // sahifalar shundan boshlanadi
        assertEquals(1, h.daysWithData(mon, tue))
        assertEquals(0L, h.averagePerDay(wed, thu))
    }

    /** Soatlar bolaning mahalliy vaqtida: 00:xx va 01:xx birinchi ustunga tushadi. */
    @Test
    fun twoHourSlots() {
        val slots = history.slots(thu)
        assertEquals(12, slots.size)
        assertEquals(45 * min, slots[0])
        assertEquals(0L, slots.drop(1).sum())
    }

    @Test
    fun appsInOneSlot() {
        val apps = history.appTotalsAt(wed, hourFrom = 20, hourToExclusive = 22)
        assertEquals(listOf("com.whatsapp" to 54 * min), apps.map { it.packageName to it.millis })
    }

    @Test
    fun negativeValuesDoNotSubtract() {
        val h = UsageHistory(listOf(app("a", mon to mapOf(1 to -5 * min, 2 to 3 * min))))
        assertEquals(3 * min, h.totalOn(mon))
    }
}
