package uz.tikoncha_parent.domain.model.app_usage

import kotlinx.serialization.Serializable
import uz.tikoncha_parent.domain.model.HourMinute

/**
 * Bugungi umumiy vaqt va eng ko'p ishlatilgan ilovalar (ko'pdan kamga).
 * [recentTopApps] — oxirgi kunlar bo'yicha; bugun hali hech narsa ishlatilmaganda ko'rsatish uchun.
 */
data class TodayUsage(
    val total: HourMinute,
    val topApps: List<TopApp>,
    val recentTopApps: List<TopApp>,
    /** [total] va [topApps] qaysi kunniki: bugun bo'sh bo'lsa — ma'lumoti bor eng so'nggi kun (null — umuman yo'q). */
    val day: kotlinx.datetime.LocalDate? = null,
    val isToday: Boolean = true,
)

@Serializable
data class TopApp(
    val packageName: String,
    val name: String,
    val iconUrl: String?,
    val millis: Long,
)