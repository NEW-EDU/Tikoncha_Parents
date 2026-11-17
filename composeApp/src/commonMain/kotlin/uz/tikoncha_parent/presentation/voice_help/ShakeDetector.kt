package uz.tikoncha_parent.presentation.voice_help

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Akselerometr: har örniş — tezlanişning umumiy kattaligi (g, Yer tortişi bilan; tinç turganda ≈ 1). */
internal interface ShakeSensor {
    fun start(onSample: (gForce: Double, timeMs: Long) -> Unit)
    fun stop()
}

/** Android — SensorManager akselerometri, iOS — CoreMotion. Akselerometr yöq bölsa — null. Asosiy oqimda. */
internal expect fun createShakeSensor(): ShakeSensor?

/**
 * Telefon silkitilganda [onShake]. [enabled] false bölsa yoki ilova orqa fonda bölsa — sensor öçiq
 * (ekrandan çiqilganda ham töxtaydi).
 */
@Composable
fun ShakeDetector(enabled: Boolean, onShake: () -> Unit) {
    if (!enabled) return
    val latest by rememberUpdatedState(onShake)
    val sensor = remember { createShakeSensor() } ?: return
    LifecycleResumeEffect(sensor) {
        val filter = ShakeFilter()
        sensor.start { g, t -> if (filter.onSample(g, t)) latest() }
        onPauseOrDispose { sensor.stop() }
    }
}

/**
 * Silkitiş = ~1 s içida [thresholdG] dan oşgan ikki zarba (oldinga-orqaga). Bitta zarba (telefonni stolga qöyiş,
 * tuşirib yuboriş) hisoblanmaydi. Topilgaç [debounceMs] davomida yangisi hisoblanmaydi.
 */
internal class ShakeFilter(
    private val thresholdG: Double = 2.5,
    private val debounceMs: Long = 1_000,
) {
    private var firstPeakAt = NONE
    private var lastShakeAt = NONE

    fun onSample(gForce: Double, timeMs: Long): Boolean {
        if (gForce < thresholdG) return false
        if (lastShakeAt != NONE && timeMs - lastShakeAt < debounceMs) return false
        val first = firstPeakAt
        if (first == NONE || timeMs - first > PEAK_WINDOW_MS) {
            firstPeakAt = timeMs
            return false
        }
        if (timeMs - first < MIN_PEAK_GAP_MS) return false // öşa zarbaning davomi
        firstPeakAt = NONE
        lastShakeAt = timeMs
        return true
    }

    private companion object {
        const val NONE = Long.MIN_VALUE
        const val PEAK_WINDOW_MS = 1_000L
        const val MIN_PEAK_GAP_MS = 100L
    }
}
