package uz.tikoncha_parent.domain.model

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

@Serializable
enum class WeekDay(val num: Int): JavaSerializable {
    MON(1), TUE(2), WED(3), THU(4), FRI(5), SAT(6), SUN(7);

    companion object {
        fun fromNum(n: Int): WeekDay = entries.first { it.num == n }
        val ordered: List<WeekDay> = listOf(MON, TUE, WED, THU, FRI, SAT, SUN)
    }
}
