package uz.tikoncha_parent.presentation.otp

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uz.tikoncha_parent.ui.TextFieldCornerRadius
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Stable
private fun String.onlyDigits(max: Int): String = filter(Char::isDigit).take(max)

/**
 * Kod kiritish: BITTA matn maydoni, katakchalar faqat chizilgan. Shuning uchun yozish, o'chirish (backspace
 * ketma-ket hamma raqamni o'chiradi) va joylash (paste) oddiy maydondagidek ishlaydi — har katakni alohida
 * bosish shart emas. Bosilgan joydan qat'i nazar kursor oxirida turadi.
 */
@Composable
fun OtpInput(
    otpLength: Int = 6,
    otpText: String? = null,
    textColor: Color = AppColors.text.primary,
    autoFocus: Boolean = true,
    onOtpUpdate: (String) -> Unit,
) {
    val onOtpUpdateLatest by rememberUpdatedState(onOtpUpdate)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var value by remember { mutableStateOf(TextFieldValue(otpText?.onlyDigits(otpLength).orEmpty())) }
    var focused by remember { mutableStateOf(false) }

    LaunchedEffect(otpLength, autoFocus) {
        if (autoFocus) focusRequester.requestFocus()
    }

    // Tashqaridan kelgan qiymat (masalan, SMS'dan avtomatik to'ldirish yoki tozalash)
    LaunchedEffect(otpText, otpLength) {
        val incoming = otpText?.onlyDigits(otpLength).orEmpty()
        if (incoming != value.text) value = TextFieldValue(incoming, TextRange(incoming.length))
    }

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            val digits = newValue.text.onlyDigits(otpLength)
            val changed = digits != value.text
            value = TextFieldValue(digits, TextRange(digits.length))
            if (changed) onOtpUpdateLatest(digits)
            // Kod to'liq kiritildi — klaviatura yopiladi (raqam klaviaturasida yopish tugmasi yo'q)
            if (changed && digits.length == otpLength) focusManager.clearFocus()
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        cursorBrush = SolidColor(Color.Transparent),
        textStyle = AppTypography.headlineMdMedium.merge(color = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { _ ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth(),
            ) {
                val text = value.text
                val active = text.length.coerceAtMost(otpLength - 1)
                repeat(otpLength) { index ->
                    val isActive = focused && index == active
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .sizeIn(minWidth = 34.dp, minHeight = 44.dp)
                            .clip(RoundedCornerShape(TextFieldCornerRadius))
                            // Fon rangidan qat'i nazar ko'rinsin (qorong'i rejimda field rangi karta bilan bir xil edi)
                            .background(AppColors.text.primary.copy(alpha = 0.07f))
                            .border(
                                if (isActive) 1.5.dp else 1.dp,
                                if (isActive) AppColors.action.primary else AppColors.text.primary.copy(alpha = 0.14f),
                                RoundedCornerShape(TextFieldCornerRadius),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = text.getOrNull(index)?.toString().orEmpty(),
                            style = AppTypography.headlineMdMedium.merge(textAlign = TextAlign.Center, color = textColor),
                        )
                    }
                }
            }
        },
    )
}

@Composable
@Preview
private fun Preview() {
    TikonchaParentTheme(ThemeMode.DARK) {
        OtpInput(otpText = "1234", onOtpUpdate = {})
    }
}
