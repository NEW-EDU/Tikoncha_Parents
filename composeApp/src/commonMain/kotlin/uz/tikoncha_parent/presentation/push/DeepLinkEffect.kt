package uz.tikoncha_parent.presentation.push

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.DeepLink
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.presentation.chat.chat_list.ChatScreen
import uz.tikoncha_parent.presentation.chat.chat_room.ChatRoomScreen
import uz.tikoncha_parent.presentation.statistic.StatisticScreen
import uz.tikoncha_parent.presentation.model.ChatType
import uz.tikoncha_parent.presentation.protection.ProtectionScreen
import uz.tikoncha_parent.presentation.live.ParentLiveScreen

@Composable
fun DeepLinkEffect(navigator: Navigator) {

    LaunchedEffect(navigator) {
        DeepLinkBus.events.collect { link ->
            Logger.d("DeepLinkEffect", "open=$link")
            navigateByDeepLink(navigator, link)
        }
    }
}

fun navigateByDeepLink(navigator: Navigator, link: DeepLink){
    when(link){
        is DeepLink.Chat -> navigator.push(
            listOf(
                ChatScreen(),
                ChatRoomScreen(
                    chatId = link.chatId,
                    chatAvatar = "",
                    chatTitle = link.chatTitle?:"",
                    // Ilgari doim NONE edi: yordam xizmati push'dan oçilsa sarlavha serverniki ("Feedback") çiqardi.
                    chatType = ChatType.fromServer(link.chatType)
                )
            )
        )
        DeepLink.ParentalRequest -> navigator.push(ProtectionScreen())
        DeepLink.StrictDisable -> navigator.push(ProtectionScreen())
        is DeepLink.PermissionOff -> {
            selectChildForDeepLink(link.childUserId)
            navigator.push(ProtectionScreen())
        }
        is DeepLink.Live -> navigator.push(
            ParentLiveScreen(joinSessionId = link.sessionId, childUserId = link.childId)
        )
        is DeepLink.General -> {}
    }
}

/**
 * Push körsatgan farzandni tanlangan farzand qiladi (himoya ekrani `AppSettings.selectedChild` ni öqiydi).
 * Farzand keşdagi röyxatda bölmasa hech narsa özgarmaydi — ekran joriy farzand uçun oçiladi.
 */
fun selectChildForDeepLink(childUserId: String?) {
    if (childUserId.isNullOrBlank() || AppSettings.selectedChildId == childUserId) return
    val child = AppSettings.children.firstOrNull { it.userId == childUserId } ?: return
    AppSettings.selectedChild = child
    AppSettings.selectedChildId = child.userId
}
