package uz.tikoncha_parent.presentation.policy.mapper

import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.policy.TimeCondition
import uz.tikoncha_parent.domain.model.policy.UsageLimit

/**
 * "Nima bo'ladi?" izohi uchun xom ma'lumot — jadval sozlamalaridan olinadi, matnga UI'da
 * (`PolicyInfo.sentences()`) aylanadi. Student `PolicyInfoMapper` bilan bir xil:
 *  · DENY + `"*"`          — barcha ilovalar; `excludePackages` ochiq (limitda — hisoblanmaydi)
 *  · DENY + tanlov         — faqat tanlanganlar
 *  · ALLOW + ilova/kat.    — qolgan hamma ilova yopiladi (oq ro'yxat)
 *  · ALLOW + faqat sayt    — qolgan hamma sayt yopiladi
 *  · ALLOW bo'sh           — qurilma uni oq ro'yxat deb bilmaydi: hech narsa yopilmaydi
 */
data class PolicyInfo(
    val what: InfoWhat,
    val closed: TargetCounts = TargetCounts(),
    val open: TargetCounts = TargetCounts(),
    val allowList: Boolean = false,
    val openNotCounted: Boolean = false,
    val time: TimeCondition? = null,
    val location: LocationRule? = null,
    val limit: UsageLimit? = null,
    val mentionProtected: Boolean = false,
    val disabled: Boolean = false,
)

enum class InfoWhat { NOTHING, ALL_APPS, ALL_SITES, ALL_APPS_AND_SITES, SELECTED }

data class TargetCounts(val apps: Int = 0, val categories: Int = 0, val sites: Int = 0) {
    val isEmpty: Boolean get() = apps == 0 && categories == 0 && sites == 0
}

/** @param enabled null — hali saqlanmagan (yaratish ekrani): "o'chiq" deyilmaydi. */
fun PolicyDraft.toInfo(enabled: Boolean? = null): PolicyInfo {
    val t = targets
    val apps = t.packages.count { it != PolicyTargets.ALL_APPS } + t.features.size
    val base = PolicyInfo(
        what = InfoWhat.NOTHING,
        time = conditions.time,
        location = conditions.location,
        limit = limits.usage,
        disabled = enabled == false,
    )
    return when (action) {
        PolicyAction.ALLOW -> {
            val coversApps = apps > 0 || t.categories.isNotEmpty()
            val coversSites = t.sites.isNotEmpty()
            base.copy(
                what = when {
                    coversApps && coversSites -> InfoWhat.ALL_APPS_AND_SITES
                    coversApps -> InfoWhat.ALL_APPS
                    coversSites -> InfoWhat.ALL_SITES
                    else -> InfoWhat.NOTHING
                },
                open = TargetCounts(apps, t.categories.size, t.sites.size),
                allowList = true,
                mentionProtected = coversApps,
            )
        }
        PolicyAction.DENY -> when {
            t.isAllApps -> base.copy(
                what = InfoWhat.ALL_APPS,
                closed = TargetCounts(sites = t.sites.size),
                open = TargetCounts(apps = t.excludePackages.size),
                openNotCounted = limits.usage != null,
                mentionProtected = true,
            )
            apps == 0 && t.categories.isEmpty() && t.sites.isEmpty() -> base
            else -> base.copy(
                what = InfoWhat.SELECTED,
                closed = TargetCounts(apps, t.categories.size, t.sites.size),
                open = TargetCounts(apps = t.excludePackages.size),
                openNotCounted = limits.usage != null,
            )
        }
    }
}
