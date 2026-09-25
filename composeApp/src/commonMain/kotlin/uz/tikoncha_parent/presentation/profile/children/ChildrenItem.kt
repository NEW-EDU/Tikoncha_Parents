package uz.tikoncha_parent.presentation.profile.children

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.link
import tikoncha_parents.composeapp.generated.resources.ohirgi_faollik
import tikoncha_parents.composeapp.generated.resources.plus_home_sheet_subscribe
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun ChildrenItem(
    modifier: Modifier = Modifier,
    endingIcon: (@Composable () -> Unit)? = null,
    onMenuClick: () -> Unit = {},
    onClick: () -> Unit = {},
    imageUrl: String = "",
    lastSeen: String = "",
    gadget: String = "",
    name: String = "",
    subscription: String? = null,
) {
    val hasSubscription = !subscription.isNullOrBlank() && subscription != "FREE"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .singleClick { onClick() }
            .background(AppColors.bg.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Avatar: clip qilingan doira + yonida (kesilmaydigan) badge ──
        Box(
            modifier = Modifier.size(61.dp),
            contentAlignment = Alignment.Center
        ) {
            // Doira + rasm (clip qilinadi, border shu yerda)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .then(
                        if (hasSubscription) {
                            Modifier.border(2.dp, AppColors.border.accentSuccess, CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .background(AppColors.bg.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "",
                    error = painterResource(Res.drawable.profile_hedgehog_img),
                    placeholder = painterResource(Res.drawable.profile_hedgehog_img),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            // Obuna belgisi — clip QILINMAGAN tashqi Box ichida, shuning uchun kesilmaydi
            if (hasSubscription) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-8).dp)
                ) {
                    Image(
                        painter = painterResource(Res.drawable.plus_home_sheet_subscribe),
                        contentDescription = "",
                        modifier = Modifier
                            .height(22.dp)
                            .width(36.dp)
                    )
                }
            }
        }
        Space(18.dp)

        // ── Ism → telefon → oxirgi faollik (har biri o'z qatorida) ──
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = gadget,
                style = AppTypography.titleSmMedium,
                color = AppColors.text.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (lastSeen.isNotBlank()) {
                Text(
                    text = "${stringResource(Res.string.ohirgi_faollik)} $lastSeen",
                    style = AppTypography.emphasizedSmRegular,
                    color = AppColors.text.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(12.dp))

        if (endingIcon != null) {
            endingIcon()
        } else {
            Icon(
                painter = painterResource(Res.drawable.link),
                contentDescription = "",
                tint = AppColors.icon.secondary,
                modifier = Modifier
                    .size(24.dp)
                    .singleClick { onMenuClick() }
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        ChildrenItem(
            name = "Jaloliddin",
            gadget = "+998901234567",
            lastSeen = "Bugun 10:05",
            subscription = "PLUS"
        )
    }
}