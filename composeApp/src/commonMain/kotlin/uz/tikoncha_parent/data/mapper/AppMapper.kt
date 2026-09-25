package uz.tikoncha_parent.data.mapper

import uz.tikoncha_parent.data.remote.model.AppDto
import uz.tikoncha_parent.presentation.policy.app_site_selection.AppSelectionUi
import uz.tikoncha_parent.presentation.policy.app_site_selection.CategoryLocalizer

fun AppDto.toAppSelectionUi(): AppSelectionUi = AppSelectionUi(
    name = name ?: "",
    packageName = `package`,
    iconUrl = icon,
    category = CategoryLocalizer.toCode(category ?: ""),
    order = order,
)