@file:OptIn(ExperimentalLayoutApi::class)

package uz.tikoncha_parent.presentation.chat.chat_room

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.arrow_left
import tikoncha_parents.composeapp.generated.resources.azolar
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.bildirishnomalar
import tikoncha_parents.composeapp.generated.resources.chat_icon
import tikoncha_parents.composeapp.generated.resources.close
import tikoncha_parents.composeapp.generated.resources.faol
import tikoncha_parents.composeapp.generated.resources.faol_emas
import tikoncha_parents.composeapp.generated.resources.haqiqatan_ham_bu_xabarni_o_chirmoqchimisiz
import tikoncha_parents.composeapp.generated.resources.message_edit
import tikoncha_parents.composeapp.generated.resources.message_reply
import tikoncha_parents.composeapp.generated.resources.ochirish
import tikoncha_parents.composeapp.generated.resources.ohirgi_faollik
import tikoncha_parents.composeapp.generated.resources.xabar_yozish
import tikoncha_parents.composeapp.generated.resources.xabarni_o_chirish
import tikoncha_parents.composeapp.generated.resources.xabarni_tahrirlash
import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.domain.model.ChatMessageItem
import uz.tikoncha_parent.presentation.base.CustomBottomDialog
import uz.tikoncha_parent.presentation.base.CustomText
import uz.tikoncha_parent.presentation.base.ErrorRetryState
import uz.tikoncha_parent.presentation.base.LocalToastHost
import uz.tikoncha_parent.presentation.base.ToastData
import uz.tikoncha_parent.presentation.base.ToastProvider
import uz.tikoncha_parent.presentation.base.ToastType
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.chat.ChatTextField
import uz.tikoncha_parent.presentation.chat.ChatUtil
import uz.tikoncha_parent.presentation.chat.ChatUtil.asText
import uz.tikoncha_parent.presentation.chat.chat_details.ChatDetailsScreen
import uz.tikoncha_parent.presentation.chat.item.MessageDateItem
import uz.tikoncha_parent.presentation.chat.item.MessageReceivedItem
import uz.tikoncha_parent.presentation.chat.item.MessageSentItem
import uz.tikoncha_parent.presentation.chat.model.ChatDateLabel
import uz.tikoncha_parent.presentation.chat.model.DeliveryStatus
import uz.tikoncha_parent.presentation.chat.model.MessageMenuAction
import uz.tikoncha_parent.presentation.model.ChatMessageType
import uz.tikoncha_parent.presentation.model.ChatMessageUi
import uz.tikoncha_parent.presentation.model.ChatType
import uz.tikoncha_parent.ui.ChatHeaderAvatarSize
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.HeaderHeight
import uz.tikoncha_parent.ui.NormalIconButtonPadding
import uz.tikoncha_parent.ui.NormalIconButtonSize
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.SpaceSmall
import uz.tikoncha_parent.ui.TextFieldHeight
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class ChatRoomScreen(
    private val chatId: String,
    private val chatAvatar: String,
    private val chatTitle: String,
    private val chatType: ChatType
) : Screen {
    @Composable
    override fun Content() {
        val viewModel: ChatRoomViewModel = koinScreenModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        LaunchedEffect(true) {
            event(
                ChatRoomEvent.SetChatListData(
                    chatId = chatId,
                    chatAvatar = chatAvatar,
                    chatTitle = chatTitle,
                    chatType = chatType
                )
            )
        }

        DisposableEffect(Unit) {
            event(ChatRoomEvent.Open("ChatRoomScreen"))
            onDispose { event(ChatRoomEvent.Close("ChatRoomScreen")) }
        }

        ToastProvider {
            val toast = LocalToastHost.current
            val noticeText = state.notificationNotice?.let { stringResource(it) }
            val errorText = state.notificationError?.asText()

            // Bildirishnoma yoqildi/o'chirildi yoki xato — qisqa xabar
            LaunchedEffect(noticeText, errorText) {
                val data = when {
                    errorText != null -> ToastData(type = ToastType.Error, title = errorText)
                    noticeText != null -> ToastData(type = ToastType.Success, title = noticeText)
                    else -> return@LaunchedEffect
                }
                toast.show(toast = data, durationMs = 2000)
                event(ChatRoomEvent.NotificationNoticeShown)
            }

            ChatRoomScreenUi(
                state = state,
                event = event
            )
        }
    }

}


@Composable
fun ChatRoomScreenUi(
    state: ChatRoomState,
    event: (ChatRoomEvent) -> Unit,
) {

    val navigator = LocalNavigator.current

    val listState = rememberLazyListState()

    var expandedItemKey by rememberSaveable { mutableStateOf<String?>(null) }
    var isFailedMenu by rememberSaveable { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        event(ChatRoomEvent.Open("ChatRoomScreen"))
        onDispose { event(ChatRoomEvent.Close("ChatRoomScreen")) }
    }

    LaunchedEffect(state.scrollToBottomTick) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // ✅ pagination trigger: list ENDiga yaqinlashganda
    val shouldLoadMore by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            if (total == 0) return@derivedStateOf false
            val lastVisible =
                info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= total - 1 - 6
        }
    }

    LaunchedEffect(
        shouldLoadMore,
        state.isPagingLoading,
        state.canLoadMore,
        state.isInitialLoading
    ) {
        if (!state.isInitialLoading && shouldLoadMore && !state.isPagingLoading && state.canLoadMore) {
            event(ChatRoomEvent.LoadMore)
        }
    }

    val isAtBottom by remember(listState) {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.any { it.index == 0 }
        }
    }

    val density = LocalDensity.current
    var inputHeightDp by remember { mutableStateOf(TextFieldHeight) } // fallback
    var selectedMessageIndicatorHeight by remember { mutableStateOf(0.dp) } // fallback
    val gap = 30.dp

    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    CustomBottomDialog(
        show = showConfirmDeleteDialog,
        title = stringResource(Res.string.xabarni_o_chirish),
        message = stringResource(Res.string.haqiqatan_ham_bu_xabarni_o_chirmoqchimisiz),
        confirmButtonText = stringResource(Res.string.ochirish),
        dismissButtonText = stringResource(Res.string.bekor_qilish),
        onDismiss = {
            showConfirmDeleteDialog = false
            event(ChatRoomEvent.CancelDelete)
        },
        onConfirm = {
            showConfirmDeleteDialog = false
            event(ChatRoomEvent.ConfirmDelete)
        }
    )

    // Pastda turganda yangi xabar kelsa isAtBottom o'zgarmaydi — shuning uchun oxirgi xabar id'si ham kalit.
    // Ilova fonda bo'lsa xabar ko'rilmagan, "o'qildi" yuborilmaydi; qaytganda yuboriladi.
    val lastMessageId = state.lastMessage?.id
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val isResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    LaunchedEffect(isAtBottom, lastMessageId, isResumed) {
        if (isAtBottom && isResumed) event(ChatRoomEvent.OnReachedBottom)
    }

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.elevated,
        navigationBarColor = AppColors.bg.surface
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page)
    ) {
        Box(
            Modifier
                .zIndex(1f)
                .fillMaxWidth()
                .background(color = AppColors.bg.elevated)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeaderHeight)
                    .padding(horizontal = ContainerPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.size(NormalIconButtonSize),
                    onClick = {
                        navigator?.pop()
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = AppColors.icon.primary,
                    ),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_left),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(NormalIconButtonPadding)
                    )
                }

                SpaceSmall()

                if (state.chatAvatar.isNotEmpty()) {
                    AsyncImage(
                        model = state.chatAvatar,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(ChatHeaderAvatarSize)
                            .clip(CircleShape),
                        error = painterResource(Res.drawable.chat_icon),
                        placeholder = painterResource(Res.drawable.chat_icon)

                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(ChatHeaderAvatarSize)
                            .background(AppColors.bg.primaryContainer, CircleShape)
                            .border(1.dp, AppColors.bg.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ChatUtil.getInitials(
                                fullName = state.chatTitle
                            ),
                            style = AppTypography.titleLgMedium,
                            color = AppColors.text.accentEmphasis,
                        )
                    }
                }
                SpaceSmall()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = null,
                            onClick = {
                                if (state.chatType == ChatType.CLASS) {
                                    navigator?.push(
                                        ChatDetailsScreen(
                                            chatId = state.chatId,
                                            chatTitle = state.chatTitle,
                                            chatAvatar = state.chatAvatar,
                                            chatType = state.chatType,
                                        )
                                    )
                                }
                            }
                        )
                ) {
                    Text(
                        maxLines = 1,
                        text = state.chatTitle,
                        style = AppTypography.titleMdSemiBold,
                        color = AppColors.text.primary
                    )

                    val status = when (state.chatType) {
                        ChatType.CLASS -> {
                            "${stringResource(Res.string.azolar)}: ${state.chatMembersCount}"
                        }

                        ChatType.BOT -> {
                            stringResource(Res.string.faol)
                        }

                        else -> {
                            when (state.isUserOnline) {
                                true -> stringResource(Res.string.faol)
                                false -> {
                                    if (state.lastTimeOnline is ChatDateLabel.Unknown) {
                                        stringResource(Res.string.faol_emas)
                                    } else {
                                        "${stringResource(Res.string.ohirgi_faollik)} ${state.lastTimeOnline.asText()}"
                                    }
                                }
                            }
                        }
                    }

                    val headerTextColor =
                        if (state.isUserOnline && state.chatType != ChatType.CLASS) {
                            AppColors.text.accentEmphasis
                        } else {
                            AppColors.text.placeholder
                        }

                    Text(
                        maxLines = 1,
                        text = status,
                        style = AppTypography.bodyMdMedium,
                        color = headerTextColor,
                    )
                }

                // Shaxsiy suhbatni ovozsiz qilish (holat serverdan kelgandagina ko'rinadi)
                val notificationEnabled = state.notificationEnabled
                if (notificationEnabled != null) {
                    IconButton(
                        modifier = Modifier.size(NormalIconButtonSize),
                        enabled = !state.isNotificationUpdating,
                        onClick = { event(ChatRoomEvent.ToggleNotification) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = AppColors.icon.primary,
                        ),
                    ) {
                        Icon(
                            imageVector = if (notificationEnabled) Icons.Outlined.Notifications
                            else Icons.Outlined.NotificationsOff,
                            contentDescription = stringResource(Res.string.bildirishnomalar),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(NormalIconButtonPadding)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.BottomStart
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                reverseLayout = true,
                contentPadding = PaddingValues(
                    top = 12.dp,
                    start = ContainerPadding,
                    end = ContainerPadding,
                    // 🔥 eng muhimi: input ustidan scroll ko‘rinishi uchun pastdan joy qoldiramiz
                    bottom = inputHeightDp + gap + selectedMessageIndicatorHeight
                )
            ) {
                items(
                    items = state.messages,
                    key = { item ->
                        when (item) {
                            is ChatMessageItem.DateHeader -> "date_${item.epochDay}"
                            is ChatMessageItem.Message -> {
                                val m = item.chatMessageUi
                                if (m.id.isNotBlank()) "msg_${m.id}" else "tmp_${m.clientMsgId}"
                            }
                        }
                    }
                ) { item ->
                    when (item) {

                        is ChatMessageItem.DateHeader -> {
                            MessageDateItem(date = item.dateLabel)
                        }

                        is ChatMessageItem.Message -> {
                            val msg = item.chatMessageUi
                            val k =
                                if (msg.id.isNotBlank()) "msg_${msg.id}" else "tmp_${msg.clientMsgId}"

                            if (msg.isMine) {
                                when (msg.messageType) {
                                    ChatMessageType.TEXT -> MessageSentItem(
                                        chatMessageUi = msg,
                                        menuExpanded = expandedItemKey == k && !isFailedMenu,
                                        failedMenuExpanded = expandedItemKey == k && isFailedMenu,
                                        onOpenMenu = { failedMenu ->
                                            expandedItemKey = k
                                            isFailedMenu = failedMenu
                                        },
                                        onDismissMenu = {
                                            expandedItemKey = null
                                        },
                                        onMenuAction = { action ->
                                            expandedItemKey = null
                                            when (action) {
                                                MessageMenuAction.Reply -> {
                                                    event(ChatRoomEvent.SelectMessageForReply(msg))
                                                }

                                                MessageMenuAction.Copy -> {
                                                    scope.launch {
                                                        clipboard.setText(AnnotatedString(msg.message))
                                                    }
                                                }

                                                MessageMenuAction.Edit -> {
                                                    event(ChatRoomEvent.SelectedMessageForEdit(msg))
                                                }

                                                MessageMenuAction.Delete -> {
                                                    event(ChatRoomEvent.SelectMessageForDelete(msg))
                                                    showConfirmDeleteDialog = true
                                                }

                                                MessageMenuAction.Retry -> {
                                                    event(ChatRoomEvent.Retry(msg))
                                                }

                                                MessageMenuAction.RetryDelete -> {
                                                    event(ChatRoomEvent.DeleteFailedMessage(msg))
                                                }
                                            }
                                        }
                                    )

                                    else -> {}
                                }
                            } else {
                                when (msg.messageType) {
                                    ChatMessageType.TEXT -> {
                                        MessageReceivedItem(
                                            chatMessageUi = msg,
                                            showSender = state.chatType == ChatType.CLASS,
                                            menuExpanded = expandedItemKey == k,
                                            onOpenMenu = {
                                                expandedItemKey = k
                                            },
                                            onDismissMenu = {
                                                expandedItemKey = null
                                            },
                                            onMenuAction = { action ->
                                                expandedItemKey = null
                                                when (action) {
                                                    MessageMenuAction.Reply -> {
                                                        event(
                                                            ChatRoomEvent.SelectMessageForReply(
                                                                msg
                                                            )
                                                        )
                                                    }

                                                    MessageMenuAction.Copy -> {
                                                        scope.launch {
                                                            clipboard.setText(AnnotatedString(msg.message))
                                                        }
                                                    }

                                                    else -> {}
                                                }
                                            }
                                        )
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }

            val loadFailure = state.error
            if (loadFailure != null && state.messages.isEmpty()) {
                ErrorRetryState(
                    failure = loadFailure,
                    onRetry = { event(ChatRoomEvent.LoadInitial) },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = ContainerPadding)
                )
            }
            SpaceMedium()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.bg.surface),
            ) {
                HorizontalDivider(thickness = 1.dp)

                if (state.selectedMessageForEdit != null) {
                    EditMessageUi(
                        modifier = Modifier
                            .padding(
                                start = ContainerPadding + 8.dp,
                                end = ContainerPadding,
                                top = 10.dp,
                                bottom = 10.dp
                            )
                            .onSizeChanged { size ->
                                selectedMessageIndicatorHeight =
                                    with(density) { size.height.toDp() }
                            },
                        onDismiss = {
                            event(ChatRoomEvent.CancelEdit)
                        }
                    )

                    HorizontalDivider(thickness = 1.dp)
                } else if (state.replyToMessage != null) {
                    ReplyMessageUi(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = ContainerPadding + 8.dp,
                                end = ContainerPadding,
                                top = 10.dp,
                                bottom = 10.dp
                            )
                            .onSizeChanged { size ->
                                selectedMessageIndicatorHeight =
                                    with(density) { size.height.toDp() }
                            },
                        messageUi = state.replyToMessage,
                        onDismiss = {
                            event(ChatRoomEvent.CancelReply)
                        }
                    )
                    HorizontalDivider(thickness = 1.dp)
                } else {
                    selectedMessageIndicatorHeight = 0.dp
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.bg.surface)
                        .padding(horizontal = ContainerPadding, vertical = 10.dp)
                ) {
                    val focusRequester = remember { FocusRequester() }
                    val keyboardController = LocalSoftwareKeyboardController.current

                    LaunchedEffect(state.selectedMessageForEdit) {
                        if (state.selectedMessageForEdit != null) {
                            delay(100)
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }

                    ChatTextField(
                        onFileClick = {},
                        value = state.text,
                        focusRequester = focusRequester,
                        onSend = { event(ChatRoomEvent.SendMessage) },
                        label = stringResource(Res.string.xabar_yozish),
                        onValueChange = { event(ChatRoomEvent.OnTextChange(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = TextFieldHeight, max = TextFieldHeight * 5)
                            .onSizeChanged { size ->
                                // px -> dp
                                inputHeightDp = with(density) { size.height.toDp() }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun EditMessageUi(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.message_edit),
            contentDescription = null,
            tint = MaterialTheme.extendedColor.primaryColor,
            modifier = Modifier.size(24.dp)
        )
        SpaceSmall()

        CustomText(
            text = stringResource(Res.string.xabarni_tahrirlash),
            color = MaterialTheme.extendedColor.primaryColor,
            modifier = Modifier.weight(1f)
        )
        SpaceSmall()
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.close),
                contentDescription = null,
                tint = MaterialTheme.extendedColor.primaryColor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ReplyMessageUi(
    modifier: Modifier = Modifier,
    messageUi: ChatMessageUi,
    onDismiss: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.message_reply),
            contentDescription = null,
            tint = AppColors.icon.accentPrimary,
            modifier = Modifier.size(24.dp)
        )
        SpaceSmall()

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = messageUi.senderName,
                style = AppTypography.titleSmSemiBold,
                color = AppColors.text.accentEmphasis
            )
            Text(
                maxLines = 1,
                text = messageUi.message,
                color = AppColors.text.primary,
                overflow = TextOverflow.Ellipsis,
                style = AppTypography.titleSmMedium
            )
        }
        SpaceSmall()

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.close),
                contentDescription = null,
                tint = AppColors.icon.primary
            )
        }
    }
}

@Preview
@Composable
private fun PRe() {
    TikonchaParentTheme(mode = ThemeMode.DARK) {
        ChatRoomScreenUi(
            state = ChatRoomState(
                chatTitle = "Ibroxim Odilov",
                replyToMessage = ChatMessageUi(
                    id = "1",
                    isMine = true,
                    message = "Salom",
                    messageType = ChatMessageType.TEXT,
                    senderName = "Dilshod",
                    senderAvatar = "",
                    status = DeliveryStatus.READ,
                    clientMsgId = "",
                    time = "",
                    repliedMessageText = "Salom qalesan nima gaplar qayerdasan, kecha qayerda eding, hammasi ok?",
                    repliedMessageOwner = "Ibroxim Odilov",
                    createdAt = DateTimeUtil.nowMillis(),
                    updatedAt = DateTimeUtil.nowMillis()
                )
            )
        ) { }
    }
}
