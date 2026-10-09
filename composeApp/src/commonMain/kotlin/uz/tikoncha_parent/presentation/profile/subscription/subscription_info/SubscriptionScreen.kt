package uz.tikoncha_parent.presentation.profile.subscription.info

import tikoncha_parents.composeapp.generated.resources.toliq_nazorat_statistika
import tikoncha_parents.composeapp.generated.resources.uchtagacha_jadval
import tikoncha_parents.composeapp.generated.resources.qattiq_bloklash_qalqon
import tikoncha_parents.composeapp.generated.resources.ap_from_per_month_card
import tikoncha_parents.composeapp.generated.resources.ap_subscribe_for_child
import tikoncha_parents.composeapp.generated.resources.ap_plus_unlocks
import tikoncha_parents.composeapp.generated.resources.ap_child_no_plus_title
import tikoncha_parents.composeapp.generated.resources.ap_for_child
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.farzandingizni_tanlang
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.widthIn
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.sumText
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.ChildAvatar
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.new_home.SelectionChildBottomSheet
import uz.tikoncha_parent.presentation.base.ChildSelectionButton
import uz.tikoncha_parent.presentation.profile.payment_history.PaymentHistoryScreen
import uz.tikoncha_parent.presentation.profile.subscription.subscription_info.AutopaySection
import uz.tikoncha_parent.presentation.profile.subscription.autopay.card.AutopayAddCardScreen
import uz.tikoncha_parent.presentation.profile.subscription.autopay.cards.AutopayCardsScreen
import uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm.AutopayConfirmScreen
import uz.tikoncha_parent.presentation.profile.subscription.autopay.method.ChildArgs
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.dot
import tikoncha_parents.composeapp.generated.resources.faol
import tikoncha_parents.composeapp.generated.resources.keyingi_tolov
import tikoncha_parents.composeapp.generated.resources.kun
import tikoncha_parents.composeapp.generated.resources.narx
import tikoncha_parents.composeapp.generated.resources.obuna
import tikoncha_parents.composeapp.generated.resources.obuna_boshlangan_sana
import tikoncha_parents.composeapp.generated.resources.obuna_turi
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.oylik
import tikoncha_parents.composeapp.generated.resources.plus_sub
import tikoncha_parents.composeapp.generated.resources.qolgan_kunlar
import tikoncha_parents.composeapp.generated.resources.tikoncha_logo
import tikoncha_parents.composeapp.generated.resources.tikoncha_plus_new
import tikoncha_parents.composeapp.generated.resources.tolov_tafsilotlari
import tikoncha_parents.composeapp.generated.resources.tugagan
import tikoncha_parents.composeapp.generated.resources.tugash_sanasi
import tikoncha_parents.composeapp.generated.resources.uzs
import tikoncha_parents.composeapp.generated.resources.xatolik
import tikoncha_parents.composeapp.generated.resources.yillik
import uz.tikoncha_parent.common.Util.toCurrency
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.subscription.PlanDuration
import uz.tikoncha_parent.domain.model.subscription.PlanType
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.DashedDivider
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.presentation.profile.subscription.subscription_info.SubscriptionEffect
import uz.tikoncha_parent.presentation.profile.subscription.subscription_info.SubscriptionEvent
import uz.tikoncha_parent.presentation.profile.subscription.subscription_info.SubscriptionState
import uz.tikoncha_parent.presentation.profile.subscription.subscription_info.SubscriptionViewModel
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class SubscriptionScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinScreenModel<SubscriptionViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        // Boshqa ekrandan qaytganda (to'lov, karta) — jim yangilanadi; birinchi ochilishda VM o'zi yuklaydi
        LaunchedEffect(Unit) { event(SubscriptionEvent.Resumed) }

        // ⬇️ Yagona joy navigatsiyani boshqaradi
        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    SubscriptionEffect.NavigateToPayment -> {
                        navigator?.replace(SubscriptionPaymentScreen(AppSettings.selectedChild))
                        Logger.d("SubscriptionScreen", "NavigateToPayment")
                    }


                    SubscriptionEffect.NavigateToInfoMode -> {
                        navigator?.push(
                            SubscriptionPaymentScreen(
                                AppSettings.selectedChild,
                                isInfoMode = true
                            )
                        )
                        Logger.d("SubscriptionScreen", "NavigateToInfoMode")
                    }

                    SubscriptionEffect.PopBack -> {
                        navigator?.pop()
                        Logger.d("SubscriptionScreen", "PopBack")
                    }

                    SubscriptionEffect.OpenPaywall -> navigator?.push(SubscriptionPaymentScreen(AppSettings.selectedChild))

                    is SubscriptionEffect.OpenAutopayConfirm -> navigator?.push(
                        AutopayConfirmScreen(selectedChildArgs(), effect.planId, effect.period.wire, effect.amount)
                    )
                    SubscriptionEffect.OpenCards -> navigator?.push(AutopayCardsScreen())
                    SubscriptionEffect.OpenAddCard -> navigator?.push(AutopayAddCardScreen(selectedChildArgs()))
                    SubscriptionEffect.OpenHistory -> navigator?.push(PaymentHistoryScreen())
                }
            }
        }

        SubscriptionUI(
            navigator = navigator,
            state = state,
            event = event
        )
    }
}

@Composable
fun SubscriptionUI(
    navigator: Navigator? = null,
    state: SubscriptionState = SubscriptionState(),
    event: (SubscriptionEvent) -> Unit = {}
) {
    val loading = state.subscriptionStatusState is ResponseState.Loading
    val errorText = state.subscriptionStatusState.errorText()
    var showErrorDialog by remember { mutableStateOf(false) }

    // O'chirish / qayta urinish / karta almashtirish ham serverga boradi — kutish ko'rinsin
    LoadingDialog(loading || state.autopayBusy)
    var showChildSheet by remember { mutableStateOf(false) }

    LaunchedEffect(errorText) {
        if (errorText.isNotEmpty()) showErrorDialog = true
    }

    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = showErrorDialog,
        title = stringResource(Res.string.xatolik),
        message = errorText,
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = {
            showErrorDialog = false
            event(SubscriptionEvent.OnErrorDismissed)
        },
        onButtonClick = {
            showErrorDialog = false
            event(SubscriptionEvent.OnErrorDismissed)
        }
    )

    val subscription = state.subscription
    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFBA8837), Color(0xFF906019))
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
            title = stringResource(Res.string.obuna),
            onBackClick = { event(SubscriptionEvent.OnBackClick) },
            // Statistika'dagi tanlagichning o'zi — obuna qaysi farzandniki ekani doim ko'rinib tursin
            trailingIcon = {
                ChildSelectionButton(
                    modifier = Modifier.widthIn(120.dp, 160.dp),
                    text = state.selectedChild?.name ?: "",
                    imageUrl = state.selectedChild?.avatarUrl ?: "",
                    label = stringResource(Res.string.farzandingizni_tanlang),
                    userInfo = state.selectedChild,
                    onClick = {
                        if (state.children.isEmpty()) navigator?.push(AddChildScreen())
                        else showChildSheet = true
                    }
                )
            }
        )

        if (showChildSheet) {
            SelectionChildBottomSheet(
                navigator = navigator,
                items = state.children,
                selectedItem = state.selectedChild,
                onDismiss = { showChildSheet = false },
                title = stringResource(Res.string.farzandlaringiz),
                onItemSelected = {
                    event(SubscriptionEvent.SelectChild(it))
                    showChildSheet = false
                }
            )
        }

        // PLUS bor — karta va tafsilotlar; PLUS yo'q (yoki tugagan) — pastdagi "… uchun obuna bo'lish".
        if (subscription != null && subscription.planType == PlanType.PLUS) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = ContainerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Gradient card (info modeda to'lov ekraniga olib boradi) ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = bgGradient, shape = RoundedCornerShape(24.dp))
                        .singleClick { event(SubscriptionEvent.OnCardClick) }   // ⬅️ event emit
                        .padding(start = 24.dp, end = 25.dp, top = 27.dp, bottom = 7.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(Res.drawable.tikoncha_logo),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(AppColors.text.inverse),
                                modifier = Modifier.width(124.dp).height(25.dp)
                            )
                            Spacer(Modifier.width(7.dp))
                            Image(
                                painter = painterResource(Res.drawable.plus_sub),
                                contentDescription = null,
                                modifier = Modifier.width(54.dp).height(28.dp)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        state.selectedChild?.let { child -> ForChildChip(child.name, child.avatarUrl) }
                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = stringResource(Res.string.keyingi_tolov),
                            style = AppTypography.titleSmMedium,
                            color = AppColors.text.inverse
                        )
                        Spacer(Modifier.height(8.dp))

                        val nextPaymentText = buildAnnotatedString {
                            append(subscription.amount.toCurrency())
                            append(" ")
                            append(stringResource(Res.string.uzs))
                        }
                        Text(
                            text = subscription.expiresAt.toString(),
                            style = AppTypography.titleLgSemiBold,
                            color = AppColors.text.inverse
                        )
                        Spacer(Modifier.height(16.dp))

                        // ── Dinamik badge (Faol / Tugagan) ──
                        val isActive = !subscription.isExpired
                        Row(
                            modifier = Modifier
                                .background(AppColors.bg.primary, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.dot),
                                contentDescription = null,
                                tint = AppColors.text.inverse,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(if (isActive) Res.string.faol else Res.string.tugagan),
                                style = AppTypography.bodyMdMedium,
                                color = AppColors.text.inverse
                            )
                        }
                    }

                    Image(
                        painter = painterResource(Res.drawable.tikoncha_plus_new),
                        contentDescription = null,
                        modifier = Modifier.width(110.dp).height(165.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))

                // ── To'lov tafsilotlari ──
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

                    // Obuna turi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.obuna_turi),
                            style = AppTypography.bodyMdMedium,
                            color = AppColors.text.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        val durationText = when (subscription.planDuration) {
                            PlanDuration.MONTHLY -> stringResource(Res.string.oylik)
                            PlanDuration.ANNUAL  -> stringResource(Res.string.yillik)
                            null -> "—"
                        }
                        Text(
                            text = durationText,
                            style = AppTypography.titleSmMedium,
                            color = AppColors.text.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Narx
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val priceText = buildAnnotatedString {
                            withStyle(SpanStyle(color = AppColors.text.primary)) {
                                append(subscription.amount.toCurrency())
                            }
                            append(" ")
                            withStyle(SpanStyle(color = AppColors.text.primary)) {
                                append(stringResource(Res.string.uzs))
                            }
                        }
                        Text(
                            text = "${ stringResource(Res.string.narx) }:",
                            style = AppTypography.bodyMdMedium,
                            color = AppColors.text.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = priceText,
                            style = AppTypography.titleSmMedium,
                            color = AppColors.text.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Boshlangan sana
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.obuna_boshlangan_sana),
                            style = AppTypography.bodyMdMedium,
                            color = AppColors.text.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = subscription.createdAt.orEmpty(),
                            style = AppTypography.titleSmMedium,
                            color = AppColors.text.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Tugash sanasi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.tugash_sanasi),
                            style = AppTypography.bodyMdMedium,
                            color = AppColors.text.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = subscription.expiresAt.orEmpty(),
                            style = AppTypography.titleSmMedium,
                            color = AppColors.text.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Qolgan kunlar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.qolgan_kunlar),
                            style = AppTypography.bodyMdMedium,
                            color = AppColors.text.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${subscription.remainingDays} ${stringResource(Res.string.kun)}",
                            style = AppTypography.titleSmMedium,
                            color = AppColors.text.primary
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                AutopaySection(state = state, event = event)
                Spacer(Modifier.height(24.dp))
            }
        } else if (subscription != null) {
            FreeChildBlock(
                name = state.selectedChild?.name.orEmpty(),
                fromPrice = state.freeFromPrice,
                onSubscribe = { event(SubscriptionEvent.OpenPaywall) },
            )
        }
    }
}

/** Oltin kartadagi "Ali uchun" — qaysi farzandning obunasi ekani. */
@Composable
private fun ForChildChip(name: String, avatarUrl: String?) {
    Row(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChildAvatar(avatarUrl, 20.dp)
        Spacer(Modifier.width(6.dp))
        Text(stringResource(Res.string.ap_for_child, name), style = AppTypography.bodySmSemiBold, color = AppColors.text.inverse, maxLines = 1)
    }
}

/** PLUS yo'q farzand (maket 2c): tarif ekraniga o'zi o'tib ketmaydi — tanlagich joyida qoladi. */
@Composable
private fun FreeChildBlock(name: String, fromPrice: Int?, onSubscribe: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = ContainerPadding)
            .padding(top = 4.dp)
            .fillMaxWidth()
            .background(AppColors.bg.surface, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Text(stringResource(Res.string.ap_child_no_plus_title, name), style = AppTypography.titleLgSemiBold, color = AppColors.text.primary)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(Res.string.ap_plus_unlocks), style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary)
        Spacer(Modifier.height(14.dp))
        listOf(
            Icons.Rounded.Shield to Res.string.qattiq_bloklash_qalqon,
            Icons.Rounded.CalendarMonth to Res.string.uchtagacha_jadval,
            Icons.Rounded.BarChart to Res.string.toliq_nazorat_statistika,
        ).forEach { (icon, text) ->
            Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = AppColors.action.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(stringResource(text), style = AppTypography.titleSmMedium, color = AppColors.text.secondary)
            }
        }
        Spacer(Modifier.height(16.dp))
        CustomButton(
            text = stringResource(Res.string.ap_subscribe_for_child, name),
            onClick = onSubscribe,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        )
        fromPrice?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.ap_from_per_month_card, sumText(it)),
                style = AppTypography.bodySmMedium, color = AppColors.text.tertiary,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
fun PreviewPaymentInfoScreen() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        SubscriptionUI()
    }
}

/** Tanlangan farzand — avto-to'lov ekranlari uchun. */
internal fun selectedChildArgs(): ChildArgs {
    val c = AppSettings.selectedChild
    return ChildArgs(
        id = c?.userId,
        name = c?.let { listOf(it.name, it.lastName).filter { p -> p.isNotBlank() }.joinToString(" ").ifBlank { it.fullName } }.orEmpty(),
        avatarUrl = c?.avatarUrl,
        plusUntil = c?.subscription_end_date?.takeIf { c.subscription != null && c.subscription != "FREE" },
    )
}
