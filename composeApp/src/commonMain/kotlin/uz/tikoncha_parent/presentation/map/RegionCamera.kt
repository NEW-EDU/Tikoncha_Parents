package uz.tikoncha_parent.presentation.map

import uz.tikoncha_parent.platform.deviceRegionCode

/**
 * Hali hech qanday joylashuv (ota-ona ham, farzand ham) noma'lum bo'lganda xarita qayerdan ochilishi:
 * telefon hududi mamlakati (masalan, Germaniyadagi ota-onaga — Germaniya), noma'lum bo'lsa butun dunyo.
 * Avval bu Andijon edi — chet eldagi ota-onaga ma'nosiz.
 */
fun regionFallbackCamera(): CameraPosition {
    val (lat, lon, zoom) = REGION_CENTERS[deviceRegionCode()] ?: return CameraPosition(LatLng(30.0, 30.0), zoom = 2f)
    return CameraPosition(LatLng(lat, lon), zoom = zoom)
}

private val REGION_CENTERS: Map<String, Triple<Double, Double, Float>> = mapOf(
    "UZ" to Triple(41.30, 64.60, 5.2f),
    "RU" to Triple(55.75, 37.62, 5f),
    "KZ" to Triple(48.00, 67.00, 4.5f),
    "KG" to Triple(41.50, 74.80, 5.5f),
    "TJ" to Triple(38.80, 71.00, 6f),
    "TM" to Triple(39.00, 59.50, 5.5f),
    "TR" to Triple(39.00, 35.00, 5f),
    "AF" to Triple(34.00, 66.00, 5f),
    "AE" to Triple(24.40, 54.40, 6.5f),
    "AZ" to Triple(40.30, 47.70, 6.5f),
    "UA" to Triple(49.00, 31.40, 5f),
    "US" to Triple(39.50, -98.35, 3.5f),
    "CA" to Triple(56.00, -96.00, 3f),
    "GB" to Triple(54.00, -2.50, 5f),
    "DE" to Triple(51.10, 10.40, 5.5f),
    "FR" to Triple(46.60, 2.40, 5f),
    "IT" to Triple(42.50, 12.50, 5f),
    "ES" to Triple(40.30, -3.70, 5f),
    "NL" to Triple(52.20, 5.30, 6.5f),
    "PL" to Triple(52.00, 19.40, 5.5f),
    "KR" to Triple(36.50, 127.80, 6f),
    "JP" to Triple(36.50, 138.00, 4.5f),
    "CN" to Triple(35.00, 104.00, 3.5f),
    "IN" to Triple(22.00, 79.00, 4f),
    "AU" to Triple(-25.00, 134.00, 3.5f),
)
