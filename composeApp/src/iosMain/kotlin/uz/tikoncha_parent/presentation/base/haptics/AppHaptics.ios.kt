package uz.tikoncha_parent.presentation.base.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator

@Composable
actual fun rememberAppHaptics(): AppHaptics = remember { IosHaptics() }

/** UIKit generator'lari (PARENTS_UX §2.2 jadvali). Tizim "System Haptics" sozlamasini o'zi hisobga oladi. */
private class IosHaptics : AppHaptics {
    private val light = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    private val medium = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
    private val selection = UISelectionFeedbackGenerator()
    private val notification = UINotificationFeedbackGenerator()

    override fun toggle(on: Boolean) = (if (on) medium else light).impactOccurred()

    override fun tick() = selection.selectionChanged()

    override fun wheelTick() = selection.selectionChanged()

    override fun success() =
        notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)

    override fun error() =
        notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
}
