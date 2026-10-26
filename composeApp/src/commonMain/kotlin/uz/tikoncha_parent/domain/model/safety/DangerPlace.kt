package uz.tikoncha_parent.domain.model.safety

import androidx.compose.runtime.Immutable

/**
 * Bola uchun xavfli joy turlari ("Xavfli joylar" xarita qatlami). [apiKey] — backend kategoriyasi
 * (`app/modules/safety/zones.py`). Suv, qurilish va temir yo'l bu qatlamda ko'rsatilmaydi — ular joy emas, hudud.
 */
enum class DangerCategory(val apiKey: String) {
    NIGHTLIFE("nightlife"),   // tungi klub, bar, pab, qalyonxona
    GAMBLING("gambling"),     // kazino, qimorxona, bukmeker
    ADULT("adult"),           // intim do'kon, striptiz
    ALCOHOL("alcohol"),       // spirtli ichimliklar do'koni
    TOBACCO("tobacco");       // tamaki / vape do'koni

    companion object {
        fun fromApi(key: String): DangerCategory? = entries.firstOrNull { it.apiKey == key }
        val apiQuery: String = entries.joinToString(",") { it.apiKey }
    }
}

@Immutable
data class DangerPlace(
    val id: String,
    val category: DangerCategory,
    val name: String?,
    val lat: Double,
    val lng: Double,
    /** Farzandgacha masofa, metr. */
    val distanceM: Int,
)

@Immutable
data class DangerPlaces(
    val places: List<DangerPlace>,
    /** Ma'lumot manbai (OpenStreetMap, ODbL) — xaritada ko'rsatilishi shart. */
    val attribution: String,
)
