package uz.tikoncha_parent.presentation.policy.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.internal.BackHandler
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.arrow_back
import tikoncha_parents.composeapp.generated.resources.editor_inside
import tikoncha_parents.composeapp.generated.resources.editor_outside
import tikoncha_parents.composeapp.generated.resources.location_child_here
import tikoncha_parents.composeapp.generated.resources.radius
import tikoncha_parents.composeapp.generated.resources.saqlash
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.presentation.map.CameraPosition
import uz.tikoncha_parent.presentation.map.LatLng
import uz.tikoncha_parent.presentation.map.MapCircle
import uz.tikoncha_parent.presentation.map.MapMarker
import uz.tikoncha_parent.presentation.map.MarkerStyle
import uz.tikoncha_parent.presentation.map.YandexMap
import uz.tikoncha_parent.presentation.map.awaitReady
import uz.tikoncha_parent.presentation.map.rememberMapController
import uz.tikoncha_parent.presentation.map.zoomForCircleRadius
import uz.tikoncha_parent.presentation.policy.components.PolicyText
import uz.tikoncha_parent.presentation.policy.components.SegmentedTabs
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberIsDarkTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

/** Markaz ham, bola joylashuvi ham bo'lmasa — Farg'ona vodiysi (eski ekran bilan bir xil). */
private val DEFAULT_CENTER = LatLng(40.7821, 72.3442)

/**
 * Hudud tanlash (Student `LocationPicker` bilan bir xil): Yandex xaritasi, markazdagi pin,
 * radius slider, "Mening joylashuvim" va Parent'da "Farzandim joylashuvi".
 */
@OptIn(InternalVoyagerApi::class)
@Composable
fun LocationPicker(
    state: LocationPickerState,
    title: String,
    event: (LocationPickerEvent) -> Unit,
    onDone: () -> Unit,
    onClose: () -> Unit,
    /** "Hudud ichida | tashqarisida" tanlovi — faqat o'zim sozlayman jadvalida. */
    showReverse: Boolean = false,
) {
    BackHandler(true) { onClose() }
    val haptics = rememberAppHaptics()
    val isDark = rememberIsDarkTheme()
    val mapController = rememberMapController()

    val access = rememberLocationAccess(onLocated = { event(LocationPickerEvent.UserLocated(it)) })

    // Markaz yo'q (bola joylashuvi ham noma'lum) va ruxsat bor → jim so'raymiz
    LaunchedEffect(Unit) { if (state.center == null) access.requestIfGranted() }

    // "Mening joylashuvim" / "Farzandim joylashuvi" → kamera shu yerga
    LaunchedEffect(state.focusTick) {
        if (state.focusTick == 0) return@LaunchedEffect
        val target = state.focus ?: return@LaunchedEffect
        mapController.awaitReady()
        mapController.moveTo(CameraPosition(target, zoomForCircleRadius(state.radiusMeters)))
    }

    // Radius kattalashsa doira ekranga sig'sin
    LaunchedEffect(state.radiusMeters) {
        delay(200)
        val target = zoomForCircleRadius(state.radiusMeters)
        val current = mapController.getCameraPosition() ?: return@LaunchedEffect
        if (current.zoom > target + 0.5f) mapController.moveTo(CameraPosition(current.target, target))
    }

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page.copy(alpha = 0.3f),
        navigationBarColor = AppColors.bg.page.copy(alpha = 0.3f),
    )

    val accent = AppColors.action.primary
    val circles = remember(state.center, state.radiusMeters, state.reverse, accent) {
        state.center?.let {
            listOf(
                MapCircle(
                    id = "area",
                    center = it,
                    radiusMeters = state.radiusMeters.toDouble(),
                    reverse = state.reverse,
                    fillColor = accent.copy(alpha = 0.18f).toArgbLong(),
                    strokeColor = accent.toArgbLong(),
                    strokeWidthDp = 3f,
                )
            )
        }.orEmpty()
    }
    val markers = remember(state.userLocation, state.child) {
        listOfNotNull(
            state.child?.let { MapMarker(id = "child", position = it.position, style = MarkerStyle.Child(it.label, it.avatarUrl, null), zIndex = 90f) },
            state.userLocation?.let { MapMarker(id = "me", position = it, style = MarkerStyle.Self(), zIndex = 100f) },
        )
    }
    val initialCenter = remember { state.center ?: state.userLocation ?: DEFAULT_CENTER }

    Box(modifier = Modifier.fillMaxSize().then(systemBars.modifier)) {
        YandexMap(
            controller = mapController,
            initialCamera = CameraPosition(initialCenter, zoom = zoomForCircleRadius(state.radiusMeters)),
            circles = circles,
            markers = markers,
            onCameraIdle = { event(LocationPickerEvent.CameraIdle(it)) },
            isDark = isDark,
            modifier = Modifier.fillMaxSize(),
        )

        // Markazdagi pin — hudud markazi
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.align(Alignment.Center).size(48.dp).offset(y = (-24).dp),
        )

        // Tepada: orqaga + sarlavha
        Row(
            modifier = Modifier.align(Alignment.TopStart).padding(top = 8.dp, start = 16.dp, end = 80.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FloatingActionButton(
                onClick = onClose,
                shape = CircleShape,
                containerColor = AppColors.modal.primary,
                contentColor = AppColors.icon.primary,
                modifier = Modifier.size(52.dp),
            ) {
                Icon(painter = painterResource(Res.drawable.arrow_back), contentDescription = null)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = AppColors.modal.primary, tonalElevation = 4.dp) {
                Text(
                    text = title,
                    style = PolicyText.rowStrong,
                    color = AppColors.text.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }

        // O'ngda: Mening joylashuvim · Farzandim joylashuvi
        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FloatingActionButton(
                onClick = access.request,
                shape = CircleShape,
                containerColor = AppColors.modal.primary,
                contentColor = AppColors.icon.primary,
                modifier = Modifier.size(52.dp),
            ) {
                if (access.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = AppColors.icon.primary)
                } else {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                }
            }
            if (state.child != null) {
                FloatingActionButton(
                    onClick = { event(LocationPickerEvent.ChildFocused) },
                    shape = CircleShape,
                    containerColor = AppColors.modal.primary,
                    contentColor = AppColors.icon.accentPrimary,
                    modifier = Modifier.size(52.dp),
                ) {
                    Icon(Icons.Rounded.ChildCare, contentDescription = stringResource(Res.string.location_child_here))
                }
            }
        }

        // Pastki panel: radius + Saqlash
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = AppColors.modal.primary,
            tonalElevation = 8.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showReverse) {
                    SegmentedTabs(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        labels = listOf(stringResource(Res.string.editor_inside), stringResource(Res.string.editor_outside)),
                        selectedIndex = if (state.reverse) 1 else 0,
                        containerColor = AppColors.bg.page,
                        onSelect = { event(LocationPickerEvent.ReverseChanged(it == 1)) },
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = stringResource(Res.string.radius), style = PolicyText.row, color = AppColors.text.primary)
                    Text(text = "${state.radiusMeters} m", style = PolicyText.rowStrong, color = AppColors.text.accentEmphasis)
                }
                // Har qadam (50 m) — g'ildirakdagi kabi yengil tik
                var tickedRadius by remember { mutableIntStateOf(state.radiusMeters) }
                LaunchedEffect(state.radiusMeters) {
                    if (state.radiusMeters != tickedRadius) {
                        tickedRadius = state.radiusMeters
                        haptics.wheelTick()
                    }
                }
                Slider(
                    value = state.radiusMeters.toFloat(),
                    onValueChange = { event(LocationPickerEvent.RadiusChanged(it.toInt())) },
                    valueRange = LocationPickerState.MIN_RADIUS.toFloat()..LocationPickerState.MAX_RADIUS.toFloat(),
                    steps = (LocationPickerState.MAX_RADIUS - LocationPickerState.MIN_RADIUS) / LocationPickerState.RADIUS_STEP - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = accent,
                        activeTrackColor = accent,
                        inactiveTrackColor = AppColors.bg.tertiary,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent,
                    ),
                )
                CustomButtonNew(
                    text = stringResource(Res.string.saqlash),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSave,
                    onClick = onDone,
                )
            }
        }
    }
}

private fun Color.toArgbLong(): Long = toArgb().toLong() and 0xFFFFFFFFL
