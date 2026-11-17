package uz.tikoncha_parent.presentation.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.comfort_dark
import tikoncha_parents.composeapp.generated.resources.comfort_done
import tikoncha_parents.composeapp.generated.resources.comfort_hint
import tikoncha_parents.composeapp.generated.resources.comfort_light
import tikoncha_parents.composeapp.generated.resources.comfort_screen_color
import tikoncha_parents.composeapp.generated.resources.comfort_title
import tikoncha_parents.composeapp.generated.resources.ic3d_cf_dark
import tikoncha_parents.composeapp.generated.resources.ic3d_cf_light
import tikoncha_parents.composeapp.generated.resources.ic3d_cf_text_size
import tikoncha_parents.composeapp.generated.resources.text_size_extra_large
import tikoncha_parents.composeapp.generated.resources.text_size_large
import tikoncha_parents.composeapp.generated.resources.text_size_standard
import tikoncha_parents.composeapp.generated.resources.text_size_title
import uz.tikoncha_parent.presentation.base.LanguagePill
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.expressiveButtonShapes
import uz.tikoncha_parent.ui.expressiveSegment
import uz.tikoncha_parent.ui.rememberHapticSource
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.TextSize
import uz.tikoncha_parent.ui.theme.TextSizeController
import uz.tikoncha_parent.ui.theme.ThemeController
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.rememberIsDarkTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

/**
 * Birinçi işga tuşiriş, til tanlagiçi bilan bir ekranda: "Öqiş qulaymi?" — matn ölçami (A / A+ / A++) va ekran rangi
 * (yoruğ / qoronği). Tanlov darhol qöllanadi va saqlanadi (Sozlamalar → Matn ölçami / Mavzu bilan bir xil manba:
 * [TextSizeController], [ThemeController]) — şu ekranning özi kattalaşadi va rangini almaştiradi.
 * Keyingi tanişuv sahnalari öz tungi uslubida qoladi; tanlangan rang kiriş ekranidan boşlab körinadi, matn ölçami esa
 * hamma joyda darhol.
 */
class ComfortSetupScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ComfortSetupUi(onDone = { navigator.push(OnboardingScreen()) })
    }
}

/** Uç tugma: A / A+ / A++ → Odatiy / Katta / Juda katta. Harf ölçami tugmaning özida körinadi. */
private class SizeChoice(val size: TextSize, val glyph: String, val glyphSp: Int, val label: StringResource)

private val SizeChoices = listOf(
    SizeChoice(TextSize.STANDARD, "A", 24, Res.string.text_size_standard),
    SizeChoice(TextSize.LARGE, "A+", 30, Res.string.text_size_large),
    SizeChoice(TextSize.EXTRA_LARGE, "A++", 36, Res.string.text_size_extra_large),
)

@Composable
private fun ComfortSetupUi(onDone: () -> Unit) {
    val textSize by TextSizeController.size.collectAsState()
    // "Tizim" tanlangan bölsa — hozir amalda qaysi biri körinayotgani belgilanadi
    val isDark = rememberIsDarkTheme()
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.page,
    )

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page)
    ) {
        // Katta matnda ham hammasi körinadi: sığmasa ekran aylantiriladi (heç narsa kesilmaydi)
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = ContainerPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(12.dp))
                LanguagePill()
                Spacer(Modifier.height(32.dp))
                Text(
                    text = stringResource(Res.string.comfort_title),
                    style = AppTypography.headlineMdSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                    color = AppColors.text.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(28.dp))

                ComfortLabel(stringResource(Res.string.text_size_title), icon = Res.drawable.ic3d_cf_text_size)
                Row(horizontalArrangement = Arrangement.spacedBy(SegmentGap)) {
                    SizeChoices.forEachIndexed { index, choice ->
                        ComfortTile(
                            index = index,
                            count = SizeChoices.size,
                            isSelected = textSize == choice.size,
                            description = stringResource(choice.label),
                            onClick = { TextSizeController.set(choice.size) },
                        ) {
                            // Harf — joriy tanlovdan qat'i nazar öz ölçamida (farq bir qaraşda körinadi)
                            val base = LocalDensity.current
                            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = 1f)) {
                                Text(
                                    text = choice.glyph,
                                    style = AppTypography.titleMdSemiBold.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = choice.glyphSp.sp,
                                        lineHeight = (choice.glyphSp + 8).sp,
                                    ),
                                    color = AppColors.text.primary,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                ComfortLabel(stringResource(Res.string.comfort_screen_color))
                Row(horizontalArrangement = Arrangement.spacedBy(SegmentGap)) {
                    ThemeTile(0, !isDark, Res.drawable.ic3d_cf_light, stringResource(Res.string.comfort_light)) {
                        ThemeController.setMode(ThemeMode.LIGHT)
                    }
                    ThemeTile(1, isDark, Res.drawable.ic3d_cf_dark, stringResource(Res.string.comfort_dark)) {
                        ThemeController.setMode(ThemeMode.DARK)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.comfort_hint),
                    style = AppTypography.emphasizedSmRegular,
                    color = AppColors.text.tertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(24.dp))
                DoneButton(onClick = onDone)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/** Bölim sarlavhasi; [icon] — oldida 3D rasm (tintsiz): keksa ota-ona avval rasmni körsin. */
@Composable
private fun ComfortLabel(text: String, icon: DrawableResource? = null) {
    Row(
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = text,
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.secondary,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Composable
private fun RowScope.ThemeTile(index: Int, isSelected: Boolean, icon: DrawableResource, label: String, onClick: () -> Unit) {
    ComfortTile(index = index, count = 2, isSelected = isSelected, description = label, onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 3D rasm (quyoş / oy) — tintsiz
            Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Katta tanlov katagi (kamida 72dp — keksa barmoq uçun): tanlangani tonal rangda, burçaklari töliq yumaloq, ✓ belgili
 * (Sozlamalar → Mavzu ekranidagi kataklar bilan bir uslub).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RowScope.ComfortTile(
    index: Int,
    count: Int,
    isSelected: Boolean,
    description: String,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val container by animateColorAsState(
        targetValue = if (isSelected) AppColors.action.primary.copy(alpha = 0.14f) else AppColors.bg.surface,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "comfortTile",
    )
    val checkScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "comfortCheck",
    )
    Box(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 72.dp)
            .expressiveSegment(index, count, color = container, horizontal = true, selected = isSelected, debounceMs = 150L, onClick = onClick)
            .semantics {
                contentDescription = description
                selected = isSelected
                role = Role.RadioButton
            }
            .padding(horizontal = 8.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(22.dp)
                .graphicsLayer { scaleX = checkScale; scaleY = checkScale; alpha = checkScale.coerceIn(0f, 1f) }
                .clip(CircleShape)
                .background(AppColors.action.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = AppColors.icon.inverse, modifier = Modifier.size(15.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DoneButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        interactionSource = rememberHapticSource(),
        shapes = expressiveButtonShapes(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.action.primary,
            contentColor = AppColors.text.inverse,
        ),
        // Katta matnda tugma balandlaşadi (kesilmaydi)
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
    ) {
        Text(
            text = stringResource(Res.string.comfort_done),
            style = AppTypography.titleLgSemiBold,
            color = AppColors.text.inverse,
            textAlign = TextAlign.Center,
        )
    }
}
