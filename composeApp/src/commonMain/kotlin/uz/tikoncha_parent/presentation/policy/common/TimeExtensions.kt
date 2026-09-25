package uz.tikoncha_parent.presentation.policy.common


import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.pluralStringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.hours
import tikoncha_parents.composeapp.generated.resources.minutes

/** LocalTime ni daqiqalar soniga aylantiradi (00:00 dan). */
fun LocalTime.toMinutes(): Int = hour * 60 + minute

/** LocalTime ni "HH:mm" ko'rinishida formatlaydi. */
fun LocalTime.toHhMm(): String {
    val hh = hour.toString().padStart(2, '0')
    val mm = minute.toString().padStart(2, '0')
    return "$hh:$mm"
}

@Composable
fun formatDuration(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    val hourText = pluralStringResource(Res.plurals.hours, hours, hours)
    val minuteText = pluralStringResource(Res.plurals.minutes, minutes, minutes)

    return when {
        hours == 0 -> minuteText
        minutes == 0 -> hourText
        else -> "$hourText, $minuteText"
    }
}