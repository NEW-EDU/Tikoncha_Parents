package uz.tikoncha_parent.presentation.policy.special

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.blocklist
import tikoncha_parents.composeapp.generated.resources.camera
import tikoncha_parents.composeapp.generated.resources.chat_dialog_icon
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.location
import tikoncha_parents.composeapp.generated.resources.parent_special_conv_desc
import tikoncha_parents.composeapp.generated.resources.parent_special_conv_title
import tikoncha_parents.composeapp.generated.resources.parent_special_footer
import tikoncha_parents.composeapp.generated.resources.parent_special_incognito_desc
import tikoncha_parents.composeapp.generated.resources.parent_special_incognito_title
import tikoncha_parents.composeapp.generated.resources.parent_special_info_1
import tikoncha_parents.composeapp.generated.resources.parent_special_info_2
import tikoncha_parents.composeapp.generated.resources.parent_special_info_3
import tikoncha_parents.composeapp.generated.resources.parent_special_info_title
import tikoncha_parents.composeapp.generated.resources.parent_special_nsfw_desc
import tikoncha_parents.composeapp.generated.resources.parent_special_nsfw_title
import tikoncha_parents.composeapp.generated.resources.parent_special_paywall
import tikoncha_parents.composeapp.generated.resources.parent_special_title
import tikoncha_parents.composeapp.generated.resources.parent_special_zones_desc
import tikoncha_parents.composeapp.generated.resources.parent_special_zones_title
import tikoncha_parents.composeapp.generated.resources.stat_quick_paywall_title
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.domain.model.safety.SafetySwitch
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.DelayedLoadingIndicator
import uz.tikoncha_parent.presentation.base.SubscriptionBottomDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.haptics.ErrorHaptic
import uz.tikoncha_parent.presentation.policy.components.GroupDivider
import uz.tikoncha_parent.presentation.policy.components.IconTone
import uz.tikoncha_parent.presentation.policy.components.PolicyIcon
import uz.tikoncha_parent.presentation.policy.components.PolicyInfoBox
import uz.tikoncha_parent.presentation.policy.components.PolicySwitch
import uz.tikoncha_parent.presentation.policy.components.PolicyText
import uz.tikoncha_parent.presentation.policy.components.SettingGroup
import uz.tikoncha_parent.presentation.policy.components.SettingRow
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

/** Maxsus himoya: farzand telefonida ota-ona yoqadigan himoyalar (nomaqbul rasm, xavfli yozişma, yaşirin rejim, xavfli joylar). */
@OptIn(InternalVoyagerApi::class)
class SpecialProtectionScreen(private val childId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        val viewModel = koinScreenModel<SpecialProtectionViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { viewModel.start(childId) }
        BackHandler(true) { navigator.pop() }

        val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
        Column(modifier = Modifier.fillMaxSize().background(AppColors.bg.page).then(systemBars.modifier)) {
            CustomHeader(title = stringResource(Res.string.parent_special_title), showBackButton = true, onBackClick = { navigator.pop() })
            if (!state.loaded) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DelayedLoadingIndicator(color = AppColors.action.primary)
                }
                return@Column
            }
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = ContainerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(2.dp))
                PolicyInfoBox(
                    title = stringResource(Res.string.parent_special_info_title),
                    lines = listOf(
                        stringResource(Res.string.parent_special_info_1),
                        stringResource(Res.string.parent_special_info_2),
                        stringResource(Res.string.parent_special_info_3),
                    ),
                )
                SettingGroup {
                    ROWS.forEachIndexed { i, row ->
                        SettingRow(
                            title = stringResource(row.title),
                            subtitle = stringResource(row.desc),
                            leading = { PolicyIcon(icon = row.icon, tone = if (state.isOn(row.switch)) IconTone.SOLID else IconTone.GRAY, size = 44.dp) },
                            trailing = {
                                PolicySwitch(
                                    checked = state.isOn(row.switch),
                                    onCheckedChange = { viewModel.toggle(row.switch, it) },
                                    busy = state.busy == row.switch,
                                )
                            },
                        )
                        if (i < ROWS.lastIndex) GroupDivider()
                    }
                }
                Text(
                    text = stringResource(Res.string.parent_special_footer),
                    style = PolicyText.hint,
                    color = AppColors.text.tertiary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        SubscriptionBottomDialog(
            show = state.showPayWall,
            title = stringResource(Res.string.stat_quick_paywall_title),
            message = stringResource(Res.string.parent_special_paywall),
            onConfirm = { viewModel.dismissPayWall(); navigator.push(SubscriptionPaymentScreen()) },
            onDismiss = viewModel::dismissPayWall,
        )
        val error = state.error
        ErrorHaptic(error)
        CustomDialog(
            painter = painterResource(Res.drawable.dialog_failed),
            show = error != null,
            title = stringResource(Res.string.xatolik),
            message = error?.asText().orEmpty(),
            onDismiss = viewModel::dismissError,
            onButtonClick = viewModel::dismissError,
        )
    }

    private class Row(val switch: SafetySwitch, val title: StringResource, val desc: StringResource, val icon: DrawableResource)

    private companion object {
        val ROWS = listOf(
            Row(SafetySwitch.IMAGE, Res.string.parent_special_nsfw_title, Res.string.parent_special_nsfw_desc, Res.drawable.camera),
            Row(SafetySwitch.CONVERSATION, Res.string.parent_special_conv_title, Res.string.parent_special_conv_desc, Res.drawable.chat_dialog_icon),
            Row(SafetySwitch.INCOGNITO, Res.string.parent_special_incognito_title, Res.string.parent_special_incognito_desc, Res.drawable.blocklist),
            Row(SafetySwitch.DANGER_ZONES, Res.string.parent_special_zones_title, Res.string.parent_special_zones_desc, Res.drawable.location),
        )
    }
}
