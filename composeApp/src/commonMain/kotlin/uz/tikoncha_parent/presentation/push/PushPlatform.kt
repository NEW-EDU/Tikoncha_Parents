package uz.tikoncha_parent.presentation.push

expect object PushPlatform {
    fun initialize()

    /**
     * Tizimning bildirishnoma ruxsati oynasini ko'rsatadi (ilova ochilganda emas — tanishtiruvdagi alohida
     * ekrandan). Ruxsat berilgan bo'lsa true; Android 12 va pastida doim true.
     */
    suspend fun requestNotificationPermission(): Boolean
}
