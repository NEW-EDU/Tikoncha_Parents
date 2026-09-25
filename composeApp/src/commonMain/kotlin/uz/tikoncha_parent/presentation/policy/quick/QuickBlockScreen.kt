package uz.tikoncha_parent.presentation.policy.quick

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import tikoncha_parents.composeapp.generated.resources.davom_ettirish
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.jadval_toxtatilgan_gacha
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
import tikoncha_parents.composeapp.generated.resources.quick_no_apps
import tikoncha_parents.composeapp.generated.resources.quick_others_note
import tikoncha_parents.composeapp.generated.resources.quick_pause
import tikoncha_parents.composeapp.generated.resources.quick_title
import tikoncha_parents.composeapp.generated.resources.stat_quick_paywall_message
import tikoncha_parents.composeapp.generated.resources.stat_quick_paywall_title
import tikoncha_parents.composeapp.generated.resources.stat_quick_remove_cd
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.CustomSwitch
import uz.tikoncha_parent.presentation.base.SubscriptionBottomDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.haptics.ErrorHaptic
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.presentation.base.haptics.rememberToggleHaptic
import uz.tikoncha_parent.presentation.policy.common.toHhMm
import uz.tikoncha_parent.presentation.policy.policy_list.PausePolicySheet
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.TextFieldCornerRadius
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
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
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary,
    )

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ) {
        CustomHeader(title = stringResource(Res.string.quick_title), showBackButton = true, onBackClick = onBack)

        if (!state.loaded) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.action.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Card {
                    EnabledRow(state = state, onToggle = { event(QuickBlockEvent.EnabledToggled(it)) })
                    if (state.isOn) {
                        RowDivider()
                        PauseRow(
                            pausedUntil = state.pausedUntil,
                            busy = state.pausing,
                            onClick = { event(if (state.pausedUntil != null) QuickBlockEvent.ResumeClicked else QuickBlockEvent.PauseClicked) },
                        )
                    }
                }

                InfoBox(lines = infoLines(state))

                Section(title = stringResource(Res.string.quick_blocked_apps)) {
                    Card {
                        if (state.ownPackages.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.quick_no_apps),
                                style = AppTypography.titleSmMedium,
                                color = AppColors.text.tertiary,
                                modifier = Modifier.padding(vertical = 14.dp),
                            )
                        }
                        state.ownPackages.forEach { pkg ->
                            AppLine(
                                name = state.label(pkg),
                                iconUrl = state.iconUrl(pkg),
                                trailing = { RemoveButton(busy = state.isRemoving(pkg), onClick = { event(QuickBlockEvent.AppRemoved(pkg)) }) },
                            )
                            RowDivider()
                        }
                        AddRow(adding = state.adding, onClick = { event(QuickBlockEvent.AddClicked) })
                    }
                }

                if (state.childPackages.isNotEmpty()) {
                    Section(title = stringResource(Res.string.quick_child_blocks)) {
                        Card { ReadOnlyApps(state, state.childPackages) }
                    }
                }
                if (state.coParentPackages.isNotEmpty()) {
                    Section(title = stringResource(Res.string.quick_coparent_blocks)) {
                        Card { ReadOnlyApps(state, state.coParentPackages) }
                    }
                }
                if (state.childPackages.isNotEmpty() || state.coParentPackages.isNotEmpty()) {
                    Text(
                        text = stringResource(Res.string.quick_others_note),
                        style = AppTypography.emphasizedXsRegular,
                        color = AppColors.text.tertiary,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (state.showAppsSheet) {
        QuickAddAppsSheet(
            apps = state.addableApps,
            onAdd = { event(QuickBlockEvent.AppsAdded(it)) },
            onDismiss = { event(QuickBlockEvent.SheetDismissed) },
        )
    }

    if (state.showPauseSheet) {
        PausePolicySheet(
            isPaused = state.pausedUntil != null,
            onSelect = { event(QuickBlockEvent.PauseSelected(it)) },
            onResume = { event(QuickBlockEvent.ResumeClicked) },
            onDismiss = { event(QuickBlockEvent.PauseDismissed) },
        )
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

/** Ro'yxat doim yopiq · o'chiq/pauza holati · statistikadan ham · qayta yoqilishi · obuna. */
@Composable
private fun infoLines(state: QuickBlockState): List<String> {
    val paused = state.pausedUntil
    return listOfNotNull(
        stringResource(Res.string.quick_info_always),
        stringResource(Res.string.quick_info_off).takeIf { state.own != null && !state.isOn && state.ownPackages.isNotEmpty() },
        paused?.let { stringResource(Res.string.quick_info_paused, it.localHhMm()) },
        stringResource(Res.string.quick_info_stats),
        stringResource(Res.string.quick_info_readd).takeIf { state.own != null && !state.isOn },
        stringResource(Res.string.stat_quick_paywall_message).takeIf { state.quick.paid == false },
    )
}

private fun Instant.localHhMm(): String = toLocalDateTime(TimeZone.currentSystemDefault()).time.toHhMm()

@Composable
private fun EnabledRow(state: QuickBlockState, onToggle: (Boolean) -> Unit) {
    // Switch server tasdiqlagan holatni ko'rsatadi — tebranish holat haqiqatan o'zgarganda
    val armHaptic = rememberToggleHaptic(state.isOn)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.quick_enabled),
            style = AppTypography.titleMdMedium,
            color = AppColors.text.primary,
            modifier = Modifier.weight(1f),
        )
        if (state.pendingEnabled != null) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AppColors.action.primary, strokeWidth = 2.dp)
        } else {
            CustomSwitch(
                checked = state.isOn,
                enabled = state.canToggle,
                onCheckedChange = {
                    armHaptic()
                    onToggle(it)
                },
            )
        }
    }
}

@Composable
private fun PauseRow(pausedUntil: Instant?, busy: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !busy, onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(if (pausedUntil != null) Res.string.davom_ettirish else Res.string.quick_pause),
                style = AppTypography.titleMdMedium,
                color = AppColors.text.primary,
            )
            if (pausedUntil != null) {
                Text(
                    text = stringResource(Res.string.jadval_toxtatilgan_gacha, pausedUntil.localHhMm()),
                    style = AppTypography.emphasizedXsRegular,
                    color = AppColors.text.accentWarning,
                )
            }
        }
        if (busy) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AppColors.action.primary, strokeWidth = 2.dp)
        } else {
            Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null, tint = AppColors.icon.secondary)
        }
    }
}

@Composable
private fun ReadOnlyApps(state: QuickBlockState, packages: List<String>) {
    packages.forEachIndexed { i, pkg ->
        AppLine(name = state.label(pkg), iconUrl = state.iconUrl(pkg))
        if (i < packages.lastIndex) RowDivider()
    }
}

@Composable
private fun RemoveButton(busy: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
        if (busy) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = AppColors.action.primary, strokeWidth = 2.dp)
        } else {
            IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(Res.string.stat_quick_remove_cd),
                    modifier = Modifier.size(18.dp),
                    tint = AppColors.icon.accentDanger,
                )
            }
        }
    }
}

@Composable
private fun AddRow(adding: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !adding, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(AppColors.bg.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (adding) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AppColors.action.primary, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = AppColors.action.primary)
            }
        }
        Text(
            text = stringResource(Res.string.quick_add_app),
            style = AppTypography.titleMdMedium,
            color = AppColors.action.primary,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = AppTypography.titleSmSemiBold,
            color = AppColors.text.secondary,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        content()
    }
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.bg.surface, RoundedCornerShape(TextFieldCornerRadius))
            .padding(horizontal = ContainerPadding),
        content = content,
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(thickness = 1.dp, color = AppColors.border.secondary.copy(alpha = 0.12f))
}
