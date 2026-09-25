package uz.tikoncha_parent.presentation.policy.policy_setup

import kotlinx.datetime.TimeZone
import uz.tikoncha_parent.presentation.policy.policy_list.PauseOption
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * Jadval muddatining tayyor variantlari.
 * Aniq vaqt tanlash paytida emas, saqlash paytida hisoblanadi — server o'tgan vaqtni 422 bilan rad etadi.
 */
enum class ExpiryOption {
    ONE_HOUR,
    TWO_HOURS,
    THREE_HOURS,
    UNTIL_TOMORROW,
    UNTIL_MONDAY;

    fun until(now: Instant, zone: TimeZone): Instant = when (this) {
        ONE_HOUR -> now + 1.hours
        TWO_HOURS -> now + 2.hours
        THREE_HOURS -> now + 3.hours
        UNTIL_TOMORROW -> PauseOption.UNTIL_TOMORROW.until(now, zone)
        UNTIL_MONDAY -> PauseOption.UNTIL_MONDAY.until(now, zone)
    }
}