package uz.tikoncha_parent.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import uz.tikoncha_parent.platform.appContext

private val vibrator: Vibrator? by lazy {
    val ctx = appContext ?: return@lazy null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ctx.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        ctx.getSystemService(Vibrator::class.java)
    }
}

/**
 * Sifat tartibi: (1) vibratsiya primitivlari — Pixel/Samsung flagmanlarida aniq, "qisqa va quruq" zarbalar,
 * kuchi [scale] bilan; (2) tayyor effektlar (CLICK/TICK/HEAVY_CLICK); (3) eski qurilmalarda — qisqa zarba.
 */
internal actual fun platformHaptic(kind: HapticKind, level: HapticsLevel) {
    val v = vibrator ?: return
    if (!v.hasVibrator()) return
    val scale = when (level) {
        HapticsLevel.OFF -> return
        HapticsLevel.LIGHT -> 0.4f
        HapticsLevel.STANDARD -> 0.7f
        HapticsLevel.STRONG -> 1f
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val c = VibrationEffect.startComposition()
        val primitives = when (kind) {
            HapticKind.TAP -> listOf(VibrationEffect.Composition.PRIMITIVE_CLICK to 0)
            HapticKind.TICK -> listOf(VibrationEffect.Composition.PRIMITIVE_TICK to 0)
            HapticKind.TOGGLE -> listOf(VibrationEffect.Composition.PRIMITIVE_CLICK to 0)
            HapticKind.SUCCESS -> listOf(
                VibrationEffect.Composition.PRIMITIVE_CLICK to 0,
                VibrationEffect.Composition.PRIMITIVE_CLICK to 90,
            )
            HapticKind.WARNING -> listOf(VibrationEffect.Composition.PRIMITIVE_THUD to 0)
            HapticKind.ERROR -> listOf(
                VibrationEffect.Composition.PRIMITIVE_THUD to 0,
                VibrationEffect.Composition.PRIMITIVE_THUD to 110,
            )
        }
        if (v.areAllPrimitivesSupported(*primitives.map { it.first }.distinct().toIntArray())) {
            primitives.forEach { (p, delay) -> c.addPrimitive(p, scale, delay) }
            v.vibrate(c.compose())
            return
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val effect = when (kind) {
            HapticKind.TAP, HapticKind.TOGGLE ->
                if (level == HapticsLevel.STRONG) VibrationEffect.EFFECT_HEAVY_CLICK else VibrationEffect.EFFECT_CLICK
            HapticKind.TICK -> VibrationEffect.EFFECT_TICK
            HapticKind.SUCCESS -> VibrationEffect.EFFECT_DOUBLE_CLICK
            HapticKind.WARNING, HapticKind.ERROR -> VibrationEffect.EFFECT_HEAVY_CLICK
        }
        // Yengil darajada tayyor effektlar juda kuchli — qisqa, past amplitudali zarba bilan almashtiramiz
        if (level == HapticsLevel.LIGHT && kind != HapticKind.TICK) {
            v.vibrate(VibrationEffect.createOneShot(12, 90))
        } else {
            v.vibrate(VibrationEffect.createPredefined(effect))
        }
        return
    }

    val (ms, amp) = when (kind) {
        HapticKind.TICK -> 8L to (60 * scale).toInt()
        HapticKind.SUCCESS, HapticKind.WARNING, HapticKind.ERROR -> 30L to (200 * scale).toInt()
        else -> 15L to (160 * scale).toInt()
    }
    v.vibrate(VibrationEffect.createOneShot(ms, amp.coerceIn(1, 255)))
}
