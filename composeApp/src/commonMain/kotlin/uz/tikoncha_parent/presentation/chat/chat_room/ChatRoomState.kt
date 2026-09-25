package uz.tikoncha_parent.presentation.chat.chat_room

import org.jetbrains.compose.resources.StringResource
import uz.tikoncha_parent.domain.model.ChatMessageItem
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.chat.model.ChatDateLabel
import uz.tikoncha_parent.presentation.model.ChatMessageUi
import uz.tikoncha_parent.presentation.model.ChatType


data class ChatRoomState(
    val chatId: String = "",
    val chatTitle: String = "",
    val chatAvatar: String = "",
    val chatType: ChatType = ChatType.NONE,

    val text: String = "",

    // ✅ server DESC
    val allMessages: List<ChatMessageUi> = emptyList(),
    val messages: List<ChatMessageItem> = emptyList(),
    val lastMessage: ChatMessageUi? = null,
    val replyToMessage: ChatMessageUi? = null,
    val selectedMessageForEdit: ChatMessageUi? = null,
    val selectedMessageForDelete: ChatMessageUi? = null,

    val isInitialLoading: Boolean = false,
    val isPagingLoading: Boolean = false,
    val canLoadMore: Boolean = true,

    // ✅ sinceId cursor (current oldest message id)
    val sinceId: String? = null,

    val error: Outcome.Failure? = null,

    // header subscription_info (sizda bor)
    val chatMembersCount: Int = 0,
    val isUserOnline: Boolean = false,
    val lastTimeOnline: ChatDateLabel = ChatDateLabel.Unknown,

    val isConnected: Boolean = false,

    // ✅ har update -> bottom scroll
    val scrollToBottomTick: Long = 0L,

    // Shaxsiy suhbat bildirishnomasi: null — hali yuklanmagan yoki bu turdagi suhbatda yo'q
    val notificationEnabled: Boolean? = null,
    val isNotificationUpdating: Boolean = false,
    val notificationNotice: StringResource? = null,
    val notificationError: Outcome.Failure? = null,
)