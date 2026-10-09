package uz.tikoncha_parent.presentation.profile.subscription.subscription_info

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.domain.model.autopay.AutopayInfo
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomSwitch
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.CardMask
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.RadioMark
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.formatDate
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.sumText
import uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm.Group
import uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm.InfoRow
import uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm.RowDivider
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * "Obuna" ekranidagi avto-to'lov bo'limi: holat, keyingi to'lov, karta; to'lov o'tmasa banner;
 * o'chirish pastdan varaq bilan tasdiqlanadi (to'langan kunlar saqlanishi aniq aytiladi).
 */
@Composable
fun AutopaySection(state: SubscriptionState, event: (SubscriptionEvent) -> Unit) {
    val info = state.autopay ?: return
    if (!info.available && !info.enabled) return

    if (info.otherPayer) {
        // Farzandning boshqa ota-onasi to'laydi — karta va summa ko'rinmaydi, boshqaruv yo'q (maket P7)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Group {
                InfoRow(Icons.Rounded.Autorenew, stringResource(Res.string.ap_autopay), sub = stringResource(if (info.payerIsChild) Res.string.ap_child_pays else Res.string.ap_other_payer)) {
                    Text(stringResource(Res.string.ap_on), style = AppTypography.titleSmSemiBold, color = AppColors.action.primary)
                }
            }
            Text(stringResource(if (info.payerIsChild) Res.string.ap_child_pays_note else Res.string.ap_other_payer_note), style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary, modifier = Modifier.padding(horizontal = 4.dp))
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (info.pastDue) FailureBanner(info, busy = state.autopayBusy, event = event)

        Group {
            InfoRow(Icons.Rounded.Autorenew, stringResource(Res.string.ap_autopay), sub = if (info.enabled) null else stringResource(Res.string.ap_off)) {
                CustomSwitch(
                    checked = info.enabled,
                    enabled = !state.autopayBusy,
                    showCheckIcon = true,
                    onCheckedChange = { event(SubscriptionEvent.AutopaySwitch(it)) },
                )
            }
            if (info.enabled) {
                info.nextChargeAt?.let { next ->
                    RowDivider()
                    InfoRow(Icons.Rounded.CalendarMonth, stringResource(Res.string.ap_next_payment), sub = info.amount?.let { sumText(it) }) {
                        Text(formatDate(next), style = AppTypography.titleSmMedium, color = AppColors.text.primary)
                    }
                }
                RowDivider()
                InfoRow(Icons.Rounded.CreditCard, stringResource(Res.string.ap_card), onClick = { event(SubscriptionEvent.OpenCardPicker) }) {
                    info.card?.let { CardMask(it, color = AppColors.text.primary) }
                }
            }
        }

        Group {
            InfoRow(Icons.Rounded.Wallet, stringResource(Res.string.ap_my_cards), onClick = { event(SubscriptionEvent.OpenCards) }) {}
            RowDivider()
            InfoRow(Icons.AutoMirrored.Rounded.ReceiptLong, stringResource(Res.string.tolovlar_tarixi), onClick = { event(SubscriptionEvent.OpenHistory) }) {}

        }

        if (info.enabled) {
            Text(
                text = stringResource(Res.string.ap_turn_off),
                style = AppTypography.titleSmSemiBold,
                color = AppColors.text.accentDanger,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = !state.autopayBusy) { event(SubscriptionEvent.AutopaySwitch(false)) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }

    if (state.confirmDisable) DisableSheet(info, event)
    if (state.showCardPicker) ChangeCardSheet(state, event)

    CustomDialog(
        show = state.autopayError != null || state.retryDeclinedText != null,
        painter = painterResource(Res.drawable.dialog_failed),
        title = if (state.retryDeclinedText != null) stringResource(Res.string.ap_payment_failed) else stringResource(Res.string.xatolik),
        message = state.retryDeclinedText?.ifBlank { stringResource(Res.string.ap_payment_failed_generic) }
            ?: state.autopayError?.asText().orEmpty(),
        buttonText = stringResource(Res.string.yopish),
        onDismiss = { event(SubscriptionEvent.AutopayErrorDismissed) },
        onButtonClick = { event(SubscriptionEvent.AutopayErrorDismissed) },
    )
}

@Composable
private fun FailureBanner(info: AutopayInfo, busy: Boolean, event: (SubscriptionEvent) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(AppColors.bg.accentDanger.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .border(1.dp, AppColors.bg.accentDanger.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(14.dp),
    ) {
        Row {
            Icon(Icons.Rounded.WarningAmber, null, tint = AppColors.text.accentDanger, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(stringResource(Res.string.ap_payment_failed), style = AppTypography.titleSmSemiBold, color = AppColors.text.primary)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listOfNotNull(
                        info.lastErrorText?.takeIf { it.isNotBlank() },
                        stringResource(Res.string.ap_retry_note, info.failCount.coerceAtLeast(1)),
                    ).joinToString(" "),
                    style = AppTypography.bodyMdMedium, color = AppColors.text.secondary,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomButton(
                text = stringResource(Res.string.ap_retry_now),
                enabled = !busy,
                onClick = { event(SubscriptionEvent.RetryCharge) },
                modifier = Modifier.weight(1f).height(42.dp),
            )
            OutlinedButton(
                onClick = { event(SubscriptionEvent.OpenCardPicker) },
                enabled = !busy,
                border = BorderStroke(1.dp, AppColors.border.secondary.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f).height(42.dp),
            ) {
                Text(stringResource(Res.string.ap_other_card), style = AppTypography.titleSmSemiBold, color = AppColors.text.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisableSheet(info: AutopayInfo, event: (SubscriptionEvent) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { event(SubscriptionEvent.DismissDisable) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.bg.elevated,
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            Box(Modifier.size(52.dp).background(AppColors.bg.accentDanger.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PowerSettingsNew, null, tint = AppColors.text.accentDanger, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(stringResource(Res.string.ap_off_title), style = AppTypography.titleLgSemiBold, color = AppColors.text.primary)
            Spacer(Modifier.height(8.dp))
            Text(
                text = info.paidUntil?.let { stringResource(Res.string.ap_off_body, formatDate(it)) } ?: stringResource(Res.string.ap_off_body_no_date),
                style = AppTypography.titleSmMedium, color = AppColors.text.secondary,
            )
            Spacer(Modifier.height(20.dp))
            CustomButton(
                text = stringResource(Res.string.ap_off_confirm),
                color = AppColors.action.accentDanger,
                onClick = { event(SubscriptionEvent.ConfirmDisable) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { event(SubscriptionEvent.DismissDisable) },
                border = BorderStroke(1.dp, AppColors.border.secondary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text(stringResource(Res.string.ap_keep_on), style = AppTypography.titleSmSemiBold, color = AppColors.text.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeCardSheet(state: SubscriptionState, event: (SubscriptionEvent) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { event(SubscriptionEvent.DismissCardPicker) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.bg.elevated,
    ) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 28.dp)) {
            Text(stringResource(Res.string.ap_pick_card), style = AppTypography.titleLgSemiBold, color = AppColors.text.primary, modifier = Modifier.padding(4.dp))
            Spacer(Modifier.height(12.dp))
            Group {
                state.cards.forEachIndexed { i, card ->
                    if (i > 0) RowDivider()
                    Row(
                        Modifier.fillMaxWidth().clickable { event(SubscriptionEvent.PickCard(card.id)) }.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CardMask(card, Modifier.weight(1f), color = AppColors.text.primary)
                        RadioMark(card.id == state.autopay?.card?.id)
                    }
                }
                if (state.cards.isNotEmpty()) RowDivider()
                Row(
                    Modifier.fillMaxWidth().clickable { event(SubscriptionEvent.AddCard) }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Add, null, tint = AppColors.action.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(Res.string.ap_add_new_card), style = AppTypography.titleSmSemiBold, color = AppColors.action.primary)
                }
            }
        }
    }
}
