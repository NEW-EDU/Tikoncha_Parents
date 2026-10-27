package uz.tikoncha_parent.presentation.profile.coin_purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.biz_bilan_aloqa
import tikoncha_parents.composeapp.generated.resources.pay_abroad_coins_body
import tikoncha_parents.composeapp.generated.resources.pay_abroad_title
import tikoncha_parents.composeapp.generated.resources.bekor_qilindi
import tikoncha_parents.composeapp.generated.resources.chegirma
import tikoncha_parents.composeapp.generated.resources.jami
import tikoncha_parents.composeapp.generated.resources.muvaffaqiyatli
import tikoncha_parents.composeapp.generated.resources.narx
import tikoncha_parents.composeapp.generated.resources.promokod
import tikoncha_parents.composeapp.generated.resources.promokod_tasdiqlandi
import tikoncha_parents.composeapp.generated.resources.siz_tangachalarni_muvaqqiyatli_sotib_oldingiz
import tikoncha_parents.composeapp.generated.resources.sorov_holati
import tikoncha_parents.composeapp.generated.resources.sotib_olish
import tikoncha_parents.composeapp.generated.resources.tanga_s
import tikoncha_parents.composeapp.generated.resources.tangachalar
import tikoncha_parents.composeapp.generated.resources.tolov_click_platformasi_orqali_amalga_oshiriladi
import tikoncha_parents.composeapp.generated.resources.tolov_kutilmoqda
import tikoncha_parents.composeapp.generated.resources.xatolik
import tikoncha_parents.composeapp.generated.resources.yopish
import uz.tikoncha_parent.common.Util.toCurrency
import uz.tikoncha_parent.domain.model.PaymentStatus
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.platform.PaymentUtil
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.LocalToastHost
import uz.tikoncha_parent.presentation.base.ToastData
import uz.tikoncha_parent.presentation.base.ToastProvider
import uz.tikoncha_parent.presentation.base.ToastType
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.presentation.profile.subscription.payment.PaymentOption
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.DividerHorizontal
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars


class CoinPurchaseScreen(
    private val coins: Int = 0,
    private val totalPrice: Int = 0,
    private val discountPrice: Int = 0
) : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current?:return

        val viewModel = koinScreenModel<CoinPurchaseViewModel>()
        val state = viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        LaunchedEffect(Unit) {
            event(CoinPurchaseEvent.SetupData(
                coins = coins,
                totalPrice = totalPrice,
                discountPrice = discountPrice
            ))
        }

        ToastProvider {
            CoinPurchaseUi(
                event = event,
                state = state.value,
                navigator = navigator,
                effect = viewModel.effect
            )
        }
    }
}
@Composable
fun CoinPurchaseUi(
    navigator: Navigator?,
    state: CoinPurchaseState,
    event: (CoinPurchaseEvent) -> Unit,
    effect: Flow<CoinPurchaseEffect>
) {

    val toast = LocalToastHost.current
    val promoCodeSuccessMessage = stringResource(Res.string.promokod_tasdiqlandi)

    var paymentFailure by remember { mutableStateOf<Outcome.Failure?>(null) }
    var promoErrorFailure by remember { mutableStateOf<Outcome.Failure?>(null) }
    val promoErrorText = promoErrorFailure?.asText()

    LaunchedEffect(promoErrorText) {
        promoErrorText?.let {
            toast.show(toast = ToastData(type = ToastType.Error, title = it))
            promoErrorFailure = null
        }
    }

    var showPaymentSuccessDialog by remember {
        mutableStateOf(false)
    }

    // Tangalar faqat Click orqali sotiladi — O'zbekistondan tashqarida u işlamaydi: boşi berk yöl örniga aniq javob
    val region: uz.tikoncha_parent.domain.repository.PaymentRegionRepository = org.koin.compose.koinInject()
    val localPayments by region.localPayments.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { region.refresh() }
    var showAbroad by remember { mutableStateOf(false) }
    var showSupport by remember { mutableStateOf(false) }
    uz.tikoncha_parent.presentation.base.CustomDialog(
        show = showAbroad,
        title = stringResource(Res.string.pay_abroad_title),
        message = stringResource(Res.string.pay_abroad_coins_body),
        buttonText = stringResource(Res.string.biz_bilan_aloqa),
        showCloseButton = true,
        onDismiss = { showAbroad = false },
        onButtonClick = { showAbroad = false; showSupport = true },
    )
    if (showSupport) uz.tikoncha_parent.presentation.support.SupportSheet(onDismiss = { showSupport = false })

    val uriHandler = LocalUriHandler.current
    LaunchedEffect(Unit) {
        effect.collect { eff ->
            when (eff) {
                is CoinPurchaseEffect.ShowPromoCodeErrorToast -> {
                    promoErrorFailure = eff.failure
                }

                CoinPurchaseEffect.ShowPromoCodeSuccessToast -> {
                    toast.show(
                        toast = ToastData(
                            type = ToastType.Success,
                            title = promoCodeSuccessMessage
                        )
                    )
                }

                is CoinPurchaseEffect.OpenClickPayment -> {
                    val url = PaymentUtil.openClickUrl(
                        serviceId = eff.serviceId.toString(),
                        merchantId = eff.merchantId.toString(),
                        amount = eff.amount.toString(),
                        transactionId = eff.transactionId
                    )
                    uriHandler.openUri(url)
                }

                is CoinPurchaseEffect.PaymentFailed -> {
                    paymentFailure = eff.failure
                }

                CoinPurchaseEffect.PaymentSuccess -> {
                    showPaymentSuccessDialog = true
                }
            }
        }
    }


    CustomDialog(
        show = paymentFailure != null,
        onDismiss = { paymentFailure = null },
        title = stringResource(Res.string.xatolik),
        message = paymentFailure?.asText().orEmpty(),
        buttonText = stringResource(Res.string.yopish),
        showCloseButton = false,
        onButtonClick = { paymentFailure = null },
    )

    CustomDialog(
        show = showPaymentSuccessDialog,
        onDismiss = { showPaymentSuccessDialog = false },
        title = stringResource(Res.string.muvaffaqiyatli),
        message = stringResource(Res.string.siz_tangachalarni_muvaqqiyatli_sotib_oldingiz),
        buttonText = stringResource(Res.string.yopish),
        showCloseButton = false,
        onButtonClick = {
            showPaymentSuccessDialog = false
            navigator?.pop()
        },
    )

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.page
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page)
    ) {

        CustomHeader(
            showBackButton = true,
            title = "",
            onBackClick = {
                navigator?.pop()
            }
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(start = ContainerPadding, end = ContainerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(10.dp))

            PaymentOption(
                isSelected = true,
                onClick = { },
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.tolov_click_platformasi_orqali_amalga_oshiriladi),
                color = AppColors.text.secondary,
                style = AppTypography.bodyMdMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            PromoCodeContainer(
                modifier = Modifier
                    .fillMaxWidth(),
                showLoading = state.promoCodeLoading,
                enabled = !state.promoActivated,
                onClick = { promoCode ->
                    event(CoinPurchaseEvent.ApplyPromoCode(promoCode))
                }
            )
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .simpleShadow(RoundedCornerShape(16.dp))
                    .background(AppColors.bg.surface, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${stringResource(Res.string.tangachalar)}:",
                        color = AppColors.text.secondary,
                        style = AppTypography.titleSmMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = stringResource(Res.string.tanga_s, state.coins.toCurrency()),
                        color = AppColors.text.secondary,
                        style = AppTypography.titleSmMedium,
                    )

                }
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${stringResource(Res.string.narx)}:",
                        color = AppColors.text.secondary,
                        style = AppTypography.titleSmMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${state.totalPrice.toCurrency()} UZS",
                        color = AppColors.text.secondary,
                        style = AppTypography.titleSmMedium,
                    )

                }
                if (state.promoCodeDiscountPrice > 0) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${stringResource(Res.string.promokod)}:",
                            color = AppColors.text.secondary,
                            style = AppTypography.titleSmMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "-${state.promoCodeDiscountPrice.toCurrency()} UZS",
                            color = AppColors.text.accentEmphasis,
                            style = AppTypography.titleSmMedium,
                        )

                    }
                }
                if (state.discountPrice > 0) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${stringResource(Res.string.chegirma)}:",
                            color = AppColors.text.secondary,
                            style = AppTypography.titleSmMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "-${state.discountPrice.toCurrency()} UZS",
                            color = AppColors.text.accentDanger,
                            style = AppTypography.titleSmMedium,
                        )

                    }
                }
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${stringResource(Res.string.jami)}:",
                        color = AppColors.text.primary,
                        style = AppTypography.titleLgSemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${state.finalPrice.toCurrency()} UZS",
                        color = AppColors.text.accentEmphasis,
                        style = AppTypography.titleLgSemiBold,
                    )

                }

                if (state.paymentStatus != PaymentStatus.START) {
                    Spacer(Modifier.height(8.dp))
                    DividerHorizontal()
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        val statusColor = when (state.paymentStatus) {
                            PaymentStatus.PENDING -> {
                                AppColors.text.accentWarning
                            }

                            PaymentStatus.COMPLETED -> {
                                AppColors.text.accentEmphasis
                            }

                            PaymentStatus.CANCELLED -> {
                                AppColors.text.accentDanger
                            }

                            PaymentStatus.FAILED -> {
                                AppColors.text.accentDanger
                            }

                            PaymentStatus.START -> {
                                AppColors.text.secondary
                            }
                        }

                        val statusText = when (state.paymentStatus) {
                            PaymentStatus.PENDING -> {
                                stringResource(Res.string.tolov_kutilmoqda)
                            }

                            PaymentStatus.COMPLETED -> {
                                stringResource(Res.string.muvaffaqiyatli)
                            }

                            PaymentStatus.CANCELLED -> {
                                stringResource(Res.string.bekor_qilindi)
                            }

                            PaymentStatus.FAILED -> {
                                stringResource(Res.string.xatolik)
                            }

                            else -> {
                                ""
                            }
                        }

                        Text(
                            text = "${stringResource(Res.string.sorov_holati)}:",
                            color = AppColors.text.secondary,
                            style = AppTypography.titleSmMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = statusText,
                            color = statusColor,
                            style = AppTypography.titleSmMedium,
                        )
                    }
                }
            }
        }

        CustomButton(
            text = "${state.finalPrice.toCurrency()} UZS - ${stringResource(Res.string.sotib_olish)}",
            onClick = {
                if (localPayments == false) showAbroad = true else event(CoinPurchaseEvent.StartPayment)
            },
            modifier = Modifier.padding(ContainerPadding)
                .fillMaxWidth()
                .height(52.dp),
            enabled = !state.paymentActive
        )
    }
}


@Preview
@Composable
private fun Pre() {
    TikonchaParentTheme (
        ThemeMode.LIGHT
    ) {
        ToastProvider {
            CoinPurchaseUi(
                navigator = null,
                state = CoinPurchaseState(
                    promoCodeDiscountPrice = 2000,
                    discountPrice = 20000
                ),
                event = {},
                effect = MutableSharedFlow()
            )
        }
    }
}