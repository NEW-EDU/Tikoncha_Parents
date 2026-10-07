package uz.tikoncha_parent.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import uz.tikoncha_parent.domain.model.policy.PolicyKind
import uz.tikoncha_parent.domain.model.policy.PolicyName
import uz.tikoncha_parent.domain.model.policy.PolicyName.Kind
import uz.tikoncha_parent.domain.model.policy.PolicyPreset

/** Tayyor jadval va standart nomlar yaratuvchining tilidan qat'i nazar tanib olinadi (Student bilan bir xil). */
class PolicyNameTest {

    private fun kindOf(raw: String, kind: PolicyKind = PolicyKind.STANDARD, preset: PolicyPreset? = null) =
        PolicyName.of(raw, kind, preset).kind

    @Test
    fun presetDecidesEvenWhenTheNameIsInAnotherLanguage() {
        assertEquals(Kind.SLEEP, kindOf("Bedtime", preset = PolicyPreset.SLEEP))
        assertEquals(Kind.APP_LIMIT, kindOf("Лимит времени", preset = PolicyPreset.APP_LIMIT))
        assertEquals(Kind.SCHOOL, kindOf("School hours", preset = PolicyPreset.SCHOOL))
        assertEquals(Kind.CONTENT_PROTECTION, kindOf("Азартные приложения", preset = PolicyPreset.PROTECTION))
    }

    @Test
    fun quickBlockIsAlwaysTheQuickBlock() {
        assertEquals(Kind.QUICK_BLOCK, kindOf("Tezkor blok", kind = PolicyKind.QUICK_BLOCK))
        assertEquals(Kind.QUICK_BLOCK, kindOf("", kind = PolicyKind.QUICK_BLOCK))
    }

    @Test
    fun defaultNamesAreRecognisedInAllThreeLanguages() {
        assertEquals(Kind.NEW_SCHEDULE, kindOf("Yangi jadval"))
        assertEquals(Kind.NEW_SCHEDULE, kindOf("Новое расписание"))
        assertEquals(Kind.NEW_SCHEDULE, kindOf("  new   SCHEDULE "))
        assertEquals(Kind.SCHEDULE, kindOf("Jadval"))
        assertEquals(Kind.CATEGORY_BLOCK, kindOf("Toifalar bo'yicha bloklash"))
        assertEquals(Kind.CATEGORY_BLOCK, kindOf("Toifalar bo‘yicha bloklash"))
    }

    @Test
    fun templateNamesWithoutAPresetAreRecognised() {
        assertEquals(Kind.SLEEP, kindOf("Время сна"))
        assertEquals(Kind.QUICK_BLOCK, kindOf("Quick block"))
    }

    @Test
    fun aNameTheUserTypedStaysAsItIs() {
        val name = PolicyName.of("Dars paytida")
        assertEquals(Kind.CUSTOM, name.kind)
        assertEquals("Dars paytida", name.raw)
        assertEquals(Kind.CUSTOM, kindOf("Jadval 2"))
        assertEquals(Kind.CUSTOM, kindOf("Instagram"))
    }
}
