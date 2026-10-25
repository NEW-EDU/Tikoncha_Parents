package uz.tikoncha_parent.presentation.assistant

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
import androidx.compose.material.icons.rounded.PauseCircle
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
import tikoncha_parents.composeapp.generated.resources.ai_suggest_night
import tikoncha_parents.composeapp.generated.resources.ai_suggest_permissions
import tikoncha_parents.composeapp.generated.resources.ai_suggest_task
import tikoncha_parents.composeapp.generated.resources.ai_suggest_week
import tikoncha_parents.composeapp.generated.resources.ai_suggest_where
import tikoncha_parents.composeapp.generated.resources.ai_summary_label
import tikoncha_parents.composeapp.generated.resources.ai_thinking
import tikoncha_parents.composeapp.generated.resources.arrow_left
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.chat_ai_icon
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
 * Ota-ona yordamçisi. Tepada farzand hisoboti (suhbat boşlanganda yig'iladi), pastda suhbat: savol yoki
 * "qoida ayting" → tasdiqlaş kartasi (jadval, to'xtatiş, öçiriş, topşiriq, sörov javobi), joylaşuv kartasi.
 * Eng köp işlatiladigani — yoziş maydoni — bosh barmoq ostida.
 */
class AssistantScreen(
    /** Faqat debug: ekran ochilishi bilan beriladigan savol (SplashScreen `-hoglt.ask`). */
    private val initialQuestion: String? = null,
    /** Faqat debug: "day" · "yesterday" · "week". */
    private val initialPeriod: String? = null,
) : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<AssistantViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        var started by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            if (started) return@LaunchedEffect
            started = true
            ReportPeriod.entries.firstOrNull { it.key == initialPeriod }?.let {
                viewModel.onEvent(AssistantEventUi.SelectPeriod(it))
            }
            initialQuestion?.takeIf { it.isNotBlank() }?.let { viewModel.onEvent(AssistantEventUi.Ask(it)) }
        }
        AssistantUi(state = state, event = viewModel::onEvent)
    }
}

@Composable
private fun AssistantUi(state: AssistantState, event: (AssistantEventUi) -> Unit) {
    val navigator = LocalNavigator.current
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var bottomHeight by remember { mutableStateOf(120.dp) }
    var confirmNewChat by remember { mutableStateOf(false) }

    // Suhbat boşlangach hisobot tepada bir qatorga yig'iladi (bosilsa oçiladi), suhbat esa pastdan ösadi
    val conversation = state.items.isNotEmpty() || state.streaming
    var reportOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(conversation) { if (!conversation) reportOpen = false }

    val itemCount = state.items.size + (if (state.streaming) 1 else 0) + (if (conversation) 0 else 1)
    LaunchedEffect(state.items.size, state.streaming, state.streamingText.length / 80) {
        if (conversation && itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    state.draftError?.let { failure ->
        AlertDialog(
            onDismissRequest = { event(AssistantEventUi.DismissDraftError) },
            confirmButton = {
                TextButton(onClick = { event(AssistantEventUi.DismissDraftError) }) {
                    Text(stringResource(Res.string.yopish))
                }
            },
            text = { Text(failure.asText(), style = AppTypography.bodyLgRegular, color = AppColors.text.primary) },
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
            streaming = state.streaming,
            status = state.status,
            coins = state.coins,
            canReset = conversation && !state.streaming,
            onBack = { navigator?.pop() },
            onNewChat = {
                val pending = state.items.any { it is AssistantItem.Draft && it.status == DraftStatus.PENDING }
                if (pending) confirmNewChat = true else event(AssistantEventUi.NewChat)
            },
            onCoins = { navigator?.push(CoinsScreen()) },
        )

        if (conversation) {
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
                // Javob va kartalar pastdan, yoziş maydoni yonida (bosh barmoq) ösadi
                verticalArrangement = Arrangement.spacedBy(14.dp, if (conversation) Alignment.Bottom else Alignment.Top),
            ) {
                if (!conversation) {
                    item(key = "report") {
                        ReportCard(state = state, open = true, onToggle = {}, event = event)
                    }
                }
                items(state.items, key = { it.id }) { item ->
                    when (item) {
                        is AssistantItem.Parent -> ParentBubble(item.text)
                        is AssistantItem.Reply -> AssistantReply(item.text, streaming = false)
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
                        else AssistantReply(state.streamingText, streaming = true)
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
                if (!state.streaming) {
                    SuggestionChips(
                        conversation = conversation,
                        asked = state.items.mapNotNull { (it as? AssistantItem.Parent)?.text }.toSet(),
                        onAsk = { event(AssistantEventUi.Ask(it)) },
                    )
                }
                InputBar(
                    value = state.text,
                    streaming = state.streaming,
                    onChange = { event(AssistantEventUi.OnText(it)) },
                    onSend = { event(AssistantEventUi.Send) },
                    onStop = { event(AssistantEventUi.Stop) },
                )
            }
        }
    }
}

/** Serverdagi bo'ş otasining ismi "null" bo'lib keladi ("Shuxratov Saidburxon null"). */
private fun String.cleanName(): String =
    split(' ').filter { it.isNotBlank() && !it.equals("null", ignoreCase = true) }.joinToString(" ")

/** Oltin tugmalar ustidagi matn: oq emas, to'q jigarrang — kontrast ~6:1. */
private val OnAccent = Color(0xFF2B1D12)

// ── tepa qism ──

@Composable
private fun Header(
    streaming: Boolean,
    status: String,
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
        MascotAvatar(size = 40.dp, active = false)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.ai_assistant_title),
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
                maxLines = 1,
            )
            // Holat (nima qilayotgani) — faqat suhbatdagi pufakda, sarlavhada takrorlanmaydi
        }
        AnimatedVisibility(visible = canReset, enter = fadeIn(), exit = fadeOut()) {
            IconButton(
                onClick = onNewChat,
                interactionSource = rememberHapticSource(),
                modifier = Modifier.size(40.dp),
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
            painter = painterResource(Res.drawable.chat_ai_icon),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(CircleShape).background(AppColors.bg.secondary),
        )
    }
}

// ── hisobot ──

@Composable
private fun ReportCard(
    state: AssistantState,
    open: Boolean,
    onToggle: () -> Unit,
    event: (AssistantEventUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val conversation = state.items.isNotEmpty() || state.streaming
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.bg.surface)
            .animateContentSize(tween(260))
    ) {
        if (!open) {
            ReportCollapsed(state, onToggle)
            return@Column
        }
        if (state.children.size > 1) {
            // Chiplar kartaning çetigaça boradi (ichki chekinish ularni kesmasin)
            ChildChips(
                children = state.children,
                selected = state.child,
                onSelect = { event(AssistantEventUi.SelectChild(it)) },
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = if (state.children.size > 1) 12.dp else 16.dp)) {
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
private fun ChildChips(children: List<UserInfo>, selected: UserInfo?, onSelect: (UserInfo) -> Unit, modifier: Modifier = Modifier) {
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
        contentPadding = PaddingValues(horizontal = 16.dp),
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
                    .clip(CircleShape)
                    .background(bg)
                    .clickable(interactionSource = rememberHapticSource(), indication = null) { onSelect(child) }
                    .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
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

/** Tezkor savollar — yoziş maydoni ustida bitta qator; suhbatdan keyin — davomi uçun. */
@Composable
private fun SuggestionChips(conversation: Boolean, asked: Set<String>, onAsk: (String) -> Unit) {
    val starters = listOf(
        Res.string.ai_suggest_week,
        Res.string.ai_suggest_permissions,
        Res.string.ai_suggest_where,
        Res.string.ai_suggest_night,
        Res.string.ai_suggest_task,
        Res.string.ai_suggest_games,
    )
    val followUps = listOf(
        Res.string.ai_suggest_task,
        Res.string.ai_suggest_permissions,
        Res.string.ai_suggest_week,
        Res.string.ai_suggest_where,
    )
    val prompts = (if (conversation) followUps + starters else starters).distinct()
        .map { stringResource(it) }.filter { it !in asked }.take(if (conversation) 4 else 6)
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
                    .heightIn(min = 40.dp)
                    .clip(CircleShape)
                    .background(AppColors.bg.surface)
                    .clickable(interactionSource = rememberHapticSource(), indication = null) { onAsk(text) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
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

/** Yordamçi javobi: kiçkina tipratikan + matn. Yozilayotganda oxirida milt-milt kursor. */
@Composable
private fun AssistantReply(text: String, streaming: Boolean) {
    val caretAlpha = if (streaming) {
        val t = rememberInfiniteTransition(label = "caret")
        t.animateFloat(0f, 1f, infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "c").value
    } else 0f
    Row(Modifier.fillMaxWidth().padding(end = 12.dp), verticalAlignment = Alignment.Top) {
        Image(
            painter = painterResource(Res.drawable.chat_ai_icon),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.padding(top = 2.dp).size(26.dp).clip(CircleShape).background(Color.White),
        )
        Spacer(Modifier.width(10.dp))
        val body = markdownLite(text)
        Text(
            text = if (streaming) buildAnnotatedString {
                append(body)
                val start = length
                append(" ▍")
                addStyle(SpanStyle(color = AppColors.text.accentEmphasis.copy(alpha = caretAlpha)), start, length)
            } else body,
            style = AppTypography.titleSmRegular,
            color = AppColors.text.primary,
            modifier = Modifier.weight(1f),
        )
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

private fun kindIcon(kind: String): ImageVector = when (kind) {
    "policy_update" -> Icons.Rounded.PauseCircle
    "policy_delete" -> Icons.Rounded.DeleteOutline
    "task_create" -> Icons.Rounded.TaskAlt
    "request_decide" -> Icons.Rounded.MarkEmailUnread
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
    val confirmLabel = when (d.confirm) {
        "confirm" -> Res.string.ai_draft_do_confirm
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
                    d.kind == "task_create" -> Res.string.ai_done_task_sent
                    d.kind == "request_decide" && d.confirm == "reject" -> Res.string.ai_done_rejected
                    d.kind == "request_decide" -> Res.string.ai_done_approved
                    d.kind == "policy_delete" -> Res.string.ai_done_deleted
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
private fun agoText(minutes: Int): String = when {
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

@Composable
private fun InputBar(
    value: String,
    streaming: Boolean,
    onChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSend = value.isNotBlank() && !streaming
    val sendBg by animateColorAsState(
        if (canSend || streaming) AppColors.action.primary else AppColors.bg.surface,
        tween(180),
        label = "send",
    )
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
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(sendBg)
                .clickable(
                    enabled = canSend || streaming,
                    interactionSource = rememberHapticSource(),
                    indication = null,
                    onClick = if (streaming) onStop else onSend,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (streaming) {
                Box(
                    Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(OnAccent)
                )
            } else {
                Icon(
                    Icons.Rounded.ArrowUpward,
                    contentDescription = stringResource(Res.string.ai_send),
                    tint = if (canSend) OnAccent else AppColors.text.placeholder,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

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
