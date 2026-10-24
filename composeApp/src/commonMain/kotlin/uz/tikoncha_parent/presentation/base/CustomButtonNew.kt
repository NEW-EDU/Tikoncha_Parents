package uz.tikoncha_parent.presentation.base

import uz.tikoncha_parent.ui.rememberHapticSource
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import uz.tikoncha_parent.ui.expressiveButtonShapes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import uz.tikoncha_parent.ui.*


import uz.tikoncha_parent.ui.TextFieldCornerRadius
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import kotlin.time.TimeSource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomButtonNew(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = AppTypography.titleMdSemiBold,
    containerColor: Color = AppColors.button.primary,
    contentColor: Color = AppColors.text.inverse,
    enabled: Boolean = true,
    onClick: () -> Unit,
    leadingIcon: (@Composable () -> Unit)? = null,
    endingIcon: (@Composable () -> Unit)? = null,
    /** Dam olishdagi shakl; sukut — M3 Expressive bo'yicha to'liq yumaloq, bosilganda siqiladi. */
    shape: Shape = CircleShape,
    disabledContainerColor: Color = AppColors.button.disabled,
    disabledContentColor: Color = AppColors.text.disabledTertiary
) {

    val timSource = TimeSource.Monotonic
    var lastClickTime by remember { mutableStateOf(timSource.markNow()) }

    val debouncedClick = {
        if (lastClickTime.elapsedNow().inWholeMilliseconds > 600L) {
            lastClickTime = timSource.markNow()
            onClick()
        }
    }

    Button(
        onClick = debouncedClick,
        interactionSource = rememberHapticSource(),
        shapes = expressiveButtonShapes(shape),
        modifier = modifier
            .height(ButtonDefaults.MediumContainerHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        ),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            SpaceSmall()
        }

        Text(
            color = contentColor,
            text = text,
            style = style
        )

        if (endingIcon != null) {
            SpaceSmall()
            endingIcon()
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        CustomButtonNew(
            text = "OK",
            onClick = {}
        )
    }
}