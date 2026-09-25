package uz.tikoncha_parent.presentation.policy.protection

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.policy_content_protection
import tikoncha_parents.composeapp.generated.resources.policy_content_protection_others
import tikoncha_parents.composeapp.generated.resources.policy_n_apps
import tikoncha_parents.composeapp.generated.resources.policy_n_categories
import tikoncha_parents.composeapp.generated.resources.policy_n_sites
import tikoncha_parents.composeapp.generated.resources.policy_paywall_protection
import tikoncha_parents.composeapp.generated.resources.preset_enabled
import tikoncha_parents.composeapp.generated.resources.protection_footer
import tikoncha_parents.composeapp.generated.resources.protection_section
import tikoncha_parents.composeapp.generated.resources.shield
import tikoncha_parents.composeapp.generated.resources.stat_quick_paywall_title
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.domain.model.policy.ProtectionPackStatus
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.SubscriptionBottomDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.haptics.ErrorHaptic
import uz.tikoncha_parent.presentation.policy.components.GroupDivider
import uz.tikoncha_parent.presentation.policy.components.IconTone
import uz.tikoncha_parent.presentation.policy.components.PolicyIcon
import uz.tikoncha_parent.presentation.policy.components.PolicySwitch
import uz.tikoncha_parent.presentation.policy.components.PolicyText
import uz.tikoncha_parent.presentation.policy.components.SectionLabel
import uz.tikoncha_parent.presentation.policy.components.SettingGroup
import uz.tikoncha_parent.presentation.policy.components.SettingRow
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

/** Kontent himoya — Student ekrani bilan bir xil: bitta switch, paketlar faqat ma'lumot. */
@OptIn(InternalVoyagerApi::class)
class ContentProtectionScreen(private val childId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        val viewModel = koinScreenModel<ContentProtectionViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) { viewModel.onEvent(ContentProtectionEvent.Init(childId)) }
        BackHandler(true) { navigator.pop() }

        ContentProtectionUi(
            state = state,
            event = viewModel::onEvent,
            onBack = { navigator.pop() },
            onOpenSubscription = {
                viewModel.onEvent(ContentProtectionEvent.PayWallDismissed)
                navigator.push(SubscriptionPaymentScreen())
            },
        )
    }
}

@Composable
fun ContentProtectionUi(
    state: ContentProtectionState,
    event: (ContentProtectionEvent) -> Unit,
    onBack: () -> Unit,
    onOpenSubscription: () -> Unit,
) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    val lang = remember { LanguagePrefs.loadOrDefault().languageCode }
    val active = state.isEnabled || state.byOthers

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg.page).then(systemBars.modifier)) {
        CustomHeader(title = stringResource(Res.string.policy_content_protection), showBackButton = true, onBackClick = onBack)

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

            // ── Mening himoyam; farzand / ikkinchi ota-ona yoqqani — alohida belgi ──
            SettingGroup {
                if (state.byOthers) OthersBanner(stringResource(Res.string.policy_content_protection_others))
                SettingRow(
                    title = stringResource(Res.string.preset_enabled),
                    trailing = {
                        PolicySwitch(
                            checked = state.isEnabled,
                            onCheckedChange = { event(ContentProtectionEvent.Toggled(it)) },
                            busy = state.busy,
                            enabled = state.isAvailable,
                        )
                    },
                )
            }

            // ── Nimalar himoya qilinadi — faqat ma'lumot ──
            Column(modifier = Modifier.alpha(if (active) 1f else 0.45f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel(stringResource(Res.string.protection_section))
                SettingGroup {
                    state.packs.forEachIndexed { i, pack ->
                        PackRow(
                            status = pack,
                            lang = lang,
                            tone = if (pack.enabledByAnyone) IconTone.SOLID else IconTone.GRAY,
                            expanded = pack.pack.code in state.expanded,
                            onClick = { event(ContentProtectionEvent.PackClicked(pack.pack.code)) },
                        )
                        if (i < state.packs.lastIndex) GroupDivider()
                    }
                }
                Text(
                    text = stringResource(Res.string.protection_footer),
                    style = PolicyText.hint,
                    color = AppColors.text.tertiary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    SubscriptionBottomDialog(
        show = state.showPayWall,
        title = stringResource(Res.string.stat_quick_paywall_title),
        message = stringResource(Res.string.policy_paywall_protection),
        onConfirm = onOpenSubscription,
        onDismiss = { event(ContentProtectionEvent.PayWallDismissed) },
    )

    val error = state.error
    ErrorHaptic(error)
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = error != null,
        title = stringResource(Res.string.xatolik),
        message = error?.asText().orEmpty(),
        onDismiss = { event(ContentProtectionEvent.ErrorDismissed) },
        onButtonClick = { event(ContentProtectionEvent.ErrorDismissed) },
    )
}

/** Paket qatori: qalqon · nom · nimalar (sayt/ilova/kategoriya soni) · ochilsa tavsif. */
@Composable
private fun PackRow(status: ProtectionPackStatus, lang: String, tone: IconTone, expanded: Boolean, onClick: () -> Unit) {
    val pack = status.pack
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "packChevron")
    val subtitle = listOfNotNull(
        pack.siteCount.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_sites, it) },
        pack.packageCount.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_apps, it) },
        pack.categoryCount.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_categories, it) },
    ).joinToString(" · ").ifBlank { null }

    Column(modifier = Modifier.animateContentSize()) {
        SettingRow(
            title = pack.title(lang),
            subtitle = subtitle,
            leading = { PolicyIcon(icon = Res.drawable.shield, tone = tone, size = 44.dp) },
            onClick = onClick,
            trailing = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).rotate(rotation),
                    tint = AppColors.icon.secondary,
                )
            },
        )
        if (expanded) {
            val description = pack.desc(lang)
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = PolicyText.subtitle,
                    color = AppColors.text.tertiary,
                    modifier = Modifier.padding(start = 72.dp, end = 16.dp, bottom = 14.dp),
                )
            }
        }
    }
}

/** Farzand yoki ikkinchi ota-ona yoqqan — men o'chira olmayman. */
@Composable
private fun OthersBanner(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(AppColors.bg.accentWarningContainer).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(20.dp), tint = AppColors.icon.accentWarning)
        Text(text = text, style = PolicyText.bannerStrong, color = AppColors.text.primary, maxLines = 2)
    }
}
