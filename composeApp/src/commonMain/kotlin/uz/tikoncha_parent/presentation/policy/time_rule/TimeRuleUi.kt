package uz.tikoncha_parent.presentation.policy.time_rule

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import uz.tikoncha_parent.domain.model.WeekDay

@Serializable
data class TimeRuleUi(
    val id: Int = 0,
    val startTime: LocalTime = LocalTime(0, 0),
    val endTime: LocalTime = LocalTime(0, 0),
    /** true → tanlangan oraliqdan TASHQARIDA bloklansin (inverted) */
    val reverse: Boolean = false,
    val allDay: Boolean = false,
    val weekDays: Set<WeekDay> = emptySet(),
) : JavaSerializable {

    /** Bir kundagi bloklanish vaqti (daqiqa). */
    val perDayMinutes: Int
        get() = when {
            allDay -> MINUTES_PER_DAY
            reverse -> MINUTES_PER_DAY - intervalMinutes
            else -> intervalMinutes
        }

    /** Haftalik jami bloklanish vaqti (daqiqa). */
    val totalWeekMinutes: Int
        get() = perDayMinutes * weekDays.size

    private val intervalMinutes: Int
        get() {
            val s = startTime.hour * 60 + startTime.minute
            val e = endTime.hour * 60 + endTime.minute
            return if (e >= s) e - s else (MINUTES_PER_DAY - s) + e
        }

    companion object {
        const val MINUTES_PER_DAY = 24 * 60
    }
}