package uz.tikoncha_parent.presentation.profile.personal_information

import org.jetbrains.compose.resources.StringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ertalabdan
import tikoncha_parents.composeapp.generated.resources.tushlikdan

/**
 * Maktab smenasi.
 *
 * Kalitlar farzand ilovasidagi (uz.tikoncha.student) ShiftType bilan bir xil bo'lishi shart:
 * tushlikdan keyingi smena serverdan "evening" kaliti bilan keladi.
 */
enum class ShiftType(
    val key: String,
    val resId: StringResource,
) {
    MORNING(key = "morning", resId = Res.string.ertalabdan),
    AFTERNOON(key = "evening", resId = Res.string.tushlikdan);

    companion object {
        fun getShiftByKey(key: String?): ShiftType? = entries.find { it.key == key }
    }
}