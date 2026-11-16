package uz.tikoncha_parent.presentation.profile.settings.text_size

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.text_size_extra_large
import tikoncha_parents.composeapp.generated.resources.text_size_footer
import tikoncha_parents.composeapp.generated.resources.text_size_large
import tikoncha_parents.composeapp.generated.resources.text_size_preview
import tikoncha_parents.composeapp.generated.resources.text_size_small
import tikoncha_parents.composeapp.generated.resources.text_size_standard
import tikoncha_parents.composeapp.generated.resources.text_size_title
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.island.BottomAnchoredColumn
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.expressiveSegment
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.TextSize
import uz.tikoncha_parent.ui.theme.TextSizeController
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class TextSizeScreen : Screen {
    @Composable
    override fun Content() {
        TextSizeUi()
    }
}

/*
 * Matn o'lchami — Tebranish ekranidagidek guruhlangan ro'yxat. Saqlash tugmasi yo'q: tanlov darhol butun ilovaga
 * qo'llanadi (shu ekranning o'zi ham kattalashadi), tepada namuna matn turadi.
 */
@Composable
private fun TextSizeUi() {
    val navigator = LocalNavigator.current
    val current by TextSizeController.size.collectAsState()
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ) {
        CustomHeader(
            title = stringResource(Res.string.text_size_title),
            showBackButton = true,
            onBackClick = { navigator?.pop() },
        )
        BottomAnchoredColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = ContainerPadding, end = ContainerPadding, bottom = 8.dp),
        ) {
            // Namuna: tanlangan o'lchamda haqiqiy matn
            Text(
                text = stringResource(Res.string.text_size_preview),
                style = AppTypography.titleMdMedium,
                color = AppColors.text.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.bg.surface)
                    .padding(16.dp),
            )
            Spacer(Modifier.size(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
                val all = TextSize.entries
                all.forEachIndexed { index, size ->
                    TextSizeRow(
                        size = size,
                        selected = size == current,
                        index = index,
                        count = all.size,
                        onClick = { TextSizeController.set(size) },
                    )
                }
            }
            Text(
                text = stringResource(Res.string.text_size_footer),
                style = AppTypography.emphasizedSmRegular,
                color = AppColors.text.tertiary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            )
        }
    }
}

private val TextSize.title: StringResource
    get() = when (this) {
        TextSize.SMALL -> Res.string.text_size_small
        TextSize.STANDARD -> Res.string.text_size_standard
        TextSize.LARGE -> Res.string.text_size_large
        TextSize.EXTRA_LARGE -> Res.string.text_size_extra_large
    }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TextSizeRow(size: TextSize, selected: Boolean, index: Int, count: Int, onClick: () -> Unit) {
    val container by animateColorAsState(
        targetValue = if (selected) AppColors.action.primary.copy(alpha = 0.14f) else AppColors.bg.surface,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "textSizeRow",
    )
    val checkScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "textSizeCheck",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .expressiveSegment(index, count, color = container, selected = selected, debounceMs = 150L, haptic = true, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // "A" belgisi — har qatorda o'z o'lchamida (joriy tanlovga bog'liq emas), farq bir qarashda ko'rinadi
        val base = LocalDensity.current
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AppColors.icon.accentPrimary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = size.scale)) {
                Text("A", fontSize = 18.sp, style = AppTypography.titleMdSemiBold, color = AppColors.icon.accentPrimary)
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = stringResource(size.title),
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.primary,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .graphicsLayer { scaleX = checkScale; scaleY = checkScale; alpha = checkScale }
                .clip(CircleShape)
                .background(AppColors.action.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = AppColors.icon.inverse, modifier = Modifier.size(18.dp))
        }
    }
}
