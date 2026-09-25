package uz.tikoncha_parent.domain.repository.policy

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PackCatalog

interface ProtectionPackRepository {
    /** [force] bo'lmasa keshdan qaytaradi. */
    suspend fun catalog(force: Boolean = false): Outcome<PackCatalog>
    fun cachedCatalog(): PackCatalog?
}