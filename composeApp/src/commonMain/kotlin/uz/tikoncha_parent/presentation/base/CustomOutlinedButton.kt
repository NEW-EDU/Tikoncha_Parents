package uz.tikoncha_parent.presentation.base

import uz.tikoncha_parent.ui.rememberHapticSource
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import uz.tikoncha_parent.ui.expressiveButtonShapes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import uz.tikoncha_parent.ui.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomOutlinedButton(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = AppTypography.titleMdSemiBold,
    containerColor: Color = Color.Transparent,
    contentColor: Color = AppColors.text.accentEmphasis,
    borderColor: Color = AppColors.border.primary,
    enabled: Boolean = true,
    onClick: () -> Unit,
    shape: Shape = CircleShape,
    leadingIcon: (@Composable () -> Unit)? = null,
    endingIcon: (@Composable () -> Unit)? = null,
    disabledContainerColor: Color = AppColors.button.disabled,
    disabledContentColor: Color = AppColors.text.disabledTertiary
) {


    OutlinedButton(
        onClick = onClick,
        interactionSource = rememberHapticSource(),
        shapes = expressiveButtonShapes(shape),
        modifier = modifier
            .height(ButtonDefaults.MediumContainerHeight),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
        border = BorderStroke(1.dp, borderColor),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 5.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .padding(horizontal = 10.dp)
        ) {

            if (leadingIcon != null) {
                leadingIcon()
            }

            Text(
                text = text,
                style = style,
                textAlign = TextAlign.Center
            )

            if (endingIcon != null) {
                endingIcon()
            }
        }
    }
}

@Preview()
@Composable
private fun Pre() {
    TikonchaParentTheme {
        CustomOutlinedButton(
            text = "smile",
            onClick = {},
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.lock),
                    contentDescription = "",
                    tint = PrimaryColor,
                )
            },
            enabled = true
        )
    }
}
