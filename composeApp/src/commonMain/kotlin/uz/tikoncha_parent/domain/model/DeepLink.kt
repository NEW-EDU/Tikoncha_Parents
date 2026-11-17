package uz.tikoncha_parent.domain.model

sealed class DeepLink {
    data class Chat(
        val chatId: String,
        val chatTitle: String? = null,
        val text: String? = null,
        /** Server turi (SUPPORT, BOT, …) — push'dan oçilgan suhbat ham ilova tilidagi nom bilan çiqsin. */
        val chatType: String? = null,
    ) : DeepLink()

    data object ParentalRequest : DeepLink()
    data object StrictDisable : DeepLink()

    /** PERMISSION_OFF push: farzand telefonida muhim ruxsat öçirildi → şu farzandning himoya ekrani. */
    data class PermissionOff(val childUserId: String? = null) : DeepLink()

    /** Incoming live session (child SOS / parent request) → open the live viewer to watch. */
    data class Live(
        val sessionId: String,
        val childId: String,
        val kind: String? = null,
    ) : DeepLink()

    data class General(val title: String? = null, val message: String? = null) : DeepLink()
}