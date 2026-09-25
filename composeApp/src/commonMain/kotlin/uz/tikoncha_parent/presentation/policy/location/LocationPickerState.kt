package uz.tikoncha_parent.presentation.policy.location

import uz.tikoncha_parent.domain.model.ChildLocation
import uz.tikoncha_parent.domain.model.GeoType
import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.presentation.map.LatLng

/** Xaritadagi bola belgisi. */
data class ChildPin(val position: LatLng, val label: String, val avatarUrl: String?)

fun ChildLocation.toPin(): ChildPin? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return ChildPin(LatLng(lat, lng), listOfNotNull(firstName, lastName).joinToString(" "), avatarUrl)
}

/**
 * Hudud tanlash (Student `LocationPickerState` bilan bir xil): xarita markazidagi pin = markaz,
 * slider = radius. Parent'da xarita bolaning oxirgi joylashuvidan boshlanadi (ota-ona odatda
 * maktabda emas) va "Farzandim joylashuvi" tugmasi bor. "Saqlash" → [toRule].
 */
data class LocationPickerState(
    val center: LatLng? = null,
    val radiusMeters: Int = DEFAULT_RADIUS,
    val userLocation: LatLng? = null,
    val child: ChildPin? = null,
    /** true — hudud TASHQARISIDA amal qiladi (faqat "o'zim sozlayman"da). */
    val reverse: Boolean = false,
    /** Kamera shu nuqtaga ko'chadi; har ko'chishda [focusTick] oshadi. */
    val focus: LatLng? = null,
    val focusTick: Int = 0,
    /** Farzand / ikkinchi ota-ona / maktab jadvalining hududi — faqat ko'rish: doira joyida turadi. */
    val readOnly: Boolean = false,
) {
    val canSave: Boolean get() = center != null

    fun toRule(): LocationRule? = center?.let {
        LocationRule(
            geoType = GeoType.CIRCLE,
            centerLat = it.lat,
            centerLng = it.lon,
            radiusMeters = radiusMeters,
            polygon = null,
            reverse = reverse,
        )
    }

    companion object {
        const val DEFAULT_RADIUS = 200
        const val MIN_RADIUS = 50
        const val MAX_RADIUS = 2000
        const val RADIUS_STEP = 50

        /** Mavjud qoidadan; yo'q bo'lsa markaz — bolaning joylashuvi (u ham yo'q bo'lsa bo'sh). */
        fun from(rule: LocationRule?, child: ChildPin?): LocationPickerState {
            val lat = rule?.centerLat
            val lng = rule?.centerLng
            return LocationPickerState(
                center = if (lat != null && lng != null) LatLng(lat, lng) else child?.position,
                radiusMeters = (rule?.radiusMeters ?: DEFAULT_RADIUS).coerceIn(MIN_RADIUS, MAX_RADIUS),
                child = child,
                reverse = rule?.reverse ?: false,
            )
        }
    }
}

sealed interface LocationPickerEvent {
    /** Kamera to'xtadi — markaz shu yerda. */
    data class CameraIdle(val target: LatLng) : LocationPickerEvent
    data class RadiusChanged(val meters: Int) : LocationPickerEvent
    /** Telefon joylashuvi keldi — markaz ham shu yerga ko'chadi. */
    data class UserLocated(val position: LatLng) : LocationPickerEvent
    /** "Farzandim joylashuvi" — markaz bolaning oxirgi joyiga. */
    data object ChildFocused : LocationPickerEvent
    data class ReverseChanged(val reverse: Boolean) : LocationPickerEvent
    /** "Hududni ko'rsatish" — kamera hudud markaziga. */
    data object AreaFocused : LocationPickerEvent
}

fun LocationPickerState.reduce(e: LocationPickerEvent): LocationPickerState = when {
    readOnly -> when (e) {
        // Faqat ko'rish: xaritani surish hududni ko'chirmaydi, faqat kamera ko'chadi
        is LocationPickerEvent.UserLocated -> copy(userLocation = e.position, focus = e.position, focusTick = focusTick + 1)
        LocationPickerEvent.ChildFocused -> child?.let { copy(focus = it.position, focusTick = focusTick + 1) } ?: this
        LocationPickerEvent.AreaFocused -> center?.let { copy(focus = it, focusTick = focusTick + 1) } ?: this
        else -> this
    }
    else -> reduceEditable(e)
}

private fun LocationPickerState.reduceEditable(e: LocationPickerEvent): LocationPickerState = when (e) {
    is LocationPickerEvent.CameraIdle -> copy(center = e.target)
    is LocationPickerEvent.RadiusChanged -> copy(
        radiusMeters = (e.meters / LocationPickerState.RADIUS_STEP * LocationPickerState.RADIUS_STEP)
            .coerceIn(LocationPickerState.MIN_RADIUS, LocationPickerState.MAX_RADIUS),
    )
    is LocationPickerEvent.UserLocated -> copy(userLocation = e.position, center = e.position, focus = e.position, focusTick = focusTick + 1)
    LocationPickerEvent.ChildFocused -> child?.let { copy(center = it.position, focus = it.position, focusTick = focusTick + 1) } ?: this
    is LocationPickerEvent.ReverseChanged -> copy(reverse = e.reverse)
    LocationPickerEvent.AreaFocused -> center?.let { copy(focus = it, focusTick = focusTick + 1) } ?: this
}
