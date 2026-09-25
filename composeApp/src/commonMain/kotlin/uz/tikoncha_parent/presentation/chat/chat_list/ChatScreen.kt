package uz.tikoncha_parent.presentation.chat.chat_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import uz.tikoncha_parent.presentation.base.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.farzand_qoshilgandan_keyin_korinish
import tikoncha_parents.composeapp.generated.resources.farzand_qoshilmagan
import tikoncha_parents.composeapp.generated.resources.hozircha_suhbat_yoq
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.suhbat
import tikoncha_parents.composeapp.generated.resources.suhbatlar_yoq
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.presentation.base.AppEmptyList
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.ErrorRetryState
import uz.tikoncha_parent.presentation.base.NoInternetDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.rememberInternetCheck
import uz.tikoncha_parent.presentation.chat.ChatMessageAiScreen
import uz.tikoncha_parent.presentation.chat.chat_room.ChatRoomScreen
import uz.tikoncha_parent.presentation.model.ChatType
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.DividerHorizontal
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars


class ChatScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return

        val viewModel = koinScreenModel<ChatViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        ChatUi(
            navigator = navigator,
            state = state,
            event = event
        )
    }
}


@Composable
fun ChatUi(
    navigator: Navigator?,
    state: ChatState,
    event: (ChatEvent) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val internetCheck = rememberInternetCheck(scope)
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.page
    )

    DisposableEffect(Unit) {
        event(ChatEvent.OnScreenOpened("ChatScreen"))
        onDispose {
            event(ChatEvent.OnScreenClosed("ChatScreen"))
        }
    }

    val loadFailure = state.error
    val errorText = loadFailure?.asText()

    // Ro'yxat bo'sh bo'lsa xato ekran ichida ko'rsatiladi — dialog ortiqcha.
    // Dialog faqat yangilash yiqilganda kerak (ro'yxat allaqachon ko'rinib turibdi).
    LaunchedEffect(errorText, state.chats.isEmpty()) {
        if (!errorText.isNullOrEmpty() && state.chats.isNotEmpty()) {
            showDialog = true
        }
    }

    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = showDialog,
        title = stringResource(Res.string.xatolik),
        message = errorText ?: "",
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = {
            showDialog = false
            event(ChatEvent.ClearError)
        },
        onButtonClick = {
            showDialog = false
            event(ChatEvent.ClearError)
        }
    )

    NoInternetDialog(internetCheck)

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        modifier = Modifier.fillMaxSize(),
        onRefresh = {
            internetCheck.check {
                event(ChatEvent.Refresh)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(systemBars.modifier)
                .background(AppColors.bg.page)
        ) {

            CustomHeader(
                showBackButton = true,
                onBackClick = {
                    navigator?.pop()
                },
                title = stringResource(Res.string.suhbat),
            )

            when {
                !state.hasLoadedOnce -> {
                    ChatListShimmer(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = ContainerPadding)
                    )
                }

                loadFailure != null && state.chats.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            ErrorRetryState(
                                failure = loadFailure,
                                onRetry = { event(ChatEvent.Refresh) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                state.chats.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            if (!state.hasChild) {
                                AppEmptyList(
                                    title = stringResource(Res.string.farzand_qoshilmagan),
                                    message = stringResource(Res.string.farzand_qoshilgandan_keyin_korinish),
                                )
                            } else {
                                AppEmptyList(
                                    title = stringResource(Res.string.suhbatlar_yoq),
                                    message = stringResource(Res.string.hozircha_suhbat_yoq),
                                )
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.padding(horizontal = ContainerPadding)
                    ) {
                        LazyColumn {
                            items(
                                items = state.chats,
                                key = { it.chatId },
                                contentType = { "chat_item" }
                            ) { item ->
                                val onClick = remember(item.chatId) {
                                    {
                                        if (item.type == ChatType.BOT) {
                                            navigator?.push(
                                                ChatMessageAiScreen(
                                                    chatId = item.chatId,
                                                    chatTitle = item.title,
                                                )
                                            )
                                        } else {
                                            navigator?.push(
                                                ChatRoomScreen(
                                                    chatId = item.chatId,
                                                    chatAvatar = item.avatar,
                                                    chatTitle = item.title,
                                                    chatType = item.type
                                                )
                                            )
                                        }
                                        Unit
                                    }
                                }

                                Column {
                                    ChatListItem(
                                        chatUi = item,
                                        onClick = onClick
                                    )
                                    DividerHorizontal()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(mode = ThemeMode.LIGHT) {
        ChatUi(
            navigator = null,
            state = ChatState(),
            event = {}
        )
    }
}