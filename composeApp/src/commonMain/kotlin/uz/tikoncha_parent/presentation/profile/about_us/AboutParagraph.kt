package uz.tikoncha_parent.presentation.profile.about_us

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

// AboutBlocks.kt
@Composable
fun AboutParagraph(text: org.jetbrains.compose.resources.StringResource) {
    Text(
        text = stringResource(text),
        style = AppTypography.emphasizedMdRegular,
        color = AppColors.text.primary,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AboutHeading(text: org.jetbrains.compose.resources.StringResource) {
    Text(
        text = stringResource(text),
        style = AppTypography.titleMdSemiBold,
        color = AppColors.text.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
fun AboutBullet(text: org.jetbrains.compose.resources.StringResource) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp, end = 12.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(AppColors.action.primary)
        )
        Text(
            text = stringResource(text),
            style = AppTypography.emphasizedMdRegular,
            color = AppColors.text.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AboutQuote(text: org.jetbrains.compose.resources.StringResource) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.bg.tertiary)
            .padding(16.dp)
    ) {
        // IntrinsicSize.Min — qator balandligi matnga teng bo'ladi, chiziq butun matn bo'ylab cho'ziladi.
        // Busiz LazyColumn ichida fillMaxHeight() ishlamaydi va chiziq 24dp'da qoladi.
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .heightIn(min = 24.dp)
                    .fillMaxHeight()
                    .background(AppColors.action.primary)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(text),
                style = AppTypography.emphasizedMdMedium,
                fontStyle = FontStyle.Italic,
                color = AppColors.text.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}