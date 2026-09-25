package uz.tikoncha_parent.domain.model.app_usage

import kotlinx.datetime.LocalDate

/** Ilovaning oraliqdagi jami vaqti. [perDayMillis] — oraliqdagi ma'lumotli kunlarga bo'lingan. */
data class AppTotal(
    val packageName: String,
    val name: String,
    val iconUrl: String?,
    val millis: Long,
    val perDayMillis: Long,
)

/**
 * Serverdan kelgan soatlik foydalanish — sana va soat BOLANING mahalliy vaqtida
 * (bola ilovasi shunday yuboradi). Statistika ekranining barcha hisobi shu yerda,
 * ViewModel faqat UI modelga o'giradi.
 *
 * O'rtacha qoidasi (Student bilan bir xil): bo'luvchi — oraliqdagi MA'LUMOTLI kunlar
 * (bola telefonidan biror vaqt kelgan kunlar), ilovaning o'zi ishlatilgan kunlar emas.
 * Aks holda 2 kun ishlatilgan ilova 4 kunlik haftada ikki barobar ko'p ko'rinardi.
 */
class UsageHistory(val apps: List<AppUsage>) {

    private val dayTotals: Map<LocalDate, Long> = buildMap {
        apps.forEach { app ->
            app.usage.forEach { (day, hours) ->
                val sum = hours.values.sumOf { it.coerceAtLeast(0L) }
                if (sum > 0L) this[day] = (this[day] ?: 0L) + sum
            }
        }
    }

    /** Eng birinchi kelgan sana (bo'sh kun bo'lsa ham) — sahifalar shundan boshlanadi. */
    val firstDay: LocalDate? = apps.flatMap { it.usage.keys }.minOrNull()

    fun totalOn(day: LocalDate): Long = dayTotals[day] ?: 0L

    fun total(from: LocalDate, to: LocalDate): Long =
        dayTotals.entries.sumOf { (d, ms) -> if (d in from..to) ms else 0L }

    fun daysWithData(from: LocalDate, to: LocalDate): Int = dayTotals.keys.count { it in from..to }

    fun averagePerDay(from: LocalDate, to: LocalDate): Long {
        val days = daysWithData(from, to)
        return if (days == 0) 0L else total(from, to) / days
    }

    /** Kun ichidagi ustunlar: 24 / [hoursPerSlot] ta, har biri [hoursPerSlot] soatlik jami. */
    fun slots(day: LocalDate, hoursPerSlot: Int = 2): List<Long> =
        (0 until HOURS_PER_DAY / hoursPerSlot).map { slot ->
            val hours = slot * hoursPerSlot until (slot + 1) * hoursPerSlot
            apps.sumOf { app -> app.usage[day].orEmpty().entries.sumOf { (h, ms) -> if (h in hours) ms.coerceAtLeast(0L) else 0L } }
        }

    /** Oraliqdagi ilovalar, ko'pdan kamga; ishlatilmaganlari yo'q. */
    fun appTotals(from: LocalDate, to: LocalDate): List<AppTotal> {
        val days = daysWithData(from, to)
        return totals(days) { app ->
            app.usage.entries.sumOf { (d, hours) -> if (d in from..to) hours.values.sumOf { it.coerceAtLeast(0L) } else 0L }
        }
    }

    /** Bitta kunning [hourFrom] … [hourToExclusive] oralig'idagi ilovalar (grafik ustuni bosilganda). */
    fun appTotalsAt(day: LocalDate, hourFrom: Int, hourToExclusive: Int): List<AppTotal> =
        totals(days = 1) { app ->
            app.usage[day].orEmpty().entries.sumOf { (h, ms) -> if (h in hourFrom until hourToExclusive) ms.coerceAtLeast(0L) else 0L }
        }

    private inline fun totals(days: Int, millisOf: (AppUsage) -> Long): List<AppTotal> =
        apps.mapNotNull { app ->
            val ms = millisOf(app)
            if (ms <= 0L) null
            else AppTotal(
                packageName = app.packageName,
                name = app.name,
                iconUrl = app.iconUrl,
                millis = ms,
                perDayMillis = if (days > 0) ms / days else ms,
            )
        }.sortedByDescending { it.millis }

    private companion object { const val HOURS_PER_DAY = 24 }
}
