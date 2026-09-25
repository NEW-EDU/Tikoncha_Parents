package uz.tikoncha_parent.presentation.policy.preset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bugun
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.kunlar
import tikoncha_parents.composeapp.generated.resources.kunlik
import tikoncha_parents.composeapp.generated.resources.policy_active_until
import tikoncha_parents.composeapp.generated.resources.policy_location
import tikoncha_parents.composeapp.generated.resources.policy_n_apps
import tikoncha_parents.composeapp.generated.resources.policy_n_categories
import tikoncha_parents.composeapp.generated.resources.policy_n_sites
import tikoncha_parents.composeapp.generated.resources.preset_add_app
import tikoncha_parents.composeapp.generated.resources.preset_all_apps
import tikoncha_parents.composeapp.generated.resources.preset_counted
import tikoncha_parents.composeapp.generated.resources.preset_enabled
import tikoncha_parents.composeapp.generated.resources.preset_ends
import tikoncha_parents.composeapp.generated.resources.preset_limit
import tikoncha_parents.composeapp.generated.resources.preset_not_counted
import tikoncha_parents.composeapp.generated.resources.preset_of
import tikoncha_parents.composeapp.generated.resources.preset_only_selected
import tikoncha_parents.composeapp.generated.resources.preset_optional
import tikoncha_parents.composeapp.generated.resources.preset_paused_until
import tikoncha_parents.composeapp.generated.resources.preset_per_day_label
import tikoncha_parents.composeapp.generated.resources.preset_per_hour_label
import tikoncha_parents.composeapp.generated.resources.preset_radius
import tikoncha_parents.composeapp.generated.resources.preset_reset
import tikoncha_parents.composeapp.generated.resources.preset_resume
import tikoncha_parents.composeapp.generated.resources.preset_school_area
import tikoncha_parents.composeapp.generated.resources.preset_starts
import tikoncha_parents.composeapp.generated.resources.preset_stays_open
import tikoncha_parents.composeapp.generated.resources.preset_this_hour
import tikoncha_parents.composeapp.generated.resources.preset_used_up
import tikoncha_parents.composeapp.generated.resources.preset_what_closed
import tikoncha_parents.composeapp.generated.resources.preset_when
import tikoncha_parents.composeapp.generated.resources.saqlash
import tikoncha_parents.composeapp.generated.resources.soatlik
import tikoncha_parents.composeapp.generated.resources.vaqtincha_toxtatish
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.haptics.ErrorHaptic
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.presentation.policy.components.AddRow
import uz.tikoncha_parent.presentation.policy.components.AppsSheetContent
import uz.tikoncha_parent.presentation.policy.components.Chevron
import uz.tikoncha_parent.presentation.policy.components.DaysSheetContent
import uz.tikoncha_parent.presentation.policy.components.ExceptionRow
import uz.tikoncha_parent.presentation.policy.components.GroupDivider
import uz.tikoncha_parent.presentation.policy.components.LimitTile
import uz.tikoncha_parent.presentation.policy.components.LimitWheelSheetContent
import uz.tikoncha_parent.presentation.policy.components.PausedBanner
import uz.tikoncha_parent.presentation.policy.components.PauseSheetContent
import uz.tikoncha_parent.presentation.policy.components.PolicyInfoBox
import uz.tikoncha_parent.presentation.policy.components.PolicySheet
import uz.tikoncha_parent.presentation.policy.components.PolicySwitch
import uz.tikoncha_parent.presentation.policy.components.QuickChips
import uz.tikoncha_parent.presentation.policy.components.SectionLabel
import uz.tikoncha_parent.presentation.policy.components.SegmentedTabs
import uz.tikoncha_parent.presentation.policy.components.SettingGroup
import uz.tikoncha_parent.presentation.policy.components.SettingRow
import uz.tikoncha_parent.presentation.policy.components.TimeHero
import uz.tikoncha_parent.presentation.policy.components.TimeWheelSheetContent
import uz.tikoncha_parent.presentation.policy.components.UsageBar
import uz.tikoncha_parent.presentation.policy.components.asClock
import uz.tikoncha_parent.presentation.policy.components.daysText
import uz.tikoncha_parent.presentation.policy.components.durationText
import uz.tikoncha_parent.presentation.policy.components.sentences
import uz.tikoncha_parent.presentation.policy.components.title
import uz.tikoncha_parent.presentation.policy.mapper.toInfo
import uz.tikoncha_parent.presentation.policy.location.LocationPicker
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.presentation.policy.targets.TargetsEditor
import uz.tikoncha_parent.presentation.policy.targets.TargetsFlavor
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars
import kotlin.time.Instant

/**
 * Tayyor jadval (Uyqu vaqti / Vaqt limiti / Dars vaqti) — Student ekrani bilan bir xil.
 * [onOpenTargets] va [onOpenLocation] — nishonlar muharriri va maktab hududi xaritasi
 * (alohida ekranlar, natija `TargetsApplied` / `LocationApplied` bilan qaytadi).
 */
@OptIn(InternalVoyagerApi::class)
class PresetPolicyScreen(private val childId: String, private val kind: PresetKind) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        val viewModel = koinScreenModel<PresetPolicyViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val haptics = rememberAppHaptics()
        val title = kind.title()

        LaunchedEffect(Unit) { viewModel.onEvent(PresetPolicyEvent.Init(childId, kind, title)) }
        LaunchedEffect(Unit) {
            viewModel.effect.collect { e ->
                when (e) {
                    PresetPolicyEffect.Saved -> {
                        haptics.success()
                        navigator.pop()
                    }
                }
            }
        }

        val targets = state.targets
        if (targets != null) {
            TargetsEditor(
                state = targets,
                apps = state.childApps,
                flavor = TargetsFlavor.Preset(kind),
                event = { viewModel.onEvent(PresetPolicyEvent.Targets(it)) },
                onDone = { viewModel.onEvent(PresetPolicyEvent.TargetsDone) },
                onClose = { viewModel.onEvent(PresetPolicyEvent.TargetsClosed) },
                appUsage = state.appUsage,
            )
            return
        }
        val location = state.location
        if (location != null) {
            LocationPicker(
                state = location,
                title = stringResource(Res.string.preset_school_area),
                event = { viewModel.onEvent(PresetPolicyEvent.Location(it)) },
                onDone = { viewModel.onEvent(PresetPolicyEvent.LocationDone) },
                onClose = { viewModel.onEvent(PresetPolicyEvent.LocationClosed) },
            )
            return
        }
        BackHandler(true) { navigator.pop() }
        PresetPolicyUi(state = state, event = viewModel::onEvent, onBack = { navigator.pop() })
    }
}

@Composable
fun PresetPolicyUi(
    state: PresetPolicyState,
    event: (PresetPolicyEvent) -> Unit,
    onBack: () -> Unit,
) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    val draft = state.draft

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg.page).then(systemBars.modifier)) {
        CustomHeader(
            title = state.kind.title(),
            showBackButton = true,
            onBackClick = onBack,
            trailingIcon = {
                IconButton(onClick = { event(PresetPolicyEvent.ResetClicked) }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = stringResource(Res.string.preset_reset), tint = AppColors.icon.primary)
                }
            },
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
            HeaderGroup(state = state, event = event)
            PolicyInfoBox(lines = draft.toInfo(enabled = state.isEnabled).sentences())

            // O'chiq bo'lsa sozlamalar xira, lekin tahrirlanadi va saqlanadi
            Column(modifier = Modifier.alpha(if (state.isEnabled) 1f else 0.45f), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                when (state.kind) {
                    PresetKind.SLEEP -> {
                        WhenBlock(draft = draft, event = event, showDays = false)
                        TargetsBlock(state = state, draft = draft, onClick = { event(PresetPolicyEvent.TargetsClicked) })
                        if (draft.targets.hasExclusionScope) ExceptionsBlock(state = state, event = event, title = stringResource(Res.string.preset_stays_open))
                    }
                    PresetKind.LIMIT -> {
                        LimitBlock(state = state, draft = draft, event = event)
                        TargetsBlock(state = state, draft = draft, onClick = { event(PresetPolicyEvent.TargetsClicked) })
                        if (draft.targets.hasExclusionScope) ExceptionsBlock(state = state, event = event, title = stringResource(Res.string.preset_not_counted))
                    }
                    PresetKind.SCHOOL -> {
                        WhenBlock(draft = draft, event = event, showDays = true)
                        TargetsBlock(state = state, draft = draft, onClick = { event(PresetPolicyEvent.TargetsClicked) })
                        if (draft.targets.hasExclusionScope) ExceptionsBlock(state = state, event = event, title = stringResource(Res.string.preset_stays_open))
                        LocationBlock(draft = draft, event = event)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        CustomButtonNew(
            text = stringResource(Res.string.saqlash),
            modifier = Modifier.fillMaxWidth().padding(horizontal = ContainerPadding, vertical = 12.dp),
            enabled = state.hasChanges && !state.saving,
            onClick = { event(PresetPolicyEvent.SaveClicked) },
        )
    }

    PresetSheets(state = state, event = event)

    val error = state.error
    ErrorHaptic(error)
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = error != null,
        title = stringResource(Res.string.xatolik),
        message = error?.asText().orEmpty(),
        onDismiss = { event(PresetPolicyEvent.ErrorDismissed) },
        onButtonClick = { event(PresetPolicyEvent.ErrorDismissed) },
    )
}

private fun Instant.asLocalClock(): String {
    val t = toLocalDateTime(TimeZone.currentSystemDefault())
    return (t.hour * 60 + t.minute).asClock()
}

/** Yoqilgan switch'i · Vaqtincha to'xtatish · to'xtatilgan banner. */
@Composable
private fun HeaderGroup(state: PresetPolicyState, event: (PresetPolicyEvent) -> Unit) {
    SettingGroup {
        val paused = state.pausedUntil
        if (state.isPaused && paused != null) {
            PausedBanner(
                text = stringResource(Res.string.preset_paused_until, paused.asLocalClock()),
                actionText = stringResource(Res.string.preset_resume),
                onResume = { event(PresetPolicyEvent.ResumeClicked) },
            )
        }
        SettingRow(
            title = stringResource(Res.string.preset_enabled),
            trailing = {
                PolicySwitch(checked = state.isEnabled, busy = state.pendingEnabled != null, onCheckedChange = { event(PresetPolicyEvent.EnabledToggled(it)) })
            },
        )
        if (state.isEnabled && state.policyId != null) {
            GroupDivider()
            SettingRow(
                title = stringResource(Res.string.vaqtincha_toxtatish),
                subtitle = paused?.takeIf { state.isPaused }?.let { stringResource(Res.string.policy_active_until, it.asLocalClock()) },
                onClick = { if (!state.busy) event(PresetPolicyEvent.PauseClicked) },
                trailing = { Chevron() },
            )
        }
    }
}

/** "Qachon": vaqt katagi + tez tanlov (+ Kunlar). */
@Composable
private fun WhenBlock(draft: PolicyDraft, event: (PresetPolicyEvent) -> Unit, showDays: Boolean) {
    val rule = draft.conditions.time ?: return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(stringResource(Res.string.preset_when))
        SettingGroup {
            TimeHero(
                startMin = rule.startMin,
                endMin = rule.endMin,
                startLabel = stringResource(Res.string.preset_starts),
                endLabel = stringResource(Res.string.preset_ends),
                onStart = { event(PresetPolicyEvent.StartClicked) },
                onEnd = { event(PresetPolicyEvent.EndClicked) },
            )
            if (!showDays) {
                // Uyqu vaqti — har kuni; tez tanlov oynalari
                val quick = listOf(21 * 60 to 7 * 60, 22 * 60 to 7 * 60, 23 * 60 to 8 * 60)
                QuickChips(
                    labels = quick.map { (a, b) -> "${a.asClock()} – ${b.asClock()}" },
                    selectedIndex = quick.indexOf(rule.startMin to rule.endMin),
                    onSelect = { i -> event(PresetPolicyEvent.QuickTimeSelected(quick[i].first, quick[i].second)) },
                )
            } else {
                GroupDivider()
                SettingRow(
                    title = stringResource(Res.string.kunlar),
                    value = daysText(rule.days.map { it.num }.toSet()),
                    onClick = { event(PresetPolicyEvent.DaysClicked) },
                    trailing = { Chevron() },
                )
            }
        }
    }
}

/** "Limit": Kunlik | Soatlik · katak · tez tanlov · Bugun/Shu soatda · Kunlar. */
@Composable
private fun LimitBlock(state: PresetPolicyState, draft: PolicyDraft, event: (PresetPolicyEvent) -> Unit) {
    val rule = draft.limits.usage ?: return
    val hourly = rule.window == LimitWindow.HOUR
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(stringResource(Res.string.preset_limit))
        SettingGroup {
            SegmentedTabs(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                labels = listOf(stringResource(Res.string.kunlik), stringResource(Res.string.soatlik)),
                selectedIndex = if (hourly) 1 else 0,
                containerColor = AppColors.bg.page,
                onSelect = { i -> event(PresetPolicyEvent.LimitWindowSelected(if (i == 1) LimitWindow.HOUR else LimitWindow.DAY)) },
            )
            LimitTile(
                label = stringResource(if (hourly) Res.string.preset_per_hour_label else Res.string.preset_per_day_label),
                value = durationText(rule.minutes),
                onClick = { event(PresetPolicyEvent.LimitClicked) },
            )
            val quick = if (hourly) listOf(10, 20, 30) else listOf(60, 120, 180)
            QuickChips(
                labels = quick.map { durationText(it) },
                selectedIndex = quick.indexOf(rule.minutes),
                onSelect = { i -> event(PresetPolicyEvent.QuickLimitSelected(quick[i])) },
            )
            val used = state.usedMinutes
            if (used != null && state.policyId != null) {
                GroupDivider()
                val head = stringResource(if (hourly) Res.string.preset_this_hour else Res.string.bugun)
                val done = used >= rule.minutes
                UsageBar(
                    headline = "$head · " + if (done) stringResource(Res.string.preset_used_up) else durationText(used),
                    trailing = stringResource(Res.string.preset_of, durationText(rule.minutes)),
                    usedMinutes = used,
                    limitMinutes = rule.minutes,
                )
            }
            GroupDivider()
            SettingRow(
                title = stringResource(Res.string.kunlar),
                value = daysText(rule.days.map { it.num }.toSet()),
                onClick = { event(PresetPolicyEvent.DaysClicked) },
                trailing = { Chevron() },
            )
        }
    }
}

/** "Nimalar yopiladi" / "Hisoblanadi" — nishonlar xulosasi; bosilsa muharrir ochiladi. */
@Composable
private fun TargetsBlock(state: PresetPolicyState, draft: PolicyDraft, onClick: () -> Unit) {
    val t = draft.targets
    val label = stringResource(if (state.kind == PresetKind.LIMIT) Res.string.preset_counted else Res.string.preset_what_closed)
    val allApps = t.isAllApps
    val title = stringResource(if (allApps) Res.string.preset_all_apps else Res.string.preset_only_selected)
    val counts = listOfNotNull(
        (t.packages.count { it != PolicyTargets.ALL_APPS } + t.features.size).takeIf { it > 0 && !allApps }
            ?.let { stringResource(Res.string.policy_n_apps, it) },
        t.categories.size.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_categories, it) },
        t.sites.size.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_sites, it) },
    ).joinToString(" · ") { it.replace(' ', ' ') }.ifBlank { null }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(label)
        SettingGroup { SettingRow(title = title, subtitle = counts, onClick = onClick, trailing = { Chevron() }) }
    }
}

/** Istisno ilovalar: Uyqu/Dars — "Ochiq qoladi"; Limit — "Hisoblanmaydi". */
@Composable
private fun ExceptionsBlock(state: PresetPolicyState, event: (PresetPolicyEvent) -> Unit, title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(title)
        SettingGroup {
            state.exceptions.forEach { app ->
                ExceptionRow(label = app.label, iconUrl = app.iconUrl, onRemove = { event(PresetPolicyEvent.ExceptionRemoved(app.packageName)) })
                GroupDivider()
            }
            AddRow(text = stringResource(Res.string.preset_add_app), onClick = { event(PresetPolicyEvent.AddExceptionClicked) })
        }
    }
}

/** Dars vaqti: "Faqat maktab hududida" — switch yoqilsa yoki qator bosilsa xarita ochiladi. */
@Composable
private fun LocationBlock(draft: PolicyDraft, event: (PresetPolicyEvent) -> Unit) {
    val rule = draft.conditions.location
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(stringResource(Res.string.policy_location), hint = stringResource(Res.string.preset_optional))
        SettingGroup {
            SettingRow(
                title = stringResource(Res.string.preset_school_area),
                subtitle = rule?.radiusMeters?.let { stringResource(Res.string.preset_radius, it) },
                onClick = if (rule != null) ({ event(PresetPolicyEvent.LocationClicked) }) else null,
                trailing = {
                    PolicySwitch(checked = rule != null, onCheckedChange = { event(PresetPolicyEvent.LocationToggled(it)) })
                },
            )
        }
    }
}

@Composable
private fun PresetSheets(state: PresetPolicyState, event: (PresetPolicyEvent) -> Unit) {
    val sheet = state.sheet ?: return
    val draft = state.draft ?: return
    PolicySheet(onDismiss = { event(PresetPolicyEvent.SheetDismissed) }) {
        when (sheet) {
            PresetSheet.TIME_START -> TimeWheelSheetContent(
                title = stringResource(Res.string.preset_starts),
                initialMinute = draft.conditions.time?.startMin ?: 0,
                onDone = { event(PresetPolicyEvent.TimeSet(it)) },
            )
            PresetSheet.TIME_END -> TimeWheelSheetContent(
                title = stringResource(Res.string.preset_ends),
                initialMinute = draft.conditions.time?.endMin ?: 0,
                onDone = { event(PresetPolicyEvent.TimeSet(it)) },
            )
            PresetSheet.LIMIT -> LimitWheelSheetContent(
                hourly = draft.limits.usage?.window == LimitWindow.HOUR,
                initialMinutes = draft.limits.usage?.minutes ?: 180,
                onDone = { event(PresetPolicyEvent.LimitSet(it)) },
            )
            PresetSheet.DAYS -> DaysSheetContent(
                initialDays = (if (state.kind == PresetKind.LIMIT) draft.limits.usage?.days else draft.conditions.time?.days)
                    .orEmpty().map { it.num }.toSet(),
                onDone = { event(PresetPolicyEvent.DaysSet(it)) },
            )
            PresetSheet.PAUSE -> PauseSheetContent(onPause = { event(PresetPolicyEvent.PauseSelected(it)) })
            PresetSheet.APPS -> AppsSheetContent(
                installedApps = state.childApps,
                excluded = state.exceptions.map { it.packageName }.toSet(),
                onAdd = { event(PresetPolicyEvent.ExceptionsAdded(it)) },
            )
        }
    }
}
