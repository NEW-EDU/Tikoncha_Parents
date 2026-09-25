package uz.tikoncha_parent.domain.model.policy

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable
import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.domain.model.WeekDay

@Serializable
data class TimeCondition(
    val days: Set<WeekDay>,
    val startMin: Int,
    val endMin: Int,
    val include: Boolean = true
): JavaSerializable {
    val isAllDay: Boolean get() = startMin == 0 && endMin >= END_OF_DAY_SENTINEL

    companion object {
        const val MINUTES_PER_DAY = 1440
        const val END_OF_DAY_SENTINEL = 1439
    }
}

@Serializable
data class WifiCondition(val ssid: String, val include: Boolean = true): JavaSerializable

/**
 * Shartlar VA bilan birlashadi. v2.1: bitta jadval — bitta vaqt oralig'i, bitta hudud
 * (`null` = shart yo'q). Wi-Fi hech bir mijozda ishlatilmaydi — faqat tahrirda yo'qolmasin.
 */
@Serializable
data class PolicyConditions(
    val time: TimeCondition? = null,
    val location: LocationRule? = null,
    val wifi: List<WifiCondition> = emptyList()
): JavaSerializable {
    val isEmpty: Boolean get() = time == null && location == null && wifi.isEmpty()
}
