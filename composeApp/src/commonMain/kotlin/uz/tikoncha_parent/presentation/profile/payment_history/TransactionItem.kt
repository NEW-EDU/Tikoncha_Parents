package uz.tikoncha_parent.presentation.profile.payment_history.components

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import tikoncha_parents.composeapp.generated.resources.ap_history_click
import tikoncha_parents.composeapp.generated.resources.ap_autopay
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.BrandMark
import uz.tikoncha_parent.domain.model.autopay.CardVendor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.coin
import tikoncha_parents.composeapp.generated.resources.coins
import tikoncha_parents.composeapp.generated.resources.coins_profile
import tikoncha_parents.composeapp.generated.resources.foydalanuvchi_nomi_yoq
import tikoncha_parents.composeapp.generated.resources.hali_royxatdan_otmagan
import tikoncha_parents.composeapp.generated.resources.icon_coins
import tikoncha_parents.composeapp.generated.resources.nomalum_tolov
import tikoncha_parents.composeapp.generated.resources.obuna
import tikoncha_parents.composeapp.generated.resources.obuna_oylik
import tikoncha_parents.composeapp.generated.resources.obuna_yillik
import tikoncha_parents.composeapp.generated.resources.som
import tikoncha_parents.composeapp.generated.resources.status_bekor_qilindi
import tikoncha_parents.composeapp.generated.resources.status_kutilmoqda
import tikoncha_parents.composeapp.generated.resources.status_muvaffaqiyatsiz
import tikoncha_parents.composeapp.generated.resources.status_nomalum
import tikoncha_parents.composeapp.generated.resources.status_tolandi
import tikoncha_parents.composeapp.generated.resources.tanga_format
import tikoncha_parents.composeapp.generated.resources.tanga_toplami
import tikoncha_parents.composeapp.generated.resources.telegrams_star
import tikoncha_parents.composeapp.generated.resources.tikoncha_plan_format
import tikoncha_parents.composeapp.generated.resources.tugaydi_format
import uz.tikoncha_parent.common.DateTimeUtil.toUIData
import uz.tikoncha_parent.common.DateTimeUtil.toUiTime
import uz.tikoncha_parent.common.Util.toCurrency
import uz.tikoncha_parent.domain.model.transaction.PlanDuration
import uz.tikoncha_parent.domain.model.transaction.PurchaseType
import uz.tikoncha_parent.domain.model.transaction.Transaction
import uz.tikoncha_parent.domain.model.transaction.TransactionStatus
import uz.tikoncha_parent.presentation.base.DashedDivider
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val isCancelledOrFailed = transaction.status == TransactionStatus.CANCELLED ||
            transaction.status == TransactionStatus.FAILED

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.bg.surface)
            .then(if (onClick != null) Modifier.singleClick(onClick = onClick) else Modifier)
            .padding(14.dp)
    ) {
        TransactionTopRow(transaction, isCancelledOrFailed)

        Spacer(Modifier.height(12.dp))
        DashedDivider()
        Spacer(Modifier.height(12.dp))

        TransactionUserRow(transaction)

        if (!transaction.isUserRegistered) {
            Spacer(Modifier.height(10.dp))
            ChipBox(
                text = stringResource(Res.string.hali_royxatdan_otmagan),
                textColor = AppColors.text.accentWarning,
                backgroundColor = AppColors.text.accentWarning.copy(alpha = 0.14f),
            )
        }

        if (transaction.purchaseType == PurchaseType.SUBSCRIPTION &&
            transaction.expiredAt != null
        ) {
            Spacer(Modifier.height(10.dp))
            ChipBox(
                text = stringResource(
                    Res.string.tugaydi_format,
                    transaction.expiredAt.toUIData()
                ),
                textColor = AppColors.text.accentEmphasis,
                backgroundColor = AppColors.bg.primary.copy(alpha = 0.12f),
            )
        }

        if (transaction.purchaseType == PurchaseType.SUBSCRIPTION) {
            Spacer(Modifier.height(10.dp))
            PaymentSource(transaction)
        }

        Spacer(Modifier.height(12.dp))
        TransactionBottomRow(transaction)
    }
}

/** "Avto-to'lov · [HUMO] •• 4821" yoki "Click · bir martalik". */
@Composable
private fun PaymentSource(transaction: Transaction) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (transaction.isAutopay) {
            Text(text = stringResource(Res.string.ap_autopay) + " · ", style = AppTypography.bodyMdMedium, color = AppColors.text.secondary)
            val mask = transaction.cardMask.orEmpty()
            val vendor = CardVendor.of(mask.substringBefore(" "))
            if (vendor != CardVendor.OTHER) {
                BrandMark(vendor, 14.dp)
                Spacer(Modifier.width(5.dp))
            }
            Text(text = "•• " + mask.takeLast(4), style = AppTypography.bodyMdMedium, color = AppColors.text.secondary)
        } else {
            Text(text = stringResource(Res.string.ap_history_click), style = AppTypography.bodyMdMedium, color = AppColors.text.secondary)
        }
    }
}

@Composable
private fun TransactionBottomRow(transaction: Transaction) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatCreatedAt(transaction),
            style = AppTypography.bodyMdRegular,
            color = AppColors.text.tertiary,
            modifier = Modifier.weight(1f),
        )
        StatusPill(status = transaction.status)
    }
}

@Composable
private fun TransactionTopRow(
    transaction: Transaction,
    amountFaded: Boolean,
) {
    val (title, subtitle) = titleAndSubtitle(transaction)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TypeIcon(purchaseType = transaction.purchaseType)
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.titleSmSemiBold,
                color = AppColors.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AppTypography.bodyMdRegular,
                    color = AppColors.text.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            val amountColor = if (amountFaded) {
                AppColors.text.disabledTertiary
            } else {
                AppColors.text.accentEmphasis
            }
            Text(
                text = transaction.amount.toCurrency(),
                style = AppTypography.titleSmSemiBold,
                color = amountColor,
                textDecoration = if (amountFaded) TextDecoration.LineThrough else TextDecoration.None,
            )
            Text(
                text = stringResource(Res.string.som),
                style = AppTypography.bodySmRegular,
                color = if (amountFaded) AppColors.text.disabledTertiary else AppColors.text.tertiary,
            )
        }
    }
}

@Composable
private fun TypeIcon(purchaseType: PurchaseType) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.bg.primary.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        // Drawable resurslari qo'shilgandan keyin Image()'ga almashtiriladi:
        // Image(painter = painterResource(Res.drawable.ic_crown), ...)
        val painter = when (purchaseType) {
            PurchaseType.SUBSCRIPTION -> painterResource(Res.drawable.telegrams_star)
            PurchaseType.COINS -> painterResource(Res.drawable.coins_profile)
            PurchaseType.UNKNOWN -> painterResource(Res.drawable.coins_profile)
        }
        Icon(
            painter = painter,
            contentDescription = "",
            tint = AppColors.icon.accentPrimary,
            modifier = Modifier
                .size(16.dp)
        )
    }
}


@Composable
private fun TransactionUserRow(transaction: Transaction) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        UserAvatar(transaction = transaction)
        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            val name = transaction.userFullName
            if (name != null) {
                Text(
                    text = name,
                    style = AppTypography.bodyLgMedium,
                    color = AppColors.text.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    text = stringResource(Res.string.foydalanuvchi_nomi_yoq),
                    style = AppTypography.bodyLgRegular.copy(fontStyle = FontStyle.Italic),
                    color = AppColors.text.tertiary,
                )
            }

            if (transaction.hasUserPhone) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = transaction.userPhone,
                    style = AppTypography.bodyMdRegular,
                    color = AppColors.text.tertiary,
                )
            }
        }
    }
}

@Composable
private fun UserAvatar(transaction: Transaction) {
    val name = transaction.userFullName
    val initials = name
        ?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
        ?.take(2)
        ?.joinToString("")
        ?.ifEmpty { null }

    val bg = if (name != null) {
        AppColors.bg.primary.copy(alpha = 0.12f)
    } else {
        AppColors.bg.tertiary
    }
    val fg = if (name != null) {
        AppColors.text.accentEmphasis
    } else {
        AppColors.text.tertiary
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials ?: "?",
            style = AppTypography.bodyMdMedium,
            color = fg,
        )
    }
}

@Composable
private fun StatusPill(status: TransactionStatus) {
    val (label, color) = when (status) {
        TransactionStatus.PENDING ->
            stringResource(Res.string.status_kutilmoqda) to AppColors.text.accentWarning
        TransactionStatus.COMPLETED ->
            stringResource(Res.string.status_tolandi) to AppColors.text.accentSuccess
        TransactionStatus.CANCELLED ->
            stringResource(Res.string.status_bekor_qilindi) to AppColors.text.tertiary
        TransactionStatus.FAILED ->
            stringResource(Res.string.status_muvaffaqiyatsiz) to AppColors.text.accentDanger
        TransactionStatus.UNKNOWN -> {
            stringResource(Res.string.status_nomalum) to AppColors.text.tertiary
        }
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = AppTypography.bodySmMedium,
            color = color,
        )
    }
}

/* ===== titleAndSubtitle endi @Composable ===== */
@Composable
private fun titleAndSubtitle(transaction: Transaction): Pair<String, String> {
    return when (transaction.purchaseType) {
        PurchaseType.SUBSCRIPTION -> {
            val planName = transaction.planName ?: "PLUS"
            val title = stringResource(Res.string.tikoncha_plan_format, planName)
            val subtitle = when (transaction.planDuration) {
                PlanDuration.MONTHLY -> stringResource(Res.string.obuna_oylik)
                PlanDuration.ANNUAL -> stringResource(Res.string.obuna_yillik)
                else -> stringResource(Res.string.obuna)
            }
            title to subtitle
        }
        PurchaseType.COINS -> {
            val title = stringResource(Res.string.tanga_format, transaction.coins.toString())
            val subtitle = stringResource(Res.string.tanga_toplami)
            title to subtitle
        }
        PurchaseType.UNKNOWN -> {
            stringResource(Res.string.nomalum_tolov) to ""
        }
    }
}

private fun formatCreatedAt(transaction: Transaction): String {
    val ldt = transaction.createdAt ?: return ""
    return "${ldt.toUIData()} · ${ldt.toUiTime()}"
}

@Composable
private fun ChipBox(
    text: String,
    textColor: Color,
    backgroundColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = AppTypography.bodyMdMedium,
            color = textColor,
        )
    }
}

private fun sampleTx(
    id: String = "id-1",
    purchaseType: PurchaseType = PurchaseType.SUBSCRIPTION,
    planDuration: PlanDuration? = PlanDuration.ANNUAL,
    status: TransactionStatus = TransactionStatus.COMPLETED,
    amount: Long = 199_000L,
    coins: Long = 600L,
    firstName: String = "Akmal",
    lastName: String = "Karimov",
    phone: String = "+998 99 123 45 67",
    isRegistered: Boolean = true,
    expiredAt: LocalDateTime? = null,
    createdAt: LocalDateTime = LocalDateTime(2026, 5, 14, 10, 8),
): Transaction = Transaction(
    id = id,
    userId = "user-1",
    childUserId = null,
    userFirstName = firstName,
    userLastName = lastName,
    userPhone = phone,
    isUserRegistered = isRegistered,
    merchantTransId = "SUB-xxx-001",
    amount = amount,
    originalAmount = null,
    coins = coins,
    status = status,
    purchaseType = purchaseType,
    planName = if (purchaseType == PurchaseType.SUBSCRIPTION) "PLUS" else null,
    planDuration = if (purchaseType == PurchaseType.SUBSCRIPTION) planDuration else null,
    expiredAt = expiredAt,
    createdAt = createdAt,
)



/* ---------- INDIVIDUAL PREVIEWS ---------- */

@Preview
@Composable
private fun Preview_Subscription_Completed_WithExpiry() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Box(
            modifier = Modifier
                .background(AppColors.bg.page)
                .padding(16.dp),
        ) {
            TransactionItem(
                transaction = sampleTx(
                    status = TransactionStatus.COMPLETED,
                    planDuration = PlanDuration.ANNUAL,
                    expiredAt = LocalDateTime(2027, 5, 14, 10, 8),
                )
            )
        }
    }
}

@Preview
@Composable
private fun Preview_Subscription_Pending_NotRegistered() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Box(
            modifier = Modifier
                .background(AppColors.bg.page)
                .padding(16.dp),
        ) {
            TransactionItem(
                transaction = sampleTx(
                    status = TransactionStatus.PENDING,
                    planDuration = PlanDuration.MONTHLY,
                    amount = 29_000L,
                    firstName = "",
                    lastName = "",
                    phone = "+998 99 555 12 34",
                    isRegistered = false,
                )
            )
        }
    }
}

@Preview
@Composable
private fun Preview_Coins_Completed() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Box(
            modifier = Modifier
                .background(AppColors.bg.page)
                .padding(16.dp),
        ) {
            TransactionItem(
                transaction = sampleTx(
                    purchaseType = PurchaseType.COINS,
                    planDuration = null,
                    status = TransactionStatus.COMPLETED,
                    amount = 12_000L,
                    coins = 600L,
                )
            )
        }
    }
}

@Preview
@Composable
private fun Preview_Subscription_Failed() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Box(
            modifier = Modifier
                .background(AppColors.bg.page)
                .padding(16.dp),
        ) {
            TransactionItem(
                transaction = sampleTx(
                    status = TransactionStatus.FAILED,
                    planDuration = PlanDuration.ANNUAL,
                    firstName = "Diyor",
                    lastName = "Mansurov",
                    phone = "+998 90 222 33 44",
                )
            )
        }
    }
}

@Preview
@Composable
private fun Preview_Coins_Cancelled() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Box(
            modifier = Modifier
                .background(AppColors.bg.page)
                .padding(16.dp),
        ) {
            TransactionItem(
                transaction = sampleTx(
                    purchaseType = PurchaseType.COINS,
                    planDuration = null,
                    status = TransactionStatus.CANCELLED,
                    amount = 6_000L,
                    coins = 300L,
                )
            )
        }
    }
}

@Preview
@Composable
private fun Preview_NoName_NoPhone() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Box(
            modifier = Modifier
                .background(AppColors.bg.page)
                .padding(16.dp),
        ) {
            TransactionItem(
                transaction = sampleTx(
                    purchaseType = PurchaseType.COINS,
                    planDuration = null,
                    status = TransactionStatus.PENDING,
                    amount = 43_500L,
                    coins = 500L,
                    firstName = "",
                    lastName = "",
                    phone = "",
                    isRegistered = false,
                )
            )
        }
    }
}

/* ---------- COMBINED PREVIEW ---------- */

@Preview
@Composable
private fun Preview_AllVariants_Light() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Column(
            modifier = Modifier
                .background(AppColors.bg.page)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TransactionItem(
                transaction = sampleTx(
                    status = TransactionStatus.COMPLETED,
                    expiredAt = LocalDateTime(2027, 5, 14, 10, 8),
                )
            )
            TransactionItem(
                transaction = sampleTx(
                    id = "id-2",
                    purchaseType = PurchaseType.COINS,
                    planDuration = null,
                    status = TransactionStatus.COMPLETED,
                    amount = 12_000L,
                )
            )
            TransactionItem(
                transaction = sampleTx(
                    id = "id-3",
                    status = TransactionStatus.PENDING,
                    planDuration = PlanDuration.MONTHLY,
                    amount = 29_000L,
                    firstName = "",
                    lastName = "",
                    isRegistered = false,
                )
            )
            TransactionItem(
                transaction = sampleTx(
                    id = "id-4",
                    status = TransactionStatus.FAILED,
                    firstName = "Diyor",
                    lastName = "Mansurov",
                )
            )
            TransactionItem(
                transaction = sampleTx(
                    id = "id-5",
                    purchaseType = PurchaseType.COINS,
                    planDuration = null,
                    status = TransactionStatus.CANCELLED,
                    amount = 6_000L,
                    coins = 300L,
                )
            )
        }
    }
}

@Preview
@Composable
private fun Preview_AllVariants_Dark() {
    TikonchaParentTheme(ThemeMode.DARK) {
        Column(
            modifier = Modifier
                .background(AppColors.bg.page)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TransactionItem(
                transaction = sampleTx(
                    status = TransactionStatus.COMPLETED,
                    expiredAt = LocalDateTime(2027, 5, 14, 10, 8),
                )
            )
            TransactionItem(
                transaction = sampleTx(
                    id = "id-2",
                    status = TransactionStatus.PENDING,
                    planDuration = PlanDuration.MONTHLY,
                    amount = 29_000L,
                    isRegistered = false,
                )
            )
            TransactionItem(
                transaction = sampleTx(
                    id = "id-3",
                    status = TransactionStatus.FAILED,
                )
            )
        }
    }
}