package uz.tikoncha_parent.presentation.protection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.rounded.AppBlocking
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PhonelinkLock
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.*
import tikoncha_parents.composeapp.generated.resources.holat_nomalum
import tikoncha_parents.composeapp.generated.resources.holat_nomalum_desc
import tikoncha_parents.composeapp.generated.resources.mode_default_desc
import tikoncha_parents.composeapp.generated.resources.mode_default_title
import tikoncha_parents.composeapp.generated.resources.mode_gujanak_desc
import tikoncha_parents.composeapp.generated.resources.mode_gujanak_title
import tikoncha_parents.composeapp.generated.resources.mode_qalqon_desc
import tikoncha_parents.composeapp.generated.resources.mode_qalqon_title
import uz.tikoncha_parent.domain.model.protection.ChildMode
import uz.tikoncha_parent.domain.model.protection.ChildPermission
import uz.tikoncha_parent.domain.model.protection.StrictMethod

// ─────────── Enum → string resurslari ───────────

fun ChildMode.title(): StringResource = when (this) {
    ChildMode.DEFAULT -> Res.string.mode_default_title
    ChildMode.GUJANAK -> Res.string.mode_gujanak_title
    ChildMode.STRICT -> Res.string.mode_qalqon_title
    ChildMode.UNKNOWN -> Res.string.holat_nomalum
}

fun ChildMode.desc(): StringResource = when (this) {
    ChildMode.DEFAULT -> Res.string.mode_default_desc
    ChildMode.GUJANAK -> Res.string.mode_gujanak_desc
    ChildMode.STRICT -> Res.string.mode_qalqon_desc
    ChildMode.UNKNOWN -> Res.string.holat_nomalum_desc
}

fun ChildMode.levelLabel(): StringResource = when (this) {
    ChildMode.DEFAULT -> Res.string.himoya_boshlangich
    ChildMode.GUJANAK -> Res.string.himoya_orta
    ChildMode.STRICT -> Res.string.himoya_toliq
    ChildMode.UNKNOWN -> Res.string.holat_nomalum
}

fun StrictMethod.title(): StringResource = when (this) {
    StrictMethod.TIMER -> Res.string.usul_taymer
    StrictMethod.SECRET_CODE -> Res.string.usul_maxfiy_kod
    StrictMethod.TEXT -> Res.string.usul_maxfiy_matn
    StrictMethod.PARENT_REQUEST -> Res.string.ota_ona_ruxsati
    StrictMethod.UNKNOWN -> Res.string.holat_nomalum
}

fun ChildPermission.label(): StringResource = when (this) {
    ChildPermission.NOTIFICATION -> Res.string.perm_notification
    ChildPermission.USAGE_STATS -> Res.string.perm_usage_stats
    ChildPermission.OVERLAY -> Res.string.perm_overlay
    ChildPermission.OVERLAY_POPUP -> Res.string.perm_overlay_popup
    ChildPermission.LOCATION -> Res.string.perm_location
    ChildPermission.GPS_ENABLED -> Res.string.perm_gps
    ChildPermission.BATTERY -> Res.string.perm_battery
    ChildPermission.AUTO_START -> Res.string.perm_auto_start
    ChildPermission.ACCESSIBILITY -> Res.string.perm_accessibility
    ChildPermission.DEVICE_ADMIN -> Res.string.perm_device_admin
}

/** Ota-ona uçun nom: sozlama nima beradi (Android sozlamasining nomi emas — u [label] da, "qayerda yoqiladi" uçun). */
fun ChildPermission.title(): StringResource = when (this) {
    ChildPermission.NOTIFICATION -> Res.string.perm_title_notification
    ChildPermission.USAGE_STATS -> Res.string.perm_title_usage_stats
    ChildPermission.OVERLAY -> Res.string.perm_title_overlay
    ChildPermission.OVERLAY_POPUP -> Res.string.perm_title_overlay_popup
    ChildPermission.LOCATION -> Res.string.perm_title_location
    ChildPermission.GPS_ENABLED -> Res.string.perm_title_gps
    ChildPermission.BATTERY -> Res.string.perm_title_battery
    ChildPermission.AUTO_START -> Res.string.perm_title_auto_start
    ChildPermission.ACCESSIBILITY -> Res.string.perm_title_accessibility
    ChildPermission.DEVICE_ADMIN -> Res.string.perm_title_device_admin
}

/** Qator belgisi: matndan oldin ma'noni rasm bilan beradi (keyin 3D rasmlar bilan almaştiriladi). */
fun ChildPermission.icon(): ImageVector = when (this) {
    ChildPermission.NOTIFICATION -> Icons.Rounded.Notifications
    ChildPermission.USAGE_STATS -> Icons.Rounded.HourglassBottom
    ChildPermission.OVERLAY, ChildPermission.OVERLAY_POPUP -> Icons.Rounded.AppBlocking
    ChildPermission.LOCATION -> Icons.Rounded.LocationOn
    ChildPermission.GPS_ENABLED -> Icons.Rounded.GpsFixed
    ChildPermission.BATTERY -> Icons.Rounded.BatteryChargingFull
    ChildPermission.AUTO_START -> Icons.Rounded.RestartAlt
    ChildPermission.ACCESSIBILITY -> Icons.Rounded.Language
    ChildPermission.DEVICE_ADMIN -> Icons.Rounded.PhonelinkLock
}

// ─────────── Vaqt helpers ───────────

fun parseInstantOrNull(iso: String?): Instant? {
    if (iso.isNullOrBlank()) return null
    runCatching { return Instant.parse(iso) }
    return runCatching { LocalDateTime.parse(iso).toInstant(TimeZone.UTC) }.getOrNull()
}

/** "hozirgina" / "5 daqiqa oldin" / "3 soat oldin" / "2 kun oldin" */
@Composable
fun relativeTimeText(instant: Instant?): String {
    if (instant == null) return stringResource(Res.string.sinxron_yoq)
    val minutes = (kotlin.time.Clock.System.now() - instant).inWholeMinutes
    return when {
        minutes < 1 -> stringResource(Res.string.hozirgina)
        minutes < 60 -> stringResource(Res.string.daqiqa_oldin, minutes.toInt())
        minutes < 60 * 24 -> stringResource(Res.string.soat_oldin, (minutes / 60).toInt())
        else -> stringResource(Res.string.kun_oldin, (minutes / (60 * 24)).toInt())
    }
}

/** 24 soatgacha: "23:14:09", 1 soatdan kam: "44:09" */
fun formatCountdown(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    fun p(n: Int) = n.toString().padStart(2, '0')
    return if (h > 0) "${p(h)}:${p(m)}:${p(s)}" else "${p(m)}:${p(s)}"
}

// ─────────── Ripple'siz click ───────────

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )
}


fun ChildMode.icon(): ImageVector = when (this) {
    ChildMode.STRICT -> Icons.Default.Security    // to'liq himoya
    ChildMode.GUJANAK -> Icons.Default.Shield      // o'rta
    else -> Icons.Default.GppMaybe                 // himoya o'chiq / noma'lum
}

fun ChildMode.shortDesc(): StringResource = when (this) {
    ChildMode.DEFAULT -> Res.string.himoya_ochiq_qisqa
    ChildMode.GUJANAK -> Res.string.gujanak_faol_qisqa
    ChildMode.STRICT -> Res.string.toliq_himoya_faol_qisqa
    ChildMode.UNKNOWN -> Res.string.holat_nomalum_desc
}