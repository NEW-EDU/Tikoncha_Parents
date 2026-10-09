package uz.tikoncha_parent.platform

expect object BuildConfig {
    val isDebug: Boolean
    val deviceType: String // "ANDROID" yoki "IOS"
    /** Faqat sinov build'i: `-Ptikoncha.api=http://127.0.0.1:8000`. Bo'sh — prod. iOS'da doim bo'sh. */
    val apiOverride: String
}