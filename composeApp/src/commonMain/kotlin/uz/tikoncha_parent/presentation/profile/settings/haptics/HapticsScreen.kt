package uz.tikoncha_parent.presentation.profile.settings.haptics

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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.haptics_footer
import tikoncha_parents.composeapp.generated.resources.haptics_light
import tikoncha_parents.composeapp.generated.resources.haptics_light_desc
import tikoncha_parents.composeapp.generated.resources.haptics_off
import tikoncha_parents.composeapp.generated.resources.haptics_off_desc
import tikoncha_parents.composeapp.generated.resources.haptics_section
import tikoncha_parents.composeapp.generated.resources.haptics_standard
import tikoncha_parents.composeapp.generated.resources.haptics_standard_desc
import tikoncha_parents.composeapp.generated.resources.haptics_strong
import tikoncha_parents.composeapp.generated.resources.haptics_strong_desc
import tikoncha_parents.composeapp.generated.resources.haptics_title
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.island.BottomAnchoredColumn
import uz.tikoncha_parent.presentation.profile.ProfileSectionLabel
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.Haptics
import uz.tikoncha_parent.ui.HapticsLevel
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.expressiveSegment
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class HapticsScreen : Screen {
    @Composable
    override fun Content() {
        HapticsUi()
    }
}

/*
 * Tebranish kuchi — Til ekranidagidek guruhlangan ro'yxat: tanlangan qator to'liq yumaloqlanib tonal rangga kiradi.
 * Saqlash tugmasi yo'q: tanlov darhol qo'llanadi va o'sha kuchdagi zarba bilan "ko'rsatiladi".
 */
@Composable
private fun HapticsUi() {
    val navigator = LocalNavigator.current
    var current by remember { mutableStateOf(Haptics.level) }
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
            title = stringResource(Res.string.haptics_title),
            showBackButton = true,
            onBackClick = { navigator?.pop() },
        )
        BottomAnchoredColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = ContainerPadding, end = ContainerPadding, bottom = 8.dp),
        ) {
            ProfileSectionLabel(stringResource(Res.string.haptics_section))
            Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
                val all = HapticsLevel.entries
                all.forEachIndexed { index, level ->
                    HapticsRow(
                        level = level,
                        selected = level == current,
                        index = index,
                        count = all.size,
                        onClick = {
                            current = level
                            Haptics.level = level
                            Haptics.preview(level)
                        },
                    )
                }
            }
            Text(
                text = stringResource(Res.string.haptics_footer),
                style = AppTypography.emphasizedSmRegular,
                color = AppColors.text.tertiary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            )
        }
    }
}

private val HapticsLevel.title: StringResource
    get() = when (this) {
        HapticsLevel.OFF -> Res.string.haptics_off
        HapticsLevel.LIGHT -> Res.string.haptics_light
        HapticsLevel.STANDARD -> Res.string.haptics_standard
        HapticsLevel.STRONG -> Res.string.haptics_strong
    }

private val HapticsLevel.desc: StringResource
    get() = when (this) {
        HapticsLevel.OFF -> Res.string.haptics_off_desc
        HapticsLevel.LIGHT -> Res.string.haptics_light_desc
        HapticsLevel.STANDARD -> Res.string.haptics_standard_desc
        HapticsLevel.STRONG -> Res.string.haptics_strong_desc
    }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HapticsRow(level: HapticsLevel, selected: Boolean, index: Int, count: Int, onClick: () -> Unit) {
    val container by animateColorAsState(
        targetValue = if (selected) AppColors.action.primary.copy(alpha = 0.14f) else AppColors.bg.surface,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "hapticRow",
    )
    val checkScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "hapticCheck",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .expressiveSegment(index, count, color = container, selected = selected, debounceMs = 150L, haptic = false, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StrengthBars(level)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(level.title), style = AppTypography.titleMdSemiBold, color = AppColors.text.primary)
            Text(stringResource(level.desc), style = AppTypography.emphasizedSmRegular, color = AppColors.text.tertiary)
        }
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

/** Kuch belgisi: 0–3 ustun (o'chiqda — bo'sh ustunlar). */
@Composable
private fun StrengthBars(level: HapticsLevel) {
    val filled = level.ordinal
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(AppColors.icon.accentPrimary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
            repeat(3) { i ->
                Box(
                    Modifier
                        .width(4.dp)
                        .height((7 + i * 4).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (i < filled) AppColors.icon.accentPrimary
                            else AppColors.icon.accentPrimary.copy(alpha = 0.25f)
                        )
                )
            }
        }
    }
}
