package uz.tikoncha_parent.presentation.tracking

import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.domain.model.ChildLocation
import uz.tikoncha_parent.presentation.map.LatLng

internal fun ChildLocation.toPerson(): Person {
    val location = if (
        latitude != null && longitude != null &&
        !(latitude == 0.0 && longitude == 0.0)
    ) LatLng(latitude, longitude) else null

    return Person(
        id = childId,
        name = firstName ?: "User",
        location = location,
        isSelf = false,
        lastSeen = updatedAt ?: "",
        lastSeenEpochMs = DateTimeUtil.toMillis(updatedAt),
        avatarUrl = avatarUrl,
        isMock = isMock == true,
        accuracyMeters = accuracyMeters?.takeIf { it > 0.0 },
    )
}