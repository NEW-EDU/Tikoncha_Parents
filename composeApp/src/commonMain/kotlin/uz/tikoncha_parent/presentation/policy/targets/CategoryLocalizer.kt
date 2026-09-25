package uz.tikoncha_parent.presentation.policy.targets

import uz.tikoncha_parent.domain.model.apps.AppCategory
import uz.tikoncha_parent.presentation.domain.model.LanguageType


data class LocalizedCategory(
    val name: String,
    val emoji: String,
)

object CategoryLocalizer {

    fun localize(category: AppCategory, language: LanguageType): LocalizedCategory {
        val name = when (language) {
            LanguageType.RU -> ruNames[category]
            else -> uzNames[category]
        } ?: category.id
        val emoji = emojis[category] ?: "📦"
        return LocalizedCategory(name, emoji)
    }

    fun toCode(englishName: String): String {
        return englishToCode[englishName.trim()] ?: englishName.uppercase().replace(" ", "_")
    }

    private val englishToCode = mapOf(
        // Apps
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
        // Games
        "Action" to "GAMES",
        "Adventure" to "GAMES",
        "Arcade" to "GAMES",
        "Board" to "GAMES",
        "Card" to "GAMES",
        "Casino" to "GAMES",
        "Casual" to "GAMES",
        "Educational" to "GAMES",
        "Music" to "GAMES",
        "Puzzle" to "GAMES",
        "Racing" to "GAMES",
        "Role Playing" to "GAMES",
        "Simulation" to "GAMES",
        "Strategy" to "GAMES",
        "Trivia" to "GAMES",
        "Word" to "GAMES",
    )

    private val uzNames = mapOf(
        AppCategory.ART_AND_DESIGN to "San'at va dizayn",
        AppCategory.AUTO_AND_VEHICLES to "Avtomobil",
        AppCategory.BEAUTY to "Go'zallik",
        AppCategory.BOOKS_AND_REFERENCE to "Kitoblar",
        AppCategory.BUSINESS to "Biznes",
        AppCategory.COMICS to "Komikslar",
        AppCategory.COMMUNICATION to "Aloqa",
        AppCategory.DATING to "Tanishuvlar",
        AppCategory.EDUCATION to "Ta'lim",
        AppCategory.ENTERTAINMENT to "Ko'ngilochar",
        AppCategory.EVENTS to "Tadbirlar",
        AppCategory.FINANCE to "Moliya",
        AppCategory.FOOD_AND_DRINK to "Ovqat va ichimlik",
        AppCategory.HEALTH_AND_FITNESS to "Salomatlik",
        AppCategory.HOUSE_AND_HOME to "Uy-joy",
        AppCategory.LIBRARIES_AND_DEMO to "Kutubxona",
        AppCategory.LIFESTYLE to "Turmush tarzi",
        AppCategory.MAPS_AND_NAVIGATION to "Xaritalar",
        AppCategory.MEDICAL to "Tibbiyot",
        AppCategory.MUSIC_AND_AUDIO to "Musiqa",
        AppCategory.NEWS_AND_MAGAZINES to "Yangiliklar",
        AppCategory.PARENTING to "Ota-onalik",
        AppCategory.PERSONALIZATION to "Shaxsiylashtirish",
        AppCategory.PHOTOGRAPHY to "Fotografiya",
        AppCategory.PRODUCTIVITY to "Samaradorlik",
        AppCategory.SHOPPING to "Xaridlar",
        AppCategory.SOCIAL to "Ijtimoiy tarmoq",
        AppCategory.SPORTS to "Sport",
        AppCategory.TOOLS to "Asboblar",
        AppCategory.TRAVEL_AND_LOCAL to "Sayohat",
        AppCategory.VIDEO_PLAYERS to "Video",
        AppCategory.WEATHER to "Ob-havo",
        AppCategory.GAMES to "O'yinlar",
        AppCategory.OTHER to "Boshqa",
    )

    private val ruNames = mapOf(
        AppCategory.ART_AND_DESIGN to "Искусство и дизайн",
        AppCategory.AUTO_AND_VEHICLES to "Авто и транспорт",
        AppCategory.BEAUTY to "Красота",
        AppCategory.BOOKS_AND_REFERENCE to "Книги",
        AppCategory.BUSINESS to "Бизнес",
        AppCategory.COMICS to "Комиксы",
        AppCategory.COMMUNICATION to "Общение",
        AppCategory.DATING to "Знакомства",
        AppCategory.EDUCATION to "Образование",
        AppCategory.ENTERTAINMENT to "Развлечения",
        AppCategory.EVENTS to "Мероприятия",
        AppCategory.FINANCE to "Финансы",
        AppCategory.FOOD_AND_DRINK to "Еда и напитки",
        AppCategory.HEALTH_AND_FITNESS to "Здоровье",
        AppCategory.HOUSE_AND_HOME to "Дом",
        AppCategory.LIBRARIES_AND_DEMO to "Библиотеки",
        AppCategory.LIFESTYLE to "Стиль жизни",
        AppCategory.MAPS_AND_NAVIGATION to "Карты и навигация",
        AppCategory.MEDICAL to "Медицина",
        AppCategory.MUSIC_AND_AUDIO to "Музыка",
        AppCategory.NEWS_AND_MAGAZINES to "Новости",
        AppCategory.PARENTING to "Родительство",
        AppCategory.PERSONALIZATION to "Персонализация",
        AppCategory.PHOTOGRAPHY to "Фотография",
        AppCategory.PRODUCTIVITY to "Продуктивность",
        AppCategory.SHOPPING to "Покупки",
        AppCategory.SOCIAL to "Социальные сети",
        AppCategory.SPORTS to "Спорт",
        AppCategory.TOOLS to "Инструменты",
        AppCategory.TRAVEL_AND_LOCAL to "Путешествия",
        AppCategory.VIDEO_PLAYERS to "Видео",
        AppCategory.WEATHER to "Погода",
        AppCategory.GAMES to "Игры",
        AppCategory.OTHER to "Другое",
    )

    private val emojis = mapOf(
        AppCategory.ART_AND_DESIGN to "🎨",
        AppCategory.AUTO_AND_VEHICLES to "🚗",
        AppCategory.BEAUTY to "💄",
        AppCategory.BOOKS_AND_REFERENCE to "📖",
        AppCategory.BUSINESS to "💼",
        AppCategory.COMICS to "💬",
        AppCategory.COMMUNICATION to "📱",
        AppCategory.DATING to "❤️",
        AppCategory.EDUCATION to "📚",
        AppCategory.ENTERTAINMENT to "🎬",
        AppCategory.EVENTS to "🎪",
        AppCategory.FINANCE to "💰",
        AppCategory.FOOD_AND_DRINK to "🍔",
        AppCategory.HEALTH_AND_FITNESS to "❤️‍🩹",
        AppCategory.HOUSE_AND_HOME to "🏠",
        AppCategory.LIBRARIES_AND_DEMO to "📂",
        AppCategory.LIFESTYLE to "🌿",
        AppCategory.MAPS_AND_NAVIGATION to "🗺️",
        AppCategory.MEDICAL to "🏥",
        AppCategory.MUSIC_AND_AUDIO to "🎵",
        AppCategory.NEWS_AND_MAGAZINES to "📰",
        AppCategory.PARENTING to "👶",
        AppCategory.PERSONALIZATION to "🎭",
        AppCategory.PHOTOGRAPHY to "📷",
        AppCategory.PRODUCTIVITY to "⚡",
        AppCategory.SHOPPING to "🛒",
        AppCategory.SOCIAL to "👥",
        AppCategory.SPORTS to "⚽",
        AppCategory.TOOLS to "🔧",
        AppCategory.TRAVEL_AND_LOCAL to "✈️",
        AppCategory.VIDEO_PLAYERS to "🎥",
        AppCategory.WEATHER to "🌤️",
        AppCategory.GAMES to "🎮",
        AppCategory.OTHER to "📦",
    )
}