@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package uz.tikoncha_parent.presentation.voice_help

import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import kotlin.math.sqrt

internal actual fun createShakeSensor(): ShakeSensor? =
    if (Accelerometer.available) IosShakeSensor() else null

private class IosShakeSensor : ShakeSensor {
    private var listener: ((Double, Long) -> Unit)? = null

    override fun start(onSample: (gForce: Double, timeMs: Long) -> Unit) {
        stop()
        listener = onSample
        Accelerometer.add(onSample)
    }

    override fun stop() {
        val l = listener ?: return
        listener = null
        Accelerometer.remove(l)
    }
}

/**
 * Apple tavsiyasi: ilovada bitta CMMotionManager. Ekranlar almaşayotganda (yangisi yoqiladi, eskisi öçiriladi)
 * bir-biriga xalaqit bermasligi uçun tinglovçilar royxati; royxat böşaganda yangilaniş töxtatiladi.
 * Yangilanişlar asosiy navbatda (main queue) keladi; ruxsat (NSMotionUsageDescription) kerak emas.
 */
private object Accelerometer {
    private const val UPDATE_INTERVAL_S = 1.0 / 50.0

    private val manager by lazy { CMMotionManager() }
    private val listeners = mutableListOf<(Double, Long) -> Unit>()

    val available: Boolean get() = manager.accelerometerAvailable

    fun add(listener: (Double, Long) -> Unit) {
        listeners += listener
        if (listeners.size > 1) return
        manager.accelerometerUpdateInterval = UPDATE_INTERVAL_S
        manager.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue) { data, _ ->
            val sample = data ?: return@startAccelerometerUpdatesToQueue
            val g = sample.acceleration.useContents { sqrt(x * x + y * y + z * z) }
            val timeMs = (sample.timestamp * 1000.0).toLong()
            listeners.toList().forEach { it(g, timeMs) }
        }
    }

    fun remove(listener: (Double, Long) -> Unit) {
        if (!listeners.remove(listener)) return
        if (listeners.isEmpty()) manager.stopAccelerometerUpdates()
    }
}
