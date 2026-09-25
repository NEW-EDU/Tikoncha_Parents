package uz.tikoncha_parent.domain.policy

/**
 * Tikoncha ilovalari HECH QACHON bloklanmaydi — ataylab qattiq kodlangan.
 *
 * Bola ilovasi yopilsa himoyaning o'zi o'chadi, Parent ilovasi yopilsa bola
 * telefonidan ota-onaga yozib bo'lmaydi. Server ham rad etadi (`OWN_PACKAGES`),
 * Student ilovasi ham (`NeverBlock`, evaluator'ning birinchi qatori) — bu yerda
 * tanlash ro'yxatlari va statistika qulfidan yashirish uchun. Olib tashlamang.
 */
object NeverBlock {
    val PACKAGES: Set<String> = setOf(
        "uz.tikoncha.student",
        "uz.tikoncha.parent",
    )

    fun isNeverBlocked(packageName: String): Boolean = packageName.trim().lowercase() in PACKAGES
}
