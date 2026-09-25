package uz.tikoncha_parent.domain.policy

/**
 * Server bloklashni rad etadigan paketlar — `Tikoncha_Backend/app/modules/policies_v2/protected.py`
 * (`PROTECTED_PACKAGES`) bilan BIR XIL tutilsin. Bola qurilmasining o'z tasnifi (launcher,
 * standart klaviatura va h.k.) Parent'da yo'q; kamida shu ro'yxat — aks holda qulf bosilganda
 * server `policy_protected_package` xatosini qaytaradi.
 */
object ProtectedPackages {

    /** Qo'ng'iroq, kontaktlar, SMS, klaviatura — bola doim aloqada bo'lsin. */
    private val COMM = setOf(
        "com.android.phone",
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.contacts",
        "com.google.android.contacts",
        "com.samsung.android.contacts",
        "com.samsung.android.app.contacts",
        "com.hihonor.contacts",
        "com.android.mms",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.hihonor.mms",
        "com.samsung.android.honeyboard",
        "com.sec.android.inputmethod",
        "com.google.android.inputmethod.latin",
        "com.touchtype.swiftkey",
        "com.android.incallui",
        "com.samsung.android.incallui",
        "com.android.emergency",
    )

    /** Sozlamalar va ruxsatlar — yopilsa Tikoncha'ga kerakli ruxsatni tiklab bo'lmaydi. */
    private val SETTINGS = setOf(
        "com.android.settings",
        "com.android.settings.intelligence",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.samsung.android.packageinstaller",
        "com.android.systemui",
    )

    val PACKAGES: Set<String> = NeverBlock.PACKAGES + COMM + SETTINGS

    fun isProtected(packageName: String): Boolean = packageName.trim().lowercase() in PACKAGES

    /** Tezkor blok qulfi va tanlash ro'yxatlari shu bilan tekshiriladi. */
    fun canBlock(packageName: String): Boolean =
        packageName.isNotBlank() && packageName != "*" && !isProtected(packageName)
}
