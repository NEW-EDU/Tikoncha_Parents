package uz.tikoncha_parent.presentation.profile.logout

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

@Composable
fun BulletTex(
    text: String,
    style: TextStyle = AppTypography.bodyMdRegular,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•  ",
            style = style,
            color = AppColors.text.accentSuccess
        )
        Text(
            text = text,
            style = style,
            color = AppColors.text.secondary
        )
    }
}