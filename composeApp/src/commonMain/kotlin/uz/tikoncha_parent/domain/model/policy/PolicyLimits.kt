package uz.tikoncha_parent.domain.model.policy

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable
import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.WeekDay

@Serializable
data class UsageLimit(
    val days: Set<WeekDay>,
    val window: LimitWindow,
    val minutes: Int
): JavaSerializable
@Serializable
data class LaunchLimit(
    val days: Set<WeekDay>,
    val maxLaunches: Int
): JavaSerializable
/** v2.1: bitta foydalanish limiti; ochishlar limiti Parent'da yaratilmaydi — faqat saqlanadi. */
@Serializable
data class PolicyLimits(
    val usage: UsageLimit? = null,
    val launch: LaunchLimit? = null
): JavaSerializable {
    val isEmpty: Boolean get() = usage == null && launch == null
}
