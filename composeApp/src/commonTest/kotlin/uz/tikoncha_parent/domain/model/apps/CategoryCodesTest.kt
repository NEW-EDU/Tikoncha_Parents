package uz.tikoncha_parent.domain.model.apps

import kotlin.test.Test
import kotlin.test.assertEquals
import uz.tikoncha_parent.presentation.policy.targets.CategoryLocalizer
import uz.tikoncha_parent.presentation.policy.targets.appCategory

/** Bitta kategoriya ro'yxati — Google Play ID'lari (server `policies_v2/categories.py` bilan bir xil). */
class CategoryCodesTest {

    @Test
    fun playNameBecomesPlayId() {
        assertEquals("GAME_CASINO", CategoryLocalizer.toCode("Casino"))
        assertEquals("GAME_EDUCATIONAL", CategoryLocalizer.toCode("Educational"))
        assertEquals("VIDEO_PLAYERS", CategoryLocalizer.toCode("Video Players & Editors"))
        assertEquals("GAME_MUSIC", CategoryLocalizer.toCode("Music"))
        assertEquals("MUSIC_AND_AUDIO", CategoryCodes.normalize("MUSIC"))   // eski server kodi
        assertEquals("GAME_CASINO", CategoryCodes.normalize("GAMBLING"))
    }

    @Test
    fun pickerGroupsGamesTogether() {
        assertEquals(AppCategory.GAMES, AppCategory.from("GAME_CASINO"))
        assertEquals(AppCategory.VIDEO_PLAYERS, AppCategory.from("VIDEO"))
        assertEquals(AppCategory.OTHER, AppCategory.from(null))
    }

    @Test
    fun serverCategoryIdWinsOverName() {
        val app = InstalledApp("com.slots", "Slots", category = "Casino", iconUrl = null, order = 1, categoryId = "GAME_CASINO")
        assertEquals(AppCategory.GAMES, app.appCategory)
        val old = InstalledApp("com.yt", "YouTube", category = "Video Players & Editors", iconUrl = null, order = 2)
        assertEquals(AppCategory.VIDEO_PLAYERS, old.appCategory)
    }

    @Test
    fun casinoCodeShowsAsGambling() {
        assertEquals("Qimor", CategoryLocalizer.localizeCode("GAME_CASINO", uz.tikoncha_parent.presentation.domain.model.LanguageType.UZ).name)
    }
}
