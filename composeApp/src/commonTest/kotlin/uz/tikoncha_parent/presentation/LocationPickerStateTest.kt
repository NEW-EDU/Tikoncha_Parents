package uz.tikoncha_parent.presentation

import uz.tikoncha_parent.domain.model.GeoType
import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.presentation.map.LatLng
import uz.tikoncha_parent.presentation.policy.location.ChildPin
import uz.tikoncha_parent.presentation.policy.location.LocationPickerEvent
import uz.tikoncha_parent.presentation.policy.location.LocationPickerState
import uz.tikoncha_parent.presentation.policy.location.reduce
import kotlin.test.Test
import kotlin.test.assertEquals

class LocationPickerStateTest {

    private val school = LatLng(41.30, 69.24)
    private val rule = LocationRule(GeoType.CIRCLE, centerLat = school.lat, centerLng = school.lon, radiusMeters = 300, reverse = false)
    private val child = ChildPin(LatLng(41.35, 69.20), "Ali", null)

    @Test
    fun readOnlyAreaNeverMoves() {
        val s = LocationPickerState.from(rule, child).copy(readOnly = true)
            .reduce(LocationPickerEvent.CameraIdle(LatLng(40.0, 70.0)))
            .reduce(LocationPickerEvent.RadiusChanged(1000))
            .reduce(LocationPickerEvent.ReverseChanged(true))
            .reduce(LocationPickerEvent.UserLocated(LatLng(41.0, 69.0)))
            .reduce(LocationPickerEvent.ChildFocused)
        assertEquals(school, s.center)
        assertEquals(300, s.radiusMeters)
        assertEquals(false, s.reverse)
        assertEquals(LatLng(41.0, 69.0), s.userLocation)
        assertEquals(child.position, s.focus)            // kamera bolaga ko'chdi, hudud joyida
        assertEquals(school, s.reduce(LocationPickerEvent.AreaFocused).focus)
    }

    @Test
    fun editableAreaFollowsCameraAndUser() {
        val s = LocationPickerState.from(rule, child)
            .reduce(LocationPickerEvent.CameraIdle(LatLng(40.0, 70.0)))
        assertEquals(LatLng(40.0, 70.0), s.center)
        assertEquals(LatLng(41.0, 69.0), s.reduce(LocationPickerEvent.UserLocated(LatLng(41.0, 69.0))).center)
    }
}
