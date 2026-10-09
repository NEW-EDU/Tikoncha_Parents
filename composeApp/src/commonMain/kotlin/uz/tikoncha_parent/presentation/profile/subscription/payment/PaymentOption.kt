package uz.tikoncha_parent.presentation.profile.subscription.payment

import uz.tikoncha_parent.ui.theme.AppTypography
import androidx.compose.material3.Text
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.*
import uz.tikoncha_parent.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.click_pay
import uz.tikoncha_parent.presentation.base.CustomRadio
import uz.tikoncha_parent.presentation.base.CustomText
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor

@Composable
fun PaymentOption(
    modifier: Modifier = Modifier,
    painter: Painter = painterResource(Res.drawable.click_pay),
    /** Rasm o'rniga yozuv (masalan "App Store" — logosi yo'q). */
    label: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .simpleShadow(RoundedCornerShape(16.dp))
            .background(AppColors.bg.surface, RoundedCornerShape(16.dp))
            .singleClick { onClick() }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(8.dp))
            ){
                if (label != null) {
                    Text(
                        text = label,
                        style = AppTypography.titleSmSemiBold,
                        color = Color(0xFF111111),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                } else {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .width(63.dp)
                            .height(16.dp),
                    )
                }
            }

            CustomRadio(
                checked = isSelected,
                onChecked = { onClick() }
            )
        }
    }
}



@Composable
@Preview
private fun Preview() {
    TikonchaParentTheme(mode = ThemeMode.DARK){
        PaymentOption(
            onClick = {},
            isSelected = false
        )
    }

}
