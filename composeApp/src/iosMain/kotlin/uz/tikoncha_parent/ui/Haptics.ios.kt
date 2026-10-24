package uz.tikoncha_parent.ui

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

private val light by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight) }
private val medium by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium) }
private val heavy by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy) }
private val rigid by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleRigid) }
private val soft by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleSoft) }
private val notification by lazy { UINotificationFeedbackGenerator() }

internal actual fun platformHaptic(kind: HapticKind, level: HapticsLevel) {
    val intensity = when (level) {
        HapticsLevel.OFF -> return
        HapticsLevel.LIGHT -> 0.55
        HapticsLevel.STANDARD -> 0.8
        HapticsLevel.STRONG -> 1.0
    }
    when (kind) {
        HapticKind.TAP -> when (level) {
            HapticsLevel.LIGHT -> light
            HapticsLevel.STANDARD -> medium
            else -> heavy
        }.impactOccurredWithIntensity(intensity)
        HapticKind.TICK -> rigid.impactOccurredWithIntensity(intensity * 0.9)
        HapticKind.TOGGLE -> (if (level == HapticsLevel.STRONG) heavy else soft).impactOccurredWithIntensity(intensity)
        HapticKind.SUCCESS -> notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
        HapticKind.WARNING -> notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeWarning)
        HapticKind.ERROR -> notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
    }
}
