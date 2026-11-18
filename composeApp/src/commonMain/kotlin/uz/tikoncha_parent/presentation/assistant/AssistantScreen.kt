package uz.tikoncha_parent.presentation.assistant

import androidx.compose.ui.unit.sp
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tikoncha_parents.composeapp.generated.resources.ai_ask_where
import tikoncha_parents.composeapp.generated.resources.ai_auto_speak_off
import tikoncha_parents.composeapp.generated.resources.ai_auto_speak_on
import tikoncha_parents.composeapp.generated.resources.ai_done_verified
import tikoncha_parents.composeapp.generated.resources.ai_err_forbidden
import tikoncha_parents.composeapp.generated.resources.ai_err_no_coins_todo
import tikoncha_parents.composeapp.generated.resources.ai_err_plus_button
import tikoncha_parents.composeapp.generated.resources.ai_err_plus_only
import tikoncha_parents.composeapp.generated.resources.ai_err_todo_not_done
import tikoncha_parents.composeapp.generated.resources.ai_hero_hint
import tikoncha_parents.composeapp.generated.resources.ai_mic_denied_body
import tikoncha_parents.composeapp.generated.resources.ai_mic_denied_title
import tikoncha_parents.composeapp.generated.resources.ai_read_aloud
import tikoncha_parents.composeapp.generated.resources.ai_suggest_task_named
import tikoncha_parents.composeapp.generated.resources.ai_suggest_today
import tikoncha_parents.composeapp.generated.resources.ai_suggest_youtube
import tikoncha_parents.composeapp.generated.resources.ai_voice_mic
import tikoncha_parents.composeapp.generated.resources.tracking_permission_settings_open
import uz.tikoncha_parent.domain.model.ai.DraftApplyError
import uz.tikoncha_parent.domain.model.ai.NavScreen
import uz.tikoncha_parent.domain.model.ai.NavTarget
import uz.tikoncha_parent.platform.openAppSettings
import uz.tikoncha_parent.presentation.assistant.voice.rememberMicPermissionRequest
import uz.tikoncha_parent.presentation.chat.chat_list.ChatScreen
import uz.tikoncha_parent.presentation.live.ParentLiveScreen
import uz.tikoncha_parent.presentation.policy.list.PolicyListScreen
import uz.tikoncha_parent.presentation.profile.ProfileScreen
import uz.tikoncha_parent.presentation.profile.subscription.info.SubscriptionScreen
import uz.tikoncha_parent.presentation.statistic.StatisticScreen
import uz.tikoncha_parent.presentation.task.TaskScreen
import kotlinx.datetime.toLocalDateTime
import uz.tikoncha_parent.domain.model.ai.SafetyAlert
import coil3.compose.AsyncImage
import androidx.compose.ui.text.style.TextDirection
import tikoncha_parents.composeapp.generated.resources.ai_done_saved
import tikoncha_parents.composeapp.generated.resources.ai_done_deleted
import tikoncha_parents.composeapp.generated.resources.ai_done_approved
import tikoncha_parents.composeapp.generated.resources.ai_done_rejected
import tikoncha_parents.composeapp.generated.resources.ai_done_task_sent
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.outlined.AddComment
import tikoncha_parents.composeapp.generated.resources.ai_reminder_sent
import tikoncha_parents.composeapp.generated.resources.ai_remind_child
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.NotificationsActive
import tikoncha_parents.composeapp.generated.resources.ai_task_reward
import tikoncha_parents.composeapp.generated.resources.ai_new_chat_confirm
import tikoncha_parents.composeapp.generated.resources.ai_location_gps_off
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.ui.graphics.vector.ImageVector
import uz.tikoncha_parent.presentation.protection.ProtectionScreen
import uz.tikoncha_parent.domain.model.ai.ProtectionCard
import tikoncha_parents.composeapp.generated.resources.mode_default_title
import tikoncha_parents.composeapp.generated.resources.mode_gujanak_title
import tikoncha_parents.composeapp.generated.resources.mode_qalqon_title
import tikoncha_parents.composeapp.generated.resources.ai_open_protection
import tikoncha_parents.composeapp.generated.resources.ai_protection_ok
import tikoncha_parents.composeapp.generated.resources.ai_protection_title
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ai_no_child_body
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.no_child_title
import tikoncha_parents.composeapp.generated.resources.ai_ago_days
import tikoncha_parents.composeapp.generated.resources.ai_ago_hours
import tikoncha_parents.composeapp.generated.resources.ai_ago_minutes
import tikoncha_parents.composeapp.generated.resources.ai_assistant_subtitle
import tikoncha_parents.composeapp.generated.resources.ai_assistant_title
import tikoncha_parents.composeapp.generated.resources.ai_buy_coins
import tikoncha_parents.composeapp.generated.resources.ai_cost_hint
import tikoncha_parents.composeapp.generated.resources.ai_draft_applied
import tikoncha_parents.composeapp.generated.resources.ai_draft_confirm
import tikoncha_parents.composeapp.generated.resources.ai_draft_discarded
import tikoncha_parents.composeapp.generated.resources.ai_draft_do_approve
import tikoncha_parents.composeapp.generated.resources.ai_draft_do_confirm
import tikoncha_parents.composeapp.generated.resources.ai_draft_do_delete
import tikoncha_parents.composeapp.generated.resources.ai_draft_do_reject
import tikoncha_parents.composeapp.generated.resources.ai_draft_done
import tikoncha_parents.composeapp.generated.resources.ai_draft_hint
import tikoncha_parents.composeapp.generated.resources.ai_error_generic
import tikoncha_parents.composeapp.generated.resources.ai_error_network
import tikoncha_parents.composeapp.generated.resources.ai_input_hint
import tikoncha_parents.composeapp.generated.resources.ai_location_ago
import tikoncha_parents.composeapp.generated.resources.ai_location_now
import tikoncha_parents.composeapp.generated.resources.ai_location_title
import tikoncha_parents.composeapp.generated.resources.ai_new_chat
import tikoncha_parents.composeapp.generated.resources.ai_open_map
import tikoncha_parents.composeapp.generated.resources.ai_period_today
import tikoncha_parents.composeapp.generated.resources.ai_period_week
import tikoncha_parents.composeapp.generated.resources.ai_period_yesterday
import tikoncha_parents.composeapp.generated.resources.ai_report_avg
import tikoncha_parents.composeapp.generated.resources.ai_report_failed
import tikoncha_parents.composeapp.generated.resources.ai_report_learning
import tikoncha_parents.composeapp.generated.resources.ai_report_night
import tikoncha_parents.composeapp.generated.resources.ai_report_no_data
import tikoncha_parents.composeapp.generated.resources.ai_report_screen_time
import tikoncha_parents.composeapp.generated.resources.ai_report_tasks
import tikoncha_parents.composeapp.generated.resources.ai_report_top_apps
import tikoncha_parents.composeapp.generated.resources.ai_send
import tikoncha_parents.composeapp.generated.resources.ai_stop
import tikoncha_parents.composeapp.generated.resources.ai_suggest_games
import tikoncha_parents.composeapp.generated.resources.ai_suggest_task
import tikoncha_parents.composeapp.generated.resources.ai_suggest_week
import tikoncha_parents.composeapp.generated.resources.ai_suggest_where
import tikoncha_parents.composeapp.generated.resources.ai_summary_label
import tikoncha_parents.composeapp.generated.resources.ai_thinking
import tikoncha_parents.composeapp.generated.resources.arrow_left
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.ai_avatar
import tikoncha_parents.composeapp.generated.resources.coin_3d
import tikoncha_parents.composeapp.generated.resources.qayta_urinish
import tikoncha_parents.composeapp.generated.resources.yopish
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.ai.ChildReport
import uz.tikoncha_parent.domain.model.ai.LocationCard
import uz.tikoncha_parent.domain.model.ai.ReportPeriod
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.island.ChildAvatar
import uz.tikoncha_parent.presentation.profile.coins.CoinsScreen
import uz.tikoncha_parent.presentation.tracking.TrackingScreen
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.HeaderHeight
import uz.tikoncha_parent.ui.NormalIconButtonPadding
import uz.tikoncha_parent.ui.NormalIconButtonSize
import uz.tikoncha_parent.ui.expressiveSegment
import uz.tikoncha_parent.ui.rememberHapticSource
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

/**
 * Ota-ona yordamçisi — ilovaning hamma imkoniyati bitta joyda. Tepada katta tipratikan (holatini körsatadi),
 * "Bugun qisqaça" kartalari va farzand hisoboti (suhbat boşlanganda yig'iladi), pastda suhbat: savol yoki
 * "qoida ayting" → tasdiqlaş kartasi, joylaşuv / himoya kartasi, "Oçiş: …" tugmasi.
 * Asosiy kiritiş — bosh barmoq ostidagi katta mikrofon; yoziş maydoni — ikkinçi yöl.
 */
class AssistantScreen(
    /** Faqat debug: ekran ochilishi bilan beriladigan savol (SplashScreen `-hoglt.ask`). */
    private val initialQuestion: String? = null,
    /** Faqat debug: "day" · "yesterday" · "week". */
    private val initialPeriod: String? = null,
    /** Hoglt Chat ro'yxatidagi "Bilimdon AI" suhbati — oçilganda öqilgan deb belgilanadi (ogohlantirişlar shu yerda). */
    private val botChatId: String? = null,
    /** Oçilişi bilan tinglaşni boşlaydi (navbar'dagi mikrofon tugmasi). */
    private val startListening: Boolean = false,
) : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<AssistantViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val requestMic = rememberMicPermissionRequest()
        val scope = rememberCoroutineScope()
        // Bosilgan paytdagi holat öqiladi (lambda eski holatni ushlab qolmasin)
        val onMic: () -> Unit = {
            val s = viewModel.state.value
            when {
                s.streaming -> viewModel.onEvent(AssistantEventUi.Stop)
                s.voice == VoicePhase.LISTENING -> viewModel.onEvent(AssistantEventUi.StopListening)
                s.voice == VoicePhase.UNDERSTANDING -> Unit
                else -> scope.launch {
                    viewModel.onEvent(if (requestMic()) AssistantEventUi.StartListening else AssistantEventUi.MicDenied)
                }
            }
        }
        var started by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            if (started) return@LaunchedEffect
            started = true
            botChatId?.let { viewModel.markChatRead(it) }
            ReportPeriod.entries.firstOrNull { it.key == initialPeriod }?.let {
                viewModel.onEvent(AssistantEventUi.SelectPeriod(it))
            }
            initialQuestion?.takeIf { it.isNotBlank() }?.let { viewModel.onEvent(AssistantEventUi.Ask(it)) }
            if (startListening && initialQuestion.isNullOrBlank()) {
                viewModel.onEvent(if (requestMic()) AssistantEventUi.StartListening else AssistantEventUi.MicDenied)
            }
        }
        // Boşqa ekranga ötilsa — yozuv taşlanadi, ovoz töxtaydi
        DisposableEffect(Unit) {
            onDispose { viewModel.onEvent(AssistantEventUi.CancelListening) }
        }
        AssistantUi(state = state, voiceLevel = viewModel.voiceLevel, event = viewModel::onEvent, onMic = onMic)
    }
}

/**
 * Yordamçi tavsiya qilgan ekran (SSE `nav`). [childId] bölsa — öşa farzand tanlanadi.
 * null — ekran emas (yordam xizmati — pastdan oçiladigan oyna, [AssistantUi] özi körsatadi).
 */
private fun navTarget(target: NavTarget, selectedChildId: String?): Screen? = when (target.screen) {
    NavScreen.MAP -> TrackingScreen()
    NavScreen.PROTECTION -> ProtectionScreen()
    NavScreen.RULES -> PolicyListScreen()
    NavScreen.TASKS -> TaskScreen()
    NavScreen.ADD_CHILD -> uz.tikoncha_parent.presentation.add_child.AddChildScreen()
    NavScreen.SUBSCRIPTION -> SubscriptionScreen()
    NavScreen.COINS -> CoinsScreen()
    NavScreen.LIVE -> ParentLiveScreen(childUserId = target.childId ?: selectedChildId)
    NavScreen.STATS -> StatisticScreen()
    NavScreen.CHAT -> ChatScreen()
    NavScreen.PROFILE -> ProfileScreen()
    NavScreen.SETTINGS -> uz.tikoncha_parent.presentation.profile.settings.SettingsScreen()
    NavScreen.TEXT_SIZE -> uz.tikoncha_parent.presentation.profile.settings.text_size.TextSizeScreen()
    NavScreen.THEME -> uz.tikoncha_parent.presentation.profile.settings.theme.ThemeScreen()
    NavScreen.LANGUAGE -> uz.tikoncha_parent.presentation.profile.language.LanguageScreen()
    NavScreen.NOTIFICATIONS -> uz.tikoncha_parent.presentation.notification.NotificationScreen()
    NavScreen.TUTORIALS -> uz.tikoncha_parent.presentation.tutorial_clips.TutorialClipsScreen()
    NavScreen.PAYMENT_HISTORY -> uz.tikoncha_parent.presentation.profile.payment_history.PaymentHistoryScreen()
    NavScreen.CHILDREN -> uz.tikoncha_parent.presentation.profile.children.ChildrenSelectScreen()
    NavScreen.SUPPORT -> null
}

@Composable
private fun AssistantUi(
    state: AssistantState,
    voiceLevel: StateFlow<Float>,
    event: (AssistantEventUi) -> Unit,
    onMic: () -> Unit,
) {
    val navigator = LocalNavigator.current
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var bottomHeight by remember { mutableStateOf(120.dp) }
    var confirmNewChat by remember { mutableStateOf(false) }
    var fieldFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    var showSupport by remember { mutableStateOf(false) }
    if (showSupport) uz.tikoncha_parent.presentation.support.SupportSheet(onDismiss = { showSupport = false })
    val openNav: (AssistantItem.Nav) -> Unit = { item ->
        item.target.childId?.let { event(AssistantEventUi.ShowOnMap(it)) }  // farzandni tanlaydi
        val screen = navTarget(item.target, state.child?.userId)
        if (screen != null) navigator?.push(screen) else showSupport = true
    }
    // Javob faqat "Oçiş: …" bölsa — ekran özi oçiladi (bir marta)
    LaunchedEffect(state.autoOpen?.id) {
        val nav = state.autoOpen ?: return@LaunchedEffect
        delay(400)  // ota-ona tugmani körib ulgursin
        // Ikkalasi ham to'xtovsiz (suspend emas): holat özgarib effekt qayta boşlansa ham ekran oçiladi
        event(AssistantEventUi.AutoOpenHandled)
        openNav(nav)
    }

    // Suhbat boşlangach hisobot tepada bir qatorga yig'iladi (bosilsa oçiladi), suhbat esa pastdan ösadi
    val conversation = state.items.isNotEmpty() || state.streaming
    var reportOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(conversation) {
        if (!conversation) {
            reportOpen = false
            listState.scrollToItem(0)  // yangi suhbat — yana tepadan: tipratikan va "Bugun qisqaça"
        }
    }

    // Tepada katta tipratikan (1-element) + suhbat
    val itemCount = 1 + state.items.size + (if (state.streaming) 1 else 0)
    LaunchedEffect(state.items.size, state.streaming, state.streamingText.length / 80) {
        if (conversation) listState.animateScrollToItem(itemCount - 1)
    }
    // Katta tipratikan körinmay qolsa — sarlavhada kiçkinasi (56dp) çiqadi
    val heroHidden by remember {
        derivedStateOf {
            val hero = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == "hero" }
            hero == null || -hero.offset > hero.size * 0.55f
        }
    }

    state.draftError?.let { error ->
        DraftErrorDialog(
            error = error,
            onDismiss = { event(AssistantEventUi.DismissDraftError) },
            onPlus = {
                event(AssistantEventUi.DismissDraftError)
                navigator?.push(SubscriptionScreen())
            },
            onCoins = {
                event(AssistantEventUi.DismissDraftError)
                navigator?.push(CoinsScreen())
            },
        )
    }
    if (state.micDenied) {
        AlertDialog(
            onDismissRequest = { event(AssistantEventUi.DismissMicDenied) },
            confirmButton = {
                TextButton(onClick = { event(AssistantEventUi.DismissMicDenied); openAppSettings() }) {
                    Text(stringResource(Res.string.tracking_permission_settings_open), color = AppColors.text.accentEmphasis)
                }
            },
            dismissButton = {
                TextButton(onClick = { event(AssistantEventUi.DismissMicDenied) }) {
                    Text(stringResource(Res.string.bekor_qilish), color = AppColors.text.secondary)
                }
            },
            title = { Text(stringResource(Res.string.ai_mic_denied_title), style = AppTypography.titleMdSemiBold, color = AppColors.text.primary) },
            text = { Text(stringResource(Res.string.ai_mic_denied_body), style = AppTypography.bodyLgRegular, color = AppColors.text.primary) },
            containerColor = AppColors.bg.elevated,
        )
    }
    if (confirmNewChat) {
        AlertDialog(
            onDismissRequest = { confirmNewChat = false },
            confirmButton = {
                TextButton(onClick = { confirmNewChat = false; event(AssistantEventUi.NewChat) }) {
                    Text(stringResource(Res.string.ai_new_chat), color = AppColors.text.accentEmphasis)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmNewChat = false }) {
                    Text(stringResource(Res.string.bekor_qilish), color = AppColors.text.secondary)
                }
            },
            text = { Text(stringResource(Res.string.ai_new_chat_confirm), style = AppTypography.bodyLgRegular, color = AppColors.text.primary) },
            containerColor = AppColors.bg.elevated,
        )
    }

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.elevated,
        navigationBarColor = AppColors.bg.surface,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page)
    ) {
        Header(
            mascot = state.mascot,
            showMascot = heroHidden,
            autoSpeak = state.autoSpeak,
            showAutoSpeak = state.ttsAvailable,
            onToggleAutoSpeak = { event(AssistantEventUi.ToggleAutoSpeak) },
            coins = state.coins,
            canReset = conversation && !state.streaming,
            onBack = { navigator?.pop() },
            onNewChat = {
                val pending = state.items.any { it is AssistantItem.Draft && it.status == DraftStatus.PENDING }
                if (pending) confirmNewChat = true else event(AssistantEventUi.NewChat)
            },
            onCoins = { navigator?.push(CoinsScreen()) },
        )

        // Farzand yo'q bo'lsa suhbat tepasida hisobot qatori ham yo'q ("Farzand qo'shish" kartasi faqat boshida)
        if (conversation && state.child != null) {
            ReportCard(
                state = state,
                open = reportOpen,
                onToggle = { reportOpen = !reportOpen },
                event = event,
                modifier = Modifier.padding(start = ContainerPadding, end = ContainerPadding, top = 10.dp),
            )
        }

        Box(Modifier.fillMaxSize().imePadding()) {
            if (conversation) {
                // Tepadagi hisobot qatori ostidan suhbat yumşoq yöqoladi (keskin kesilmaydi)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .align(Alignment.TopCenter)
                        .zIndex(1f)
                        .background(Brush.verticalGradient(listOf(AppColors.bg.page, AppColors.bg.page.copy(alpha = 0f))))
                )
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = ContainerPadding,
                    end = ContainerPadding,
                    top = 12.dp,
                    bottom = bottomHeight + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(key = "hero") {
                    Hero(mascot = state.mascot, listening = state.voice == VoicePhase.LISTENING, voiceLevel = voiceLevel)
                }
                if (!conversation) {
                    val child = state.child
                    if (child != null) {
                        item(key = "glance") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (state.children.size > 1) {
                                    ChildChips(
                                        children = state.children,
                                        selected = child,
                                        onSelect = { event(AssistantEventUi.SelectChild(it)) },
                                        contentPadding = PaddingValues(0.dp),
                                    )
                                }
                                GlanceGrid(
                                    glance = state.glance,
                                    childName = child.firstName(),
                                    onAsk = { event(AssistantEventUi.Ask(it)) },
                                )
                            }
                        }
                    }
                    item(key = "report") {
                        ReportCard(state = state, open = true, onToggle = {}, event = event, showChildChips = false)
                    }
                    // Farzand Bilimdonga yuborgan xavotirli xabarlar (oxirgi 7 kun) — hisobot ostida, birinçi körinadi
                    items(state.alerts, key = { "alert-" + it.id }) { alert -> SafetyAlertCard(alert) }
                }
                items(state.items, key = { it.id }) { item ->
                    when (item) {
                        is AssistantItem.Parent -> ParentBubble(item.text)
                        is AssistantItem.Reply -> AssistantReply(
                            item.text,
                            streaming = false,
                            speaker = state.ttsAvailable,
                            speaking = state.speakingId == item.id,
                            onSpeak = { event(AssistantEventUi.Speak(item.id)) },
                        )
                        is AssistantItem.Nav -> NavCardView(item.target.screen, onOpen = { openNav(item) })
                        is AssistantItem.Draft -> DraftCard(
                            item = item,
                            children = state.children,
                            onApply = { coin -> event(AssistantEventUi.Apply(item.id, coin)) },
                            onDiscard = { event(AssistantEventUi.Discard(item.id)) },
                        )
                        is AssistantItem.Location -> LocationCardView(
                            card = item.card,
                            reminded = item.id in state.reminded,
                            onRemind = { event(AssistantEventUi.Remind(item.id, item.card.childId, "gps")) },
                            onOpen = {
                                event(AssistantEventUi.ShowOnMap(item.card.childId))
                                navigator?.push(TrackingScreen())
                            },
                        )
                        is AssistantItem.Protection -> ProtectionCardView(
                            card = item.card,
                            reminded = item.id in state.reminded,
                            onRemind = { event(AssistantEventUi.Remind(item.id, item.card.childId, "permissions")) },
                            onOpen = {
                                event(AssistantEventUi.ShowOnMap(item.card.childId))  // farzandni tanlaydi
                                navigator?.push(ProtectionScreen())
                            },
                        )
                        is AssistantItem.Problem -> ProblemCard(
                            item = item,
                            onRetry = { event(AssistantEventUi.Retry(item.id)) },
                            onBuyCoins = { navigator?.push(CoinsScreen()) },
                        )
                    }
                }
                if (state.streaming) {
                    item(key = "streaming") {
                        if (state.streamingText.isBlank()) ThinkingRow(state.status)
                        else AssistantReply(state.streamingText, streaming = true, speaker = false, speaking = false, onSpeak = {})
                    }
                }
            }

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .onSizeChanged { bottomHeight = with(density) { it.height.toDp() } }
                    .background(AppColors.bg.page)
            ) {
                val voiceBusy = state.voice != VoicePhase.IDLE
                if (!state.streaming && !voiceBusy) {
                    SuggestionChips(
                        conversation = conversation,
                        asked = state.items.mapNotNull { (it as? AssistantItem.Parent)?.text }.toSet(),
                        childName = state.child?.firstName(),
                        onAsk = { event(AssistantEventUi.Ask(it)) },
                    )
                }
                // Klaviatura oçiq bölsa katta mikrofon yaşirinadi (joy yozişga), maydonda kiçkinasi qoladi
                val dockVisible = !fieldFocused || voiceBusy
                AnimatedVisibility(visible = dockVisible, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    VoiceDock(
                        phase = state.voice,
                        streaming = state.streaming,
                        missed = state.voiceMissed,
                        level = voiceLevel,
                        onTap = onMic,
                    )
                }
                InputBar(
                    value = state.text,
                    streaming = state.streaming,
                    dockVisible = dockVisible,
                    onFocus = { fieldFocused = it },
                    onChange = { event(AssistantEventUi.OnText(it)) },
                    onSend = { event(AssistantEventUi.Send) },
                    onStop = { event(AssistantEventUi.Stop) },
                    onMic = {
                        focusManager.clearFocus()
                        onMic()
                    },
                )
            }
        }
    }
}

/** Serverdagi bo'ş otasining ismi "null" bo'lib keladi ("Shuxratov Saidburxon null"). */
private fun String.cleanName(): String =
    split(' ').filter { it.isNotBlank() && !it.equals("null", ignoreCase = true) }.joinToString(" ")

/** Oltin tugmalar ustidagi matn: oq emas, to'q jigarrang — kontrast ~6:1. */
internal val OnAccent = Color(0xFF2B1D12)

/** Farzandning ismi (savollarda): "Malika". */
private fun UserInfo.firstName(): String = name.ifBlank { fullName.cleanName().split(' ').firstOrNull().orEmpty() }

/** Katta tipratikan (≈150dp) va bir qator — tepada, suhbat qisqa bölganda körinadi. */
@Composable
private fun Hero(mascot: MascotState, listening: Boolean, voiceLevel: StateFlow<Float>) {
    val level by voiceLevel.collectAsState()
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Mascot(state = mascot, size = 150.dp, rings = listening, level = { level })
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(Res.string.ai_hero_hint),
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.primary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** Qoidani tasdiqlaş xatosi: maşina kodi böyiça oddiy matn; Plus kerak bölsa — obuna ekraniga tugma. */
@Composable
private fun DraftErrorDialog(error: DraftApplyError, onDismiss: () -> Unit, onPlus: () -> Unit, onCoins: () -> Unit) {
    val paid = error.errorCode == "quick_block_paid" || error.errorCode == "protection_paid"
    val coins = error.errorCode == "insufficient_coins_for_todo"
    val message = when (error.errorCode) {
        "quick_block_paid", "protection_paid" -> stringResource(Res.string.ai_err_plus_only)
        "todo_not_child_done" -> stringResource(Res.string.ai_err_todo_not_done)
        "insufficient_coins_for_todo" -> stringResource(Res.string.ai_err_no_coins_todo)
        "forbidden" -> stringResource(Res.string.ai_err_forbidden)
        else -> error.failure.asText()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            when {
                paid -> TextButton(onClick = onPlus) {
                    Text(stringResource(Res.string.ai_err_plus_button), color = AppColors.text.accentEmphasis)
                }
                coins -> TextButton(onClick = onCoins) {
                    Text(stringResource(Res.string.ai_buy_coins), color = AppColors.text.accentEmphasis)
                }
                else -> TextButton(onClick = onDismiss) { Text(stringResource(Res.string.yopish)) }
            }
        },
        dismissButton = if (paid || coins) {
            { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.yopish), color = AppColors.text.secondary) } }
        } else null,
        text = { Text(message, style = AppTypography.bodyLgRegular, color = AppColors.text.primary) },
        containerColor = AppColors.bg.elevated,
    )
}

// ── tepa qism ──

@Composable
private fun Header(
    mascot: MascotState,
    showMascot: Boolean,
    autoSpeak: Boolean,
    showAutoSpeak: Boolean,
    onToggleAutoSpeak: () -> Unit,
    coins: Int?,
    canReset: Boolean,
    onBack: () -> Unit,
    onNewChat: () -> Unit,
    onCoins: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.bg.elevated)
            .height(HeaderHeight + 8.dp)
            .padding(start = ContainerPadding - 4.dp, end = ContainerPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            modifier = Modifier.size(NormalIconButtonSize),
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent,
                contentColor = AppColors.icon.primary,
            ),
        ) {
            Icon(
                painter = painterResource(Res.drawable.arrow_left),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(NormalIconButtonPadding),
            )
        }
        Spacer(Modifier.width(6.dp))
        // Katta tipratikan yuqoriga surilib ketgaç — şu yerda kiçkinasi (holati bilan)
        AnimatedVisibility(
            visible = showMascot,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Mascot(state = mascot, size = 56.dp)
                Spacer(Modifier.width(8.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            // Tor sarlavhada nom kesilmasin ("Aqlli…") — şrift kiçrayadi
            androidx.compose.foundation.text.BasicText(
                text = stringResource(Res.string.ai_assistant_title),
                style = AppTypography.titleMdSemiBold.copy(color = AppColors.text.primary),
                maxLines = 1,
                autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(
                    minFontSize = 11.sp, maxFontSize = AppTypography.titleMdSemiBold.fontSize, stepSize = 0.5.sp,
                ),
            )
            // Holat (nima qilayotgani) — faqat suhbatdagi pufakda, sarlavhada takrorlanmaydi
        }
        if (showAutoSpeak) {
            // Javoblarni avtomatik ovoz bilan öqiş (standart — öçiq)
            IconButton(
                onClick = onToggleAutoSpeak,
                interactionSource = rememberHapticSource(),
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    if (autoSpeak) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                    contentDescription = stringResource(if (autoSpeak) Res.string.ai_auto_speak_on else Res.string.ai_auto_speak_off),
                    tint = if (autoSpeak) AppColors.icon.accentPrimary else AppColors.icon.secondary,
                )
            }
        }
        AnimatedVisibility(visible = canReset, enter = fadeIn(), exit = fadeOut()) {
            IconButton(
                onClick = onNewChat,
                interactionSource = rememberHapticSource(),
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    Icons.Outlined.AddComment,
                    contentDescription = stringResource(Res.string.ai_new_chat),
                    tint = AppColors.icon.secondary,
                )
            }
        }
        if (coins != null) {
            Spacer(Modifier.width(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.bg.secondary)
                    .clickable(interactionSource = rememberHapticSource(), indication = null, onClick = onCoins)
                    .padding(start = 6.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.coin_3d),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "$coins",
                    style = AppTypography.titleSmSemiBold,
                    color = AppColors.text.primary,
                )
            }
        }
    }
}

/** Yordamçi: tipratikan. Javob yozayotganda atrofida yumşoq nafas olayotgan halqa. */
@Composable
private fun MascotAvatar(size: androidx.compose.ui.unit.Dp, active: Boolean) {
    val t = rememberInfiniteTransition(label = "mascot")
    val pulse by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "pulse")
    val ring = if (active) AppColors.action.primary.copy(alpha = 0.25f + 0.45f * pulse) else Color.Transparent
    Box(
        modifier = Modifier
            .size(size + 6.dp)
            .border(2.dp, ring, CircleShape)
            .padding(3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.ai_avatar),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(CircleShape).background(AppColors.bg.secondary),
        )
    }
}

// ── hisobot ──

/** Xavotirli xabar: sarlavha, ota-onaga maslahat, vaqt va (giyohvand modda / qurol / zöravonlik bölsa) rasm. */
@Composable
private fun SafetyAlertCard(alert: SafetyAlert) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.bg.secondary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                alert.title,
                style = AppTypography.titleSmSemiBold,
                color = AppColors.text.accentWarning,
                modifier = Modifier.weight(1f),
                maxLines = 2,
            )
            Text(alertTime(alert.createdAt), style = AppTypography.bodyMdRegular, color = AppColors.text.tertiary)
        }
        Text(alert.text, style = AppTypography.bodyLgRegular, color = AppColors.text.primary)
        alert.imageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
private fun alertTime(iso: String): String = runCatching {
    val local = kotlin.time.Instant.parse(iso).let {
        kotlinx.datetime.Instant.fromEpochMilliseconds(it.toEpochMilliseconds())
    }.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val hm = "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
    "${local.dayOfMonth.toString().padStart(2, '0')}.${local.monthNumber.toString().padStart(2, '0')} $hm"
}.getOrDefault("")

@Composable
private fun ReportCard(
    state: AssistantState,
    open: Boolean,
    onToggle: () -> Unit,
    event: (AssistantEventUi) -> Unit,
    modifier: Modifier = Modifier,
    /** Bösh suhbatda farzand tanlaş "Bugun qisqaça" ustida — kartada takrorlanmaydi. */
    showChildChips: Boolean = true,
) {
    val conversation = state.items.isNotEmpty() || state.streaming
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.bg.surface)
            .animateContentSize(tween(260))
    ) {
        if (state.child == null) {
            // Farzand ulanmagan: bo'sh hisobot (faqat davr tugmalari) o'rniga — nima qilish kerakligi va tugma
            NoChildCard()
            return@Column
        }
        if (!open) {
            ReportCollapsed(state, onToggle)
            return@Column
        }
        val chips = showChildChips && state.children.size > 1
        if (chips) {
            // Chiplar kartaning çetigaça boradi (ichki chekinish ularni kesmasin)
            ChildChips(
                children = state.children,
                selected = state.child,
                onSelect = { event(AssistantEventUi.SelectChild(it)) },
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = if (chips) 12.dp else 16.dp)) {
            PeriodSegments(selected = state.period, onSelect = { event(AssistantEventUi.SelectPeriod(it)) })
            Spacer(Modifier.height(18.dp))

            val report = state.report
            when {
                state.child == null -> Unit
                report == null && state.reportLoading -> ReportSkeleton()
                report == null && state.reportFailed -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.ai_report_failed),
                        style = AppTypography.bodyLgRegular,
                        color = AppColors.text.secondary,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { event(AssistantEventUi.RetryReport) }) {
                        Text(stringResource(Res.string.qayta_urinish), color = AppColors.text.accentEmphasis)
                    }
                }
                report != null -> ReportBody(report, loading = state.reportLoading)
            }
            if (conversation) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clickable(interactionSource = rememberHapticSource(), indication = null, onClick = onToggle)
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AppColors.icon.primary,
                        modifier = Modifier.rotate(180f),
                    )
                }
            }
        }
    }
}

/** Farzand hali ulanmagan — Bosh sahifadagi oyna bilan bir xil matn va to'g'ridan-to'g'ri "Farzand qo'shish". */
@Composable
private fun NoChildCard() {
    val navigator = LocalNavigator.current
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            stringResource(Res.string.no_child_title),
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.primary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(Res.string.ai_no_child_body),
            style = AppTypography.bodyLgRegular,
            color = AppColors.text.secondary,
        )
        Spacer(Modifier.height(14.dp))
        uz.tikoncha_parent.presentation.base.CustomButtonNew(
            text = stringResource(Res.string.farzand_qo_shish),
            onClick = { navigator?.push(uz.tikoncha_parent.presentation.add_child.AddChildScreen()) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Yig'ilgan hisobot: farzand · davr · ekran vaqti — bir qator, bosilsa oçiladi. */
@Composable
private fun ReportCollapsed(state: AssistantState, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = rememberHapticSource(), indication = null, onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.child?.let { ChildAvatar(it.avatarUrl, Modifier.size(32.dp)) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = state.child?.let { it.name.ifBlank { it.fullName.cleanName() } }.orEmpty(),
                style = AppTypography.titleSmSemiBold,
                color = AppColors.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

        }
        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = AppColors.icon.primary)
    }
}

@Composable
private fun ReportBody(report: ChildReport, loading: Boolean) {
    Column(Modifier.alpha(if (loading) 0.5f else 1f)) {
        Text(
            stringResource(Res.string.ai_report_screen_time),
            style = AppTypography.bodyMdMedium,
            color = AppColors.text.tertiary,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = report.totalText,
                style = AppTypography.headlineSmSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                color = AppColors.text.primary,
            )
            if (report.totalMinutes > 0) report.changePercent?.let { p -> ChangeBadge(p) }
        }
        if (report.period == ReportPeriod.WEEK && report.hasData) {
            Text(
                stringResource(Res.string.ai_report_avg, report.avgPerDayText),
                style = AppTypography.bodyLgRegular,
                color = AppColors.text.tertiary,
            )
        }

        if (report.topApps.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(Res.string.ai_report_top_apps),
                style = AppTypography.bodyMdMedium,
                color = AppColors.text.tertiary,
            )
            Spacer(Modifier.height(8.dp))
            val max = report.topApps.maxOf { it.minutes }.coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                report.topApps.forEach { app -> AppBar(app.name, app.text, app.minutes / max.toFloat()) }
            }
        }

        val facts = buildList {
            if (report.nightsWithUse > 0) add(Icons.Rounded.Bedtime to stringResource(Res.string.ai_report_night, report.nightsWithUse, report.nightText))
            if (report.tasksAssigned > 0) add(Icons.Rounded.TaskAlt to stringResource(Res.string.ai_report_tasks, report.tasksDone, report.tasksAssigned))
            if (report.tutorQuestions > 0) add(Icons.Rounded.School to stringResource(Res.string.ai_report_learning, report.tutorQuestions))
        }
        if (facts.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                facts.forEach { (icon, text) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, null, tint = AppColors.icon.secondary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(text, style = AppTypography.bodyLgRegular, color = AppColors.text.secondary)
                    }
                }
            }
        }

        val summary = report.summary ?: if (!report.hasData) stringResource(Res.string.ai_report_no_data) else null
        if (summary != null) {
            Spacer(Modifier.height(16.dp))
            if (report.summary != null) {
                Text(
                    stringResource(Res.string.ai_summary_label),
                    style = AppTypography.bodyMdMedium,
                    color = AppColors.text.tertiary,
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(text = summary, style = AppTypography.titleSmRegular, color = AppColors.text.primary)
        }
    }
}

/** Ekran vaqti özgarişi — neytral (ikkinçi urğu rangi yöq): strelka va foiz. */
@Composable
private fun ChangeBadge(percent: Int) {
    Spacer(Modifier.width(10.dp))
    Text(
        text = (if (percent < 0) "↓ " else if (percent > 0) "↑ " else "") + "${kotlin.math.abs(percent)}%",
        style = AppTypography.bodyMdMedium,
        color = AppColors.text.secondary,
        modifier = Modifier
            .clip(CircleShape)
            .background(AppColors.bg.secondary)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun AppBar(name: String, time: String, fraction: Float) {
    val f by animateFloatAsState(fraction.coerceIn(0.04f, 1f), tween(500), label = "bar")
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                name,
                style = AppTypography.bodyLgMedium,
                color = AppColors.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(time, style = AppTypography.bodyMdMedium, color = AppColors.text.secondary)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(AppColors.bg.secondary)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(AppColors.action.primary)
            )
        }
    }
}

@Composable
private fun ReportSkeleton() {
    val t = rememberInfiniteTransition(label = "skeleton")
    val a by t.animateFloat(0.35f, 0.7f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(0.35f, 0.6f, 1f, 0.85f).forEach { f ->
            Box(
                Modifier
                    .fillMaxWidth(f)
                    .height(14.dp)
                    .alpha(a)
                    .clip(CircleShape)
                    .background(AppColors.bg.secondary)
            )
        }
    }
}

private fun periodLabel(p: ReportPeriod): StringResource = when (p) {
    ReportPeriod.TODAY -> Res.string.ai_period_today
    ReportPeriod.YESTERDAY -> Res.string.ai_period_yesterday
    ReportPeriod.WEEK -> Res.string.ai_period_week
}

@Composable
private fun PeriodSegments(selected: ReportPeriod, onSelect: (ReportPeriod) -> Unit) {
    val periods = ReportPeriod.entries
    // Bitta yumşoq yo'lak, tanlangani unda ajralib turadi (tanlanmaganlar fonsiz)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(AppColors.bg.page.copy(alpha = 0.55f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        periods.forEachIndexed { i, period ->
            val isSelected = period == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .expressiveSegment(
                        i, periods.size,
                        color = if (isSelected) AppColors.action.primary.copy(alpha = 0.22f) else Color.Transparent,
                        horizontal = true,
                        selected = isSelected,
                        debounceMs = 150L,
                        onClick = { onSelect(period) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(periodLabel(period)),
                    style = AppTypography.titleSmSemiBold,
                    color = if (isSelected) AppColors.text.primary else AppColors.text.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun ChildChips(
    children: List<UserInfo>,
    selected: UserInfo?,
    onSelect: (UserInfo) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    // Tanlangan farzand ko'rinib tursin (hisobot aynan u haqida)
    val rowState = rememberLazyListState()
    val density = LocalDensity.current
    LaunchedEffect(selected?.userId) {
        val i = children.indexOfFirst { it.userId == selected?.userId }
        // Faqat tanlangan chip körinmasa suriladi (aks holda boşlanğiç toza qoladi)
        val info = rowState.layoutInfo
        val item = info.visibleItemsInfo.firstOrNull { it.index == i }
        val fullyVisible = item != null && item.offset >= info.viewportStartOffset &&
            item.offset + item.size <= info.viewportEndOffset
        if (i > 0 && !fullyVisible) rowState.animateScrollToItem(i)
    }
    val firstNames = children.map { it.name.ifBlank { it.fullName } }
    fun label(child: UserInfo): String {
        val first = child.name.ifBlank { child.fullName }
        val surname = child.fullName.cleanName().split(' ').firstOrNull { it.isNotBlank() && it != first }.orEmpty()
        return if (surname.isNotBlank()) "$first ${surname.first()}." else first
    }
    LazyRow(
        state = rowState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(children, key = { it.userId }) { child ->
            val isSelected = child.userId == selected?.userId
            val bg by animateColorAsState(
                if (isSelected) AppColors.action.primary.copy(alpha = 0.18f) else AppColors.bg.secondary,
                tween(200),
                label = "chip",
            )
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .background(bg)
                    .clickable(interactionSource = rememberHapticSource(), indication = null) { onSelect(child) }
                    .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChildAvatar(child.avatarUrl, Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    label(child),
                    // Lotin ism arab interfeysida ham "Saidburxon S." bölib qolsin (".Saidburxon S" emas)
                    style = AppTypography.titleSmSemiBold.copy(textDirection = TextDirection.Content),
                    color = AppColors.text.primary,
                    maxLines = 1,
                )
            }
        }
    }
}

// ── suhbat ──

/**
 * Tezkor buyruqlar — mikrofon ustida bitta qator. Qisqa, gapirgandek: yordamçi faqat javob bermay, işni
 * özi qiladi (öyinlarni yopadi, vazifa beradi). Suhbatdan keyin — davomi uçun.
 */
@Composable
private fun SuggestionChips(conversation: Boolean, asked: Set<String>, childName: String?, onAsk: (String) -> Unit) {
    val games = stringResource(Res.string.ai_suggest_games)
    val task = if (!childName.isNullOrBlank()) stringResource(Res.string.ai_suggest_task_named, childName)
    else stringResource(Res.string.ai_suggest_task)
    val youtube = stringResource(Res.string.ai_suggest_youtube)
    val today = stringResource(Res.string.ai_suggest_today)
    val where = if (!childName.isNullOrBlank()) stringResource(Res.string.ai_ask_where, childName)
    else stringResource(Res.string.ai_suggest_where)
    val week = stringResource(Res.string.ai_suggest_week)
    val starters = listOf(games, task, youtube, today, where, week)
    val followUps = listOf(today, task, games, where)
    val prompts = (if (conversation) followUps + starters else starters).distinct()
        .filter { it !in asked }.take(if (conversation) 4 else 6)
    if (prompts.isEmpty()) return
    LazyRow(
        contentPadding = PaddingValues(horizontal = ContainerPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        items(prompts) { text ->
            Text(
                text = text,
                style = AppTypography.titleSmMedium,
                color = AppColors.text.primary,
                maxLines = 1,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .background(AppColors.bg.surface)
                    .clickable(interactionSource = rememberHapticSource(), indication = null) { onAsk(text) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun ParentBubble(text: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Text(
            text = text,
            style = AppTypography.titleSmRegular,
            color = AppColors.text.primary,
            modifier = Modifier
                .padding(start = 48.dp)
                .clip(RoundedCornerShape(22.dp, 22.dp, 6.dp, 22.dp))
                .background(AppColors.action.primary.copy(alpha = 0.16f))
                .padding(horizontal = 16.dp, vertical = 11.dp),
        )
    }
}

/**
 * Yordamçi javobi: kiçkina tipratikan + matn. Yozilayotganda oxirida milt-milt kursor. [speaker] — ostida
 * "Ovoz çiqarib öqiş" tugmasi (öqilayotganda — töxtatiş).
 */
@Composable
private fun AssistantReply(text: String, streaming: Boolean, speaker: Boolean, speaking: Boolean, onSpeak: () -> Unit) {
    val caretAlpha = if (streaming) {
        val t = rememberInfiniteTransition(label = "caret")
        t.animateFloat(0f, 1f, infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "c").value
    } else 0f
    Row(Modifier.fillMaxWidth().padding(end = 12.dp), verticalAlignment = Alignment.Top) {
        Image(
            painter = painterResource(Res.drawable.ai_avatar),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.padding(top = 2.dp).size(26.dp).clip(CircleShape).background(AppColors.bg.secondary),
        )
        Spacer(Modifier.width(10.dp))
        val body = markdownLite(text)
        Column(Modifier.weight(1f)) {
            Text(
                text = if (streaming) buildAnnotatedString {
                    append(body)
                    val start = length
                    append(" ▍")
                    addStyle(SpanStyle(color = AppColors.text.accentEmphasis.copy(alpha = caretAlpha)), start, length)
                } else body,
                style = AppTypography.titleSmRegular,
                color = AppColors.text.primary,
            )
            if (speaker) {
                IconButton(
                    onClick = onSpeak,
                    interactionSource = rememberHapticSource(),
                    modifier = Modifier.padding(top = 2.dp).size(48.dp).offset(x = (-12).dp),
                ) {
                    if (speaking) {
                        val stopLabel = stringResource(Res.string.ai_stop)
                        Box(
                            Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(AppColors.icon.accentPrimary)
                                .semantics { contentDescription = stopLabel },
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = stringResource(Res.string.ai_read_aloud),
                            tint = AppColors.icon.secondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingRow(status: String) {
    val t = rememberInfiniteTransition(label = "thinking")
    val phase by t.animateFloat(0f, 3f, infiniteRepeatable(tween(1050)), label = "dots")
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = 32.dp)) {
        MascotAvatar(size = 26.dp, active = false)
        Spacer(Modifier.width(8.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.bg.surface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { i ->
                val d = (phase - i).let { if (it < 0) it + 3 else it }
                val lift = if (d < 1f) kotlin.math.sin(d * kotlin.math.PI).toFloat() else 0f
                Box(
                    Modifier
                        .padding(horizontal = 2.dp)
                        .size(7.dp)
                        .scale(0.85f + 0.3f * lift)
                        .alpha(0.35f + 0.65f * lift)
                        .clip(CircleShape)
                        .background(AppColors.icon.accentPrimary)
                )
            }
            val s = status.trimStart { !it.isLetterOrDigit() }
            if (s.isNotBlank()) {
                Spacer(Modifier.width(10.dp))
                Text(s, style = AppTypography.bodyLgRegular, color = AppColors.text.secondary, maxLines = 1)
            }
        }
    }
}

/** Qoralama turi belgisi; noma'lum (yangi) tur — qalqon. Turlar ro'yxati çeklanmaydi: karta har qandayini körsatadi. */
private fun kindIcon(kind: String): ImageVector = when (kind) {
    // To'xtatiş, davom ettiriş, öçiriş/yoqiş va vaqt/kun/ilovalarni özgartiriş — hammasi bitta tur
    "policy_update" -> Icons.Rounded.Tune
    "policy_delete", "task_delete" -> Icons.Rounded.DeleteOutline
    "task_create", "task_verify" -> Icons.Rounded.TaskAlt
    "task_remind" -> Icons.Rounded.NotificationsActive
    "request_decide" -> Icons.Rounded.MarkEmailUnread
    "quick_block" -> Icons.Rounded.Block
    "content_filter" -> Icons.Rounded.FilterAlt
    "chat_message" -> Icons.Rounded.ChatBubble
    else -> Icons.Rounded.Shield
}

@Composable
private fun DraftCard(
    item: AssistantItem.Draft,
    children: List<UserInfo>,
    onApply: (coin: Int?) -> Unit,
    onDiscard: () -> Unit,
) {
    val d = item.draft
    val danger = d.confirm == "delete" || d.confirm == "reject"
    // Xabar / eslatma — "Yuboriş" (server fe'li baribir "confirm")
    val sends = d.kind == "chat_message" || d.kind == "task_remind"
    val confirmLabel = when (d.confirm) {
        "confirm" -> if (sends) Res.string.ai_send else Res.string.ai_draft_do_confirm
        "delete" -> Res.string.ai_draft_do_delete
        "approve" -> Res.string.ai_draft_do_approve
        "reject" -> Res.string.ai_draft_do_reject
        else -> Res.string.ai_draft_confirm
    }
    val child = children.firstOrNull { it.userId == d.childId }
    // Topşiriq mukofoti kartaning özida özgaradi — qayta söraş (va tangaça) shart emas
    var coins by rememberSaveable(d.draftId) { mutableStateOf(d.coins) }
    val pending = item.status == DraftStatus.PENDING
    if (item.status == DraftStatus.APPLIED || item.status == DraftStatus.DISCARDED) {
        ResolvedDraftRow(d, applied = item.status == DraftStatus.APPLIED)
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.bg.surface)
            .animateContentSize()
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.bg.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    kindIcon(d.kind),
                    null,
                    tint = if (danger) AppColors.icon.accentDanger else AppColors.icon.accentPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(d.title, style = AppTypography.titleMdSemiBold, color = AppColors.text.primary)
                if (d.childName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        if (child != null) {
                            ChildAvatar(child.avatarUrl, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            child?.fullName?.cleanName()?.ifBlank { null } ?: d.childName,
                            style = AppTypography.bodyMdMedium,
                            color = AppColors.text.tertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        // Topşiriqda oxirgi qator (muddat) — mayda metama'lumot; asosiy matn — bola oladigan topşiriq
        val meta = if (d.kind == "task_create") d.lines.lastOrNull() else null
        val lines = if (meta != null) d.lines.dropLast(1) else d.lines
        if (lines.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                lines.forEach { line ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier
                                .padding(top = 9.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(AppColors.icon.secondary)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(line, style = AppTypography.titleSmRegular, color = AppColors.text.primary)
                    }
                }
            }
        }
        if (meta != null) {
            Spacer(Modifier.height(if (lines.isEmpty()) 8.dp else 6.dp))
            Text(meta, style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary)
        }
        // Qoida hozir işlamaydi (rejim yoki ruxsat) — ota-ona "yoqdim, ammo hech narsa yopilmadi" deb qolmasin
        d.warning?.let { warning ->
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = AppColors.text.accentWarning,
                    modifier = Modifier.padding(top = 1.dp).size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(warning, style = AppTypography.bodyMdMedium, color = AppColors.text.accentWarning)
            }
        }
        if (d.kind == "task_create" && coins != null) {
            Spacer(Modifier.height(12.dp))
            CoinStepper(value = coins ?: 0, enabled = pending, onChange = { coins = it })
        }
        Spacer(Modifier.height(16.dp))
        when (item.status) {
            DraftStatus.PENDING, DraftStatus.APPLYING -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp)
                            .clip(CircleShape)
                            .background(if (danger) AppColors.action.accentDanger else AppColors.action.primary)
                            .clickable(
                                enabled = pending,
                                interactionSource = rememberHapticSource(),
                                indication = null,
                                onClick = { onApply(if (d.kind == "task_create") coins else null) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (item.status == DraftStatus.APPLYING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = if (danger) Color.White else OnAccent,
                            )
                        } else {
                            Text(
                                stringResource(confirmLabel),
                                style = AppTypography.titleSmSemiBold,
                                color = if (danger) Color.White else OnAccent,
                            )
                        }
                    }
                    TextButton(onClick = onDiscard, enabled = pending) {
                        Text(stringResource(Res.string.bekor_qilish), color = AppColors.text.secondary)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(Res.string.ai_draft_hint),
                    style = AppTypography.bodyMdRegular,
                    color = AppColors.text.placeholder,
                )
            }
            DraftStatus.APPLIED -> Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CheckCircle, null, tint = AppColors.icon.accentPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(if (d.kind == "policy_create") Res.string.ai_draft_applied else Res.string.ai_draft_done),
                    style = AppTypography.titleSmSemiBold,
                    color = AppColors.text.primary,
                )
            }
            DraftStatus.DISCARDED -> Text(
                stringResource(Res.string.ai_draft_discarded),
                style = AppTypography.titleSmMedium,
                color = AppColors.text.tertiary,
            )
        }
    }
}

/** Bajarilgan yoki bekor qilingan karta — bir qator: belgi, sarlavha, holat. */
@Composable
private fun ResolvedDraftRow(d: uz.tikoncha_parent.domain.model.ai.RuleDraft, applied: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.bg.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (applied) Icons.Rounded.CheckCircle else kindIcon(d.kind),
            null,
            tint = if (applied) AppColors.icon.accentPrimary else AppColors.icon.secondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            d.title,
            style = AppTypography.titleSmMedium,
            color = if (applied) AppColors.text.primary else AppColors.text.tertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(
                when {
                    !applied -> Res.string.ai_draft_discarded
                    d.kind == "task_create" || d.kind == "task_remind" || d.kind == "chat_message" -> Res.string.ai_done_task_sent
                    d.kind == "task_verify" -> Res.string.ai_done_verified
                    d.confirm == "reject" -> Res.string.ai_done_rejected
                    d.kind == "request_decide" -> Res.string.ai_done_approved
                    d.confirm == "delete" -> Res.string.ai_done_deleted
                    else -> Res.string.ai_done_saved
                }
            ),
            style = AppTypography.bodyMdMedium,
            color = AppColors.text.tertiary,
        )
    }
}

/** Mukofot: − 5 + (0…100, 5 lik qadam). */
@Composable
private fun CoinStepper(value: Int, enabled: Boolean, onChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.bg.secondary)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Image(painterResource(Res.drawable.coin_3d), null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(Res.string.ai_task_reward),
            style = AppTypography.titleSmMedium,
            color = AppColors.text.secondary,
            modifier = Modifier.weight(1f),
        )
        StepButton("−", enabled && value > 0) { onChange((value - 5).coerceAtLeast(0)) }
        Text(
            "$value",
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.primary,
            modifier = Modifier.widthIn(min = 40.dp).padding(horizontal = 6.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        StepButton("+", enabled && value < 100) { onChange((value + 5).coerceAtMost(100)) }
    }
}

@Composable
private fun StepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(AppColors.bg.surface)
            .clickable(enabled = enabled, interactionSource = rememberHapticSource(), indication = null, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = AppTypography.titleMdSemiBold, color = AppColors.text.primary)
    }
}

@Composable
internal fun agoText(minutes: Int): String = when {
    minutes < 1 -> stringResource(Res.string.ai_location_now)
    minutes < 60 -> stringResource(Res.string.ai_location_ago, stringResource(Res.string.ai_ago_minutes, minutes))
    minutes < 48 * 60 -> stringResource(Res.string.ai_location_ago, stringResource(Res.string.ai_ago_hours, minutes / 60))
    else -> stringResource(Res.string.ai_location_ago, stringResource(Res.string.ai_ago_days, minutes / 1440))
}

@Composable
private fun LocationCardView(card: LocationCard, reminded: Boolean, onRemind: () -> Unit, onOpen: () -> Unit) {
    val stale = card.minutesAgo > 30
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.bg.surface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.bg.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.LocationOn, null, tint = AppColors.icon.accentPrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.ai_location_title), style = AppTypography.titleSmSemiBold, color = AppColors.text.primary)
                Row {
                    if (card.childName.isNotBlank()) {
                        Text("${card.childName} · ", style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary)
                    }
                    Text(
                        agoText(card.minutesAgo),
                        style = AppTypography.bodyMdMedium,
                        color = if (stale) AppColors.text.accentWarning else AppColors.text.tertiary,
                    )
                }
            }
        }
        if (card.gpsOff) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOff, null, tint = AppColors.icon.accentWarning, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.ai_location_gps_off), style = AppTypography.bodyLgRegular, color = AppColors.text.secondary)
            }
        }
        Spacer(Modifier.height(14.dp))
        CardActions(
            showRemind = card.gpsOff,
            reminded = reminded,
            onRemind = onRemind,
            openLabel = stringResource(Res.string.ai_open_map),
            onOpen = onOpen,
        )
    }
}

/**
 * Karta tugmalari — tonal (töliq oltin faqat özgarişni tasdiqlaşda). Farzand telefonida tuzatiladigan muammo
 * bölsa, birinçisi — "Farzandga eslatma yuboriş" (bosilgaç "Eslatma yuborildi").
 */
@Composable
private fun CardActions(
    showRemind: Boolean,
    reminded: Boolean,
    onRemind: () -> Unit,
    openLabel: String,
    onOpen: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showRemind) {
            TonalButton(
                text = stringResource(if (reminded) Res.string.ai_reminder_sent else Res.string.ai_remind_child),
                icon = if (reminded) Icons.Rounded.Check else Icons.Rounded.NotificationsActive,
                accent = true,
                enabled = !reminded,
                onClick = onRemind,
                modifier = Modifier.weight(1f),
            )
        }
        TonalButton(text = openLabel, icon = null, accent = false, enabled = true, onClick = onOpen,
                    modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TonalButton(
    text: String,
    icon: ImageVector?,
    accent: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .background(AppColors.bg.secondary)
            .clickable(enabled = enabled, interactionSource = rememberHapticSource(), indication = null, onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val color = if (accent) AppColors.text.accentEmphasis else AppColors.text.primary
        if (icon != null) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = AppTypography.titleSmSemiBold, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Himoya: rejim nomi, yetişmagan ruxsatlar (yoki "hammasi joyida") va Himoya ekraniga tugma. */
@Composable
private fun ProtectionCardView(card: ProtectionCard, reminded: Boolean, onRemind: () -> Unit, onOpen: () -> Unit) {
    val ok = card.problems.isEmpty()
    val modeName = when (card.mode) {
        "STRICT" -> stringResource(Res.string.mode_qalqon_title)
        "GUJANAK" -> stringResource(Res.string.mode_gujanak_title)
        "DEFAULT" -> stringResource(Res.string.mode_default_title)
        else -> null
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.bg.surface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.bg.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (ok) Icons.Rounded.VerifiedUser else Icons.Rounded.GppMaybe,
                    null,
                    tint = if (ok) AppColors.icon.accentPrimary else AppColors.icon.accentWarning,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.ai_protection_title) + (modeName?.let { " · $it" } ?: ""),
                    style = AppTypography.titleSmSemiBold,
                    color = AppColors.text.primary,
                )
                Text(card.childName, style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary)
            }
        }
        Spacer(Modifier.height(12.dp))
        if (ok) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Check, null, tint = AppColors.icon.accentPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.ai_protection_ok), style = AppTypography.titleSmRegular, color = AppColors.text.primary)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                card.problems.forEach { p ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier
                                .padding(top = 8.dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(AppColors.text.accentWarning)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(p, style = AppTypography.titleSmRegular, color = AppColors.text.primary)
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        CardActions(
            showRemind = !ok,
            reminded = reminded,
            onRemind = onRemind,
            openLabel = stringResource(Res.string.ai_open_protection),
            onOpen = onOpen,
        )
    }
}

@Composable
private fun ProblemCard(item: AssistantItem.Problem, onRetry: () -> Unit, onBuyCoins: () -> Unit) {
    val text = item.text?.takeIf { it.isNotBlank() } ?: when (item.code) {
        "network" -> stringResource(Res.string.ai_error_network)
        else -> stringResource(Res.string.ai_error_generic)
    }
    val retryable = item.code in setOf("network", "server", "ai_unavailable", "rate_limited")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AppColors.bg.surface)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.Warning, null, tint = AppColors.icon.accentWarning, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, style = AppTypography.titleSmRegular, color = AppColors.text.primary, modifier = Modifier.weight(1f))
        }
        if (item.code == "no_coins" || retryable) {
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(if (item.code == "no_coins") Res.string.ai_buy_coins else Res.string.qayta_urinish),
                style = AppTypography.titleSmSemiBold,
                color = OnAccent,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.action.primary)
                    .clickable(
                        interactionSource = rememberHapticSource(),
                        indication = null,
                        onClick = if (item.code == "no_coins") onBuyCoins else onRetry,
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}

// ── yoziş maydoni ──

/**
 * Yoziş maydoni — ikkinçi yöl (asosiysi — mikrofon). Ön tugma: matn bölsa — yuboriş; javob yozilayotganda va
 * katta mikrofon yaşirin bölsa — töxtatiş; klaviatura oçiq va matn böş bölsa — kiçkina mikrofon.
 */
@Composable
private fun InputBar(
    value: String,
    streaming: Boolean,
    dockVisible: Boolean,
    onFocus: (Boolean) -> Unit,
    onChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onMic: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSend = value.isNotBlank() && !streaming
    val trailing = when {
        streaming -> if (dockVisible) null else TrailingAction.STOP
        canSend -> TrailingAction.SEND
        !dockVisible -> TrailingAction.MIC
        else -> null
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = ContainerPadding, end = ContainerPadding, top = 10.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            Modifier
                .weight(1f)
                .heightIn(min = 50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(AppColors.bg.surface)
                .padding(horizontal = 18.dp, vertical = 13.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(
                    stringResource(Res.string.ai_input_hint),
                    style = AppTypography.titleSmRegular,
                    color = AppColors.text.placeholder,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                textStyle = AppTypography.titleSmRegular.copy(color = AppColors.text.primary),
                cursorBrush = SolidColor(AppColors.action.primary),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                modifier = Modifier.fillMaxWidth().onFocusChanged { onFocus(it.isFocused) },
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            val accent = trailing != TrailingAction.MIC
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (accent) AppColors.action.primary else AppColors.bg.surface)
                    .clickable(
                        interactionSource = rememberHapticSource(),
                        indication = null,
                        onClick = when (trailing) {
                            TrailingAction.STOP -> onStop
                            TrailingAction.SEND -> onSend
                            TrailingAction.MIC -> onMic
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when (trailing) {
                    TrailingAction.STOP -> Box(
                        Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(OnAccent)
                    )
                    TrailingAction.SEND -> Icon(
                        Icons.Rounded.ArrowUpward,
                        contentDescription = stringResource(Res.string.ai_send),
                        tint = OnAccent,
                        modifier = Modifier.size(22.dp),
                    )
                    TrailingAction.MIC -> Icon(
                        Icons.Rounded.Mic,
                        contentDescription = stringResource(Res.string.ai_voice_mic),
                        tint = AppColors.icon.accentPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

private enum class TrailingAction { STOP, SEND, MIC }

/** **qalin** va "* " / "- " ro'yxatlari — yordamçi javoblari shundan ortig'ini ishlatmaydi. */
private fun markdownLite(text: String): AnnotatedString = buildAnnotatedString {
    text.lines().forEachIndexed { i, raw ->
        if (i > 0) append('\n')
        var line = raw
        val bullet = Regex("^\\s*[*-]\\s+").find(line)
        if (bullet != null) {
            append("• ")
            line = line.substring(bullet.range.last + 1)
        }
        var rest = line
        while (true) {
            val start = rest.indexOf("**")
            val end = if (start >= 0) rest.indexOf("**", start + 2) else -1
            if (start < 0 || end < 0) {
                append(rest)
                break
            }
            append(rest.substring(0, start))
            val boldStart = length
            append(rest.substring(start + 2, end))
            addStyle(SpanStyle(fontWeight = FontWeight.SemiBold), boldStart, length)
            rest = rest.substring(end + 2)
        }
    }
}
