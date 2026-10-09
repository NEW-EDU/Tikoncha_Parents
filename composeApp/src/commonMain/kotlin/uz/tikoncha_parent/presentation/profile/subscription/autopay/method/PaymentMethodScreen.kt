package uz.tikoncha_parent.presentation.profile.subscription.autopay.method

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ap_card_sub
import tikoncha_parents.composeapp.generated.resources.ap_card_title
import tikoncha_parents.composeapp.generated.resources.ap_child_no_plus
import tikoncha_parents.composeapp.generated.resources.ap_child_plus_until
import tikoncha_parents.composeapp.generated.resources.ap_click_sub
import tikoncha_parents.composeapp.generated.resources.ap_continue_with
import tikoncha_parents.composeapp.generated.resources.ap_method_title
import tikoncha_parents.composeapp.generated.resources.ap_note_card
import tikoncha_parents.composeapp.generated.resources.ap_note_click
import tikoncha_parents.composeapp.generated.resources.ap_plan_line
import tikoncha_parents.composeapp.generated.resources.ap_save_percent
import tikoncha_parents.composeapp.generated.resources.ap_soon_chip
import tikoncha_parents.composeapp.generated.resources.ap_soon_row
import tikoncha_parents.composeapp.generated.resources.ap_soon_row_ios
import tikoncha_parents.composeapp.generated.resources.ic_click
import tikoncha_parents.composeapp.generated.resources.oylik
import tikoncha_parents.composeapp.generated.resources.yillik
import uz.tikoncha_parent.domain.model.SubscriptionDuration
import uz.tikoncha_parent.domain.model.autopay.PlanPeriod
import uz.tikoncha_parent.platform.isIos
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.profile.subscription.autopay.card.AutopayAddCardScreen
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.AcceptedCards
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.AccentIconTile
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.ForWhomRow
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.PayOptionRow
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.WhiteLogoTile
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.sumText
import uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm.AutopayConfirmScreen
import uz.tikoncha_parent.presentation.profile.subscription.payment.PaymentTypeScreen
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

/** Farzand haqida — barcha avto-to'lov ekranlariga beriladi. */
data class ChildArgs(val id: String?, val name: String, val avatarUrl: String?, val plusUntil: String?) : JavaSerializable

/**
 * "To'lov usuli" — Student'da tasdiqlangan dizayn (UI_DIZAYN_TOLOV/maketlar/parent_tolov.png, P2):
 * har usul o'z narxi bilan, karta Click'dan arzon bo'lsa tejash foizi, tepada "Kim uchun".
 */
class PaymentMethodScreen(
    val child: ChildArgs,
    val planId: String,
    val period: String,
    val cardAmount: Int,
    val clickAmount: Int,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val vm = koinScreenModel<PaymentMethodViewModel>()
        val state by vm.state.collectAsStateWithLifecycle()
        val planPeriod = PlanPeriod.of(period) ?: PlanPeriod.ANNUAL

        LaunchedEffect(Unit) {
            vm.onEvent(PaymentMethodEvent.Init(child.id, planId, planPeriod, cardAmount, clickAmount))
        }
        LaunchedEffect(Unit) {
            vm.effect.collect { e ->
                when (e) {
                    is PaymentMethodEffect.OpenClick -> navigator?.push(
                        PaymentTypeScreen(
                            subDuration = if (e.period == PlanPeriod.ANNUAL) SubscriptionDuration.ANNUAL else SubscriptionDuration.MONTHLY,
                            amount = e.amount,
                            planId = e.planId,
                        )
                    )
                    is PaymentMethodEffect.OpenConfirm -> navigator?.push(
                        AutopayConfirmScreen(child, e.planId, e.period.wire, e.amount)
                    )
                    is PaymentMethodEffect.OpenAddCard -> navigator?.push(
                        AutopayAddCardScreen(child, e.planId, e.period.wire, e.amount, openConfirmAfter = true)
                    )
                }
            }
        }
        PaymentMethodUi(state = state, child = child, event = vm::onEvent, onBack = { navigator?.pop() })
    }
}

@Composable
fun PaymentMethodUi(
    state: PaymentMethodState,
    child: ChildArgs,
    event: (PaymentMethodEvent) -> Unit,
    onBack: () -> Unit,
) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    val periodName = stringResource(if (state.period == PlanPeriod.ANNUAL) Res.string.yillik else Res.string.oylik)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page),
    ) {
        CustomHeader(showBackButton = true, onBackClick = onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(text = stringResource(Res.string.ap_method_title), style = AppTypography.headlineMdSemiBold, color = AppColors.text.primary)
                Spacer(Modifier.height(6.dp))
                Text(text = stringResource(Res.string.ap_plan_line, periodName), style = AppTypography.titleSmMedium, color = AppColors.text.tertiary)
            }
            ForWhomRow(
                name = child.name,
                avatarUrl = child.avatarUrl,
                subtitle = child.plusUntil?.let { stringResource(Res.string.ap_child_plus_until, it) } ?: stringResource(Res.string.ap_child_no_plus),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            )

            if (state.loading) {
                Column(Modifier.fillMaxWidth().padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AppColors.action.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.cardAvailable) {
                        PayOptionRow(
                            selected = state.selected == PayMethod.CARD,
                            onClick = { event(PaymentMethodEvent.Select(PayMethod.CARD)) },
                            leading = { AccentIconTile() },
                            title = stringResource(Res.string.ap_card_title),
                            price = sumText(state.cardAmount),
                            note = state.cardSavingPercent?.let { stringResource(Res.string.ap_save_percent, it) },
                        ) {
                            AcceptedCards(modifier = Modifier.padding(end = 8.dp))
                            Text(text = stringResource(Res.string.ap_card_sub), style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary, maxLines = 1)
                        }
                    }
                    PayOptionRow(
                        selected = state.selected == PayMethod.CLICK,
                        onClick = { event(PaymentMethodEvent.Select(PayMethod.CLICK)) },
                        leading = { WhiteLogoTile(painterResource(Res.drawable.ic_click), "Click") },
                        title = "Click",
                        price = sumText(state.clickAmount),
                    ) {
                        Text(text = stringResource(Res.string.ap_click_sub), style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary, maxLines = 1)
                    }
                    ComingSoonRow(Modifier.padding(top = 4.dp))
                }
            }
        }

        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 22.dp)) {
            val card = state.selected == PayMethod.CARD && state.cardAvailable
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (card) Icons.Rounded.Lock else Icons.Rounded.Autorenew,
                    contentDescription = null, tint = AppColors.text.tertiary, modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(if (card) Res.string.ap_note_card else Res.string.ap_note_click),
                    style = AppTypography.bodyMdMedium,
                    color = AppColors.text.tertiary,
                )
            }
            Spacer(Modifier.height(12.dp))
            CustomButton(
                text = stringResource(Res.string.ap_continue_with, sumText(state.selectedAmount)),
                enabled = !state.loading,
                onClick = { event(PaymentMethodEvent.Continue) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
        }
    }
}

@Composable
private fun ComingSoonRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.border.secondary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = Icons.Rounded.Wallet, contentDescription = null, tint = AppColors.text.tertiary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(if (isIos()) Res.string.ap_soon_row_ios else Res.string.ap_soon_row),
            style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary, modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(Res.string.ap_soon_chip),
            style = AppTypography.bodySmSemiBold,
            color = AppColors.text.tertiary,
            modifier = Modifier
                .background(AppColors.bg.tertiary, RoundedCornerShape(9.dp))
                .padding(horizontal = 9.dp, vertical = 4.dp),
        )
    }
}
