package uz.tikoncha_parent.presentation.profile.coins

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.minus_symbol
import tikoncha_parents.composeapp.generated.resources.plus_symbol
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun CoinNumberPicker(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChanged: (Int) -> Unit,
    min: Int = 0,
    max: Int = 10000
) {

    val focusManager = LocalFocusManager.current
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value.toString(), TextRange(value.toString().length)))
    }

    Row(
        modifier = modifier
            .height(44.dp)
            .background(AppColors.action.primary.copy(alpha = 0.14f), shape = CircleShape)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                focusManager.clearFocus()
                val newValue = (value - 1).coerceIn(min, max)
                onValueChanged(newValue)
            },
            modifier = Modifier
                .size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = AppColors.icon.accentPrimary,
                disabledContentColor = AppColors.icon.disabledTertiary
            ),
            enabled = value > min
        ) {
            Icon(
                painter = painterResource(Res.drawable.minus_symbol),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
        }

        BasicTextField(
            value = textFieldValue,
            onValueChange = { newTfValue->
                val filtered = newTfValue.text.filter { it.isDigit() }
                if (filtered.isEmpty()){
                    textFieldValue = TextFieldValue(filtered, TextRange(filtered.length))
                }
                else{
                    val parsed = filtered.toIntOrNull() ?: min
                    val clamped = parsed.coerceIn(min, max)
                    val clampedString = clamped.toString()
                    textFieldValue = TextFieldValue(clampedString, TextRange(clampedString.length))
                    onValueChanged(clamped)
                }
            },
            modifier = Modifier
                .width(72.dp)
                .height(28.dp),
            textStyle = TextStyle(
                color = AppColors.text.primary,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.W500
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    innerTextField()
                }
            },
        )

        IconButton(
            onClick = {
                focusManager.clearFocus()
                val newValue = (value + 1).coerceIn(min, max)
                onValueChanged(newValue)
            },
            modifier = Modifier
                .size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = AppColors.icon.accentPrimary,
                disabledContentColor = AppColors.icon.disabledTertiary
            ),
            enabled = value < max
        ) {
            Icon(
                painter = painterResource(Res.drawable.plus_symbol),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}

@Preview
@Composable
private fun Pre() {
    TikonchaParentTheme (
        ThemeMode.LIGHT
    ) {
        CoinNumberPicker(
            value = 0,
            onValueChanged = {}
        )
    }
}

