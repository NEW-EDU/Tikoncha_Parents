package uz.tikoncha_parent.data.mapper

import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.remote.model.ChatDto
import uz.tikoncha_parent.data.remote.model.ChatMemberDto
import uz.tikoncha_parent.data.remote.model.ChatMessageDto
import uz.tikoncha_parent.presentation.chat.ChatDateTimeUtil
import uz.tikoncha_parent.presentation.chat.model.DeliveryStatus
import uz.tikoncha_parent.presentation.model.ChatMemberUi
import uz.tikoncha_parent.presentation.model.ChatMessageType
import uz.tikoncha_parent.presentation.model.ChatMessageUi
import uz.tikoncha_parent.presentation.model.ChatType
import uz.tikoncha_parent.presentation.model.ChatUi
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs


fun ChatDto.toChatUi(): ChatUi {
    val date = last_message?.created_at
    val millis = DateTimeUtil.toMillis(date)
    val lang = LanguagePrefs.loadOrDefault()
    val dateTime = ChatDateTimeUtil.formatChatDate(millis = millis, langType = lang)

    val type = when(type){
        "BOT" -> ChatType.BOT
        "PARENT_CHILD" -> ChatType.PARENT_CHILD
        "CLASS" -> ChatType.CLASS
        "SIBLINGS" -> ChatType.SIBLINGS
        "SUPPORT" -> ChatType.SUPPORT   // "Feedback" — ekranda "Yordam xizmati" (ChatUtil.displayTitle)
        else -> ChatType.NONE
    }

    return ChatUi(
        title = title,
        chatId = id,
        type = type,
        lastMessage = last_message?.text?:"",
        unreadCount = unread_count,
        dateTime = dateTime,
        lastMessageIsMine = last_message?.is_mine?:false,
        lastMessageIsRead = last_message?.is_read?:false,
        avatar = avatar.prepareAvatar()

    )
}

fun ChatMessageDto.toChatMessageUi(): ChatMessageUi{
    val dateMillis = DateTimeUtil.toMillis(created_at)

    // Server tahrirdan keyin ikkita hodisa yuboradi: ikkinchisida
    // is_mine/is_read maydonlari umuman bo'lmaydi. Maydon yo'q bo'lsa
    // "meniki"ni yuboruvchi id'sidan aniqlaymiz.
    val mine = is_mine ?: (sender_id == AppSettings.userId)
    val read = is_read ?: false

    val status = when {
        mine && read -> DeliveryStatus.READ
        else -> DeliveryStatus.SENT
    }


    return ChatMessageUi(
        id = id,
        isMine = mine,
        isRead = read,
        isEdited = !edited_at.isNullOrBlank(),
        message = text,
        createdAt = dateMillis,
        time = ChatDateTimeUtil.millisToHHmm(dateMillis),
        senderName = sender_name,
        senderAvatar = sender_avatar.prepareAvatar(),
        remoteUrl = attachment_url ?: "",
        messageType = ChatMessageType.getChatMessageTypeByName(type),
        duration = meta?.duration,
        clientMsgId = client_msg_id,
        replyToId = reply_to_id,
        repliedMessageText = replied_message_text,
        repliedMessageOwner = replied_message_owner,
        status = status
    )
}

fun ChatMemberDto.toChatMemberUi(): ChatMemberUi{
    return ChatMemberUi(
        id = user_id,
        name = first_name,
        avatar = avatar.prepareAvatar(),
        isOnline = online?:false,
        lastSeen = ""

    )
}

fun buildOptimisticTextMessage(
    text: String,
    clientMsgId: String,
    replyToId: String? = null,
    replyToMessageOwner: String? = null,
    replyToMessageTex: String? = null,
    nowMillis: Long = DateTimeUtil.nowMillis()
): ChatMessageUi {
    return ChatMessageUi(
        id = "", // hali yo‘q
        isMine = true,
        message = text,
        createdAt = nowMillis,
        time = ChatDateTimeUtil.millisToHHmm(nowMillis), // local tavsiya
        isRead = false,
        senderName = "",
        senderAvatar = "",
        remoteUrl = "",
        messageType = ChatMessageType.TEXT,
        amplitudes = emptyList(),
        duration = null,
        clientMsgId = clientMsgId,
        status = DeliveryStatus.SENDING,
        replyToId = replyToId,
        repliedMessageOwner = replyToMessageOwner,
        repliedMessageText = replyToMessageTex
    )
}

fun ChatMessageUi.stableKey(): String =
    if (id.isNotBlank()) "id:$id" else "tmp:$clientMsgId"