package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.ChildLocation
import uz.tikoncha_parent.domain.model.app_error.getOrNull
import uz.tikoncha_parent.domain.repository.ChildRepository

/** Bolaning oxirgi ma'lum joylashuvi — hudud tanlashda xarita shu yerdan boshlanadi. */
class GetChildLocationUseCase(
    private val repository: ChildRepository,
) {
    suspend operator fun invoke(childId: String): ChildLocation? =
        repository.childrenLocation().getOrNull()
            ?.firstOrNull { it.childId == childId && it.latitude != null && it.longitude != null }
}
