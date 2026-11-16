package uz.tikoncha_parent.data.mapper

import uz.tikoncha_parent.data.remote.model.ChildrenLocationItemDto
import uz.tikoncha_parent.domain.model.ChildLocation

fun ChildrenLocationItemDto.toChildLocation(): ChildLocation? {
    val id = child_user_id ?: return null
    return ChildLocation(
        childId = id,
        firstName = first_name,
        lastName = last_name,
        avatarUrl = avatar_url,
        latitude = lat,
        longitude = lng,
        updatedAt = updated_at,
        isMock = is_mock,
        accuracyMeters = accuracy_m,
    )
}