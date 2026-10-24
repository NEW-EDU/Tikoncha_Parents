package uz.tikoncha_parent.presentation.profile.settings.theme

import androidx.compose.foundation.layout.PaddingValues
import uz.tikoncha_parent.presentation.base.island.BottomAnchoredColumn
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.home_layout_grid
import tikoncha_parents.composeapp.generated.resources.home_layout_list
import tikoncha_parents.composeapp.generated.resources.home_layout_title
import tikoncha_parents.composeapp.generated.resources.saqlash
import tikoncha_parents.composeapp.generated.resources.tema
import tikoncha_parents.composeapp.generated.resources.theme_dark
import tikoncha_parents.composeapp.generated.resources.theme_light
import tikoncha_parents.composeapp.generated.resources.theme_section_colors
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.new_home.HomeLayout
import uz.tikoncha_parent.presentation.new_home.HomeLayoutController
import uz.tikoncha_parent.presentation.new_home.NewHomeScreen
import uz.tikoncha_parent.presentation.profile.ProfileSectionLabel
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.expressiveSegment
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeController
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class ThemeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        ThemeUi(navigator = navigator)
    }
}

@Composable
fun ThemeUi(navigator: Navigator?) {
    val savedMode by ThemeController.mode.collectAsState()
    val savedLayout by HomeLayoutController.layout.collectAsState()
    var draftMode by remember(savedMode) { mutableStateOf(savedMode) }
    var draftLayout by remember(savedLayout) { mutableStateOf(savedLayout) }

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.page
    )

    ThemeContent(
        selectedMode = draftMode,
        selectedLayout = draftLayout,
        hasChanged = draftMode != savedMode || draftLayout != savedLayout,
        containerModifier = systemBars.modifier,
        onModeSelected = { draftMode = it },
        onLayoutSelected = { draftLayout = it },
        onBackClick = { navigator?.pop() },
        onSaveClick = {
            HomeLayoutController.set(draftLayout)
            ThemeController.setMode(draftMode)
            // Bosh sahifaga qaytamiz — yangi ko'rinish darhol ko'rinadi
            navigator?.replaceAll(NewHomeScreen())
        }
    )
}

/*
 * "Mavzu" — ikki ixcham guruh (M3 Expressive, yonma-yon segmentlar):
 *  • Rang: Tizim / Yorug' / Qorong'i — bitta qatorda, kichik maketlar bilan (avval ikki qator katta kartalar edi);
 *  • Bosh sahifa ko'rinishi: Ro'yxat (katta bloklar) / Panjara (2×3) — maketi kod bilan chiziladi.
 * Tanlangan katak tonal rangga kiradi va barcha burchaklari yumaloqlanadi.
 */
@Composable
private fun ThemeContent(
    selectedMode: ThemeMode,
    selectedLayout: HomeLayout,
    hasChanged: Boolean,
    onModeSelected: (ThemeMode) -> Unit,
    onLayoutSelected: (HomeLayout) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    containerModifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(containerModifier)
            .background(AppColors.bg.page)
    ) {
        CustomHeader(
            title = stringResource(Res.string.tema),
            showBackButton = true,
            onBackClick = onBackClick
        )

        // Bo'sh joy bo'lsa — pastga, bosh barmoqqa yaqin
        BottomAnchoredColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = ContainerPadding, end = ContainerPadding, bottom = 8.dp),
        ) {
            ProfileSectionLabel(stringResource(Res.string.theme_section_colors))
            Row(horizontalArrangement = Arrangement.spacedBy(SegmentGap)) {
                val modes = ThemeMode.entries
                modes.forEachIndexed { index, mode ->
                    OptionTile(
                        index = index,
                        count = modes.size,
                        selected = mode == selectedMode,
                        label = stringResource(mode.resId),
                        onClick = { onModeSelected(mode) },
                    ) {
                        ThemePreview(mode, Modifier.fillMaxWidth(0.78f).aspectRatio(0.62f))
                    }
                }
            }

            ProfileSectionLabel(stringResource(Res.string.home_layout_title))
            Row(horizontalArrangement = Arrangement.spacedBy(SegmentGap)) {
                val layouts = HomeLayout.entries
                layouts.forEachIndexed { index, layout ->
                    OptionTile(
                        index = index,
                        count = layouts.size,
                        selected = layout == selectedLayout,
                        label = stringResource(
                            when (layout) {
                                HomeLayout.LIST -> Res.string.home_layout_list
                                HomeLayout.GRID -> Res.string.home_layout_grid
                            }
                        ),
                        onClick = { onLayoutSelected(layout) },
                    ) {
                        LayoutPreview(layout, Modifier.width(76.dp).aspectRatio(0.62f))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        CustomButton(
            text = stringResource(Res.string.saqlash),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = ContainerPadding, end = ContainerPadding, top = 8.dp, bottom = 16.dp),
            enabled = hasChanged,
            onClick = onSaveClick
        )
    }
}

/** Yonma-yon guruh katagi: maket + nom; tanlangani tonal rangda, burchaklari to'liq yumaloq, ✓ belgili. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RowScope.OptionTile(
    index: Int,
    count: Int,
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    preview: @Composable ColumnScope.() -> Unit,
) {
    val container by animateColorAsState(
        targetValue = if (selected) AppColors.action.primary.copy(alpha = 0.14f) else AppColors.bg.surface,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "optionTile",
    )
    val checkScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "optionCheck",
    )
    Box(
        modifier = Modifier
            .weight(1f)
            .expressiveSegment(index, count, color = container, horizontal = true, selected = selected, onClick = onClick)
            .padding(top = 14.dp, bottom = 12.dp, start = 8.dp, end = 8.dp),
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            preview()
            Spacer(Modifier.height(10.dp))
            Text(
                text = label,
                style = AppTypography.titleSmMedium,
                color = AppColors.text.primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(22.dp)
                .graphicsLayer { scaleX = checkScale; scaleY = checkScale; alpha = checkScale.coerceIn(0f, 1f) }
                .clip(CircleShape)
                .background(AppColors.action.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        }
    }
}

/** Rang maketi: yorug'/qorong'i skrinshot; "Tizim" — ikkalasi diagonal bo'yicha. */
@Composable
private fun ThemePreview(mode: ThemeMode, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(10.dp))) {
        when (mode) {
            ThemeMode.LIGHT -> Image(painterResource(Res.drawable.theme_light), null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            ThemeMode.DARK -> Image(painterResource(Res.drawable.theme_dark), null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            ThemeMode.SYSTEM -> {
                val lightHalf = remember {
                    GenericShape { size, _ -> moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(0f, size.height); close() }
                }
                val darkHalf = remember {
                    GenericShape { size, _ -> moveTo(size.width, 0f); lineTo(size.width, size.height); lineTo(0f, size.height); close() }
                }
                Image(painterResource(Res.drawable.theme_light), null, Modifier.fillMaxSize().clip(lightHalf), contentScale = ContentScale.FillBounds)
                Image(painterResource(Res.drawable.theme_dark), null, Modifier.fillMaxSize().clip(darkHalf), contentScale = ContentScale.FillBounds)
            }
        }
    }
}

/** Bosh sahifa maketi — haqiqiy ranglarda chiziladi: tepada sarlavha, pastda 6 blok (ro'yxat yoki 2×3). */
@Composable
private fun LayoutPreview(layout: HomeLayout, modifier: Modifier) {
    val page = AppColors.bg.page
    val block = AppColors.section.tertiary
    val accent = AppColors.action.primary.copy(alpha = 0.55f)
    val frame = AppColors.border.tertiarySubtle
    Canvas(modifier.clip(RoundedCornerShape(12.dp)).background(page)) {
        val pad = size.width * 0.09f
        val gap = size.width * 0.03f
        val r = CornerRadius(size.width * 0.06f)
        // Sarlavha qatori: chapda tugma, o'ngda profil
        val headerH = size.height * 0.07f
        drawRoundRect(accent, Offset(pad, pad), Size(headerH, headerH), CornerRadius(headerH / 2))
        drawRoundRect(block, Offset(size.width - pad - size.width * 0.32f, pad), Size(size.width * 0.32f, headerH), CornerRadius(headerH / 2))
        val top = pad + headerH + gap * 2
        val areaW = size.width - pad * 2
        val areaH = size.height - top - pad
        when (layout) {
            HomeLayout.LIST -> {
                val rows = 6
                val h = (areaH - gap * (rows - 1)) / rows
                repeat(rows) { i ->
                    drawRoundRect(block, Offset(pad, top + i * (h + gap)), Size(areaW, h), r)
                }
            }
            HomeLayout.GRID -> {
                val rows = 3
                val w = (areaW - gap) / 2
                val h = (areaH - gap * (rows - 1)) / rows
                repeat(rows) { row ->
                    repeat(2) { col ->
                        drawRoundRect(block, Offset(pad + col * (w + gap), top + row * (h + gap)), Size(w, h), r)
                    }
                }
            }
        }
        drawRoundRect(frame, Offset.Zero, size, CornerRadius(12.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
    }
}

@Preview
@Composable
private fun PreviewThemeContentLight() {
    var selected by remember { mutableStateOf(ThemeMode.LIGHT) }
    var layout by remember { mutableStateOf(HomeLayout.LIST) }
    TikonchaParentTheme(ThemeMode.LIGHT) {
        ThemeContent(
            selectedMode = selected,
            selectedLayout = layout,
            hasChanged = selected != ThemeMode.LIGHT,
            onModeSelected = { selected = it },
            onLayoutSelected = { layout = it },
            onBackClick = {},
            onSaveClick = {}
        )
    }
}
