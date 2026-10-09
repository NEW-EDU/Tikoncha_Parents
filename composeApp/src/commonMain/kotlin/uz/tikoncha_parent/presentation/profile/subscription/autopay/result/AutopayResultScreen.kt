@file:OptIn(cafe.adriel.voyager.core.annotation.InternalVoyagerApi::class)

package uz.tikoncha_parent.presentation.profile.subscription.autopay.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.domain.model.autopay.CardVendor
import uz.tikoncha_parent.domain.model.autopay.PayCard
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.CardMask
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.ChildAvatar
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.formatDate
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.sumText
import uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm.Group
import uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm.RowDivider
import uz.tikoncha_parent.presentation.profile.subscription.autopay.leavePaymentFlow
import uz.tikoncha_parent.presentation.profile.subscription.autopay.method.ChildArgs
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars
import kotlin.time.Instant

/** Natija (maket P5): farzand avatari tilla halqada, nima to'landi, qachongacha, keyingi to'lov. */
class AutopayResultScreen(
    val child: ChildArgs,
    val scheduled: Boolean,
    val amount: Int,
    val paidUntilMs: Long?,
    val nextChargeMs: Long?,
    val vendor: String?,
    val last4: String?,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        AutopayResultUi(
            child = child,
            scheduled = scheduled,
            amount = amount,
            paidUntil = paidUntilMs?.let { Instant.fromEpochMilliseconds(it) },
            nextChargeAt = nextChargeMs?.let { Instant.fromEpochMilliseconds(it) },
            vendor = vendor,
            last4 = last4,
            onDone = { navigator?.let { leavePaymentFlow(it) } },
        )
    }
}

@Composable
fun AutopayResultUi(
    child: ChildArgs,
    scheduled: Boolean,
    amount: Int,
    paidUntil: Instant?,
    nextChargeAt: Instant?,
    vendor: String?,
    last4: String?,
    onDone: () -> Unit,
) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    val haptics = rememberAppHaptics()
    LaunchedEffect(Unit) { haptics.success() }
    BackHandler(true) { onDone() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(64.dp))
        Box {
            Box(
                Modifier
                    .border(3.dp, Brush.linearGradient(listOf(AppColors.action.primary.copy(alpha = 0.55f), AppColors.action.primary)), CircleShape)
                    .padding(5.dp),
            ) { ChildAvatar(child.avatarUrl, 96.dp) }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(36.dp)
                    .background(AppColors.bg.page, CircleShape)
                    .padding(3.dp)
                    .background(AppColors.action.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (scheduled) Icons.Rounded.EventAvailable else Icons.Rounded.Check,
                    contentDescription = null, tint = AppColors.icon.inverse, modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = stringResource(if (scheduled) Res.string.ap_result_scheduled_title else Res.string.ap_result_paid_title),
            style = AppTypography.headlineMdSemiBold, color = AppColors.text.primary, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        val sub = when {
            scheduled && nextChargeAt != null -> stringResource(Res.string.ap_result_scheduled_sub, formatDate(nextChargeAt))
            paidUntil != null -> stringResource(Res.string.ap_result_until_child, child.name, formatDate(paidUntil))
            else -> ""
        }
        if (sub.isNotEmpty()) {
            Text(text = sub, style = AppTypography.titleSmMedium, color = AppColors.text.secondary, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(28.dp))
        Group {
            if (!scheduled) {
                ResultRow(stringResource(Res.string.ap_result_paid)) { Text(sumText(amount), style = AppTypography.titleSmSemiBold, color = AppColors.text.primary) }
                RowDivider()
            }
            if (last4 != null) {
                ResultRow(stringResource(Res.string.ap_card)) {
                    CardMask(PayCard(id = "", maskedPan = last4, vendor = CardVendor.of(vendor), expire = "", inUse = true))
                }
                RowDivider()
            }
            nextChargeAt?.let {
                ResultRow(stringResource(Res.string.ap_next_payment)) { Text(formatDate(it), style = AppTypography.titleSmSemiBold, color = AppColors.text.primary) }
                RowDivider()
            }
            ResultRow(stringResource(Res.string.ap_autopay)) { Text(stringResource(Res.string.ap_on), style = AppTypography.titleSmSemiBold, color = AppColors.action.primary) }
        }
        Spacer(Modifier.weight(1f))
        CustomButton(
            text = stringResource(Res.string.ap_done),
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(54.dp),
        )
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun ResultRow(title: String, value: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = AppTypography.titleSmMedium, color = AppColors.text.secondary, modifier = Modifier.weight(1f))
        value()
    }
}
