package uz.tikoncha_parent.presentation.profile.personal_information

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.profile
import uz.tikoncha_parent.ui.segmentShape
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

/** Ma'lumot qatori — guruhlangan ro'yxat segmenti (bosilmaydi): tonal doirada ikonka, kichik xira sarlavha, qiymat. */
@Composable
fun PersonalInformationItemRow(
    icon: DrawableResource,
    title: String,
    value: String?,
    segmentIndex: Int = 0,
    segmentCount: Int = 1,
) {
    val shape = segmentShape(segmentIndex, segmentCount)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(shape)
            .background(AppColors.bg.surface, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AppColors.icon.accentPrimary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = AppColors.icon.accentPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.emphasizedSmRegular,
                color = AppColors.text.tertiary,
            )
            Text(
                text = value.orEmpty().ifBlank { "—" },
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun Pre(){
    TikonchaParentTheme(
        ThemeMode.DARK
    ){
        PersonalInformationItemRow(
            icon = Res.drawable.profile,
            title = "Ism",
            value = "Shuxratov Saidburxon Dilmurod o'g'li"
        )
    }
}