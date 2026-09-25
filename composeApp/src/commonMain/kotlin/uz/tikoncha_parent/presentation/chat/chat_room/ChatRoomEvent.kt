package uz.tikoncha_parent.presentation.chat.chat_room

import uz.tikoncha_parent.presentation.model.ChatMessageUi
import uz.tikoncha_parent.presentation.model.ChatType


sealed class ChatRoomEvent {
    data class Open(val tag: String) : ChatRoomEvent()
    data class Close(val tag: String) : ChatRoomEvent()

    data class SetChatListData(
        val chatId: String,
        val chatTitle: String,
        val chatAvatar: String,
        val chatType: ChatType
    ) : ChatRoomEvent()

    data class OnTextChange(val text: String) : ChatRoomEvent()

    data object LoadInitial : ChatRoomEvent()
    data object LoadMore : ChatRoomEvent()

    data object SendMessage : ChatRoomEvent()
    data object OnReachedBottom : ChatRoomEvent()
    data class SelectMessageForReply(val message: ChatMessageUi) : ChatRoomEvent()
    data class SelectedMessageForEdit(val message: ChatMessageUi) : ChatRoomEvent()
    data class Retry (val message: ChatMessageUi) : ChatRoomEvent()
    data class SelectMessageForDelete(val message: ChatMessageUi) : ChatRoomEvent()
    data class DeleteFailedMessage (val message: ChatMessageUi) : ChatRoomEvent()
    data object CancelEdit: ChatRoomEvent()
    data object CancelReply : ChatRoomEvent()
    data object CancelDelete : ChatRoomEvent()
    data object ConfirmDelete : ChatRoomEvent()
    data object ToggleNotification : ChatRoomEvent()
    data object NotificationNoticeShown : ChatRoomEvent()
}