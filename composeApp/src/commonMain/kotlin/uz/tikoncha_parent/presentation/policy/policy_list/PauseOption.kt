package uz.tikoncha_parent.presentation.policy.policy_list

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** Jadvalni vaqtincha to'xtatish variantlari. */
enum class PauseOption {
    ONE_HOUR,
    THREE_HOURS,
    UNTIL_TOMORROW,
    UNTIL_MONDAY;

    /**
     * Pauza tugash momenti. [zone] — ota-ona qurilmasining mintaqasi:
     * "ertagacha" degani ota-ona ko'rgan kalendar kuni bo'yicha hisoblanadi.
     */
    fun until(now: Instant, zone: TimeZone): Instant = when (this) {
        ONE_HOUR -> now + 1.hours
        THREE_HOURS -> now + 3.hours

        UNTIL_TOMORROW -> {
            val tomorrow = now.toLocalDateTime(zone).date.plus(1, DateTimeUnit.DAY)
            LocalDateTime(tomorrow, LocalTime(0, 0)).toInstant(zone)
        }

        UNTIL_MONDAY -> {
            var date = now.toLocalDateTime(zone).date.plus(1, DateTimeUnit.DAY)
            while (date.dayOfWeek != DayOfWeek.MONDAY) {
                date = date.plus(1, DateTimeUnit.DAY)
            }
            LocalDateTime(date, LocalTime(0, 0)).toInstant(zone)
        }
    }
}