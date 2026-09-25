package uz.tikoncha_parent.presentation.base.haptics

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Ilova bo'ylab bir xil "teginish tili" — iOS dagi kabi, Student ilovasi bilan bir xil.
 *
 * Qoida: haptika holat HAQIQATAN o'zgarganda (animatsiya bilan bir paytda), har bosishda emas.
 *
 *  · [toggle]    — qulf/switch/katak holati o'zgardi (yoqildi / o'chdi)
 *  · [tick]      — segment, tab, chip, variant tanlandi; sahifa almashdi
 *  · [wheelTick] — g'ildirak yoki slayder bir qadam o'tdi (tez-tez, yengil)
 *  · [success]   — saqlandi, qo'shildi
 *  · [error]     — so'rov o'tmadi
 *
 * Android: tizim "Teginish tebranishi" o'chiq bo'lsa ilova o'z vibratoriga o'tadi.
 * iOS: UIKit feedback generator'lari; tizim sozlamasi o'zi hisobga olinadi.
 */
interface AppHaptics {
    fun toggle(on: Boolean)
    fun tick()
    fun wheelTick()
    fun success()
    fun error()
}

@Composable
expect fun rememberAppHaptics(): AppHaptics

/** Xato dialogi chiqqanda bir marta "rad" tebranishi (null → jim). */
@Composable
fun ErrorHaptic(error: Any?) {
    val haptics = rememberAppHaptics()
    LaunchedEffect(error) { if (error != null) haptics.error() }
}

/**
 * Jarayon orqali o'zgaradigan switch uchun (server, tasdiq oynasi).
 * Qaytarilgan funksiyani foydalanuvchi bosganda chaqiring ("qurollash"): [checked]
 * HAQIQATAN o'zgarganda bir marta [AppHaptics.toggle] beriladi. Sinxronizatsiya
 * yoki boshqa sabab bilan kelgan o'zgarish — jim. [timeout] ichida o'zgarmasa, bekor.
 */
@Composable
fun rememberToggleHaptic(checked: Boolean, timeout: Duration = 5.seconds): () -> Unit {
    val haptics = rememberAppHaptics()
    var armedAt by remember { mutableStateOf<TimeSource.Monotonic.ValueTimeMark?>(null) }
    var shown by remember { mutableStateOf(checked) }
    LaunchedEffect(checked) {
        if (checked == shown) return@LaunchedEffect      // birinchi kompozitsiya
        shown = checked
        val mark = armedAt
        if (mark != null && mark.elapsedNow() <= timeout) haptics.toggle(checked)
        armedAt = null
    }
    return remember { { armedAt = TimeSource.Monotonic.markNow() } }
}

/**
 * Sahifalagich (kun/hafta) barmoq bilan surilib, sahifa yarmidan o'tganda — tik
 * (qo'yib yuborishni kutmasdan). Dasturiy o'tish (`scrollToPage`) — jim.
 */
@Composable
fun PagerSwipeHaptic(pagerState: PagerState) {
    val haptics = rememberAppHaptics()
    LaunchedEffect(pagerState) {
        var dragged = false
        var last = pagerState.currentPage
        launch {
            pagerState.interactionSource.interactions.collect { if (it is DragInteraction.Start) dragged = true }
        }
        launch {
            snapshotFlow { pagerState.isScrollInProgress }.collect { if (!it) dragged = false }
        }
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page != last && dragged) haptics.tick()
            last = page
        }
    }
}
