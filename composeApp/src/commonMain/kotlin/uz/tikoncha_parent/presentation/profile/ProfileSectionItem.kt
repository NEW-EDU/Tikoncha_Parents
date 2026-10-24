package uz.tikoncha_parent.presentation.profile

import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.heightIn
import uz.tikoncha_parent.ui.expressiveSegment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.profile
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.SmallIconSize
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

/**
 * Profil menyusi qatori — M3 Expressive guruhlangan ro'yxat elementi: guruhning tashqi burchaklari katta, ichkilari
 * kichik, elementlar orasida ingichka bo'shliq; chapda tonal doiradagi ikonka, o'ngda strelka. [divider] eskirgan —
 * guruh uslubida chiziq kerak emas (imzo mos kelishi uchun qoldirilgan).
 */
@Composable
fun ProfileSectionItem(
    icon: Painter,
    title: String,
    onItemClick: () -> Unit,
    divider: Boolean = true,
    textColor: Color = AppColors.text.primary,
    iconColor: Color = AppColors.icon.accentPrimary,
    segmentIndex: Int = 0,
    segmentCount: Int = 1,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .expressiveSegment(segmentIndex, segmentCount, color = AppColors.bg.surface) { onItemClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            color = textColor,
            style = AppTypography.titleMdSemiBold,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = AppColors.icon.secondary,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** Profil menyusidagi guruh sarlavhasi (kichik, xira — e'tiborni elementlarga qoldiradi). */
@Composable
fun ProfileSectionLabel(text: String) {
    Text(
        text = text,
        style = AppTypography.titleSmMedium,
        color = AppColors.text.tertiary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Preview
@Composable
private fun Pre() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        ProfileSectionItem(
            icon = painterResource(Res.drawable.profile),
            title = "Shaxsiy",
            onItemClick = {}
        )
    }
}