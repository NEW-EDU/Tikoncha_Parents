package uz.tikoncha_parent.domain.policy

import uz.tikoncha_parent.domain.model.GeoType
import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyEffectiveState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Instant

/**
 * Jadval HOZIR kuchdami (ro'yxatdagi "Hozir amalda" uchun) — server evaluator tartibida:
 *   1. TIRIKMI  — yoqilgan, pauzada emas, muddati o'tmagan, pullik gate
 *   2. SHARTLAR — vaqt VA joy VA limit kuni
 *
 * Vaqt — bolaning mahalliy vaqti (ota-ona bilan bir mintaqada deb olinadi).
 * Joy — bolaning oxirgi ma'lum joyi; noma'lum bo'lsa joyli jadval "faol" deyilmaydi.
 * [paid] `false` — bolaning tarifi bepul: tezkor blok va himoya paketlari ishlamaydi.
 */
fun Policy.isActiveNow(
    weekDay: Int,
    minuteOfDay: Int,
    now: Instant,
    paid: Boolean? = null,
    lastLat: Double? = null,
    lastLng: Double? = null,
): Boolean {
    if (effectiveState(now) != PolicyEffectiveState.ACTIVE) return false
    if (paid == false && (isQuickBlock || isProtection)) return false

    conditions.time?.let { if (!TimeRuleMatcher.matches(weekDay, minuteOfDay, it)) return false }
    conditions.location?.let { rule ->
        if (lastLat == null || lastLng == null) return false
        if (rule.contains(lastLat, lastLng) == rule.reverse) return false
    }
    limits.usage?.let { rule -> if (rule.days.none { it.num == weekDay }) return false }
    return true
}

/** Nuqta hudud ichidami ([LocationRule.reverse] hisobga olinmaydi). Buzuq qoida — ichida emas. */
fun LocationRule.contains(lat: Double, lng: Double): Boolean = when (geoType) {
    GeoType.CIRCLE -> {
        val cLat = centerLat
        val cLng = centerLng
        val r = radiusMeters
        cLat != null && cLng != null && r != null && distanceMeters(cLat, cLng, lat, lng) <= r
    }
    GeoType.POLYGON -> {
        val pts = polygon.orEmpty()
        if (pts.size < 3) false
        else {
            // Ray casting (kichik hududlar uchun tekis yaqinlashuv yetarli)
            var inside = false
            var j = pts.lastIndex
            for (i in pts.indices) {
                val (yi, xi) = pts[i].lat to pts[i].lng
                val (yj, xj) = pts[j].lat to pts[j].lng
                if ((yi > lat) != (yj > lat) && lng < (xj - xi) * (lat - yi) / (yj - yi) + xi) inside = !inside
                j = i
            }
            inside
        }
    }
}

private fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6_371_000.0
    val dLat = (lat2 - lat1).toRad()
    val dLng = (lng2 - lng1).toRad()
    val a = sin(dLat / 2) * sin(dLat / 2) + cos(lat1.toRad()) * cos(lat2.toRad()) * sin(dLng / 2) * sin(dLng / 2)
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
}

private fun Double.toRad(): Double = this * kotlin.math.PI / 180.0
