package uz.tikoncha_parent.data.local

/**
 * Ilova ishlab turgan paytdagi (xotiradagi) oxirgi ro'yxatlar: ekran qayta ochilganda shimmer/bo'sh holat
 * chaqnamasin — eski ro'yxat darhol chiziladi, yangisi jimgina keladi. Kalitga foydalanuvchi qo'shiladi,
 * logout'da ([AppSettings.clearSession]) tozalanadi.
 */
object SessionCache {
    private val values = mutableMapOf<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String): T? = values["${AppSettings.userId}|$key"] as? T

    fun put(key: String, value: Any) {
        values["${AppSettings.userId}|$key"] = value
    }

    fun clear() = values.clear()
}
