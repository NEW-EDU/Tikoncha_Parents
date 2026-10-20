package uz.tikoncha_parent.domain.model.tikonchagram

/**
 * Tikonchagram (bola ilovasidagi Telegram) qoidalari. Bola ilovalaridagi `TgChatPolicy` (Android) /
 * `ChatPolicyDTO` (iOS) bilan bir xil maydonlar. Xabarlar Tikoncha serveriga bormaydi — qoidalar
 * bolaning telefonida qo'llanadi. Sevimli chatlar bu yerda yo'q: chat ro'yxati telefondan chiqmaydi.
 */
data class TgRules(
    val enabled: Boolean = true,
    val hideChannels: Boolean = true,
    val hideGroups: Boolean = true,
    val hideBots: Boolean = true,
    val contactsOnly: Boolean = false,
    val allowNewChats: Boolean = false,
    val allowStickers: Boolean = false,
    val allowGifs: Boolean = false,
    val allowPhotos: Boolean = true,
    val allowVideos: Boolean = false,
    val allowVoice: Boolean = false,
    val allowVideoNotes: Boolean = false,
    val allowDocuments: Boolean = false,
    val allowLinks: Boolean = false,
    val allowForwards: Boolean = false,
    val showBlockedPlaceholder: Boolean = true,
    /** 0 — cheklanmagan. */
    val maxChats: Int = 30,
    /** Kunlik daqiqa chegarasi; null — farzand o'zi tanlaydi (2/5/10). */
    val dailyMaxMinutes: Int? = null,
) {
    /** Faqat chat/kontent qoidalari (vaqt va yoqilganlik hisobga olinmaydi) qaysi tayyor rejimga mos. */
    fun preset(): TgPreset? = TgPreset.entries.firstOrNull { it.rules.sameContent(this) }

    fun withPreset(p: TgPreset): TgRules = p.rules.copy(enabled = enabled, dailyMaxMinutes = dailyMaxMinutes)

    private fun sameContent(o: TgRules) = copy(enabled = o.enabled, dailyMaxMinutes = o.dailyMaxMinutes) == o

    companion object {
        val STRICT = TgRules()
        val DAILY_CHOICES = listOf(2, 5, 10)
        val MAX_CHAT_CHOICES = listOf(10, 30, 100, 0)
    }
}

/** Bola ilovalaridagi tayyor rejimlar (STRICT / BALANCED / OPEN). */
enum class TgPreset(val rules: TgRules) {
    STRICT(TgRules.STRICT),
    BALANCED(
        TgRules(
            hideGroups = false,
            allowVoice = true,
            allowDocuments = true,
            allowForwards = true,
        )
    ),
    OPEN(
        TgRules(
            hideChannels = false, hideGroups = false, hideBots = false,
            allowNewChats = true,
            allowStickers = true, allowGifs = true, allowPhotos = true,
            allowVideos = true, allowVoice = true, allowVideoNotes = true,
            allowDocuments = true, allowLinks = true, allowForwards = true,
            maxChats = 100,
        )
    ),
}

/** Serverdagi holat: ota-ona hali saqlamagan bo'lsa `isDefault`. */
data class TgRulesState(val rules: TgRules, val isDefault: Boolean)
