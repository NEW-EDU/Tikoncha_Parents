package uz.tikoncha_parent.domain

import kotlinx.serialization.json.Json
import uz.tikoncha_parent.data.remote.model.tikonchagram.TgPolicyDto
import uz.tikoncha_parent.data.repository.toDto
import uz.tikoncha_parent.data.repository.toRules
import uz.tikoncha_parent.domain.model.tikonchagram.TgPreset
import uz.tikoncha_parent.domain.model.tikonchagram.TgRules
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TgRulesTest {

    @Test
    fun defaultsAreStrict() {
        assertEquals(TgPreset.STRICT, TgRules().preset())
    }

    @Test
    fun presetIgnoresEnabledAndDailyTime() {
        val r = TgPreset.BALANCED.rules.copy(enabled = false, dailyMaxMinutes = 5)
        assertEquals(TgPreset.BALANCED, r.preset())
    }

    @Test
    fun anyContentChangeMakesItCustom() {
        assertNull(TgRules().copy(allowLinks = true).preset())
        assertNull(TgPreset.OPEN.rules.copy(maxChats = 30).preset())
    }

    @Test
    fun withPresetKeepsEnabledAndDailyTime() {
        val r = TgRules(enabled = false, dailyMaxMinutes = 2).withPreset(TgPreset.OPEN)
        assertEquals(false, r.enabled)
        assertEquals(2, r.dailyMaxMinutes)
        assertEquals(TgPreset.OPEN, r.preset())
    }

    @Test
    fun missingJsonFieldsFallBackToStrict() {
        val json = Json { ignoreUnknownKeys = true }
        val dto = json.decodeFromString(TgPolicyDto.serializer(), """{"hide_groups":false,"is_default":false,"updated_at":null}""")
        val rules = dto.toRules()
        assertEquals(false, rules.hideGroups)
        assertTrue(rules.hideChannels && rules.allowPhotos && !rules.allowLinks)
        assertEquals(rules, rules.toDto().toRules())
    }

    @Test
    fun wireNamesAreSnakeCase() {
        val text = Json { encodeDefaults = true }.encodeToString(TgPolicyDto.serializer(), TgRules(dailyMaxMinutes = 5).toDto())
        assertTrue("\"allow_video_notes\":false" in text)
        assertTrue("\"daily_max_minutes\":5" in text)
        assertTrue("\"show_blocked_placeholder\":true" in text)
    }
}
