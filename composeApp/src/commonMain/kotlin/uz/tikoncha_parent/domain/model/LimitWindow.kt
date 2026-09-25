package uz.tikoncha_parent.domain.model

import kotlinx.serialization.Serializable

/** Server bilan bir xil: DAY / HOUR (eski DAILY/HOURLY hech qayerda ishlatilmagan). */
@Serializable
enum class LimitWindow {
    DAY, HOUR;

    companion object {
        fun from(raw: String?): LimitWindow = entries.firstOrNull { it.name == raw } ?: DAY
    }
}