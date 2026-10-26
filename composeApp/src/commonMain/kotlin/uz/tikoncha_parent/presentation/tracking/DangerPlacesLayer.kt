package uz.tikoncha_parent.presentation.tracking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.Liquor
import androidx.compose.material.icons.rounded.Nightlife
import androidx.compose.material.icons.rounded.NoAdultContent
import androidx.compose.material.icons.rounded.SmokingRooms
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.danger_cat_adult
import tikoncha_parents.composeapp.generated.resources.danger_cat_alcohol
import tikoncha_parents.composeapp.generated.resources.danger_cat_gambling
import tikoncha_parents.composeapp.generated.resources.danger_cat_nightlife
import tikoncha_parents.composeapp.generated.resources.danger_cat_tobacco
import tikoncha_parents.composeapp.generated.resources.danger_distance_m
import tikoncha_parents.composeapp.generated.resources.danger_places
import tikoncha_parents.composeapp.generated.resources.danger_places_failed
import tikoncha_parents.composeapp.generated.resources.danger_places_help
import tikoncha_parents.composeapp.generated.resources.danger_places_none
import tikoncha_parents.composeapp.generated.resources.danger_places_radius
import tikoncha_parents.composeapp.generated.resources.tracking_location_unavailable
import uz.tikoncha_parent.domain.model.safety.DangerCategory
import uz.tikoncha_parent.domain.model.safety.DangerPlace
import uz.tikoncha_parent.ui.HapticKind
import uz.tikoncha_parent.ui.expressivePress
import uz.tikoncha_parent.ui.rememberHapticSource
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/*
 * "Xavfli joylar" — xarita ustidagi qatlam: tanlangan (bo'lmasa joylashuvi eng yangi) farzand atrofidagi
 * 1,5 km ichidagi tungi klub, bar, kazino, alkogol va tamaki do'konlari (OpenStreetMap, backend `/safety/zones/nearby`).
 * Tepada chapda — yoqish tugmasi; yoqilganda ostida xira izoh (kim atrofida · © OpenStreetMap), tushuntirish (i) ortida.
 * Joy bosilsa — pastda kichik karta (nomi, turi, farzandgacha masofa).
 */

internal val DangerCategory.icon: ImageVector
    get() = when (this) {
        DangerCategory.NIGHTLIFE -> Icons.Rounded.Nightlife
        DangerCategory.GAMBLING -> Icons.Rounded.Casino
        DangerCategory.ADULT -> Icons.Rounded.NoAdultContent
        DangerCategory.ALCOHOL -> Icons.Rounded.Liquor
        DangerCategory.TOBACCO -> Icons.Rounded.SmokingRooms
    }

internal val DangerCategory.label: StringResource
    get() = when (this) {
        DangerCategory.NIGHTLIFE -> Res.string.danger_cat_nightlife
        DangerCategory.GAMBLING -> Res.string.danger_cat_gambling
        DangerCategory.ADULT -> Res.string.danger_cat_adult
        DangerCategory.ALCOHOL -> Res.string.danger_cat_alcohol
        DangerCategory.TOBACCO -> Res.string.danger_cat_tobacco
    }

/** Har tur belgisi oq rangda, bitmap holida — marker chizilganda bo'yaladi (MarkerStyle.Danger). */
@Composable
internal fun rememberDangerGlyphs(): Map<DangerCategory, ImageBitmap> {
    val density = LocalDensity.current
    val painters = DangerCategory.entries.associateWith { rememberVectorPainter(it.icon) }
    return remember(density) {
        val px = with(density) { 24.dp.roundToPx() }
        painters.mapValues { (_, painter) ->
            val bitmap = ImageBitmap(px, px)
            CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), Size(px.toFloat(), px.toFloat())) {
                with(painter) { draw(size, colorFilter = ColorFilter.tint(Color.White)) }
            }
            bitmap
        }
    }
}

/** Masofa farzandgacha: 10 m gacha yaxlitlanadi. */
@Composable
private fun distanceText(meters: Int): String =
    stringResource(Res.string.danger_distance_m, ((meters + 5) / 10) * 10)

/** Tepada chapda: yoqish tugmasi + (i) + xira holat qatori. */
@Composable
internal fun DangerPlacesControl(
    state: TrackingState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    val on = state.dangerMode
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DangerToggle(
                on = on,
                loading = on && state.dangerLoading,
                count = state.dangerPlaces.size,
                onClick = {
                    if (on) showHelp = false
                    onToggle()
                },
            )
            AnimatedVisibility(visible = on, enter = fadeIn(), exit = fadeOut()) {
                MapRoundButton(onClick = { showHelp = !showHelp }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = if (showHelp) AppColors.icon.accentPrimary else AppColors.icon.secondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = on,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusLine(state)
                AnimatedVisibility(
                    visible = showHelp,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                ) {
                    HelpCard(attribution = state.dangerAttribution)
                }
            }
        }
    }
}

@Composable
private fun DangerToggle(on: Boolean, loading: Boolean, count: Int, onClick: () -> Unit) {
    val source = rememberHapticSource(HapticKind.TOGGLE)
    val contentColor = if (on) AppColors.text.onPrimary else AppColors.text.primary
    Row(
        modifier = Modifier
            .height(44.dp)
            .expressivePress(source, pressedScale = 0.96f, haptic = false)
            .clip(CircleShape)
            .background(if (on) AppColors.action.primary else AppColors.bg.surface)
            .then(if (on) Modifier else Modifier.border(1.dp, AppColors.border.secondarySubtle, CircleShape))
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .padding(start = 12.dp, end = if (on) 8.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.GppMaybe,
            contentDescription = null,
            tint = if (on) contentColor else AppColors.icon.accentPrimary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.danger_places),
            style = AppTypography.titleSmSemiBold,
            color = contentColor,
            maxLines = 1,
        )
        if (on) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = contentColor,
                    )
                } else {
                    Text(
                        text = if (count > 99) "99+" else count.toString(),
                        style = AppTypography.bodySmSemiBold,
                        color = contentColor,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** Xira, kichik holat qatori: "Ali · 1,5 km ichida · © OpenStreetMap" va h.k. */
@Composable
private fun StatusLine(state: TrackingState) {
    val anchor = state.dangerAnchor
    val text = when {
        anchor == null -> if (state.peopleLoaded) stringResource(Res.string.tracking_location_unavailable) else null
        state.dangerFailed -> stringResource(Res.string.danger_places_failed)
        state.dangerLoading -> anchor.name
        state.dangerPlaces.isEmpty() -> "${anchor.name} · ${stringResource(Res.string.danger_places_none)}"
        else -> "${anchor.name} · ${stringResource(Res.string.danger_places_radius)} · © OpenStreetMap"
    } ?: return
    Text(
        text = text,
        style = AppTypography.bodySmMedium,
        color = AppColors.text.tertiary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .widthIn(max = 300.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.bg.surface.copy(alpha = 0.94f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

@Composable
private fun HelpCard(attribution: String) {
    Column(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.bg.surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.danger_places_help),
            style = AppTypography.bodyMdRegular,
            color = AppColors.text.secondary,
        )
        Text(
            text = attribution.ifBlank { "© OpenStreetMap contributors" },
            style = AppTypography.bodySmMedium,
            color = AppColors.text.tertiary,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MapRoundButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        interactionSource = rememberHapticSource(),
        modifier = Modifier.size(44.dp),
        shapes = IconButtonDefaults.shapes(),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = AppColors.bg.surface,
            contentColor = AppColors.icon.secondary,
        ),
    ) { content() }
}

/** Tanlangan xavfli joy kartasi (pastda, orol ustida — farzand kartasi bilan bir xil shaklda). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DangerPlaceCard(
    place: DangerPlace,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    val danger = AppColors.icon.accentDanger
    val category = stringResource(place.category.label)
    Row(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.bg.surface)
            .border(1.dp, AppColors.border.secondarySubtle, shape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(danger.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(place.category.icon, contentDescription = null, tint = danger, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = place.name ?: category,
                style = AppTypography.titleLgSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                color = AppColors.text.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            // Masofa — farzandgacha (qatlam farzand atrofida qidiradi; kimligi tepadagi qatorda)
            val distance = distanceText(place.distanceM)
            Text(
                text = if (place.name != null) "$category · $distance" else distance,
                style = AppTypography.bodyMdMedium,
                color = AppColors.text.tertiary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        FilledTonalIconButton(
            onClick = onClose,
            interactionSource = rememberHapticSource(),
            modifier = Modifier.size(40.dp),
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = AppColors.text.primary.copy(alpha = 0.06f),
                contentColor = AppColors.icon.secondary,
            ),
        ) {
            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}
