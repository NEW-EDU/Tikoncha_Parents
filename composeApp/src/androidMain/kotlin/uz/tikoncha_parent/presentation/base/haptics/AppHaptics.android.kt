package uz.tikoncha_parent.presentation.base.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
actual fun rememberAppHaptics(): AppHaptics {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current.applicationContext
    return remember(haptic, context) { AndroidHaptics(haptic, context) }
}

/**
 * Ikki yo'l:
 *  · Telefonda "Teginish tebranishi" YOQIQ — tizim haptikasi (`HapticFeedbackType`),
 *    brend o'z naqshlarini beradi.
 *  · O'CHIQ (Samsung'da ko'p) — tizim haptikasi jim qoladi, shunda ilova o'z vibratori
 *    bilan tayyor effektlarni beradi (`VibrationEffect.createPredefined`) — iOS dagi kabi
 *    doim seziladi. Tebranish umuman o'chirilgan bo'lsa u ham jim.
 */
private class AndroidHaptics(
    private val haptic: HapticFeedback,
    private val context: Context,
) : AppHaptics {

    private val vibrator: Vibrator? by lazy {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }.getOrNull()?.takeIf { it.hasVibrator() }
    }

    override fun toggle(on: Boolean) = play(
        if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
        if (on) Effect.CLICK else Effect.TICK,
    )

    override fun tick() = play(HapticFeedbackType.SegmentTick, Effect.CLICK)

    override fun wheelTick() = play(HapticFeedbackType.SegmentFrequentTick, Effect.TICK)

    override fun success() = play(HapticFeedbackType.Confirm, Effect.DOUBLE_CLICK)

    override fun error() = play(HapticFeedbackType.Reject, Effect.REJECT)

    private fun play(type: HapticFeedbackType, fallback: Effect) {
        if (systemTouchHapticsEnabled()) {
            haptic.performHapticFeedback(type)
            return
        }
        val v = vibrator ?: return
        runCatching { v.vibrate(fallback.build()) }
    }

    private fun systemTouchHapticsEnabled(): Boolean = runCatching {
        Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0
    }.getOrDefault(true)

    private enum class Effect {
        TICK, CLICK, DOUBLE_CLICK, REJECT;

        fun build(): VibrationEffect = when (this) {
            // Uch qisqa zarb — "yo'q" (iOS error notification kabi)
            REJECT -> VibrationEffect.createWaveform(longArrayOf(0, 30, 60, 30, 60, 30), -1)
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                VibrationEffect.createPredefined(
                    when (this) {
                        TICK -> VibrationEffect.EFFECT_TICK
                        CLICK -> VibrationEffect.EFFECT_CLICK
                        else -> VibrationEffect.EFFECT_DOUBLE_CLICK
                    },
                )
            } else {
                when (this) {
                    TICK -> VibrationEffect.createOneShot(10, 120)
                    CLICK -> VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE)
                    else -> VibrationEffect.createWaveform(longArrayOf(0, 18, 70, 18), -1)
                }
            }
        }
    }
}
