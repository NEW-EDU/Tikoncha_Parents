package uz.tikoncha_parent.platform

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual object BuildConfig {
    actual val isDebug: Boolean = Platform.isDebugBinary
    actual val deviceType: String = "IOS"
    actual val apiOverride: String = ""
    actual val debugLaunchArgs: List<String> =
        if (isDebug) platform.Foundation.NSProcessInfo.processInfo.arguments.map { it.toString() } else emptyList()

}