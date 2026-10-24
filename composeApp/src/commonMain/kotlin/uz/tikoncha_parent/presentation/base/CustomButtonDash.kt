package uz.tikoncha_parent.presentation.base


import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import uz.tikoncha_parent.ui.expressiveButtonShapes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import uz.tikoncha_parent.ui.*
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import kotlin.time.TimeSource
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomButtonDash(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    border: Boolean = true,
    borderWidth: Dp = 1.dp,
    dashLength: Dp = 6.dp,
    dashGap: Dp = 3.dp,
    height: Dp = DialogButtonHeight,
    style: TextStyle = AppTypography.titleMdSemiBold,
    color: Color = AppColors.bg.surface,
    textColor: Color = AppColors.text.accentEmphasis,
    borderColor: Color = AppColors.border.accentEmphasis,
    leadingIcon: (@Composable () -> Unit)? = null,
    endingIcon: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(ButtonCornerRadius),
    disabledContainerColor: Color = AppColors.section.disabledTertiary,
    contentPadding: PaddingValues = PaddingValues(horizontal = 5.dp),
) {
    val contentColor = if (enabled) textColor else AppColors.text.disabledTertiary
    val actualBorderColor = if (border) borderColor else Color.Transparent
    val timSource = TimeSource.Monotonic
    var lastClickTime by remember { mutableStateOf(timSource.markNow()) }

    val debouncedClick = {
        if (lastClickTime.elapsedNow().inWholeMilliseconds > 600L) {
            lastClickTime = timSource.markNow()
            onClick()
        }
    }

    // M3 Expressive: punktir chegara o'rniga tonal (shaffof karamel) yumaloq tugma — bosilganda shakli siqiladi.
    // `border`/`dash*` parametrlari eski chaqiruvlar uchun qoldirilgan.
    Button(
        onClick = debouncedClick,
        shapes = expressiveButtonShapes(),
        modifier = modifier.height(maxOf(height, 52.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = textColor.copy(alpha = 0.12f),
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = AppColors.text.disabledTertiary
        ),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            SpaceSmall()
        }
        Text(color = contentColor, style = style, text = text)
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
        ThemeMode.LIGHT
    ) {
        CustomButtonDash(
            text = "OK",
            onClick = {}
        )
    }
}