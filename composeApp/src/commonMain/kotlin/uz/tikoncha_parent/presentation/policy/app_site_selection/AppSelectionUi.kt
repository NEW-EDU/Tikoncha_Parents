package uz.tikoncha_parent.presentation.policy.app_site_selection

import uz.tikoncha_parent.presentation.policy.targets.CategoryLocalizer

import uz.tikoncha_parent.domain.model.apps.InstalledApp

data class AppSelectionUi(
    val name: String,
    val packageName: String,
    val category: String? = null,
    val order: Int = 0,
    val iconUrl: String? = null,
    val usageMinutes: Long = 0,
)

fun InstalledApp.toAppSelectionUi(): AppSelectionUi = AppSelectionUi(
    name = name,
    packageName = packageName,
    iconUrl = iconUrl,
    category = category?.let(CategoryLocalizer::toCode),
    order = order,
)
