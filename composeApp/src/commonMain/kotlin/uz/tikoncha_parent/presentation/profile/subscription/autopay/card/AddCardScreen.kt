@file:OptIn(cafe.adriel.voyager.core.annotation.InternalVoyagerApi::class)

package uz.tikoncha_parent.presentation.profile.subscription.autopay.card

import uz.tikoncha_parent.presentation.base.LoadingDialog
import cafe.adriel.voyager.koin.koinScreenModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.internal.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.domain.model.autopay.CardVendor
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.BrandMark
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * Karta qo'shish (1/2) va SMS kod (2/2). Pul olmaydi — buni ikkinchi qadamda ochiq aytamiz
 * (ClubTime'da foydalanuvchi SMS'dan keyin darhol pul yechilganidan chalg'igan, 2026-09-17).
 */
@Composable
fun AddCardUi(state: AddCardState, event: (AddCardEvent) -> Unit) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    BackHandler(true) { event(AddCardEvent.Back) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page)
            .imePadding(),
    ) {
        CustomHeader(
            showBackButton = true,
            title = stringResource(if (state.step == AddCardStep.FORM) Res.string.ap_add_card_title else Res.string.ap_otp_step_title),
            onBackClick = { event(AddCardEvent.Back) },
        )
        Stepper(current = if (state.step == AddCardStep.FORM) 1 else 2)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            when (state.step) {
                AddCardStep.FORM -> CardForm(state, event)
                AddCardStep.OTP -> OtpStep(state, event)
            }
        }
        // Kod olish / qayta yuborish / tasdiqlash — Paylov javobini kutish ko'rinsin
        LoadingDialog(state.submitting)
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 22.dp)) {
            if (state.step == AddCardStep.FORM) {
                CustomButton(
                    text = stringResource(Res.string.ap_get_sms),
                    enabled = state.formReady && !state.numberInvalid && !state.expireInvalid && !state.submitting,
                    onClick = { event(AddCardEvent.SendSms) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                )
            } else {
                CustomButton(
                    text = stringResource(Res.string.ap_confirm),
                    enabled = state.otpReady && !state.submitting,
                    onClick = { event(AddCardEvent.Confirm) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                )
            }
        }
    }
}

@Composable
private fun Stepper(current: Int) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(2) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(if (i < current) AppColors.action.primary else AppColors.bg.tertiary, RoundedCornerShape(2.dp)),
            )
        }
    }
}

@Composable
private fun CardForm(state: AddCardState, event: (AddCardEvent) -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Spacer(Modifier.height(10.dp))
    CardPreview(state)
    Spacer(Modifier.height(16.dp))

    FieldLabel(stringResource(Res.string.ap_card_number))
    DigitField(
        value = state.number,
        onValueChange = { event(AddCardEvent.NumberChanged(it)) },
        placeholder = "0000 0000 0000 0000",
        transformation = CardNumberTransformation,
        error = state.numberInvalid || state.notSupported,
        modifier = Modifier.focusRequester(focus),
        trailing = { if (state.vendor != CardVendor.OTHER) BrandMark(state.vendor, 18.dp) },
    )
    when {
        state.notSupported -> FieldError(stringResource(Res.string.ap_only_local_cards))
        state.numberInvalid -> FieldError(stringResource(Res.string.ap_card_number_invalid))
    }
    Spacer(Modifier.height(16.dp))

    FieldLabel(stringResource(Res.string.ap_card_expire))
    DigitField(
        value = state.expire,
        onValueChange = { event(AddCardEvent.ExpireChanged(it)) },
        placeholder = stringResource(Res.string.ap_expire_hint),
        transformation = ExpireTransformation,
        error = state.expireInvalid,
        imeAction = ImeAction.Done,
    )
    if (state.expireInvalid) FieldError(stringResource(Res.string.ap_expire_invalid))
    state.error?.let { FieldError(it.asText()) }
    Spacer(Modifier.height(16.dp))

    Note(icon = { Icon(Icons.Rounded.Lock, null, tint = AppColors.action.primary, modifier = Modifier.size(18.dp)) }, text = stringResource(Res.string.ap_card_security_note))
    Spacer(Modifier.height(16.dp))
}

/** Jonli karta ko'rinishi: raqam yozilishi bilan to'ladi, tur belgisi o'ng burchakda. */
@Composable
private fun CardPreview(state: AddCardState) {
    val grouped = state.number.padEnd(16, '•').chunked(4).joinToString(" ")
    val exp = if (state.expire.isEmpty()) stringResource(Res.string.ap_expire_hint)
    else state.expire.padEnd(4, '•').let { "${it.take(2)} / ${it.drop(2)}" }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(186.dp)
            .background(
                Brush.linearGradient(listOf(AppColors.bg.secondaryBrand, AppColors.action.primary)),
                RoundedCornerShape(20.dp),
            )
            .padding(18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 40.dp, height = 30.dp).background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(6.dp)))
            Spacer(Modifier.weight(1f))
            if (state.vendor != CardVendor.OTHER) BrandMark(state.vendor, 26.dp)
        }
        Spacer(Modifier.weight(1f))
        Text(text = grouped, style = AppTypography.headlineSmSemiBold.copy(letterSpacing = 2.sp), color = Color.White)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(text = stringResource(Res.string.ap_expire_caps), style = AppTypography.bodySmMedium, color = Color.White.copy(alpha = 0.75f), modifier = Modifier.weight(1f))
            Text(text = exp, style = AppTypography.bodyMdSemiBold, color = Color.White)
        }
    }
}

@Composable
private fun OtpStep(state: AddCardState, event: (AddCardEvent) -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Column(Modifier.fillMaxWidth().padding(top = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp).background(AppColors.bg.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Sms, null, tint = AppColors.action.primary, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(text = stringResource(Res.string.ap_otp_title), style = AppTypography.titleLgSemiBold, color = AppColors.text.primary)
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(Res.string.ap_otp_sent_to, state.phoneMask.ifBlank { "—" }),
            style = AppTypography.titleSmMedium, color = AppColors.text.secondary, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        OtpCells(value = state.otp, onValueChange = { event(AddCardEvent.OtpChanged(it)) }, modifier = Modifier.focusRequester(focus))
        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(text = it.asText(), style = AppTypography.bodyMdMedium, color = AppColors.text.accentDanger, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(16.dp))
        if (state.resendIn > 0) {
            Text(
                text = stringResource(Res.string.ap_otp_resend_in, "0:" + state.resendIn.toString().padStart(2, '0')),
                style = AppTypography.titleSmMedium, color = AppColors.text.tertiary,
            )
        } else {
            Text(
                text = stringResource(Res.string.ap_otp_resend),
                style = AppTypography.titleSmSemiBold, color = AppColors.action.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { event(AddCardEvent.Resend) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(22.dp))
        Note(icon = { Icon(Icons.Rounded.CreditCard, null, tint = AppColors.text.secondary, modifier = Modifier.size(18.dp)) }, text = stringResource(Res.string.ap_otp_note))
    }
}

@Composable
private fun OtpCells(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        modifier = modifier,
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(6) { i ->
                    val active = i == value.length
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 54.dp)
                            .background(AppColors.bg.section, RoundedCornerShape(14.dp))
                            .border(if (active) 2.dp else 1.dp, if (active) AppColors.action.primary else AppColors.border.secondary.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = value.getOrNull(i)?.toString() ?: "", style = AppTypography.headlineSmSemiBold, color = AppColors.text.primary)
                    }
                }
            }
        },
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, style = AppTypography.bodyMdSemiBold, color = AppColors.text.secondary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
}

@Composable
private fun FieldError(text: String) {
    Text(text = text, style = AppTypography.bodyMdMedium, color = AppColors.text.accentDanger, modifier = Modifier.padding(start = 4.dp, top = 6.dp))
}

@Composable
private fun DigitField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    transformation: VisualTransformation,
    error: Boolean,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    trailing: @Composable () -> Unit = {},
) {
    val border = if (error) AppColors.text.accentDanger else AppColors.border.secondary.copy(alpha = 0.25f)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = transformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        textStyle = TextStyle(color = AppColors.text.primary, fontSize = 17.sp, letterSpacing = 1.sp),
        cursorBrush = SolidColor(AppColors.action.primary),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(AppColors.bg.section, RoundedCornerShape(14.dp))
                    .border(1.dp, border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(text = placeholder, style = AppTypography.titleMdMedium, color = AppColors.text.placeholder)
                    inner()
                }
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        },
    )
}

@Composable
private fun Note(icon: @Composable () -> Unit, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(AppColors.bg.section, RoundedCornerShape(14.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        icon()
        Text(text = text, style = AppTypography.bodyMdMedium, color = AppColors.text.secondary)
    }
}

/** "8600123412341234" → "8600 1234 1234 1234". */
private object CardNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val out = raw.chunked(4).joinToString(" ")
        return TransformedText(AnnotatedString(out), object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset + (offset - 1).coerceAtLeast(0) / 4
            override fun transformedToOriginal(offset: Int): Int = (offset - offset / 5).coerceIn(0, raw.length)
        })
    }
}

/** "0929" → "09 / 29". */
private object ExpireTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val out = if (raw.length > 2) "${raw.take(2)} / ${raw.drop(2)}" else raw
        return TransformedText(AnnotatedString(out), object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = if (offset > 2) offset + 3 else offset
            override fun transformedToOriginal(offset: Int): Int = (if (offset > 4) offset - 3 else offset.coerceAtMost(2)).coerceIn(0, raw.length)
        })
    }
}

/**
 * Voyager ekrani. Karta ulangach: to'lov oqimidan kelgan bo'lsa — tasdiqlash ekraniga (shu karta tanlangan),
 * aks holda (Kartalarim / karta almashtirish) — orqaga.
 */
class AutopayAddCardScreen(
    val child: uz.tikoncha_parent.presentation.profile.subscription.autopay.method.ChildArgs? = null,
    val planId: String? = null,
    val period: String? = null,
    val amount: Int = 0,
    val openConfirmAfter: Boolean = false,
) : cafe.adriel.voyager.core.screen.Screen {
    @Composable
    override fun Content() {
        val navigator = cafe.adriel.voyager.navigator.LocalNavigator.current
        val vm = koinScreenModel<AddCardViewModel>()
        val state = vm.state.collectAsStateWithLifecycle().value
        LaunchedEffect(Unit) {
            vm.effect.collect { e ->
                when (e) {
                    is AddCardEffect.Added -> {
                        val c = child
                        if (openConfirmAfter && c?.id != null && planId != null && period != null) {
                            navigator?.replace(
                                uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm.AutopayConfirmScreen(c, planId, period, amount, cardId = e.card.id)
                            )
                        } else {
                            navigator?.pop()
                        }
                    }
                    AddCardEffect.Close -> navigator?.pop()
                }
            }
        }
        AddCardUi(state = state, event = vm::onEvent)
    }
}
