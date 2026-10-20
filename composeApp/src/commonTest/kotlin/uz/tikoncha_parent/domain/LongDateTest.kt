package uz.tikoncha_parent.domain

import kotlinx.datetime.LocalDate
import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import kotlin.test.Test
import kotlin.test.assertEquals

class LongDateTest {

    @Test
    fun serverDateTimeBecomesDayMonthNameYear() {
        val raw = "14.10.2027 20:37:54"
        assertEquals("14-oktyabr 2027", DateTimeUtil.formatLongDate(raw, LanguageType.UZ))
        assertEquals("14 октября 2027", DateTimeUtil.formatLongDate(raw, LanguageType.RU))
        assertEquals("14 October 2027", DateTimeUtil.formatLongDate(raw, LanguageType.EN))
    }

    @Test
    fun isoAndPlainDatesWork() {
        assertEquals("8 September 2027", DateTimeUtil.formatLongDate("2027-09-08T05:00:00Z", LanguageType.EN))
        assertEquals("1 January 2026", DateTimeUtil.formatLongDate(LocalDate(2026, 1, 1), LanguageType.EN))
    }

    @Test
    fun unknownTextIsReturnedAsIs() {
        assertEquals("soon", DateTimeUtil.formatLongDate("soon", LanguageType.EN))
        assertEquals("", DateTimeUtil.formatLongDate(null, LanguageType.EN))
    }
}
