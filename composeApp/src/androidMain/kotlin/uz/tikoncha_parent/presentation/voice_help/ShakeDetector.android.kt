package uz.tikoncha_parent.presentation.voice_help

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import uz.tikoncha_parent.platform.appContext
import kotlin.math.sqrt

internal actual fun createShakeSensor(): ShakeSensor? {
    val manager = appContext?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return null
    val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return null
    return AndroidShakeSensor(manager, accelerometer)
}

/** Hodisalar asosiy oqimga keladi (registerListener handler'siz — main looper). */
private class AndroidShakeSensor(
    private val manager: SensorManager,
    private val accelerometer: Sensor,
) : ShakeSensor, SensorEventListener {
    private var onSample: ((Double, Long) -> Unit)? = null

    override fun start(onSample: (gForce: Double, timeMs: Long) -> Unit) {
        val wasRunning = this.onSample != null
        this.onSample = onSample
        if (!wasRunning) manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun stop() {
        if (onSample == null) return
        onSample = null
        manager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val callback = onSample ?: return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val g = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        callback(g.toDouble(), SystemClock.elapsedRealtime())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
