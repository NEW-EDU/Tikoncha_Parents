package uz.tikoncha_parent.data.mapper

import kotlinx.datetime.LocalDate
import uz.tikoncha_parent.data.remote.model.AppUsageDataDto
import uz.tikoncha_parent.domain.model.app_usage.AppUsage
import uz.tikoncha_parent.domain.policy.NeverBlock

private fun parseDateDdMMyyyy(s: String): LocalDate {
    val (d, m, y) = s.split("-").map { it.toInt() }
    return LocalDate(y, m, d)
}

private fun parseHour(s: String): Int =
    s.substringBefore(":").toInt()

/**
 * API javobini AppUsage ro'yxatiga o'tkazadi.
 * Har bir item — bitta ilova, ichida kun va soat bo'yicha guruhlangan ms.
 */
fun AppUsageDataDto.toAppUsageList(): List<AppUsage> =
    // Tikoncha ilovalari ekran vaqti emas: bola telefonida old planda turgan Tikoncha — asosan blok ekrani
    // (masalan, kechasi uyqu jadvalida) yoki Bilimdon. Server yordamçisi ham ularni sanamaydi (`OWN_PACKAGES`) —
    // Bosh sahifa va yordamçi bir xil raqam aytsin.
    items.filterNot { NeverBlock.isNeverBlocked(it.packageName) }.map { item ->
        val usageMap: Map<LocalDate, Map<Int, Long>> = item.usage
            .entries
            .associate { (dateStr, hourMap) ->
                val date = parseDateDdMMyyyy(dateStr)
                val hours = hourMap.entries.associate { (timeStr, ms) ->
                    parseHour(timeStr) to ms
                }
                date to hours
            }

        AppUsage(
            packageName = item.packageName,
            name = item.name,
            iconUrl = item.icon,
            usage = usageMap
        )
    }