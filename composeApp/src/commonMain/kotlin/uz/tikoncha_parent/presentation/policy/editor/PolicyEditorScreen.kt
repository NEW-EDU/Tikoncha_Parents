package uz.tikoncha_parent.presentation.policy.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.apps_icon
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.editor_add_condition
import tikoncha_parents.composeapp.generated.resources.editor_added
import tikoncha_parents.composeapp.generated.resources.editor_closed
import tikoncha_parents.composeapp.generated.resources.editor_closed_in_time
import tikoncha_parents.composeapp.generated.resources.editor_closed_in_time_sub
import tikoncha_parents.composeapp.generated.resources.editor_condition_limit
import tikoncha_parents.composeapp.generated.resources.editor_condition_limit_sub
import tikoncha_parents.composeapp.generated.resources.editor_condition_location_sub
import tikoncha_parents.composeapp.generated.resources.editor_condition_time
import tikoncha_parents.composeapp.generated.resources.editor_condition_time_sub
import tikoncha_parents.composeapp.generated.resources.editor_delete_message
import tikoncha_parents.composeapp.generated.resources.editor_delete_title
import tikoncha_parents.composeapp.generated.resources.editor_in_this_time
import tikoncha_parents.composeapp.generated.resources.editor_inside
import tikoncha_parents.composeapp.generated.resources.editor_new_policy
import tikoncha_parents.composeapp.generated.resources.editor_not_selected
import tikoncha_parents.composeapp.generated.resources.editor_open
import tikoncha_parents.composeapp.generated.resources.editor_open_in_time
import tikoncha_parents.composeapp.generated.resources.editor_open_in_time_sub
import tikoncha_parents.composeapp.generated.resources.editor_outside
import tikoncha_parents.composeapp.generated.resources.editor_paywall_allow
import tikoncha_parents.composeapp.generated.resources.editor_rename
import tikoncha_parents.composeapp.generated.resources.editor_when_hint
import tikoncha_parents.composeapp.generated.resources.jadval_nomi
import tikoncha_parents.composeapp.generated.resources.jadval_nomini_kiriting
import tikoncha_parents.composeapp.generated.resources.kunlar
import tikoncha_parents.composeapp.generated.resources.kunlik
import tikoncha_parents.composeapp.generated.resources.ochirish
import tikoncha_parents.composeapp.generated.resources.policy_active_until
import tikoncha_parents.composeapp.generated.resources.policy_location
import tikoncha_parents.composeapp.generated.resources.policy_n_apps
import tikoncha_parents.composeapp.generated.resources.policy_n_categories
import tikoncha_parents.composeapp.generated.resources.policy_n_sites
import tikoncha_parents.composeapp.generated.resources.policy_per_day
import tikoncha_parents.composeapp.generated.resources.policy_per_hour
import tikoncha_parents.composeapp.generated.resources.preset_ends
import tikoncha_parents.composeapp.generated.resources.preset_enabled
import tikoncha_parents.composeapp.generated.resources.preset_paused_until
import tikoncha_parents.composeapp.generated.resources.preset_per_day_label
import tikoncha_parents.composeapp.generated.resources.preset_per_hour_label
import tikoncha_parents.composeapp.generated.resources.preset_radius
import tikoncha_parents.composeapp.generated.resources.preset_resume
import tikoncha_parents.composeapp.generated.resources.preset_starts
import tikoncha_parents.composeapp.generated.resources.preset_what_closed
import tikoncha_parents.composeapp.generated.resources.preset_when
import tikoncha_parents.composeapp.generated.resources.saqlash
import tikoncha_parents.composeapp.generated.resources.shift_clock
import tikoncha_parents.composeapp.generated.resources.soatlik
import tikoncha_parents.composeapp.generated.resources.stat_quick_paywall_title
import tikoncha_parents.composeapp.generated.resources.targets_only_selected_open
import tikoncha_parents.composeapp.generated.resources.targets_only_selected_open_sub
import tikoncha_parents.composeapp.generated.resources.targets_selected_closed
import tikoncha_parents.composeapp.generated.resources.timer
import tikoncha_parents.composeapp.generated.resources.vaqtincha_toxtatish
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.SubscriptionBottomDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.haptics.ErrorHaptic
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.presentation.policy.components.AddRow
import uz.tikoncha_parent.presentation.policy.components.Chevron
import uz.tikoncha_parent.presentation.policy.components.DaysSheetContent
import uz.tikoncha_parent.presentation.policy.components.GroupDivider
import uz.tikoncha_parent.presentation.policy.components.IconTone
import uz.tikoncha_parent.presentation.policy.components.LimitTile
import uz.tikoncha_parent.presentation.policy.components.LimitWheelSheetContent
import uz.tikoncha_parent.presentation.policy.components.OptionRow
import uz.tikoncha_parent.presentation.policy.components.PausedBanner
import uz.tikoncha_parent.presentation.policy.components.PauseSheetContent
import uz.tikoncha_parent.presentation.policy.components.PolicyIcon
import uz.tikoncha_parent.presentation.policy.components.PolicyInfoBox
import uz.tikoncha_parent.presentation.policy.components.PolicyMenu
import uz.tikoncha_parent.presentation.policy.components.PolicyMenuItem
import uz.tikoncha_parent.presentation.policy.components.PolicySheet
import uz.tikoncha_parent.presentation.policy.components.PolicySwitch
import uz.tikoncha_parent.presentation.policy.components.PolicyText
import uz.tikoncha_parent.presentation.policy.components.QuickChips
import uz.tikoncha_parent.presentation.policy.components.RadioRow
import uz.tikoncha_parent.presentation.policy.components.SectionLabel
import uz.tikoncha_parent.presentation.policy.components.SegmentedTabs
import uz.tikoncha_parent.presentation.policy.components.SettingGroup
import uz.tikoncha_parent.presentation.policy.components.SettingRow
import uz.tikoncha_parent.presentation.policy.components.SheetTitle
import uz.tikoncha_parent.presentation.policy.components.TextInputDialog
import uz.tikoncha_parent.presentation.policy.components.TimeHero
import uz.tikoncha_parent.presentation.policy.components.TimeWheelSheetContent
import uz.tikoncha_parent.presentation.policy.components.asClock
import uz.tikoncha_parent.presentation.policy.components.daysText
import uz.tikoncha_parent.presentation.policy.components.durationText
import uz.tikoncha_parent.presentation.policy.components.sentences
import uz.tikoncha_parent.presentation.policy.location.LocationPicker
import uz.tikoncha_parent.presentation.policy.mapper.toInfo
import uz.tikoncha_parent.presentation.policy.targets.TargetsEditor
import uz.tikoncha_parent.presentation.policy.targets.TargetsFlavor
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars
import kotlin.time.Instant

/**
 * "O'zim sozlayman": jadval yaratish (`policyId == null`) va tahrirlash (Student bilan bir xil).
 * Nishon muharriri, xarita, Vaqt/Limit sahifalari asosiy ekran ustida almashadi.
 * Boshqasining jadvali faqat ko'rish uchun ochiladi.
 */
@OptIn(InternalVoyagerApi::class)
class PolicyEditorScreen(private val childId: String, private val policyId: String?) : Screen {

    override val key: ScreenKey = "policy_editor:$childId:${policyId ?: "new"}"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        val viewModel = koinScreenModel<PolicyEditorViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent
        val haptics = rememberAppHaptics()
        val defaultTitle = stringResource(Res.string.editor_new_policy)

        LaunchedEffect(Unit) { event(PolicyEditorEvent.Init(childId, policyId, defaultTitle)) }
        LaunchedEffect(Unit) {
            viewModel.effect.collect { e ->
                when (e) {
                    PolicyEditorEffect.Saved, PolicyEditorEffect.Deleted -> {
                        haptics.success()
                        navigator.pop()
                    }
                }
            }
        }

        SubscriptionBottomDialog(
            show = state.payWall,
            title = stringResource(Res.string.stat_quick_paywall_title),
            message = stringResource(Res.string.editor_paywall_allow),
            onConfirm = {
                event(PolicyEditorEvent.PayWallDismissed)
                navigator.push(SubscriptionPaymentScreen())
            },
            onDismiss = { event(PolicyEditorEvent.PayWallDismissed) },
        )

        val targets = state.targets
        if (targets != null) {
            TargetsEditor(
                state = targets,
                apps = state.childApps,
                flavor = TargetsFlavor.Custom,
                event = { event(PolicyEditorEvent.Targets(it)) },
                onDone = { event(PolicyEditorEvent.TargetsDone) },
                onClose = { event(PolicyEditorEvent.TargetsClosed) },
                appUsage = state.appUsage,
                allowLocked = state.paid == false,
                onLockedClick = { event(PolicyEditorEvent.AllowLockedClicked) },
            )
            return
        }
        val location = state.location
        if (location != null) {
            LocationPicker(
                state = location,
                title = stringResource(Res.string.policy_location),
                event = { event(PolicyEditorEvent.Location(it)) },
                onDone = { event(PolicyEditorEvent.LocationDone) },
                onClose = { event(PolicyEditorEvent.LocationClosed) },
                showReverse = true,
            )
            return
        }

        when (state.page) {
            EditorPage.TIME -> TimePage(state = state, event = event)
            EditorPage.LIMIT -> LimitPage(state = state, event = event)
            EditorPage.MAIN -> {
                BackHandler(true) { navigator.pop() }
                PolicyEditorContent(state = state, event = event, onBack = { navigator.pop() })
            }
        }
        EditorSheets(state = state, event = event)
        EditorDialogs(state = state, event = event)
    }
}

// ═══════════════════════════════════════════
//  Asosiy sahifa
// ═══════════════════════════════════════════

@Composable
fun PolicyEditorContent(state: PolicyEditorState, event: (PolicyEditorEvent) -> Unit, onBack: () -> Unit) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    val draft = state.draft
    val editable = !state.readOnly

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg.page).then(systemBars.modifier)) {
        CustomHeader(
            title = draft?.name.orEmpty(),
            showBackButton = true,
            onBackClick = onBack,
            trailingIcon = if (!editable) null else ({
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { event(PolicyEditorEvent.RenameClicked) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = AppColors.icon.primary, modifier = Modifier.size(20.dp))
                    }
                    if (state.isDetail) {
                        Box {
                            IconButton(onClick = { event(PolicyEditorEvent.MenuToggled) }) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = null, tint = AppColors.icon.primary)
                            }
                            PolicyMenu(expanded = state.menuOpen, onDismiss = { event(PolicyEditorEvent.MenuToggled) }) {
                                PolicyMenuItem(
                                    text = stringResource(Res.string.editor_rename),
                                    icon = Icons.Default.Edit,
                                    onClick = { event(PolicyEditorEvent.RenameClicked) },
                                )
                                PolicyMenuItem(
                                    text = stringResource(Res.string.ochirish),
                                    icon = Icons.Default.Delete,
                                    onClick = { event(PolicyEditorEvent.DeleteClicked) },
                                    destructive = true,
                                )
                            }
                        }
                    }
                }
            }),
        )

        if (draft == null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.action.primary)
            }
            return@Column
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = ContainerPadding),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Spacer(modifier = Modifier.height(2.dp))
            if (state.isDetail && editable) HeaderGroup(state = state, event = event)
            PolicyInfoBox(lines = draft.toInfo(enabled = if (state.isDetail) state.isEnabled else null).sentences())

            Column(
                modifier = Modifier.alpha(if (!state.isDetail || state.isEnabled) 1f else 0.45f),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                TargetsBlock(draft = draft, editable = editable, event = event)
                WhenBlock(state = state, draft = draft, editable = editable, event = event)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (editable) {
            CustomButtonNew(
                text = stringResource(Res.string.saqlash),
                modifier = Modifier.fillMaxWidth().padding(horizontal = ContainerPadding, vertical = 12.dp),
                enabled = state.canSave,
                onClick = { event(PolicyEditorEvent.SaveClicked) },
            )
        }
    }
}

/** Tahrirlashda: Yoqilgan switch'i · Vaqtincha to'xtatish · to'xtatilgan banner. */
@Composable
private fun HeaderGroup(state: PolicyEditorState, event: (PolicyEditorEvent) -> Unit) {
    SettingGroup {
        val paused = state.pausedUntil
        if (state.isPaused && paused != null) {
            PausedBanner(
                text = stringResource(Res.string.preset_paused_until, paused.asLocalClock()),
                actionText = stringResource(Res.string.preset_resume),
                onResume = { event(PolicyEditorEvent.ResumeClicked) },
            )
        }
        SettingRow(
            title = stringResource(Res.string.preset_enabled),
            trailing = {
                PolicySwitch(checked = state.isEnabled, busy = state.pendingEnabled != null, onCheckedChange = { event(PolicyEditorEvent.EnabledToggled(it)) })
            },
        )
        if (state.isEnabled) {
            GroupDivider()
            SettingRow(
                title = stringResource(Res.string.vaqtincha_toxtatish),
                subtitle = paused?.takeIf { state.isPaused }?.let { stringResource(Res.string.policy_active_until, it.asLocalClock()) },
                onClick = { if (!state.busy) event(PolicyEditorEvent.PauseClicked) },
                trailing = { Chevron() },
            )
        }
    }
}

/** "Nimalar yopiladi": nishon xulosasi yoki "Tanlanmagan". */
@Composable
private fun TargetsBlock(draft: PolicyDraft, editable: Boolean, event: (PolicyEditorEvent) -> Unit) {
    val t = draft.targets
    val counts = listOfNotNull(
        (t.packages.size + t.features.size).takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_apps, it) },
        t.categories.size.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_categories, it) },
        t.sites.size.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_sites, it) },
    ).map { it.replace(' ', ' ') }
    val selected = counts.isNotEmpty()
    val allowList = draft.action == PolicyAction.ALLOW
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(stringResource(Res.string.preset_what_closed))
        SettingGroup {
            SettingRow(
                title = when {
                    !selected -> stringResource(Res.string.editor_not_selected)
                    allowList -> stringResource(Res.string.targets_only_selected_open)
                    else -> stringResource(Res.string.targets_selected_closed)
                },
                subtitle = when {
                    !selected -> null
                    allowList -> counts.joinToString(" · ") + " · " + stringResource(Res.string.targets_only_selected_open_sub)
                    else -> counts.joinToString(" · ")
                },
                leading = { PolicyIcon(icon = Res.drawable.apps_icon, tone = if (selected) IconTone.SOLID else IconTone.GRAY, size = 44.dp) },
                onClick = if (editable) ({ event(PolicyEditorEvent.TargetsClicked) }) else null,
                trailing = if (editable) ({ Chevron() }) else null,
            )
        }
    }
}

/** "Qachon": qo'shilgan shartlar (× bilan) + "Shart qo'shish". */
@Composable
private fun WhenBlock(state: PolicyEditorState, draft: PolicyDraft, editable: Boolean, event: (PolicyEditorEvent) -> Unit) {
    val conditions = state.conditions
    val canAdd = editable && conditions.size < ConditionKind.entries.size
    if (!editable && conditions.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(stringResource(Res.string.preset_when), hint = if (editable) stringResource(Res.string.editor_when_hint) else null)
        SettingGroup {
            conditions.forEachIndexed { i, kind ->
                ConditionRow(kind = kind, draft = draft, editable = editable, event = event)
                if (i < conditions.lastIndex || canAdd) GroupDivider()
            }
            if (canAdd) {
                AddRow(text = stringResource(Res.string.editor_add_condition), onClick = { event(PolicyEditorEvent.AddConditionClicked) })
            }
        }
    }
}

@Composable
private fun ConditionRow(kind: ConditionKind, draft: PolicyDraft, editable: Boolean, event: (PolicyEditorEvent) -> Unit) {
    val subtitle = when (kind) {
        ConditionKind.TIME -> draft.conditions.time?.let { r ->
            listOfNotNull(
                daysText(r.days.map { it.num }.toSet()),
                "${r.startMin.asClock()} – ${r.endMin.asClock()}",
                stringResource(if (r.include) Res.string.editor_closed else Res.string.editor_open),
            ).joinToString(" · ")
        }
        ConditionKind.LIMIT -> draft.limits.usage?.let { r ->
            listOfNotNull(
                stringResource(if (r.window == LimitWindow.HOUR) Res.string.policy_per_hour else Res.string.policy_per_day, durationText(r.minutes)),
                daysText(r.days.map { it.num }.toSet()),
            ).joinToString(" · ")
        }
        ConditionKind.LOCATION -> draft.conditions.location?.let { r ->
            listOfNotNull(
                r.radiusMeters?.let { stringResource(Res.string.preset_radius, it) },
                stringResource(if (r.reverse) Res.string.editor_outside else Res.string.editor_inside),
            ).joinToString(" · ")
        }
    }
    SettingRow(
        title = kind.title(),
        subtitle = subtitle,
        leading = { ConditionIcon(kind = kind, tone = IconTone.SOLID) },
        onClick = if (editable) ({ event(PolicyEditorEvent.ConditionClicked(kind)) }) else null,
        trailing = if (!editable) null else ({
            IconButton(onClick = { event(PolicyEditorEvent.ConditionRemoved(kind)) }, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp), tint = AppColors.icon.secondary)
            }
        }),
    )
}

@Composable
private fun ConditionIcon(kind: ConditionKind, tone: IconTone) {
    when (kind) {
        ConditionKind.TIME -> PolicyIcon(icon = Res.drawable.shift_clock, tone = tone, size = 44.dp)
        ConditionKind.LIMIT -> PolicyIcon(icon = Res.drawable.timer, tone = tone, size = 44.dp)
        ConditionKind.LOCATION -> Box(
            modifier = Modifier.size(44.dp).background(
                if (tone == IconTone.SOLID) AppColors.bg.primary else AppColors.bg.tertiary,
                RoundedCornerShape(16.dp),
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (tone == IconTone.SOLID) AppColors.icon.inverse else AppColors.icon.disabledTertiary,
            )
        }
    }
}

private val ConditionKind.titleRes: StringResource
    get() = when (this) {
        ConditionKind.TIME -> Res.string.editor_condition_time
        ConditionKind.LIMIT -> Res.string.editor_condition_limit
        ConditionKind.LOCATION -> Res.string.policy_location
    }

@Composable
private fun ConditionKind.title(): String = stringResource(titleRes)

// ═══════════════════════════════════════════
//  Vaqt sahifasi
// ═══════════════════════════════════════════

@OptIn(InternalVoyagerApi::class)
@Composable
private fun TimePage(state: PolicyEditorState, event: (PolicyEditorEvent) -> Unit) {
    val rule = state.timeEdit ?: return
    BackHandler(true) { event(PolicyEditorEvent.PageClosed) }
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg.page).then(systemBars.modifier)) {
        CustomHeader(title = stringResource(Res.string.editor_condition_time), showBackButton = true, onBackClick = { event(PolicyEditorEvent.PageClosed) })
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = ContainerPadding),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Spacer(modifier = Modifier.height(2.dp))
            SettingGroup {
                TimeHero(
                    startMin = rule.startMin,
                    endMin = rule.endMin,
                    startLabel = stringResource(Res.string.preset_starts),
                    endLabel = stringResource(Res.string.preset_ends),
                    onStart = { event(PolicyEditorEvent.TimeStartClicked) },
                    onEnd = { event(PolicyEditorEvent.TimeEndClicked) },
                )
                GroupDivider()
                SettingRow(
                    title = stringResource(Res.string.kunlar),
                    value = daysText(rule.days.map { it.num }.toSet()),
                    onClick = { event(PolicyEditorEvent.DaysClicked) },
                    trailing = { Chevron() },
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel(stringResource(Res.string.editor_in_this_time))
                SettingGroup {
                    RadioRow(
                        title = stringResource(Res.string.editor_closed_in_time),
                        subtitle = stringResource(Res.string.editor_closed_in_time_sub),
                        selected = rule.include,
                        onClick = { event(PolicyEditorEvent.TimeIncludeChanged(true)) },
                    )
                    GroupDivider()
                    RadioRow(
                        title = stringResource(Res.string.editor_open_in_time),
                        subtitle = stringResource(Res.string.editor_open_in_time_sub),
                        selected = !rule.include,
                        onClick = { event(PolicyEditorEvent.TimeIncludeChanged(false)) },
                    )
                }
            }
        }
        CustomButtonNew(
            text = stringResource(Res.string.saqlash),
            modifier = Modifier.fillMaxWidth().padding(horizontal = ContainerPadding, vertical = 12.dp),
            onClick = { event(PolicyEditorEvent.PageSaved) },
        )
    }
}

// ═══════════════════════════════════════════
//  Limit sahifasi
// ═══════════════════════════════════════════

@OptIn(InternalVoyagerApi::class)
@Composable
private fun LimitPage(state: PolicyEditorState, event: (PolicyEditorEvent) -> Unit) {
    val rule = state.limitEdit ?: return
    BackHandler(true) { event(PolicyEditorEvent.PageClosed) }
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    val hourly = rule.window == LimitWindow.HOUR

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg.page).then(systemBars.modifier)) {
        CustomHeader(title = stringResource(Res.string.editor_condition_limit), showBackButton = true, onBackClick = { event(PolicyEditorEvent.PageClosed) })
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = ContainerPadding),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Spacer(modifier = Modifier.height(2.dp))
            SettingGroup {
                SegmentedTabs(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    labels = listOf(stringResource(Res.string.kunlik), stringResource(Res.string.soatlik)),
                    selectedIndex = if (hourly) 1 else 0,
                    containerColor = AppColors.bg.page,
                    onSelect = { i -> event(PolicyEditorEvent.LimitWindowSelected(if (i == 1) LimitWindow.HOUR else LimitWindow.DAY)) },
                )
                LimitTile(
                    label = stringResource(if (hourly) Res.string.preset_per_hour_label else Res.string.preset_per_day_label),
                    value = durationText(rule.minutes),
                    onClick = { event(PolicyEditorEvent.LimitTileClicked) },
                )
                val quick = if (hourly) listOf(10, 20, 30) else listOf(60, 120, 180)
                QuickChips(
                    labels = quick.map { durationText(it) },
                    selectedIndex = quick.indexOf(rule.minutes),
                    onSelect = { i -> event(PolicyEditorEvent.QuickLimitSelected(quick[i])) },
                )
                GroupDivider()
                SettingRow(
                    title = stringResource(Res.string.kunlar),
                    value = daysText(rule.days.map { it.num }.toSet()),
                    onClick = { event(PolicyEditorEvent.DaysClicked) },
                    trailing = { Chevron() },
                )
            }
        }
        CustomButtonNew(
            text = stringResource(Res.string.saqlash),
            modifier = Modifier.fillMaxWidth().padding(horizontal = ContainerPadding, vertical = 12.dp),
            onClick = { event(PolicyEditorEvent.PageSaved) },
        )
    }
}

// ═══════════════════════════════════════════
//  Varaqlar va dialoglar
// ═══════════════════════════════════════════

@Composable
private fun EditorSheets(state: PolicyEditorState, event: (PolicyEditorEvent) -> Unit) {
    val sheet = state.sheet ?: return
    PolicySheet(onDismiss = { event(PolicyEditorEvent.SheetDismissed) }) {
        when (sheet) {
            EditorSheet.ADD_CONDITION -> {
                val added = state.conditions.toSet()
                SheetTitle(stringResource(Res.string.editor_add_condition))
                SettingGroup {
                    val rows = listOf(
                        Triple(ConditionKind.TIME, Res.string.editor_condition_time, Res.string.editor_condition_time_sub),
                        Triple(ConditionKind.LIMIT, Res.string.editor_condition_limit, Res.string.editor_condition_limit_sub),
                        Triple(ConditionKind.LOCATION, Res.string.policy_location, Res.string.editor_condition_location_sub),
                    )
                    rows.forEachIndexed { i, (kind, titleRes, subRes) ->
                        val isAdded = kind in added
                        OptionRow(
                            text = stringResource(titleRes),
                            subtitle = stringResource(subRes),
                            value = if (isAdded) stringResource(Res.string.editor_added) else null,
                            selected = false,
                            enabled = !isAdded,
                            onClick = { event(PolicyEditorEvent.ConditionPicked(kind)) },
                        )
                        if (i < rows.lastIndex) GroupDivider()
                    }
                }
            }
            EditorSheet.TIME_START -> TimeWheelSheetContent(
                title = stringResource(Res.string.preset_starts),
                initialMinute = state.timeEdit?.startMin ?: 0,
                onDone = { event(PolicyEditorEvent.TimeSet(it)) },
            )
            EditorSheet.TIME_END -> TimeWheelSheetContent(
                title = stringResource(Res.string.preset_ends),
                initialMinute = state.timeEdit?.endMin ?: 0,
                onDone = { event(PolicyEditorEvent.TimeSet(it)) },
            )
            EditorSheet.LIMIT -> LimitWheelSheetContent(
                hourly = state.limitEdit?.window == LimitWindow.HOUR,
                initialMinutes = state.limitEdit?.minutes ?: 60,
                onDone = { event(PolicyEditorEvent.LimitSet(it)) },
            )
            EditorSheet.DAYS -> DaysSheetContent(
                initialDays = (if (state.page == EditorPage.LIMIT) state.limitEdit?.days else state.timeEdit?.days)
                    .orEmpty().map { it.num }.toSet(),
                onDone = { event(PolicyEditorEvent.DaysSet(it)) },
            )
            EditorSheet.PAUSE -> PauseSheetContent(onPause = { event(PolicyEditorEvent.PauseSelected(it)) })
        }
    }
}

@Composable
private fun EditorDialogs(state: PolicyEditorState, event: (PolicyEditorEvent) -> Unit) {
    when (state.dialog) {
        EditorDialog.NAME -> TextInputDialog(
            title = stringResource(Res.string.jadval_nomi),
            confirmText = stringResource(Res.string.saqlash),
            placeholder = stringResource(Res.string.jadval_nomini_kiriting),
            initial = state.draft?.name.orEmpty(),
            onDismiss = { event(PolicyEditorEvent.DialogDismissed) },
            onConfirm = { event(PolicyEditorEvent.NameConfirmed(it)) },
        )
        EditorDialog.DELETE -> AlertDialog(
            onDismissRequest = { event(PolicyEditorEvent.DialogDismissed) },
            containerColor = AppColors.modal.primary,
            shape = RoundedCornerShape(24.dp),
            title = { Text(stringResource(Res.string.editor_delete_title), style = PolicyText.dialogTitle, color = AppColors.text.primary) },
            text = {
                Text(
                    stringResource(Res.string.editor_delete_message, state.draft?.name.orEmpty()),
                    style = PolicyText.dialogText,
                    color = AppColors.text.secondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { event(PolicyEditorEvent.DeleteConfirmed) }, enabled = !state.deleting) {
                    Text(stringResource(Res.string.ochirish), color = AppColors.text.accentDanger, style = PolicyText.action)
                }
            },
            dismissButton = {
                TextButton(onClick = { event(PolicyEditorEvent.DialogDismissed) }) {
                    Text(stringResource(Res.string.bekor_qilish), color = AppColors.text.secondary, style = PolicyText.action)
                }
            },
        )
        null -> Unit
    }

    val error = state.error
    ErrorHaptic(error)
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = error != null,
        title = stringResource(Res.string.xatolik),
        message = error?.asText().orEmpty(),
        onDismiss = { event(PolicyEditorEvent.ErrorDismissed) },
        onButtonClick = { event(PolicyEditorEvent.ErrorDismissed) },
    )
}

/** Instant → qurilma vaqt zonasida "HH:mm". */
private fun Instant.asLocalClock(): String {
    val t = toLocalDateTime(TimeZone.currentSystemDefault())
    return (t.hour * 60 + t.minute).asClock()
}
