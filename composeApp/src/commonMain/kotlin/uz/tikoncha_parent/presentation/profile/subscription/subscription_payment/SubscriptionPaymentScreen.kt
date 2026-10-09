package uz.tikoncha_parent.presentation.profile.subscription.subscription_payment

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import tikoncha_parents.composeapp.generated.resources.ap_child_no_plus
import tikoncha_parents.composeapp.generated.resources.ap_child_plus_until
import tikoncha_parents.composeapp.generated.resources.ap_pick_child_title
import tikoncha_parents.composeapp.generated.resources.price_uzs_per_month
import tikoncha_parents.composeapp.generated.resources.ap_per_month
import tikoncha_parents.composeapp.generated.resources.ap_per_year
import tikoncha_parents.composeapp.generated.resources.ap_paywall_note
import tikoncha_parents.composeapp.generated.resources.ap_paywall_month_sub
import tikoncha_parents.composeapp.generated.resources.ap_paywall_year_sub
import tikoncha_parents.composeapp.generated.resources.ap_change
import tikoncha_parents.composeapp.generated.resources.ap_for_child
import kotlin.math.roundToInt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.formatSum
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.Tag
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.RadioMark
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.ChildAvatar
import uz.tikoncha_parent.presentation.profile.subscription.autopay.method.ChildArgs
import uz.tikoncha_parent.presentation.profile.subscription.autopay.method.PaymentMethodScreen
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.platform.isIos
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import uz.tikoncha_parent.presentation.common.*
import uz.tikoncha_parent.ui.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.domain.model.SubscriptionDuration
import uz.tikoncha_parent.domain.model.SubscriptionType
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.presentation.profile.subscription.payment.PaymentTypeScreen
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

class SubscriptionPaymentScreen(
    val selectedChild: UserInfo? = null,
    val isInfoMode: Boolean = false
) : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current
        val viewModel = koinScreenModel<SubscriptionPaymentViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        LaunchedEffect(selectedChild) {
            if (selectedChild != null) {
                Logger.d("SubscriptionPaymentScreen", "selectedChild=$selectedChild")
                event(SubscriptionPaymentEvent.SetSelectedChild(selectedChild))
            }
        }

        SubscriptionPaymentUi(
            navigator = navigator,
            state = state,
            event = event,
            isInfoMode = isInfoMode
        )
    }
}

@Composable
fun SubscriptionPaymentUi(
    navigator: Navigator?,
    state: SubscriptionPaymentState = SubscriptionPaymentState(),
    event: (SubscriptionPaymentEvent) -> Unit,
    isInfoMode: Boolean = false
) {
    val planLoading = state.subscriptionPlanState is ResponseState.Loading
    val planErrorText = state.subscriptionPlanState.errorText()
    var showPlanErrorDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    LoadingDialog(planLoading)

    LaunchedEffect(planErrorText) {
        if (planErrorText.isNotEmpty()) {
            showPlanErrorDialog = true
        }
    }

    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = showPlanErrorDialog,
        title = stringResource(Res.string.xatolik),
        message = planErrorText,
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = {
            showPlanErrorDialog = false
            event(SubscriptionPaymentEvent.ResetResponseState)
        },
        onButtonClick = {
            showPlanErrorDialog = false
            event(SubscriptionPaymentEvent.ResetResponseState)
        }
    )

    if (showDialog) {
        ChildPickerSheet(
            children = state.children,
            selectedId = state.selectedChild?.userId,
            onPick = { child ->
                showDialog = false
                event(SubscriptionPaymentEvent.SetSelectedChild(child))
            },
            onDismiss = { showDialog = false },
        )
    }

    var selectedPlan by rememberSaveable { mutableIntStateOf(0) } // 0 = Yillik, 1 = Oylik

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFC3955B),
            Color(0xFF291E0F),
        )
    )

    val subscription = state.subscriptionUi
    val hasSubscription = state.currentPlan != SubscriptionType.FREE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = bgGradient)
    ) {
        // ── Scrollable content ─────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // Close button
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .singleClick {
                        navigator?.pop()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.close_remove),
                    contentDescription = "Close",
                    tint = AppColors.icon.inverse,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(Res.drawable.tikoncha_plus_new),
                    contentDescription = "Tikoncha mascot",
                    modifier = Modifier.size(140.dp)
                )
                Space(11.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.tikoncha_logo),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(AppColors.text.inverse),
                        modifier = Modifier
                            .width(150.dp)
                            .height(30.dp)
                    )
                    Spacer(Modifier.width(8.dp))

                    Image(
                        painter = painterResource(Res.drawable.plus_sub),
                        contentDescription = null,
                        modifier = Modifier
                            .width(64.dp)
                            .height(34.dp)
                    )
                }
                Space(12.dp)

                if (!isInfoMode) {
                    Text(
                        text = stringResource(Res.string.farzand_nazorat_tavsifi),
                        fontSize = 14.sp,
                        color = AppColors.text.inverse.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
            Space(27.dp)

            // ── Kim uchun (2+ farzand bo'lsa almashtiriladi) ──
            val child = state.selectedChild
            if (!isInfoMode && child != null) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .then(if (state.children.size > 1) Modifier.singleClick { showDialog = true } else Modifier)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ChildAvatar(child.avatarUrl, 30.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(Res.string.ap_for_child, childDisplayName(child)),
                        style = AppTypography.titleSmSemiBold, color = AppColors.text.inverse,
                        maxLines = 1, modifier = Modifier.weight(1f),
                    )
                    if (state.children.size > 1) {
                        Text(stringResource(Res.string.ap_change), style = AppTypography.bodyMdSemiBold, color = Color(0xFFFFE7BF))
                    }
                }
                Space(20.dp)
            }

            // ── Pricing cards ──────────────────────────────
            if (!isInfoMode) {
                if (subscription != null) {
                    // Narx — karta orqali avto-to'lov (arzonroq); server bermasa Click narxi
                    val cardPrices = subscription.cardAnnual != null && subscription.cardMonthly != null
                    val annualPrice = subscription.cardAnnual ?: subscription.annual.price
                    val monthlyPrice = subscription.cardMonthly ?: subscription.monthly.price
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SubscriptionPlanCard(
                            title = stringResource(Res.string.yillik),
                            subtitle = if (cardPrices) stringResource(Res.string.ap_paywall_year_sub, formatSum(roundTo100(annualPrice / 12f)))
                            else stringResource(Res.string.price_uzs_per_month, formatSum(roundTo100(annualPrice / 12f))),
                            price = annualPrice,
                            per = stringResource(Res.string.ap_per_year),
                            badgeText = stringResource(Res.string.eng_foydali_tanlov),
                            isSelected = selectedPlan == 0,
                            onClick = { selectedPlan = 0 }
                        )

                        SubscriptionPlanCard(
                            title = stringResource(Res.string.oylik),
                            subtitle = if (cardPrices) stringResource(Res.string.ap_paywall_month_sub) else null,
                            price = monthlyPrice,
                            per = stringResource(Res.string.ap_per_month),
                            badgeText = null,
                            isSelected = selectedPlan == 1,
                            onClick = { selectedPlan = 1 }
                        )

                        if (cardPrices) {
                            Text(
                                text = stringResource(
                                    Res.string.ap_paywall_note,
                                    formatSum(subscription.monthly.price),
                                    formatSum(subscription.annual.price),
                                ),
                                style = AppTypography.bodyMdMedium,
                                color = AppColors.text.inverse.copy(alpha = 0.72f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                            )
                        }
                    }
                }
                Space(16.dp)
            }

            // ── Feature list ───────────────────────────────
            if (subscription != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .background(Color.White.copy(0.05f), RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    SubscriptionFeatureItem(
                        iconRes = Res.drawable.chart_sub,
                        title = stringResource(Res.string.toliq_nazorat_statistika),
                        description = stringResource(Res.string.farzand_ilova_kuzatish)
                    )

                    SubscriptionFeatureItem(
                        iconRes = Res.drawable.layers_sub,
                        title = stringResource(Res.string.uchtagacha_jadval),
                        description = stringResource(Res.string.jadval_cheklovlar)
                    )

                    SubscriptionFeatureItem(
                        iconRes = Res.drawable.circle_star,
                        title = stringResource(Res.string.qattiq_bloklash_qalqon),
                        description = stringResource(Res.string.farzand_cheklov_ozgartira_olmaydi)
                    )

                    SubscriptionFeatureItem(
                        iconRes = Res.drawable.lock_sub,
                        title = stringResource(Res.string.tez_bloklash_timer),
                        description = stringResource(Res.string.vaqtinchalik_bloklash_tezi)
                    )

                    SubscriptionFeatureItem(
                        iconRes = Res.drawable.attach_sub,
                        title = stringResource(Res.string.hisobot_tahlil),
                        description = stringResource(Res.string.foydalanish_tahlil)
                    )
                }
            }
            if (!isInfoMode) {
                Space(150.dp)
            }
            else {
                Space(50.dp)
            }
        }

        // ── Bottom button ──────────────────────────────────
        if (!isInfoMode) {
            val bottomGradient = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF141A15).copy(alpha = 0.3f),
                    Color(0xFF141A15).copy(alpha = 0.7f),
                    Color(0xFF141A15).copy(alpha = 0.95f),
                    Color(0xFF141A15),
                )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(bottomGradient)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 18.dp)
                    .padding(top = 13.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomButton(
                    enabled = !hasSubscription && subscription != null,
                    text = if (hasSubscription) stringResource(Res.string.obuna_faollashtirilgan)
                    else stringResource(Res.string.obuna_bolish),
                    onClick = {
                        subscription?.let { sub ->
                            val annual = selectedPlan == 0
                            val clickPrice = if (annual) sub.annual.price else sub.monthly.price
                            val cardPrice = (if (annual) sub.cardAnnual else sub.cardMonthly) ?: clickPrice
                            val duration = if (annual) SubscriptionDuration.ANNUAL else SubscriptionDuration.MONTHLY

                            if (isIos() && AppSettings.isTestAccount) {
                                // Apple ko'rigi uchun raqam: iPhone'da faqat App Store (egasining qarori)
                                navigator?.push(PaymentTypeScreen(amount = clickPrice, subDuration = duration, planId = sub.planId))
                            } else {
                                // Avval to'lov usuli: karta (avto) yoki Click (bir martalik)
                                navigator?.push(
                                    PaymentMethodScreen(
                                        child = state.selectedChild.toChildArgs(),
                                        planId = sub.planId,
                                        period = if (annual) "ANNUAL" else "MONTHLY",
                                        cardAmount = cardPrice,
                                        clickAmount = clickPrice,
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Space(12.dp)

                Text(
                    text = stringResource(Res.string.istalgan_vaqtda_bekor),
                    style = AppTypography.bodyMdMedium,
                    color = AppColors.text.inverse,
                    textAlign = TextAlign.Center
                )
            }
        }

    }
}

@Preview
@Composable
fun PreviewSubscriptionScreen() {
    TikonchaParentTheme(
        ThemeMode.LIGHT,
    ) {
        SubscriptionPaymentUi(
            navigator = null,
            state = SubscriptionPaymentState(),
            event = {},
            isInfoMode = true
        )
    }
}

/** 16 583 → 16 600: "oyiga ~" qatori uchun. */
private fun roundTo100(value: Float): Int = (value / 100f).roundToInt() * 100

private fun childDisplayName(c: UserInfo): String =
    listOf(c.name, c.lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { c.fullName }

/** Avto-to'lov ekranlari uchun; farzand tanlanmagan bo'lsa — id yo'q (faqat Click, telefon raqami yo'li). */
private fun UserInfo?.toChildArgs(): ChildArgs = ChildArgs(
    id = this?.userId,
    name = this?.let { childDisplayName(it) }.orEmpty(),
    avatarUrl = this?.avatarUrl,
    plusUntil = this?.subscription_end_date?.takeIf { subscription != null && subscription != "FREE" },
)

/** Farzand tanlash (maket P10): har birida PLUS holati. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildPickerSheet(
    children: List<UserInfo>,
    selectedId: String?,
    onPick: (UserInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.bg.elevated,
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            Text(stringResource(Res.string.ap_pick_child_title), style = AppTypography.titleLgSemiBold, color = AppColors.text.primary)
            Spacer(Modifier.height(8.dp))
            children.forEachIndexed { i, c ->
                if (i > 0) HorizontalDivider(thickness = 1.dp, color = AppColors.border.divider)
                val plus = c.subscription != null && c.subscription != "FREE"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .singleClick { onPick(c) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ChildAvatar(c.avatarUrl, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(childDisplayName(c), style = AppTypography.titleSmSemiBold, color = AppColors.text.primary, maxLines = 1)
                        Spacer(Modifier.height(3.dp))
                        Tag(
                            if (plus) stringResource(Res.string.ap_child_plus_until, c.subscription_end_date.orEmpty())
                            else stringResource(Res.string.ap_child_no_plus)
                        )
                    }
                    RadioMark(c.userId == selectedId)
                }
            }
        }
    }
}
