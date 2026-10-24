package uz.tikoncha_parent.presentation.profile.language

import androidx.compose.ui.unit.sp
import uz.tikoncha_parent.platform.languageDisplayName
import uz.tikoncha_parent.ui.Haptics
import androidx.compose.foundation.layout.PaddingValues
import uz.tikoncha_parent.presentation.base.island.BottomAnchoredColumn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.lang_hint
import tikoncha_parents.composeapp.generated.resources.lang_name_en
import tikoncha_parents.composeapp.generated.resources.lang_name_ru
import tikoncha_parents.composeapp.generated.resources.lang_name_uz
import tikoncha_parents.composeapp.generated.resources.lang_section
import tikoncha_parents.composeapp.generated.resources.til
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.profile.ProfileSectionLabel
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.expressiveSegment
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class LanguageScreen : Screen {
    @Composable
    override fun Content() {
        LanguageUi(navigator = LocalNavigator.current)
    }
}

/*
 * Til tanlash — M3 Expressive:
 *  • tepada "salomlashuv" kartasi: tanlangan tilda salom + shakli tilga qarab morf bo'ladigan nishon (bayroq);
 *  • pastda guruhlangan ro'yxat — tanlangan qator to'liq yumaloqlanadi va tonal rangga kiradi;
 *  • "Saqlash" tugmasi yo'q — bosilishi bilan ilova darhol shu tilga o'tadi (ekran ham, jonli).
 */
@Composable
fun LanguageUi(navigator: Navigator?) {
    val controller = LocalLanguageController.current
    val current by controller.current.collectAsState()
    val haptic = LocalHapticFeedback.current

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ) {
        CustomHeader(
            title = stringResource(Res.string.til),
            showBackButton = true,
            onBackClick = { navigator?.pop() }
        )

        // Bo'sh joy bo'lsa — pastga, bosh barmoqqa yaqin
        BottomAnchoredColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = ContainerPadding, end = ContainerPadding, bottom = 8.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            LanguageHero(current)

            ProfileSectionLabel(stringResource(Res.string.lang_section))
            Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
                val all = LanguageType.entries
                all.forEachIndexed { index, lang ->
                    LanguageRow(
                        language = lang,
                        selected = lang == current,
                        index = index,
                        count = all.size,
                        onClick = {
                            if (lang != current) {
                                Haptics.tick()
                                controller.select(lang)
                            }
                        },
                    )
                }
            }
            Text(
                text = stringResource(Res.string.lang_hint),
                style = AppTypography.emphasizedSmRegular,
                color = AppColors.text.tertiary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
            )
        }
    }
}

/** Salomlashuv kartasi — matn ataylab o'sha tilning o'zida (tarjima qilinmaydi). */
@Composable
private fun LanguageHero(language: LanguageType) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(AppColors.bg.surface)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MorphingFlag(language)
        Spacer(Modifier.height(20.dp))
        AnimatedContent(
            targetState = language,
            transitionSpec = {
                val down = targetState.ordinal > initialState.ordinal
                (slideInVertically { if (down) it / 2 else -it / 2 } + fadeIn()) togetherWith
                    (slideOutVertically { if (down) -it / 2 else it / 2 } + fadeOut()) using SizeTransform(clip = false)
            },
            label = "greeting",
        ) { lang ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = lang.greeting(),
                    style = AppTypography.headlineSmSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                    color = AppColors.text.primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = lang.tagline(),
                    style = AppTypography.titleSmMedium,
                    color = AppColors.text.secondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun LanguageType.greeting() = greeting

private fun LanguageType.tagline() = tagline

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LanguageType.polygon(): RoundedPolygon = when (ordinal % 5) {
    0 -> MaterialShapes.Cookie9Sided
    1 -> MaterialShapes.Clover4Leaf
    2 -> MaterialShapes.Sunny
    3 -> MaterialShapes.Cookie6Sided
    else -> MaterialShapes.Flower
}

/** Bayroq nishoni: til almashganda shakl prujina bilan morf bo'ladi va biroz aylanadi (bayroq tik turadi). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MorphingFlag(language: LanguageType) {
    var from by remember { mutableStateOf(language.polygon()) }
    var to by remember { mutableStateOf(language.polygon()) }
    val progress = remember { Animatable(1f) }
    val morphSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(language) {
        val target = language.polygon()
        if (target === to) return@LaunchedEffect
        from = to
        to = target
        progress.snapTo(0f)
        progress.animateTo(1f, morphSpec)
    }
    val morph = remember(from, to) { Morph(from, to) }
    val rotation by animateFloatAsState(
        targetValue = language.ordinal * 72f,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "flagSpin",
    )

    Box(
        modifier = Modifier
            .size(104.dp)
            .graphicsLayer { rotationZ = rotation }
            .clip(MorphShape(morph, progress.value))
            .background(AppColors.action.primary.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = language,
            modifier = Modifier.graphicsLayer { rotationZ = -rotation },
            label = "flag",
        ) { lang ->
            LanguageFlag(lang, 48.dp)
        }
    }
}

/** Bayroq: özbek / rus / ingliz uçun rasm, qolgan tillarda — emoji bayroq. */
@Composable
internal fun LanguageFlag(language: LanguageType, size: androidx.compose.ui.unit.Dp) {
    val icon = language.iconId
    if (icon != null) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(CircleShape),
        )
    } else {
        Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            Text(text = language.flag, fontSize = (size.value * 0.62f).sp)
        }
    }
}

/** [Morph] ning [progress] dagi holati — o'lchamga moslab (shakl chegaralari bo'yicha) chiziladi. */
private class MorphShape(private val morph: Morph, private val progress: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cubics = morph.asCubics(progress)
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
        cubics.forEach { c ->
            minX = minOf(minX, c.anchor0X, c.anchor1X); maxX = maxOf(maxX, c.anchor0X, c.anchor1X)
            minY = minOf(minY, c.anchor0Y, c.anchor1Y); maxY = maxOf(maxY, c.anchor0Y, c.anchor1Y)
        }
        val sx = size.width / (maxX - minX).coerceAtLeast(0.0001f)
        val sy = size.height / (maxY - minY).coerceAtLeast(0.0001f)
        fun x(v: Float) = (v - minX) * sx
        fun y(v: Float) = (v - minY) * sy
        val path = Path()
        cubics.forEachIndexed { i, c ->
            if (i == 0) path.moveTo(x(c.anchor0X), y(c.anchor0Y))
            path.cubicTo(x(c.control0X), y(c.control0Y), x(c.control1X), y(c.control1Y), x(c.anchor1X), y(c.anchor1Y))
        }
        path.close()
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LanguageRow(
    language: LanguageType,
    selected: Boolean,
    index: Int,
    count: Int,
    onClick: () -> Unit,
) {
    val container by animateColorAsState(
        targetValue = if (selected) AppColors.action.primary.copy(alpha = 0.14f) else AppColors.bg.surface,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "langRow",
    )
    val checkScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "langCheck",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .expressiveSegment(index, count, color = container, selected = selected, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LanguageFlag(language, 40.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = language.languageName,
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
            )
            // Tilning nomi interfeys tilida ("rus tili"); öz nomi bilan bir xil bölsa — ko'rsatilmaydi
            val ui = LocalLanguageController.current.current.collectAsState().value
            val local = remember(language, ui) {
                languageDisplayName(language.languageCode, ui.localeTag).replaceFirstChar { it.uppercase() }
            }
            if (local.isNotBlank() && !local.equals(language.languageName, ignoreCase = true)) {
                Text(
                    text = local,
                    style = AppTypography.emphasizedSmRegular,
                    color = AppColors.text.tertiary,
                )
            }
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer { scaleX = checkScale; scaleY = checkScale; alpha = checkScale.coerceIn(0f, 1f) }
                .clip(CircleShape)
                .background(AppColors.action.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLanguageScreen() {
    TikonchaParentTheme(ThemeMode.DARK) {
        LanguageUi(navigator = null)
    }
}
