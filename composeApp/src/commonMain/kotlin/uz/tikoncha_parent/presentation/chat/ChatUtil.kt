package uz.tikoncha_parent.presentation.chat

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bugun
import tikoncha_parents.composeapp.generated.resources.chat_ai_title
import tikoncha_parents.composeapp.generated.resources.chat_support_title
import tikoncha_parents.composeapp.generated.resources.kecha
import uz.tikoncha_parent.presentation.chat.model.ChatDateLabel
import uz.tikoncha_parent.presentation.model.ChatType
import uz.tikoncha_parent.presentation.model.ChatUi

object ChatUtil {
    fun getInitials(firstName: String?, lastName: String?): String {
        val first = firstName?.trim()?.firstOrNull()?.uppercaseChar()
        val last = lastName?.trim()?.firstOrNull()?.uppercaseChar()

        return listOfNotNull(first, last).joinToString("")
    }

    fun getInitials(fullName: String?): String {
        return fullName
            ?.trim()
            ?.split(Regex("[\\s-]+")) // ⬅️ space + dash
            ?.take(2)
            ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
            ?.joinToString("")
            .orEmpty()
    }



    /**
     * Ekrandagi suhbat nomi. Server yordam xizmatini "Feedback", aqlli yordamçini "Bilimdon AI" deb yuboradi
     * (tarjima qilinmagan) — bu ikkisi turiga qarab ilova tilida körsatiladi; qolganlari serverdagi nomi bilan.
     */
    @Composable
    fun ChatUi.displayTitle(): String = when (type) {
        ChatType.SUPPORT -> stringResource(Res.string.chat_support_title)
        ChatType.BOT -> stringResource(Res.string.chat_ai_title)
        else -> title
    }

    @Composable
    fun ChatDateLabel.asText(): String = when (this) {
        is ChatDateLabel.Time -> hhmm
        ChatDateLabel.Yesterday -> stringResource(Res.string.kecha)
        is ChatDateLabel.Date -> ddMonth
        ChatDateLabel.Today -> stringResource(Res.string.bugun)
        ChatDateLabel.Unknown -> ""
    }
}