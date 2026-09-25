package uz.tikoncha_parent.presentation.policy.targets

import uz.tikoncha_parent.domain.model.apps.AppCategory
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.presentation.policy.model.PresetKind

/**
 * Nishon tanlash muharriri (Student `TargetsEditorState` bilan bir xil) — sof holat va reducer.
 *
 *  MODE sahifasi: rejim + Ilovalar / Kategoriyalar / Saytlar qatorlari
 *  PICKER sahifasi: uch tab, qidiruv, kataklar, "Tanlash · N"
 *
 * Qoralamaga faqat "Tayyor" bosilganda yoziladi — [applyTo].
 */
enum class TargetsMode { ALL, SELECTED }
enum class TargetsTab { APPS, CATEGORIES, SITES }
enum class TargetsPage { MODE, PICKER }
enum class SiteError { INVALID, DUPLICATE }

/** Sayt dialogi: `original == null` — yangi qo'shish, aks holda tahrirlash. */
data class SiteDialogState(val original: String? = null, val error: SiteError? = null)

data class TargetsEditorState(
    val page: TargetsPage = TargetsPage.MODE,
    val mode: TargetsMode = TargetsMode.ALL,
    val packages: Set<String> = emptySet(),
    val categories: Set<String> = emptySet(),
    val sites: Set<String> = emptySet(),
    /** Ilova ichidagi funksiyalar: YOUTUBE_SHORTS, INSTAGRAM_REELS. */
    val features: Set<String> = emptySet(),
    val tab: TargetsTab = TargetsTab.APPS,
    val searchOpen: Boolean = false,
    val query: String = "",
    /** Ochilgan qatorlar, kalit `"${tab}:${id}"` — tablar bir-biriga xalaqit bermaydi. */
    val expanded: Set<String> = emptySet(),
    /** Ota-ona qo'lda qo'shgan saytlar (uzoq bosib tahrirlanadi). */
    val customSites: Set<String> = emptySet(),
    val siteDialog: SiteDialogState? = null,
    /** Uzoq bosilgan sayt — Tahrirlash / O'chirish varag'i. */
    val siteSheet: String? = null,
) {
    /** Funksiyalar ham ilova soniga kiradi. */
    val appCount: Int get() = packages.size + features.size
    val selectedCount: Int get() = appCount + categories.size + sites.size
    fun isExpanded(tab: TargetsTab, id: String): Boolean = expandedKey(tab, id) in expanded
    /** Ilova o'z kategoriyasi orqali tanlangan — alohida tanlab bo'lmaydi. */
    fun isCovered(categoryId: String): Boolean = categoryId in categories

    companion object {
        fun expandedKey(tab: TargetsTab, id: String) = "${tab.name}:$id"
    }
}

sealed interface TargetsEditorEvent {
    data class ModeChanged(val mode: TargetsMode) : TargetsEditorEvent
    data class OpenPicker(val tab: TargetsTab) : TargetsEditorEvent
    data class TabChanged(val tab: TargetsTab) : TargetsEditorEvent
    data object SearchOpened : TargetsEditorEvent
    data object SearchClosed : TargetsEditorEvent
    data class QueryChanged(val query: String) : TargetsEditorEvent
    data class ToggleExpanded(val tab: TargetsTab, val id: String) : TargetsEditorEvent
    data class TogglePackage(val packageName: String) : TargetsEditorEvent
    data class ToggleFeature(val key: String) : TargetsEditorEvent
    /** Kategoriyalar tabi: turning o'zini tanlash (yangi o'rnatilganlar ham tushadi). */
    data class ToggleCategory(val categoryId: String) : TargetsEditorEvent
    data class ToggleSite(val domain: String) : TargetsEditorEvent
    data object AddSiteClicked : TargetsEditorEvent
    data class SiteLongPressed(val domain: String) : TargetsEditorEvent
    data object SiteSheetDismissed : TargetsEditorEvent
    data object EditSiteClicked : TargetsEditorEvent
    data object DeleteSiteClicked : TargetsEditorEvent
    data object SiteDialogDismissed : TargetsEditorEvent
    /** Dialogdagi matn — domen ("youtube.com") yoki kalit so'z ("kino"). */
    data class SiteSubmitted(val raw: String) : TargetsEditorEvent
    /** PICKER → MODE (tanlov saqlanadi). */
    data object PickerDone : TargetsEditorEvent
    /** Orqaga: qidiruv → PICKER → MODE; MODE'da VM muharrirni yopadi. */
    data object Back : TargetsEditorEvent
}

fun TargetsEditorState.reduce(e: TargetsEditorEvent): TargetsEditorState = when (e) {
    is TargetsEditorEvent.ModeChanged -> copy(mode = e.mode)
    is TargetsEditorEvent.OpenPicker -> copy(page = TargetsPage.PICKER, tab = e.tab, query = "", searchOpen = false)
    is TargetsEditorEvent.TabChanged -> copy(tab = e.tab)
    TargetsEditorEvent.SearchOpened -> copy(searchOpen = true)
    TargetsEditorEvent.SearchClosed -> copy(searchOpen = false, query = "")
    is TargetsEditorEvent.QueryChanged -> copy(query = e.query)
    is TargetsEditorEvent.ToggleExpanded -> {
        val key = TargetsEditorState.expandedKey(e.tab, e.id)
        copy(expanded = if (key in expanded) expanded - key else expanded + key)
    }
    is TargetsEditorEvent.TogglePackage ->
        copy(packages = if (e.packageName in packages) packages - e.packageName else packages + e.packageName)
    is TargetsEditorEvent.ToggleFeature ->
        copy(features = if (e.key in features) features - e.key else features + e.key)
    is TargetsEditorEvent.ToggleCategory ->
        copy(categories = if (e.categoryId in categories) categories - e.categoryId else categories + e.categoryId)
    is TargetsEditorEvent.ToggleSite ->
        copy(sites = if (e.domain in sites) sites - e.domain else sites + e.domain)
    TargetsEditorEvent.AddSiteClicked -> copy(siteDialog = SiteDialogState())
    is TargetsEditorEvent.SiteLongPressed -> if (e.domain in customSites) copy(siteSheet = e.domain) else this
    TargetsEditorEvent.SiteSheetDismissed -> copy(siteSheet = null)
    TargetsEditorEvent.EditSiteClicked -> copy(siteDialog = SiteDialogState(original = siteSheet), siteSheet = null)
    TargetsEditorEvent.DeleteSiteClicked -> siteSheet?.let { d ->
        copy(siteSheet = null, customSites = customSites - d, sites = sites - d)
    } ?: this
    TargetsEditorEvent.SiteDialogDismissed -> copy(siteDialog = null)
    is TargetsEditorEvent.SiteSubmitted -> submitSite(e.raw)
    TargetsEditorEvent.PickerDone -> copy(page = TargetsPage.MODE, query = "", searchOpen = false)
    TargetsEditorEvent.Back -> when {
        searchOpen -> copy(searchOpen = false, query = "")
        page == TargetsPage.PICKER -> copy(page = TargetsPage.MODE, query = "")
        else -> this
    }
}

/**
 * Sayt qo'shish / tahrirlash:
 *  · domen: kamida bitta nuqta ("youtube.com"), sxema, "www." va oxirgi "/" olib tashlanadi
 *  · kalit so'z: nuqtasiz, bo'shliqsiz, kamida 3 belgi ("kino") — URL matnida qidiriladi
 * Hammasi kichik harfda saqlanadi (bola ilovasidagi veb-filtr shunday solishtiradi).
 */
private fun TargetsEditorState.submitSite(raw: String): TargetsEditorState {
    val dialog = siteDialog ?: SiteDialogState()
    val site = normalizeSite(raw)
    if (!isValidSite(site)) return copy(siteDialog = dialog.copy(error = SiteError.INVALID))
    val original = dialog.original
    if (site != original && (site in customSites || site in sites)) return copy(siteDialog = dialog.copy(error = SiteError.DUPLICATE))
    return if (original == null) {
        copy(siteDialog = null, sites = sites + site, customSites = customSites + site, query = "")
    } else {
        copy(
            siteDialog = null,
            customSites = customSites - original + site,
            sites = if (original in sites) sites - original + site else sites,
        )
    }
}

fun normalizeSite(raw: String): String {
    var d = raw.trim().lowercase().removePrefix("https://").removePrefix("http://").removeSuffix("/")
    if (d.endsWith(".")) d = d.dropLast(1)
    return d.removePrefix("www.")
}

private val DOMAIN_REGEX = Regex("^([\\w-]+\\.)+[\\w-]{2,}(/.*)?$")
private const val MIN_KEYWORD_LENGTH = 3

fun isValidSite(site: String): Boolean {
    if (site.isBlank()) return false
    if (DOMAIN_REGEX.matches(site)) return true
    return site.length >= MIN_KEYWORD_LENGTH && '.' !in site && ' ' !in site &&
        site.all { it.isLetterOrDigit() || it == '-' || it == '_' }
}

/** Sayt tanlash ekranidagi standart ro'yxat; ota-ona qo'shganlari ustiga qo'shiladi. */
object PopularSites {
    val ALL: List<String> = listOf(
        "youtube.com", "instagram.com", "tiktok.com", "facebook.com", "vk.com", "likee.video",
        "snapchat.com", "twitch.tv", "discord.com", "netflix.com", "reddit.com", "x.com",
    )
}

// ═══════════════════════════════════════════
//  Qoralama ↔ muharrir
// ═══════════════════════════════════════════

/**
 * Muharrir kimga xizmat qiladi.
 *  · [Preset] — tayyor jadval: "Barcha ilovalar" (`"*"`) | "Faqat tanlanganlar". Parent'da uchala
 *    tayyor jadval ham DENY: istisnolar `excludePackages` da (ALLOW bepul ota-onaga pullik).
 *  · [Custom] — o'zim sozlayman: "Tanlanganlar yopiladi" (DENY) | "Faqat tanlanganlar ochiq" (ALLOW, Plus).
 */
sealed interface TargetsFlavor {
    data class Preset(val kind: PresetKind) : TargetsFlavor
    data object Custom : TargetsFlavor
}

/** Qoralamadan boshlang'ich holat. */
fun targetsEditorFrom(draft: PolicyDraft, flavor: TargetsFlavor, popularSites: Collection<String> = PopularSites.ALL): TargetsEditorState {
    val t = draft.targets
    val popular = popularSites.toSet()
    val selection = TargetsEditorState(
        mode = TargetsMode.SELECTED,
        packages = t.packages.filter { it != PolicyTargets.ALL_APPS }.toSet(),
        categories = t.categories.toSet(),
        sites = t.sites.toSet(),
        features = t.features.toSet(),
        customSites = t.sites.filter { it !in popular }.toSet(),
    )
    return when (flavor) {
        TargetsFlavor.Custom -> if (draft.action == PolicyAction.ALLOW) selection.copy(mode = TargetsMode.ALL) else selection
        is TargetsFlavor.Preset -> if (t.isAllApps) TargetsEditorState(mode = TargetsMode.ALL) else selection
    }
}

/**
 * "Tayyor": tanlovni qoralamaga yozadi. iOS tanlovlari va paketlar (`iosSelectionIds`, `packs`)
 * bu muharrirda tahrirlanmaydi — saqlanib qoladi. `excludePackages` faqat `"*"` yoki kategoriya
 * bilan ma'noli (server shunday) — boshqa hollarda tozalanadi.
 */
fun TargetsEditorState.applyTo(draft: PolicyDraft, flavor: TargetsFlavor): PolicyDraft {
    val t = draft.targets
    val allowList = flavor == TargetsFlavor.Custom && mode == TargetsMode.ALL
    return when {
        flavor is TargetsFlavor.Preset && mode == TargetsMode.ALL -> draft.copy(
            action = PolicyAction.DENY,
            targets = t.copy(packages = listOf(PolicyTargets.ALL_APPS), categories = emptyList(), sites = emptyList(), features = emptyList()),
        )
        else -> draft.copy(
            action = if (allowList) PolicyAction.ALLOW else PolicyAction.DENY,
            targets = t.copy(
                packages = packages.sorted(),
                categories = categories.sorted(),
                sites = sites.sorted(),
                features = features.sorted(),
                excludePackages = if (allowList || categories.isEmpty()) emptyList() else t.excludePackages,
            ),
        )
    }
}

// ═══════════════════════════════════════════
//  Ilovalar guruhi
// ═══════════════════════════════════════════

/** Server inglizcha nom yuboradi ("Art & Design", "Action") — enum kodiga. */
val InstalledApp.appCategory: AppCategory get() = AppCategory.from(category?.let(CategoryLocalizer::toCode))

data class AppGroup(val category: AppCategory, val apps: List<InstalledApp>)

/** Ilovalar → kategoriya guruhlari; "Boshqa" oxirida. */
fun groupApps(apps: List<InstalledApp>): List<AppGroup> =
    apps.groupBy { it.appCategory }
        .map { (category, list) -> AppGroup(category, list.sortedBy { it.name.lowercase() }) }
        .sortedWith(compareBy({ it.category == AppCategory.OTHER }, { it.category.ordinal }))
