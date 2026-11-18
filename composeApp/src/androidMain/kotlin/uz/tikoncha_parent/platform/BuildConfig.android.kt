package uz.tikoncha_parent.platform

import uz.tikoncha_parent.BuildConfig

actual object BuildConfig {
    actual val isDebug: Boolean = BuildConfig.DEBUG
    actual val deviceType: String = "ANDROID"
    actual val apiOverride: String = BuildConfig.API_OVERRIDE
    actual val debugLaunchArgs: List<String> get() = launchArgs

    /**
     * Faqat debug: iOS'dagi `-hoglt.*` argumentlari Android'da intent extras orqali (MainActivity tölduradi), masalan
     *   adb shell am start -S -n uz.tikoncha.parent/uz.tikoncha_parent.MainActivity \
     *     --es hoglt.screen assistant --es hoglt.ask "Testga TikTokni yop"
     * Bayroq uçun qiymat "1" (`--es hoglt.listen 1`).
     */
    @Volatile
    internal var launchArgs: List<String> = emptyList()
}