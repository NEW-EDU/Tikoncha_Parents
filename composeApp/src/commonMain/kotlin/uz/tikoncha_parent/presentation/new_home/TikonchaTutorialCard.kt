package uz.tikoncha_parent.presentation.new_home

import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toShape
import androidx.compose.material3.MaterialShapes
import uz.tikoncha_parent.ui.expressiveCard
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.hedgehog_heart
import tikoncha_parents.composeapp.generated.resources.play
import tikoncha_parents.composeapp.generated.resources.tikoncha_title_bottom
import tikoncha_parents.composeapp.generated.resources.tikoncha_title_top
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.NunitoFontFamily
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun TikonchaTutorialCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .simpleShadow(RoundedCornerShape(32.dp))
            .expressiveCard(color = AppColors.modal.primary, shape = RoundedCornerShape(32.dp), onClick = onClick)
            .padding(start = 25.dp, end = 25.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Column {
            Text(
                text = stringResource(Res.string.tikoncha_title_top),
                color = AppColors.text.primary,
                fontFamily = NunitoFontFamily(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(Res.string.tikoncha_title_bottom),
                color = AppColors.text.accentEmphasis,
                fontFamily = NunitoFontFamily(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(start = 4.dp)
            )
        }
        Space(16.dp)
        ExpressivePlayBadge()
        Space(16.dp)
        Image(
            painter = painterResource(Res.drawable.hedgehog_heart),
            contentDescription = "",
            modifier = Modifier
                .height(120.dp)
        )
    }
}

@Preview
@Composable
private fun Pre(){
    TikonchaParentTheme {
        TikonchaTutorialCard(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {}
        )
    }
}

/**
 * M3 Expressive "hero" lahzasi: o'ynash tugmasi Material shakllar kutubxonasidagi 9 qirrali "Cookie" ichida,
 * sekin aylanib turadi (ikonka esa tik turadi). Sahifada yagona shunday shakl — imzo uslubi sarf qilinmaydi.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressivePlayBadge() {
    val spin by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 24_000, easing = LinearEasing)),
    )
    val cookie = MaterialShapes.Cookie9Sided.toShape()
    Box(
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer { rotationZ = spin }
            .clip(cookie)
            .background(AppColors.button.primary),
        contentAlignment = Alignment.Center,
    ) {
        // Oq "play" uchburchagi (optik markaz uchun biroz o'ngga suriladi)
        Canvas(Modifier.size(20.dp).offset(x = 2.dp)) {
            val path = Path().apply {
                moveTo(size.width * 0.12f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(size.width * 0.12f, size.height)
                close()
            }
            drawPath(path, Color.White, style = Fill)
        }
    }
}
