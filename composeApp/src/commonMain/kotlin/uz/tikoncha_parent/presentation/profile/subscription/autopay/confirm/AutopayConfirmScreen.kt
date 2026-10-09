package uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm

import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import cafe.adriel.voyager.koin.koinScreenModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.domain.model.autopay.PlanPeriod
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.haptics.ErrorHaptic
import uz.tikoncha_parent.presentation.policy.components.PolicyText
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.CardMask
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.ChildAvatar
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.RadioMark
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.formatDate
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.sumText
import uz.tikoncha_parent.presentation.profile.subscription.autopay.method.ChildArgs
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/** Server sahifasi (backend `GET /legal/autopay-terms`) — matnni yangilash uchun ilova chiqarish shart emas. */
const val AUTOPAY_TERMS_URL = "https://api.tikoncha.uz/legal/autopay-terms"

/**
 * To'lovni tasdiqlash: tarif · karta · promokod; bugun va keyingi to'lov vaqt chizig'ida;
 * aniq rozilik belgisi. Tugma rozilik va karta bo'lmaguncha o'chiq.
 */
@Composable
fun AutopayConfirmUi(state: AutopayConfirmState, child: ChildArgs, event: (AutopayConfirmEvent) -> Unit, onBack: () -> Unit) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    val yearly = state.period == PlanPeriod.ANNUAL
    val scheduled = state.scheduledStart()
    val price = sumText(state.amount)
    val payPrice = sumText(state.payAmount)

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(systemBars.modifier)
                .background(AppColors.bg.page),
        ) {
            CustomHeader(showBackButton = true, title = stringResource(Res.string.ap_confirm_title), onBackClick = onBack)
            if (state.loading) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.action.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Group {
                        // Parent: qaysi farzandning PLUS'iga to'lanayotgani — birinchi qator
                        InfoRow(Icons.Rounded.Person, stringResource(Res.string.ap_for_whom)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ChildAvatar(child.avatarUrl, 24.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(child.name, style = AppTypography.titleSmMedium, color = AppColors.text.secondary, maxLines = 1)
                            }
                        }
                        RowDivider()
                        InfoRow(Icons.Rounded.VerifiedUser, stringResource(Res.string.ap_plan)) {
                            Text(
                                text = stringResource(Res.string.ap_plan_value, stringResource(if (yearly) Res.string.yillik else Res.string.oylik)),
                                style = AppTypography.titleSmMedium, color = AppColors.text.secondary,
                            )
                        }
                        RowDivider()
                        InfoRow(
                            Icons.Rounded.CreditCard, stringResource(Res.string.ap_card),
                            sub = stringResource(Res.string.ap_change),
                            onClick = { event(AutopayConfirmEvent.OpenCardPicker) },
                        ) {
                            state.selectedCard?.let { CardMask(it) }
                                ?: Text(stringResource(Res.string.ap_add_new_card), style = AppTypography.titleSmMedium, color = AppColors.action.primary)
                        }
                        RowDivider()
                        InfoRow(Icons.Rounded.LocalOffer, stringResource(Res.string.ap_promo), onClick = { event(AutopayConfirmEvent.OpenPromo) }) {
                            if (state.promocode.isBlank()) {
                                Text(stringResource(Res.string.ap_promo_add), style = AppTypography.titleSmMedium, color = AppColors.action.primary)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(state.promocode, style = AppTypography.titleSmMedium, color = AppColors.text.secondary, maxLines = 1)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "−${state.promoPercent}%",
                                        style = AppTypography.bodySmSemiBold, color = AppColors.text.accentSuccess,
                                        modifier = Modifier
                                            .background(AppColors.text.accentSuccess.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                }
                            }
                        }
                    }

                    Timeline(yearly = yearly, price = price, scheduled = scheduled, firstPrice = payPrice.takeIf { state.promoPercent > 0 })

                    ConsentRow(
                        checked = state.consent,
                        text = stringResource(if (yearly) Res.string.ap_consent_year_child else Res.string.ap_consent_month_child, child.name),
                        onChange = { event(AutopayConfirmEvent.ConsentChanged(it)) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 22.dp)) {
                CustomButton(
                    text = if (scheduled != null) stringResource(Res.string.ap_enable_autopay) else stringResource(Res.string.ap_pay_now, payPrice),
                    enabled = state.canPay,
                    onClick = { event(AutopayConfirmEvent.Pay) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.ap_confirm_footer),
                    style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (state.submitting || state.checking) Checking(state.checking)
    }

    if (state.showCardPicker) CardPicker(state, event)
    if (state.showPromoInput) {
        PromoDialog(
            initial = state.promocode,
            checking = state.promoChecking,
            error = state.promoError?.asText(),
            canRemove = state.promocode.isNotBlank(),
            onDismiss = { event(AutopayConfirmEvent.DismissPromo) },
            onApply = { event(AutopayConfirmEvent.PromoEntered(it)) },
            onRemove = { event(AutopayConfirmEvent.RemovePromo) },
        )
    }

    ErrorHaptic(state.error ?: state.declinedText)
    CustomDialog(
        show = state.declinedText != null,
        painter = painterResource(Res.drawable.dialog_failed),
        title = stringResource(Res.string.ap_payment_failed),
        message = state.declinedText.orEmpty().ifBlank { stringResource(Res.string.ap_payment_failed_generic) },
        buttonText = stringResource(Res.string.yopish),
        onDismiss = { event(AutopayConfirmEvent.DismissDeclined) },
        onButtonClick = { event(AutopayConfirmEvent.DismissDeclined) },
    )
    CustomDialog(
        show = state.error != null,
        painter = painterResource(Res.drawable.dialog_failed),
        title = stringResource(Res.string.xatolik),
        message = state.error?.asText().orEmpty(),
        buttonText = stringResource(Res.string.yopish),
        onDismiss = { event(AutopayConfirmEvent.DismissError) },
        onButtonClick = { event(AutopayConfirmEvent.DismissError) },
    )
    CustomDialog(
        show = state.checkTimedOut,
        painter = painterResource(Res.drawable.dialog_warning),
        title = stringResource(Res.string.ap_checking_title),
        message = stringResource(Res.string.ap_checking_long),
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = { event(AutopayConfirmEvent.DismissTimeout) },
        onButtonClick = { event(AutopayConfirmEvent.DismissTimeout) },
    )
}

@Composable
private fun Timeline(yearly: Boolean, price: String, scheduled: Instant?, firstPrice: String?) {
    val now = Clock.System.now()
    val days = if (yearly) 365 else 30
    Column(
        Modifier
            .fillMaxWidth()
            .background(AppColors.bg.section, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        if (scheduled == null) {
            TimelineStep(
                dotActive = true, title = stringResource(Res.string.ap_today),
                sub = stringResource(if (firstPrice != null) Res.string.ap_first_payment_promo else Res.string.ap_first_payment),
                value = firstPrice ?: price, oldValue = price.takeIf { firstPrice != null },
            )
            TimelineStep(
                dotActive = false,
                title = formatDate(now + (days - 1).days),
                sub = stringResource(Res.string.ap_next_auto), value = price,
            )
        } else {
            TimelineStep(dotActive = true, title = stringResource(Res.string.ap_today), sub = stringResource(Res.string.ap_no_charge_today), value = sumText(0))
            TimelineStep(
                dotActive = false, title = formatDate(scheduled),
                sub = stringResource(if (firstPrice != null) Res.string.ap_first_payment_promo else Res.string.ap_first_payment),
                value = firstPrice ?: price, oldValue = price.takeIf { firstPrice != null },
            )
        }
        TimelineStep(
            dotActive = false,
            title = stringResource(if (yearly) Res.string.ap_every_year else Res.string.ap_every_month),
            sub = stringResource(Res.string.ap_until_off), value = "", last = true,
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = AppColors.border.divider)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.NotificationsNone, null, tint = AppColors.text.secondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.ap_remind_note), style = AppTypography.bodyMdMedium, color = AppColors.text.secondary)
        }
    }
}

@Composable
private fun TimelineStep(dotActive: Boolean, title: String, sub: String, value: String, last: Boolean = false, oldValue: String? = null) {
    Row(Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(12.dp)) {
            Spacer(Modifier.height(4.dp))
            Box(Modifier.size(12.dp).background(if (dotActive) AppColors.action.primary else AppColors.text.tertiary, CircleShape))
            if (!last) Box(Modifier.width(2.dp).height(34.dp).background(AppColors.border.divider))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(bottom = if (last) 0.dp else 12.dp)) {
            Text(title, style = AppTypography.titleSmSemiBold, color = AppColors.text.primary)
            Text(sub, style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary)
        }
        if (oldValue != null) {
            // Promokod: eski narx chizilgan, yangisi yashil
            Column(horizontalAlignment = Alignment.End) {
                Text(oldValue, style = AppTypography.bodySmMedium, color = AppColors.text.tertiary, textDecoration = TextDecoration.LineThrough)
                Text(value, style = AppTypography.titleSmSemiBold, color = AppColors.text.accentSuccess)
            }
        } else if (value.isNotEmpty()) {
            Text(value, style = AppTypography.titleSmSemiBold, color = AppColors.text.primary)
        }
    }
}

/** Promokod oynasi: yozilgan zahoti KATTA harf; "Qo'llash" serverda tekshiradi, xato bo'lsa oyna ochiq qoladi. */
@Composable
private fun PromoDialog(
    initial: String,
    checking: Boolean,
    error: String?,
    canRemove: Boolean,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
    onRemove: () -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    // Kod o'zgarsa eski xato yashiriladi; yangi javob kelsa yana ko'rinadi
    var shownError by remember(error) { mutableStateOf(error) }
    val canApply = value.isNotBlank() && !checking
    AlertDialog(
        onDismissRequest = { if (!checking) onDismiss() },
        containerColor = AppColors.modal.primary,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(Res.string.ap_promo), style = PolicyText.dialogTitle, color = AppColors.text.primary) },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it.filterNot(Char::isWhitespace).uppercase()
                        shownError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !checking,
                    isError = shownError != null,
                    placeholder = { Text(stringResource(Res.string.ap_promo_hint), style = PolicyText.input, color = AppColors.text.placeholder) },
                    textStyle = PolicyText.input,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { if (canApply) onApply(value) }),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.border.accentEmphasis,
                        unfocusedBorderColor = AppColors.border.primary,
                        errorBorderColor = AppColors.text.accentDanger,
                        focusedTextColor = AppColors.text.primary,
                        unfocusedTextColor = AppColors.text.primary,
                        disabledTextColor = AppColors.text.primary,
                        errorTextColor = AppColors.text.primary,
                        cursorColor = AppColors.action.primary,
                    ),
                )
                shownError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = AppTypography.bodyMdMedium, color = AppColors.text.accentDanger)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(value) }, enabled = canApply) {
                if (checking) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = AppColors.action.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.ap_promo_checking), color = AppColors.text.tertiary, style = PolicyText.action)
                } else {
                    Text(stringResource(Res.string.ap_apply), color = AppColors.text.accentEmphasis, style = PolicyText.action)
                }
            }
        },
        dismissButton = {
            Row {
                if (canRemove) {
                    TextButton(onClick = onRemove, enabled = !checking) {
                        Text(stringResource(Res.string.ap_promo_remove), color = AppColors.text.accentDanger, style = PolicyText.action)
                    }
                }
                TextButton(onClick = onDismiss, enabled = !checking) {
                    Text(stringResource(Res.string.bekor_qilish), color = AppColors.text.secondary, style = PolicyText.action)
                }
            }
        },
    )
}

@Composable
private fun ConsentRow(checked: Boolean, text: String, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onChange)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .size(22.dp)
                .background(if (checked) AppColors.action.primary else AppColors.bg.section, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(Icons.Rounded.Check, null, tint = AppColors.icon.inverse, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = buildAnnotatedString {
                append(text)
                append(" ")
                // Shartlar — alohida havola (maxfiylik siyosati kabi): bosilsa sahifa ochiladi, belgi o'zgarmaydi
                withLink(
                    LinkAnnotation.Url(
                        url = "$AUTOPAY_TERMS_URL?lang=${LanguagePrefs.loadOrDefault().languageCode}",
                        styles = TextLinkStyles(SpanStyle(color = AppColors.action.primary, fontWeight = FontWeight.SemiBold)),
                    )
                ) {
                    append(stringResource(Res.string.ap_terms))
                }
            },
            style = AppTypography.titleSmMedium,
            color = AppColors.text.secondary,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardPicker(state: AutopayConfirmState, event: (AutopayConfirmEvent) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { event(AutopayConfirmEvent.DismissCardPicker) },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { event(AutopayConfirmEvent.PickCard(card.id)) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CardMask(card, Modifier.weight(1f), color = AppColors.text.primary)
                        Text(card.expire, style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary)
                        Spacer(Modifier.width(12.dp))
                        RadioMark(card.id == state.selectedCardId)
                    }
                }
                if (state.cards.isNotEmpty()) RowDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { event(AutopayConfirmEvent.AddNewCard) }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
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

@Composable
private fun Checking(checking: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bg.page.copy(alpha = 0.85f))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            CircularProgressIndicator(color = AppColors.action.primary)
            if (checking) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.ap_checking_title), style = AppTypography.titleMdSemiBold, color = AppColors.text.primary)
                Spacer(Modifier.height(6.dp))
                Text(stringResource(Res.string.ap_checking_sub), style = AppTypography.titleSmMedium, color = AppColors.text.secondary, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
internal fun Group(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.bg.section),
    ) { content() }
}

@Composable
internal fun RowDivider() {
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = AppColors.border.divider)
}

@Composable
internal fun InfoRow(
    icon: ImageVector,
    title: String,
    sub: String? = null,
    onClick: (() -> Unit)? = null,
    value: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).background(AppColors.bg.tertiary, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = AppColors.text.secondary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTypography.titleSmMedium, color = AppColors.text.primary)
            if (sub != null) Text(sub, style = AppTypography.bodySmMedium, color = AppColors.text.tertiary)
        }
        value()
        if (onClick != null) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = AppColors.text.tertiary, modifier = Modifier.size(18.dp))
        }
    }
}

/** Voyager ekrani — tasdiqlash. Yangi karta qo'shib qaytganda ro'yxat yangilanadi (`Resumed`). */
class AutopayConfirmScreen(
    val child: ChildArgs,
    val planId: String,
    val period: String,
    val amount: Int,
    val cardId: String? = null,
) : cafe.adriel.voyager.core.screen.Screen {
    @Composable
    override fun Content() {
        val navigator = cafe.adriel.voyager.navigator.LocalNavigator.current
        val vm = koinScreenModel<AutopayConfirmViewModel>()
        val state = vm.state.collectAsStateWithLifecycle().value
        LaunchedEffect(Unit) {
            val childId = child.id ?: return@LaunchedEffect
            vm.onEvent(AutopayConfirmEvent.Init(childId, planId, PlanPeriod.of(period) ?: PlanPeriod.ANNUAL, amount, cardId))
            vm.onEvent(AutopayConfirmEvent.Resumed)
        }
        LaunchedEffect(Unit) {
            vm.effect.collect { e ->
                when (e) {
                    is AutopayConfirmEffect.OpenAddCard -> navigator?.push(
                        uz.tikoncha_parent.presentation.profile.subscription.autopay.card.AutopayAddCardScreen(child)
                    )
                    is AutopayConfirmEffect.Done -> navigator?.replace(
                        uz.tikoncha_parent.presentation.profile.subscription.autopay.result.AutopayResultScreen(
                            child = child,
                            scheduled = e.scheduled,
                            amount = e.amount,
                            paidUntilMs = e.paidUntil?.toEpochMilliseconds(),
                            nextChargeMs = e.nextChargeAt?.toEpochMilliseconds(),
                            vendor = e.card?.vendor?.name,
                            last4 = e.card?.last4,
                        )
                    )
                    AutopayConfirmEffect.CloseToSubscription -> navigator?.let {
                        uz.tikoncha_parent.presentation.profile.subscription.autopay.leavePaymentFlow(it)
                    }
                }
            }
        }
        AutopayConfirmUi(state = state, child = child, event = vm::onEvent, onBack = { navigator?.pop() })
    }
}
