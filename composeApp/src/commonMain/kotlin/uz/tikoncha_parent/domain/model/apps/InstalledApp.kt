package uz.tikoncha_parent.domain.model.apps

/** Bola qurilmasidagi ilova (`GET /installed-apps`). [order] — kichik = ko'proq ishlatiladi. */
data class InstalledApp(
    val packageName: String,
    val name: String,
    val category: String?,
    val iconUrl: String?,
    val order: Int,
    /** Google Play ID (`GET /installed-apps` `category_id`); [category] — Play nomi. */
    val categoryId: String? = null,
)
