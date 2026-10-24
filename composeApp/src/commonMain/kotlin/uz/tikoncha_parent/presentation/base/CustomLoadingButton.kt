package uz.tikoncha_parent.presentation.base

import uz.tikoncha_parent.ui.rememberHapticSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import uz.tikoncha_parent.ui.ButtonCornerRadius
import uz.tikoncha_parent.ui.ButtonHeight
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme


@Composable
fun CustomLoadingButton(
    text:String,
    texStyle: TextStyle = AppTypography.titleSmMedium,
    modifier: Modifier = Modifier,
    color: Color = AppColors.button.primaryHover,
    textColor: Color = AppColors.text.inverse,
    enabled:Boolean = true,
    onClick:()->Unit,
    shape: Shape = RoundedCornerShape(16.dp),
    loading:Boolean = false
) {



    val isEnabled = enabled && !loading

    val disabledBg = AppColors.button.disabled
    val disabledContent = AppColors.text.disabledTertiary

    val loadingDisabledBg = color.copy(alpha = 0.55f)
    val loadingDisabledContent = Color.White.copy(alpha = 0.8f)

    val containerColor = when{
        isEnabled -> color
        loading -> loadingDisabledBg
        else -> disabledBg
    }

    val contentColor = when{
        isEnabled -> textColor
        loading -> loadingDisabledContent
        else -> disabledContent
    }


    Button(
        shape = shape,
        enabled = enabled,
        onClick = onClick,
        interactionSource = rememberHapticSource(),
        modifier = modifier
            .height(ButtonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContentColor = contentColor,
            disabledContainerColor = containerColor,
        ),
        contentPadding = PaddingValues(horizontal = 5.dp)
    ) {
        if (loading){
//            CircularProgressIndicator(
//                color = contentColor,
//                modifier = Modifier.size(25.dp),
//                strokeWidth = 3.dp,
//            )
            CustomLoadingIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                lineLength = 3.dp
            )
        } else {
            Text(
                color = contentColor,
                text = text,
                style = texStyle
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(mode = ThemeMode.LIGHT){
        CustomLoadingButton(
            text = "Salom",
            onClick = {},
            loading = true,
            enabled = false
        )
    }


}