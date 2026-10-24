package uz.tikoncha_parent.presentation.map

import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

expect object MapKitInitializer {
    fun initialize(apiKey: String)
}

// ============================================================
// EXPECT — CONTROLLER
// ============================================================

@Stable
expect class MapController() {
    val isReady: Boolean
    fun moveTo(position: CameraPosition, animated: Boolean = true)
    fun fitBounds(points: List<LatLng>, paddingDp: Float = 48f, animated: Boolean = true)
    fun getCameraTarget(): LatLng?

    fun getCameraPosition(): CameraPosition?

}

suspend fun MapController.awaitReady(maxWaitMs: Long = 5_000L) {
    val intervalMs = 50L
    val maxAttempts = (maxWaitMs / intervalMs).toInt()
    repeat(maxAttempts) {
        if (isReady) return
        delay(intervalMs)
    }
}

@Composable
expect fun rememberMapController(): MapController

// ============================================================
// EXPECT — COMPOSABLE
// ============================================================

@Composable
expect fun YandexMap(
    controller: MapController,
    initialCamera: CameraPosition,
    markers: List<MapMarker> = emptyList(),
    circles: List<MapCircle> = emptyList(),
    polygons: List<MapPolygon> = emptyList(),
    onMarkerClick: (String) -> Unit = {},
    onMapTap: (LatLng) -> Unit = {},
    onCameraIdle: (LatLng) -> Unit = {},
    isDark: Boolean = false,
    modifier: Modifier = Modifier,
    /** Berilsa — Yandex logotipi o'ng yuqori burchakda, tepadan shuncha pastda (pastda orol/kartalar bo'lsa). */
    logoTopInset: Dp? = null,
)


internal fun MarkerStyle.cacheKey(): String = when (this) {
    is MarkerStyle.Self -> "self_${accentColor}_${borderColor}"
    else -> "${backgroundColor}_${isSelected}_${avatarUrl ?: text}_${showText}"
}