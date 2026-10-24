package uz.tikoncha_parent.data.local

import kotlinx.serialization.Serializable
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.app_usage.TopApp

/**
 * Bosh sahifa kartalarining oxirgi ma'lum holati (bitta farzand uchun). Ilova ochilganda yoki farzand
 * almashganda kartalar "0" bo'lib, keyin haqiqiy raqamga sakramasligi uchun darhol shu ko'rsatiladi,
 * serverdan yangisi jimgina keladi. Bugungi vaqt faqat [usageDay] bugun bo'lsa ishlatiladi.
 */
@Serializable
data class HomeSnapshot(
    val activeTaskCount: Int = 0,
    val usageDay: String = "",
    val todayUsage: HourMinute = HourMinute(0, 0),
    val topApps: List<TopApp> = emptyList(),
    val topAppsFromRecentDays: Boolean = false,
    /** Ko'rsatilgan kun (ISO), bugun bo'lsa — bo'sh. */
    val usageShownDay: String = "",
    val protectionPendingRequestCount: Int = 0,
    val protectionPermissionOffCount: Int = 0,
    val protectionLoaded: Boolean = false,
)
