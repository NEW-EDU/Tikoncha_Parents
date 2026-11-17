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
    NONE
}