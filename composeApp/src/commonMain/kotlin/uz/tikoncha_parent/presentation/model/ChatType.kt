package uz.tikoncha_parent.presentation.model

import kotlinx.serialization.Serializable

@Serializable
enum class ChatType {
    /** Aqlli yordamçi (server nomi "Bilimdon AI"). */
    BOT,
    PARENT_CHILD,
    CLASS,
    SIBLINGS,
    /** Yordam xizmati — odam javob beradi (server nomi "Feedback"). */
    SUPPORT,
    NONE;

    companion object {
        /** Server `chat.type` / push `chat_type` → tur (notanişi NONE). */
        fun fromServer(value: String?): ChatType =
            entries.firstOrNull { it != NONE && it.name == value?.trim()?.uppercase() } ?: NONE
    }
}