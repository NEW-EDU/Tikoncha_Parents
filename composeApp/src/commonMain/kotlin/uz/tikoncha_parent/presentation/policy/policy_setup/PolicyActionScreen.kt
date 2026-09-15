package uz.tikoncha_parent.presentation.policy.policy_setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bloklash_rejimi_tanlang
import tikoncha_parents.composeapp.generated.resources.davom_etish
import tikoncha_parents.composeapp.generated.resources.ilovalarni_belgilash
import tikoncha_parents.composeapp.generated.resources.obuna_dialog_message
import tikoncha_parents.composeapp.generated.resources.oq_royhat
import tikoncha_parents.composeapp.generated.resources.premium
import tikoncha_parents.composeapp.generated.resources.qora_ro_yxat
import tikoncha_parents.composeapp.generated.resources.secret_cod_method_icon
import tikoncha_parents.composeapp.generated.resources.tanlangan_ilovalar_bloklanadi
import tikoncha_parents.composeapp.generated.resources.tanlangan_ilovalar_ochiq
import uz.tikoncha_parent.App
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.CustomRadio
import uz.tikoncha_parent.presentation.base.SubscriptionBottomDialog
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedEvent
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedModel
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedState
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.ui.CardCornerRadius
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.SpaceLarge
import uz.tikoncha_parent.ui.SpaceSmall
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars


class PolicyActionScreen : Screen {



    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current?:return
        val sharedViewModel = koinViewModel<PolicySharedModel>()
        val sharedEvent = sharedViewModel::onEvent
        val sharedState by sharedViewModel.state.collectAsStateWithLifecycle()

        PolicyActionUi(
            navigator = navigator,
            sharedState = sharedState,
            sharedEvent = sharedEvent
        )

    }

}


@Composable
fun PolicyActionUi(
    navigator: Navigator?,
    sharedState: PolicySharedState,
    sharedEvent: (PolicySharedEvent) -> Unit
) {
    val isDENY = sharedState.policyAction == PolicyAction.DENY
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.page
    )

    var showAllowPremium by remember { mutableStateOf(false) }

    // FREE tarifda oq ro'yxat pullik — server 403 kutilmaydi, darhol obuna oynasi (§5.2).
    val onAllowClick: () -> Unit = {
        if (sharedState.canSelectAllowMode) {
            sharedEvent(PolicySharedEvent.SetPolicyAction(PolicyAction.ALLOW))
        } else {
            showAllowPremium = true
        }
    }

    SubscriptionBottomDialog(
        show = showAllowPremium,
        message = stringResource(Res.string.obuna_dialog_message),
        onConfirm = {
            showAllowPremium = false
            navigator?.push(SubscriptionPaymentScreen())
        },
        onDismiss = { showAllowPremium = false },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page),
    ) {
        CustomHeader(
            showBackButton = true,
            onBackClick = {
                navigator?.pop()
            },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Image(
                painter = painterResource(Res.drawable.secret_cod_method_icon),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .width(180.dp)
                    .height(220.dp),
            )
            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.bloklash_rejimi_tanlang),
                style = AppTypography.headlineSmSemiBold,
                color = AppColors.text.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            SpaceSmall()
            Text(
                text = stringResource(Res.string.ilovalarni_belgilash),
                style = AppTypography.emphasizedLgMedium,
                color = AppColors.text.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(46.dp))

            // ── Qora ro'yxat ─────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.section.tertiary, RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        if (isDENY) AppColors.border.accentEmphasis else AppColors.border.secondarySubtle,
                        RoundedCornerShape(20.dp),
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .singleClick {
                            sharedEvent(PolicySharedEvent.SetPolicyAction(PolicyAction.DENY))
                        }
                ) {
                    Text(
                        text = stringResource(Res.string.qora_ro_yxat),
                        style = AppTypography.titleMdSemiBold,
                        color = AppColors.text.primary,
                    )
                    SpaceUltraSmall()
                    Text(
                        text = stringResource(Res.string.tanlangan_ilovalar_bloklanadi),
                        style = AppTypography.emphasizedSmMedium,
                        color = AppColors.text.secondary,
                    )
                }
                CustomRadio(
                    checked = isDENY,
                    onChecked = { sharedEvent(PolicySharedEvent.SetPolicyAction(PolicyAction.DENY)) },
                )
            }
            Spacer(Modifier.height(8.dp))

            // ── Oq ro'yxat ───────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.section.tertiary, RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        if (!isDENY) AppColors.border.accentEmphasis else AppColors.border.secondarySubtle,
                        RoundedCornerShape(20.dp),
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .singleClick { onAllowClick() },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(Res.string.oq_royhat),
                            style = AppTypography.titleMdSemiBold,
                            color = AppColors.text.primary,
                        )
                        if (!sharedState.canSelectAllowMode) {
                            SpaceSmall()
                            Text(
                                text = stringResource(Res.string.premium),
                                style = AppTypography.bodySmMedium,
                                color = AppColors.text.accentWarning,
                                modifier = Modifier
                                    .background(AppColors.bg.accentWarningContainer, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                    SpaceUltraSmall()
                    Text(
                        text = stringResource(Res.string.tanlangan_ilovalar_ochiq),
                        style = AppTypography.emphasizedSmMedium,
                        color = AppColors.text.secondary,
                    )
                }
                CustomRadio(
                    checked = !isDENY,
                    onChecked = { onAllowClick() },
                )
            }
        }
        Spacer(Modifier.weight(1f))
        CustomButtonNew(
            text = stringResource(Res.string.davom_etish),
            onClick = { navigator?.pop() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )
        SpaceLarge()
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        PolicyActionUi(
            navigator = null,
            sharedState = PolicySharedState(),
            sharedEvent = {},
        )
    }
}