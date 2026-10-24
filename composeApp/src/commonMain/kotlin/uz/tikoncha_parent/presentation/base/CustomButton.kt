package uz.tikoncha_parent.presentation.base

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import uz.tikoncha_parent.ui.expressiveButtonShapes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextOverflow
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
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    color: Color = AppColors.action.primary,
    textColor: Color = AppColors.text.inverse,
    endingIcon: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    style: TextStyle = AppTypography.titleMdSemiBold,
    /** Dam olishdagi shakl; sukut — M3 Expressive bo'yicha to'liq yumaloq, bosilganda siqiladi. */
    shape: Shape = CircleShape,
    disabledContainerColor: Color = MaterialTheme.extendedColor.disabledBgColor
) {

    val contentColor = if (enabled) textColor else AppColors.text.disabledTertiary

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
        shapes = expressiveButtonShapes(shape),
        modifier = modifier
            .height(ButtonDefaults.MediumContainerHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = AppColors.text.disabled
        ),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            SpaceSmall()
        }

        Text(
            text = text,
            maxLines = 1,
            style = style,
            color = contentColor,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 2.dp),
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
        CustomButton(
            text = "OK",
            onClick = {}
        )
    }
}