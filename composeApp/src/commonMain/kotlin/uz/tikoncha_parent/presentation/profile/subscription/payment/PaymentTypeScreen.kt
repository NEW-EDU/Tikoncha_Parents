package uz.tikoncha_parent.presentation.profile.subscription.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.common.Util.toCurrency
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.PaymentStatus
import uz.tikoncha_parent.domain.model.SubscriptionDuration
import uz.tikoncha_parent.platform.isIos
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomPaymentDialog
import uz.tikoncha_parent.presentation.base.DashedDivider
import uz.tikoncha_parent.presentation.base.LegalLinksRow
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class PaymentTypeScreen(
    val subDuration: SubscriptionDuration,
    val amount: Int,
    val planId: String
) : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current

        val viewModel = koinScreenModel<PaymentViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        LaunchedEffect(Unit) {
            event(PaymentEvent.SetSubscriptionDuration(subDuration))
            event(PaymentEvent.SetAmount(amount = amount))
            event(PaymentEvent.SetPlanId(planId))
        }

        PaymentTypeScreenUi(
            navigator = navigator,
            state = state,
            event = event
        )
    }
}

@Composable
fun PaymentTypeScreenUi(
    navigator: Navigator?,
    state: PaymentState = PaymentState(),
    event: (PaymentEvent) -> Unit = {}
) {

    var selectedPayment by remember { mutableStateOf("") }

    var isSelected by remember { mutableStateOf(false) }
    if (selectedPayment.isNotEmpty()) {
        isSelected = true
    }


    val paymentLoading = state.paymentResponseState is ResponseState.Loading
    val paymentError = state.paymentResponseState.errorText()

    val applePaymentLoading = state.applePaymentResponseState is ResponseState.Loading
    val applePaymentError = state.applePaymentResponseState.errorText()
    val applePaymentSuccess = state.applePaymentResponseState is ResponseState.Success

    LoadingDialog(paymentLoading || applePaymentLoading)


    var showPromoCodeDialog by remember {
        mutableStateOf(false)
    }

    var showCreatePaymentErrorDialog by remember {
        mutableStateOf(false)
    }

    var showAppleCreatePaymentErrorDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(paymentError) {
        if (paymentError.isNotEmpty()) {
            showCreatePaymentErrorDialog = true
        }
    }

    LaunchedEffect(applePaymentError) {
        if (applePaymentError.isNotEmpty()) {
            showAppleCreatePaymentErrorDialog = true
        }
    }

    var showPaymentCompletedDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(state.paymentStatus) {
        showPaymentCompletedDialog = state.paymentStatus == PaymentStatus.COMPLETED
    }

    LaunchedEffect(applePaymentSuccess) {
        if (applePaymentSuccess && AppSettings.isTestAccount) {
            navigator?.pop()
        }
    }

    SubscribeChildBottomSheet(
        show = state.showSubscribeChildSheet,
        onConfirm = { phoneNumber ->
            event(PaymentEvent.PayWithChildPhone(phoneNumber))
        },
        onDismiss = {
            event(PaymentEvent.DismissChildSelectionSheet)
        },



    )

    PromoCodeDialog(
        show = showPromoCodeDialog,
        state = state,
        event = event,
        onDismiss = {
            showPromoCodeDialog = false
        }
    )

    CustomPaymentDialog(
        show = showPaymentCompletedDialog,
        onDismiss = {
            showPaymentCompletedDialog = false
            event(PaymentEvent.ResetPaymentResponse)
            navigator?.pop()
        },
        onButtonClick = {
            showPaymentCompletedDialog = false
            event(PaymentEvent.ResetPaymentResponse)
            navigator?.pop()
        }
    )


    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = showCreatePaymentErrorDialog,
        title = stringResource(Res.string.xatolik),
        message = paymentError,
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = {
            showCreatePaymentErrorDialog = false
            event(PaymentEvent.ResetPaymentResponse)
        },
        onButtonClick = {
            showCreatePaymentErrorDialog = false
            event(PaymentEvent.ResetPaymentResponse)
        }
    )

    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = showAppleCreatePaymentErrorDialog,
        title = stringResource(Res.string.xatolik),
        message = applePaymentError,
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = {
            showAppleCreatePaymentErrorDialog = false
            event(PaymentEvent.ResetPaymentResponse)
        },
        onButtonClick = {
            showAppleCreatePaymentErrorDialog = false
            event(PaymentEvent.ResetPaymentResponse)
        }
    )

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.elevated
    )

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFBA8837),
            Color(0xFF906019),
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page)
    ) {
        CustomHeader(
            showBackButton = true,
            onBackClick = {
                navigator?.pop()
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = ContainerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isIos() && state.isTestAccount) {
                    PaymentOption(
                        label = PaymentType.AppStore.title,
                        isSelected = state.selectedPaymentType == PaymentType.AppStore,
                        onClick = {
                            event(PaymentEvent.SetPaymentType(PaymentType.AppStore))
                        }
                    )
                }
                // Apple ko'rigi uchun raqam iPhone'da Click'ni ko'rmaydi — faqat App Store (egasining qarori)
                if (!(isIos() && state.isTestAccount)) {
                    PaymentOption(
                        isSelected = state.selectedPaymentType == PaymentType.Click,
                        onClick = {
                            event(
                                PaymentEvent.SetPaymentType(
                                    PaymentType.Click
                                )
                            )
                        }
                    )
                }

                if (!isIos()) {
                    Box(
                        modifier = Modifier.weight(1f),
                    )
                }

            }
            Spacer(Modifier.height(16.dp))

            val paymentType = when (state.selectedPaymentType) {
                PaymentType.Click -> "Click"
                PaymentType.AppStore -> "App Store"
                null -> ""
            }
            Text(
                text = stringResource(Res.string.payment_type_text, paymentType),
                style = AppTypography.bodyMdMedium,
                color = AppColors.text.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(29.dp))


            val priceText = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = AppColors.text.inverse,
                    )
                ) {
                    append(state.originalAmount.toCurrency())
                }
                append(" ")
                withStyle(
                    SpanStyle(
                        color = AppColors.text.inverse,
                    )
                ) {
                    val text = if (state.subscriptionDuration == SubscriptionDuration.MONTHLY) {
                        stringResource(Res.string.uzs_oylik)
                    } else {
                        stringResource(Res.string.uzs_yillik)
                    }
                    append(text = text)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = bgGradient,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(start = 24.dp, end = 25.dp, top = 27.dp, bottom = 7.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.tikoncha_logo),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(AppColors.text.inverse),
                            modifier = Modifier
                                .width(124.dp)
                                .height(25.dp)
                        )
                        Spacer(Modifier.width(7.dp))

                        Image(
                            painter = painterResource(Res.drawable.plus_sub),
                            contentDescription = null,
                            modifier = Modifier
                                .width(54.dp)
                                .height(28.dp)
                        )
                    }
                    Spacer(Modifier.height(40.dp))

                    Text(
                        text = stringResource(Res.string.tanlangan_obuna),
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.inverse
                    )
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = priceText,
                        style = AppTypography.titleLgSemiBold,
                        color = AppColors.text.inverse
                    )
                    Spacer(Modifier.height(16.dp))


                    val paymentStatusColor = if (state.paymentStatus == PaymentStatus.PENDING) {
                        AppColors.bg.primary
                    } else {
                        AppColors.bg.accentDanger
                    }

                    Row(
                        modifier = Modifier
                            .background(paymentStatusColor, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val paymentStatus = if (state.paymentStatus == PaymentStatus.COMPLETED) {
                            stringResource(Res.string.tolandi)
                        } else if (state.paymentStatus == PaymentStatus.PENDING) {
                            stringResource(Res.string.tolov_kutilmoqda)
                        } else {
                            stringResource(Res.string.tolanmagan)
                        }


                        if (state.paymentStatus == PaymentStatus.PENDING) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(SmallIconSize),
                                color = AppColors.text.inverse,
                                trackColor = AppColors.text.inverse.copy(alpha = 0.3f),
                            )
                            SpaceUltraSmall()
                        }

                        Icon(
                            painter = painterResource(Res.drawable.dot),
                            contentDescription = "",
                            tint = AppColors.text.inverse,
                            modifier = Modifier.size(8.dp)
                        )
                        Spacer(Modifier.width(4.dp))

                        Text(
                            text = paymentStatus,
                            style = AppTypography.bodyMdMedium,
                            color = AppColors.text.inverse,
                        )
                    }
                }

                Image(
                    painter = painterResource(Res.drawable.tikoncha_plus_new),
                    contentDescription = null,
                    modifier = Modifier
                        .width(110.dp)
                        .height(165.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.bg.surface, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.tolov_tafsilotlari),
                    style = AppTypography.titleMdSemiBold,
                    color = AppColors.text.primary
                )
                Spacer(Modifier.height(12.dp))

                DashedDivider()
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val priceText2 = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = AppColors.text.primary,
                            )
                        ) {
                            append(state.originalAmount.toCurrency())
                        }
                        append(" ")
                        withStyle(
                            SpanStyle(
                                color = AppColors.text.primary,
                            )
                        ) {
                            val text = stringResource(Res.string.uzs)
                            append(text = text)
                        }
                    }
                    Text(
                        text = "${ stringResource(Res.string.narx) }:",
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = priceText2,
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.primary
                    )
                }
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val priceDiscount = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = AppColors.text.accentEmphasis,
                            )
                        ) {
                            append(state.discountSaving.toCurrency())
                        }
                        append(" ")
                        withStyle(
                            SpanStyle(
                                color = AppColors.text.accentEmphasis,
                            )
                        ) {
                            val text = stringResource(Res.string.uzs)
                            append(text = text)
                        }
                    }
                    Text(
                        text = stringResource(Res.string.promokod),
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "- ${priceDiscount}",
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.accentEmphasis
                    )
                }
                Spacer(Modifier.height(12.dp))

                DashedDivider()
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val amount = (state.finalAmount).toCurrency()
                    val priceFinal = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = AppColors.text.accentEmphasis,
                            )
                        ) {
                            append(amount)
                        }
                        append(" ")
                        withStyle(
                            SpanStyle(
                                color = AppColors.text.accentEmphasis,
                            )
                        ) {
                            val text = stringResource(Res.string.uzs)
                            append(text = text)
                        }
                    }
                    Text(
                        text = stringResource(Res.string.jami),
                        style = AppTypography.titleLgSemiBold,
                        color = AppColors.text.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = priceFinal,
                        style = AppTypography.titleLgSemiBold,
                        color = AppColors.text.accentEmphasis
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(
                        AppColors.bg.surface,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .singleClick {
                        if (!state.promoActivated) {
                            showPromoCodeDialog = true
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = stringResource(Res.string.promokod),
                    style = AppTypography.titleMdMedium,
                    color = AppColors.text.primary,
                    modifier = Modifier.weight(1f)
                )
                SpaceMedium()

                Icon(
                    painter = painterResource(Res.drawable.arrow_down_reg),
                    contentDescription = "",
                    tint = AppColors.icon.secondary,
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    AppColors.bg.elevated,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            CustomButtonNew(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.sotib_olish),
                enabled = state.paymentStatus != PaymentStatus.PENDING && state.selectedPaymentType != null,
                onClick = {
                    event(PaymentEvent.Pay)
                },
            )
            LegalLinksRow()
        }
    }
}

@Composable
@Preview
private fun Preview() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        PaymentTypeScreenUi(
            navigator = null,
            state = PaymentState(
                originalAmount = 199000,
                subscriptionDuration = SubscriptionDuration.ANNUAL,
                isTestAccount = true,
                selectedPaymentType = PaymentType.AppStore
            ),
            event = {}
        )
    }

}