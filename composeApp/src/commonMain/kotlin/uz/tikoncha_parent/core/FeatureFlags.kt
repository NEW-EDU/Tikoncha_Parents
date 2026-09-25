package uz.tikoncha_parent.core

/** Kodi tayyor, lekin foydalanuvchiga hali ochilmagan imkoniyatlar. */
object FeatureFlags {
    /**
     * Bonus vaqt ("Vaqtincha ruxsat").
     * Hujjat §8.1: server jonli ALLOW jadvalida ro'yxatda yo'q HAMMA ilovani yopadi —
     * backend `exclusive` / `kind = EXCEPTION` qarori chiqmaguncha yoqilmaydi.
     */
    const val BONUS_TIME: Boolean = false
}