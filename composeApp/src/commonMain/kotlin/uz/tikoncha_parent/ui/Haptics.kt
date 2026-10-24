package uz.tikoncha_parent.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import uz.tikoncha_parent.data.local.AppSettings

/*
 * Taktil qatlam (iOS student ilovasidagidek): tugma BOSILGAN zahoti (qo'yib yuborilganda emas) zarba,
 * tanlov o'zgarishida "tik", natijalarda muvaffaqiyat / ogohlantirish / xato. Kuchi Sozlamalar → Tebranish'da.
 * iOS — UIKit generatorlari; Android — vibratsiya primitivlari (bo'lmasa — tayyor effektlar, bo'lmasa — qisqa zarba).
 */

/** Sozlamadagi kuch. Standart — Kuchli (student ilovasidagidek). */
enum class HapticsLevel { OFF, LIGHT, STANDARD, STRONG }

enum class HapticKind {
    /** Tugma, karta, qator bosilishi. */
    TAP,
    /** Tanlov o'zgarishi: segment, tab, ro'yxatdan tanlash. */
    TICK,
    /** Kalit (switch) yoqish/o'chirish. */
    TOGGLE,
    SUCCESS,
    WARNING,
    ERROR,
}

internal expect fun platformHaptic(kind: HapticKind, level: HapticsLevel)

object Haptics {
    private var cached by mutableStateOf<HapticsLevel?>(null)

    var level: HapticsLevel
        get() = cached ?: (runCatching { HapticsLevel.valueOf(AppSettings.hapticsLevelName) }.getOrNull()
            ?: HapticsLevel.STRONG).also { cached = it }
        set(value) {
            cached = value
            AppSettings.hapticsLevelName = value.name
        }

    fun play(kind: HapticKind) {
        val l = level
        if (l == HapticsLevel.OFF) return
        runCatching { platformHaptic(kind, l) }
    }

    /** Sozlamalarda tanlangan kuchni darhol his qildirish. */
    fun preview(level: HapticsLevel) {
        if (level == HapticsLevel.OFF) return
        runCatching { platformHaptic(HapticKind.TAP, level) }
    }

    fun tap() = play(HapticKind.TAP)
    fun tick() = play(HapticKind.TICK)
    fun toggle() = play(HapticKind.TOGGLE)
    fun success() = play(HapticKind.SUCCESS)
    fun warning() = play(HapticKind.WARNING)
    fun error() = play(HapticKind.ERROR)
}

/**
 * M3 tugmalari uchun: `interactionSource = rememberHapticSource()` — bosilgan zahoti zarba (student ilovasidagi
 * PressableStyle kabi).
 */
@Composable
fun rememberHapticSource(kind: HapticKind = HapticKind.TAP): MutableInteractionSource {
    val source = remember { MutableInteractionSource() }
    LaunchedEffect(source, kind) {
        source.interactions.collect { if (it is PressInteraction.Press) Haptics.play(kind) }
    }
    return source
}
