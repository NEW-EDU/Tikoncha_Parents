package uz.tikoncha_parent.data.remote.model.safety

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET safety/zones/nearby` — OpenStreetMap asosidagi xavfli joylar (yaqinidan uzog'iga). */
@Serializable
data class NearbyZonesData(
    val version: String = "",
    val attribution: String = "",
    val zones: List<NearbyZoneDto> = emptyList(),
)

@Serializable
data class NearbyZoneDto(
    val id: String,
    val category: String,
    /** point | line | poly — joylar (klub, bar, do'kon) doim `point`. */
    val kind: String = "point",
    val name: String? = null,
    @SerialName("distance_m") val distanceM: Int = 0,
    /** [[lat, lng], …] — `point` uchun bitta nuqta. */
    val geometry: List<List<Double>> = emptyList(),
)
