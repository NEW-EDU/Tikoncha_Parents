package uz.tikoncha_parent.presentation.policy.quick

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
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
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.policy_active_until
import tikoncha_parents.composeapp.generated.resources.preset_paused_until
import tikoncha_parents.composeapp.generated.resources.preset_resume
import tikoncha_parents.composeapp.generated.resources.quick_add_app
import tikoncha_parents.composeapp.generated.resources.quick_blocked_apps
import tikoncha_parents.composeapp.generated.resources.quick_child_blocks
import tikoncha_parents.composeapp.generated.resources.quick_coparent_blocks
import tikoncha_parents.composeapp.generated.resources.quick_enabled
import tikoncha_parents.composeapp.generated.resources.quick_info_always
import tikoncha_parents.composeapp.generated.resources.quick_info_off
import tikoncha_parents.composeapp.generated.resources.quick_info_paused
import tikoncha_parents.composeapp.generated.resources.quick_info_readd
import tikoncha_parents.composeapp.generated.resources.quick_info_stats
import tikoncha_parents.composeapp.generated.resources.quick_others_note
import tikoncha_parents.composeapp.generated.resources.quick_pause
import tikoncha_parents.composeapp.generated.resources.quick_title
import tikoncha_parents.composeapp.generated.resources.stat_quick_paywall_message
import tikoncha_parents.composeapp.generated.resources.stat_quick_paywall_title
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.presentation.base.ChildAppIcon
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.SubscriptionBottomDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.haptics.ErrorHaptic
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.presentation.policy.components.AddRow
import uz.tikoncha_parent.presentation.policy.components.AppsSheetContent
import uz.tikoncha_parent.presentation.policy.components.Chevron
import uz.tikoncha_parent.presentation.policy.components.ExceptionRow
import uz.tikoncha_parent.presentation.policy.components.GroupDivider
import uz.tikoncha_parent.presentation.policy.components.PausedBanner
import uz.tikoncha_parent.presentation.policy.components.PauseSheetContent
import uz.tikoncha_parent.presentation.policy.components.PolicyInfoBox
import uz.tikoncha_parent.presentation.policy.components.PolicySheet
import uz.tikoncha_parent.presentation.policy.components.PolicySwitch
import uz.tikoncha_parent.presentation.policy.components.PolicyText
import uz.tikoncha_parent.presentation.policy.components.SectionLabel
import uz.tikoncha_parent.presentation.policy.components.SettingGroup
import uz.tikoncha_parent.presentation.policy.components.SettingRow
import uz.tikoncha_parent.presentation.policy.components.asClock
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars
import kotlin.time.Instant

/** Tezkor blok shabloni — shart, kategoriya va sayt yo'q; faqat doim yopiq ilovalar ro'yxati. */
@OptIn(InternalVoyagerApi::class)
class QuickBlockScreen(private val childId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        val viewModel = koinScreenModel<QuickBlockViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(childId) { viewModel.onEvent(QuickBlockEvent.Load(childId)) }
        // To'lovdan qaytganda tarif va ro'yxat qayta o'qiladi
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onEvent(QuickBlockEvent.Resumed) }
        BackHandler(true) { navigator.pop() }

        QuickBlockUi(
            state = state,
            event = viewModel::onEvent,
            onBack = { navigator.pop() },
            onOpenSubscription = {
                viewModel.onEvent(QuickBlockEvent.PaywallDismissed)
                navigator.push(SubscriptionPaymentScreen())
            },
        )
    }
}

@Composable
fun QuickBlockUi(
    state: QuickBlockState,
    event: (QuickBlockEvent) -> Unit,
    onBack: () -> Unit,
    onOpenSubscription: () -> Unit,
) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)

    // Ro'yxat server javobidan keyin o'zgaradi — haptika aynan shu paytda
    val haptics = rememberAppHaptics()
    var shownCount by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(state.loaded, state.ownPackages.size) {
        if (!state.loaded) return@LaunchedEffect
        val before = shownCount
        shownCount = state.ownPackages.size
        when {
            before == null || before == state.ownPackages.size -> Unit
            state.ownPackages.size > before -> haptics.success()
            else -> haptics.toggle(false)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg.page).then(systemBars.modifier)) {
        CustomHeader(title = stringResource(Res.string.quick_title), showBackButton = true, onBackClick = onBack)

        if (!state.loaded) {
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
            PolicyInfoBox(lines = infoLines(state))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel(stringResource(Res.string.quick_blocked_apps))
                SettingGroup {
                    state.ownPackages.forEach { pkg ->
                        ExceptionRow(
                            label = state.label(pkg),
                            iconUrl = state.iconUrl(pkg),
                            busy = state.isRemoving(pkg),
                            onRemove = { event(QuickBlockEvent.AppRemoved(pkg)) },
                        )
                        GroupDivider()
                    }
                    AddRow(text = stringResource(Res.string.quick_add_app), busy = state.adding, onClick = { event(QuickBlockEvent.AddClicked) })
                }
            }

            OthersBlock(title = stringResource(Res.string.quick_child_blocks), state = state, packages = state.childPackages)
            OthersBlock(title = stringResource(Res.string.quick_coparent_blocks), state = state, packages = state.coParentPackages)
            if (state.childPackages.isNotEmpty() || state.coParentPackages.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.quick_others_note),
                    style = PolicyText.hint,
                    color = AppColors.text.tertiary,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (state.showAppsSheet) {
        PolicySheet(onDismiss = { event(QuickBlockEvent.SheetDismissed) }) {
            AppsSheetContent(
                installedApps = state.addableApps,
                excluded = state.ownPackages.toSet(),
                onAdd = { event(QuickBlockEvent.AppsAdded(it.toSet())) },
            )
        }
    }
    if (state.showPauseSheet) {
        PolicySheet(onDismiss = { event(QuickBlockEvent.PauseDismissed) }) {
            PauseSheetContent(onPause = { event(QuickBlockEvent.PauseSelected(it)) })
        }
    }

    SubscriptionBottomDialog(
        show = state.showPaywall,
        title = stringResource(Res.string.stat_quick_paywall_title),
        message = stringResource(Res.string.stat_quick_paywall_message),
        onConfirm = onOpenSubscription,
        onDismiss = { event(QuickBlockEvent.PaywallDismissed) },
    )

    val error = state.error
    ErrorHaptic(error)
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = error != null,
        title = stringResource(Res.string.xatolik),
        message = error?.asText().orEmpty(),
        onDismiss = { event(QuickBlockEvent.ErrorDismissed) },
        onButtonClick = { event(QuickBlockEvent.ErrorDismissed) },
    )
}

/** Yoqilgan switch'i · Vaqtincha to'xtatish · to'xtatilgan banner (tayyor jadval bilan bir xil). */
@Composable
private fun HeaderGroup(state: QuickBlockState, event: (QuickBlockEvent) -> Unit) {
    SettingGroup {
        val paused = state.pausedUntil
        if (paused != null) {
            PausedBanner(
                text = stringResource(Res.string.preset_paused_until, paused.localClock()),
                actionText = stringResource(Res.string.preset_resume),
                onResume = { event(QuickBlockEvent.ResumeClicked) },
            )
        }
        SettingRow(
            title = stringResource(Res.string.quick_enabled),
            trailing = {
                PolicySwitch(
                    checked = state.isOn,
                    busy = state.pendingEnabled != null,
                    enabled = state.canToggle,
                    onCheckedChange = { event(QuickBlockEvent.EnabledToggled(it)) },
                )
            },
        )
        if (state.isOn) {
            GroupDivider()
            SettingRow(
                title = stringResource(Res.string.quick_pause),
                subtitle = paused?.let { stringResource(Res.string.policy_active_until, it.localClock()) },
                onClick = { if (!state.pausing) event(QuickBlockEvent.PauseClicked) },
                trailing = { Chevron() },
            )
        }
    }
}

/** Bola yoki ikkinchi ota-ona bloklagan ilovalar — faqat ko'rish uchun. */
@Composable
private fun OthersBlock(title: String, state: QuickBlockState, packages: List<String>) {
    if (packages.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(title)
        SettingGroup {
            packages.forEachIndexed { i, pkg ->
                SettingRow(title = state.label(pkg), leading = { ChildAppIcon(iconUrl = state.iconUrl(pkg)) })
                if (i < packages.lastIndex) GroupDivider()
            }
        }
    }
}

/** Ro'yxat doim yopiq · o'chiq/pauza holati · statistikadan ham · qayta yoqilishi · obuna. */
@Composable
private fun infoLines(state: QuickBlockState): List<String> {
    val paused = state.pausedUntil
    return listOfNotNull(
        stringResource(Res.string.quick_info_always),
        stringResource(Res.string.quick_info_off).takeIf { state.own != null && !state.isOn && state.ownPackages.isNotEmpty() },
        paused?.let { stringResource(Res.string.quick_info_paused, it.localClock()) },
        stringResource(Res.string.quick_info_stats),
        stringResource(Res.string.quick_info_readd).takeIf { state.own != null && !state.isOn },
        stringResource(Res.string.stat_quick_paywall_message).takeIf { state.quick.paid == false },
    )
}

private fun Instant.localClock(): String {
    val t = toLocalDateTime(TimeZone.currentSystemDefault())
    return (t.hour * 60 + t.minute).asClock()
}
