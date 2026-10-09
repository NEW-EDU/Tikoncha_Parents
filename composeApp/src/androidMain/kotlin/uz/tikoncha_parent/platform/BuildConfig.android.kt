package uz.tikoncha_parent.platform

import uz.tikoncha_parent.BuildConfig

actual object BuildConfig {
    actual val isDebug: Boolean = BuildConfig.DEBUG
    actual val deviceType: String = "ANDROID"
    actual val apiOverride: String = BuildConfig.API_OVERRIDE
}