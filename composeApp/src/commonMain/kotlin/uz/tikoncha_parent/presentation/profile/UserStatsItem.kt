package uz.tikoncha_parent.presentation.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.font.FontWeight
import uz.tikoncha_parent.ui.expressiveSegment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import uz.tikoncha_parent.ui.AppIconInnerPadding
import uz.tikoncha_parent.ui.ProfileStatsContainerHeight
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.coin
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.AppItemHeight
import uz.tikoncha_parent.ui.SmallCardCornerRadius
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

/**
 * Profil statistikasi plitkasi — M3 Expressive: yonma-yon ikkita plitka bitta guruh (tashqi burchaklar katta),
 * yorliq kichik va xira, qiymat katta va qalin, 3D rasm burchakdan chiqib turadi.
 */
@Composable
fun UserStatsItem(
    title: String,
    value: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    segmentIndex: Int = 0,
    segmentCount: Int = 1,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .expressiveSegment(segmentIndex, segmentCount, color = AppColors.bg.surface, horizontal = true, onClick = onClick)
            .padding(start = 16.dp, top = 14.dp, end = 12.dp, bottom = 12.dp)
    ){
        Column(modifier = Modifier.fillMaxHeight()) {
            Text(
                text = title,
                style = AppTypography.emphasizedSmMedium,
                color = AppColors.text.tertiary,
                maxLines = 1,
            )
            Spacer(Modifier.weight(1f))
            // Son o'zgarsa — sakramaydi, silliq almashadi.
            AnimatedContent(
                targetState = value,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                label = "statValue",
            ) { shown ->
                Text(
                    text = shown,
                    style = AppTypography.headlineSmSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                    color = AppColors.text.primary,
                    maxLines = 1,
                )
            }
        }
        Image(
            painter = icon,
            contentDescription = "",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(52.dp)
                .align(Alignment.BottomEnd)
        )
    }
}

@Preview
@Composable
private fun Pre(){
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ){
        UserStatsItem(
            title = "Tangalaringiz",
            value = "44 ta",
            icon = painterResource(Res.drawable.coin),
            modifier = Modifier.fillMaxWidth().height(ProfileStatsContainerHeight)
        )
    }
}