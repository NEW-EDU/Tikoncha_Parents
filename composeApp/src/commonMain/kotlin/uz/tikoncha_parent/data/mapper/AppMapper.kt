package uz.tikoncha_parent.data.mapper

import uz.tikoncha_parent.data.remote.model.AppDto
import uz.tikoncha_parent.domain.model.apps.InstalledApp

fun AppDto.toDomain(): InstalledApp = InstalledApp(
    packageName = `package`,
    name = name?.takeIf { it.isNotBlank() } ?: `package`,
    category = category?.takeIf { it.isNotBlank() },
    iconUrl = icon,
    order = order,
)
