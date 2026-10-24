package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.policy.PackCatalog
import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.ProtectionPackStatus
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository
import uz.tikoncha_parent.domain.repository.policy.ProtectionPackRepository

class GetProtectionPackStatusesUseCase(
    private val packRepository: ProtectionPackRepository,
    private val policyRepository: PolicyRepository,
) {
    /** Katalog + keshdagi PROTECTION jadvallarini qo'shib, har paket uchun holat yig'adi. */
    suspend operator fun invoke(
        childId: String,
        myUserId: String,
    ): Outcome<List<ProtectionPackStatus>> = when (val catalog = packRepository.catalog()) {
        is Outcome.Failure -> catalog
        is Outcome.Success -> Outcome.Success(statuses(catalog.data, childId, myUserId))
    }

    /** Katalog keshda bo'lsa — darhol (tarmoqsiz); yo'q bo'lsa null. */
    fun cached(childId: String, myUserId: String): List<ProtectionPackStatus>? =
        packRepository.cachedCatalog()?.let { statuses(it, childId, myUserId) }

    private fun statuses(catalog: PackCatalog, childId: String, myUserId: String): List<ProtectionPackStatus> {
        val protectionPolicies = policyRepository.cachedPolicies(childId).filter { it.isProtection }
        return catalog.packs.map { pack ->
            val forThisPack = protectionPolicies.filter { it.packCode == pack.code }
            ProtectionPackStatus(
                pack = pack,
                myPolicy = forThisPack.firstOrNull { it.isMine(myUserId) },
                enabledByCoParent = forThisPack.any {
                    it.scope == PolicyType.PARENT_CHILD &&
                            it.actorUserId != myUserId &&
                            it.isActive
                },
                enabledByChild = forThisPack.any {
                    it.scope == PolicyType.STUDENT && it.isActive
                },
            )
        }
    }
}