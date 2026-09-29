package uz.tikoncha_parent.domain.model.apps

/**
 * Ilova kategoriyalari — Google Play'ning o'z ID'lari, server bilan BITTA jadval
 * (backend `app/modules/policies_v2/categories.py`; `evaluator_cases.json` ikkalasini bog'laydi).
 *
 * Jadval kodi ham, ilovaning kategoriyasi ham Play ID: VIDEO_PLAYERS, MUSIC_AND_AUDIO,
 * GAME_ACTION, GAME_CASINO, … Ustida bitta guruh kodi bor: [GAMES] — barcha `GAME_*` janrlari.
 *
 * Server ilovaning kategoriyasini Play ko'rsatadigan inglizcha nom bilan beradi ("Casino"),
 * [playId] uni Play'ning o'z jadvali bilan ID'ga aylantiradi.
 * Cheklov: Play "Sports" nomini ham SPORTS ilovalariga, ham GAME_SPORTS o'yinlariga beradi —
 * nomdan SPORTS olinadi.
 */
object CategoryCodes {

    const val GAMES = "GAMES"
    const val GAME_CASINO = "GAME_CASINO"
    private const val GAME_PREFIX = "GAME_"

    /** Google Play ko'rsatadigan nom (inglizcha) → Play ID. */
    val PLAY_CATEGORIES: Map<String, String> = mapOf(
        // ilovalar
        "Art & Design" to "ART_AND_DESIGN",
        "Auto & Vehicles" to "AUTO_AND_VEHICLES",
        "Beauty" to "BEAUTY",
        "Books & Reference" to "BOOKS_AND_REFERENCE",
        "Business" to "BUSINESS",
        "Comics" to "COMICS",
        "Communication" to "COMMUNICATION",
        "Dating" to "DATING",
        "Education" to "EDUCATION",
        "Entertainment" to "ENTERTAINMENT",
        "Events" to "EVENTS",
        "Finance" to "FINANCE",
        "Food & Drink" to "FOOD_AND_DRINK",
        "Health & Fitness" to "HEALTH_AND_FITNESS",
        "House & Home" to "HOUSE_AND_HOME",
        "Libraries & Demo" to "LIBRARIES_AND_DEMO",
        "Lifestyle" to "LIFESTYLE",
        "Maps & Navigation" to "MAPS_AND_NAVIGATION",
        "Medical" to "MEDICAL",
        "Music & Audio" to "MUSIC_AND_AUDIO",
        "News & Magazines" to "NEWS_AND_MAGAZINES",
        "Parenting" to "PARENTING",
        "Personalization" to "PERSONALIZATION",
        "Photography" to "PHOTOGRAPHY",
        "Productivity" to "PRODUCTIVITY",
        "Shopping" to "SHOPPING",
        "Social" to "SOCIAL",
        "Sports" to "SPORTS",
        "Tools" to "TOOLS",
        "Travel & Local" to "TRAVEL_AND_LOCAL",
        "Video Players & Editors" to "VIDEO_PLAYERS",
        "Weather" to "WEATHER",
        // o'yinlar
        "Action" to "GAME_ACTION",
        "Adventure" to "GAME_ADVENTURE",
        "Arcade" to "GAME_ARCADE",
        "Board" to "GAME_BOARD",
        "Card" to "GAME_CARD",
        "Casino" to GAME_CASINO,
        "Casual" to "GAME_CASUAL",
        "Educational" to "GAME_EDUCATIONAL",
        "Music" to "GAME_MUSIC",
        "Puzzle" to "GAME_PUZZLE",
        "Racing" to "GAME_RACING",
        "Role Playing" to "GAME_ROLE_PLAYING",
        "Simulation" to "GAME_SIMULATION",
        "Strategy" to "GAME_STRATEGY",
        "Trivia" to "GAME_TRIVIA",
        "Word" to "GAME_WORD",
    )

    /** Server avval o'zi o'ylab topgan kodlar (v1 dashboard, maktab shabloni, paketlar). */
    private val LEGACY_ALIASES = mapOf(
        "VIDEO" to "VIDEO_PLAYERS",
        "MUSIC" to "MUSIC_AND_AUDIO",
        "GAMBLING" to GAME_CASINO,
        "GAME" to GAMES,
        "SOCIAL_MEDIA" to "SOCIAL",
    )

    private val byName: Map<String, String> = PLAY_CATEGORIES.entries.associate { it.key.lowercase() to it.value }
    private val nonAlnum = Regex("[^A-Za-z0-9]+")

    private fun asCode(text: String): String = nonAlnum.replace(text, "_").trim('_').uppercase()

    /** Jadvaldagi kod → Play ID. Eski server kodi nomdan ustun ("MUSIC" — MUSIC_AND_AUDIO, o'yin janri emas). */
    fun normalize(code: String): String {
        val c = asCode(code)
        LEGACY_ALIASES[c]?.let { return it }
        return byName[code.trim().lowercase()] ?: c
    }

    /** Ilovaning kategoriyasi (Play nomi yoki ID) → Play ID. */
    fun playId(category: String?): String? {
        if (category.isNullOrBlank()) return null
        return byName[category.trim().lowercase()] ?: normalize(category)
    }

    fun isGame(id: String?): Boolean = id != null && id.startsWith(GAME_PREFIX)

    /** Ilova kategoriyasiga mos jadval kodlari: o'zi va, o'yin bo'lsa, [GAMES]. */
    fun lookupKeys(appCategory: String?): List<String> {
        val id = playId(appCategory) ?: return emptyList()
        return if (isGame(id)) listOf(id, GAMES) else listOf(id)
    }
}
