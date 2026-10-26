package uz.tikoncha_parent.presentation.map

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

@Immutable
data class LatLng(val lat: Double, val lon: Double)

@Immutable
data class CameraPosition(
    val target: LatLng,
    val zoom: Float = 14f,
    val azimuth: Float = 0f,
    val tilt: Float = 0f
)

@Immutable
data class MapMarker(
    val id: String,
    val position: LatLng,
    val style: MarkerStyle,
    val zIndex: Float = 0f,
)

@Immutable
data class MapCircle(
    val id: String,
    val center: LatLng,
    val radiusMeters: Double,
    val reverse: Boolean = false,
    val fillColor: Long = 0x3322C55E,
    val strokeColor: Long = 0xFF22C55E,
    val strokeWidthDp: Float = 2f
)

@Immutable
data class MapPolygon(
    val id: String,
    val points: List<LatLng>,
    val reverse: Boolean = false,
    val fillColor: Long = 0x33FF6B6B,
    val strokeColor: Long = 0xFFFF6B6B,
    val strokeWidthDp: Float = 2f
)

sealed interface MarkerStyle {
    val backgroundColor: Long
    val text: String
    val avatarUrl: String?
    val placeholderBitmap: ImageBitmap?
    val showText: Boolean
    val isSelected: Boolean

    /** Anchor (0..1). Default — pin uchli pastki uchburchakda. */
    val anchorX: Float get() = MarkerDimensions.ANCHOR_X
    val anchorY: Float get() = MarkerDimensions.ANCHOR_Y

    data class Child(
        val label: String,
        override val avatarUrl: String?,
        override val placeholderBitmap: ImageBitmap?,
        override val backgroundColor: Long = 0xFF4BB462L,  // brand
    ) : MarkerStyle {
        override val text: String get() = label
        override val showText: Boolean get() = false
        override val isSelected: Boolean get() = false
    }

    data class ChildSelected(
        val label: String,
        override val avatarUrl: String?,
        override val placeholderBitmap: ImageBitmap?,
        override val backgroundColor: Long = 0xFF4BB462L,
    ) : MarkerStyle {
        override val text: String get() = label
        override val showText: Boolean get() = false
        override val isSelected: Boolean get() = true
    }

    /** Yandex Maps stilidagi user location indicator. */
    data class Self(
        val accentColor: Long = 0xFFE53935L,    // qizil
        val borderColor: Long = 0xFFFFFFFFL,    // oq
    ) : MarkerStyle {
        override val backgroundColor: Long get() = accentColor
        override val text: String get() = ""
        override val avatarUrl: String? get() = null
        override val placeholderBitmap: ImageBitmap? get() = null
        override val showText: Boolean get() = false
        override val isSelected: Boolean get() = false
        override val anchorX: Float get() = 0.5f
        override val anchorY: Float get() = USER_LOCATION_ANCHOR_Y
    }

    /**
     * "Xavfli joylar" qatlami: kichik doira, ichida joy turi belgisi. [glyph] — oq rangda oldindan chizilgan
     * belgi (bu yerda [tint] ga bo'yaladi). Tanlanganda doira to'la [tint], belgi oq va kattaroq.
     */
    data class Danger(
        val category: String,
        val glyph: ImageBitmap?,
        val tint: Long,
        override val isSelected: Boolean = false,
    ) : MarkerStyle {
        override val backgroundColor: Long get() = tint
        override val text: String get() = ""
        override val avatarUrl: String? get() = null
        override val placeholderBitmap: ImageBitmap? get() = glyph
        override val showText: Boolean get() = false
        override val anchorX: Float get() = 0.5f
        override val anchorY: Float get() = 0.5f
    }
}

internal const val USER_LOCATION_ANCHOR_Y = 0.7f