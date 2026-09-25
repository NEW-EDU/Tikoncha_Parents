package uz.tikoncha_parent.presentation.policy.targets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.internal.BackHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bu_ilova_kategoriya_orqali_tanlangan
import tikoncha_parents.composeapp.generated.resources.ilova_qidirish
import tikoncha_parents.composeapp.generated.resources.ilovalar
import tikoncha_parents.composeapp.generated.resources.kategoriyalar
import tikoncha_parents.composeapp.generated.resources.preset_all_apps
import tikoncha_parents.composeapp.generated.resources.preset_counted
import tikoncha_parents.composeapp.generated.resources.preset_done
import tikoncha_parents.composeapp.generated.resources.preset_not_found
import tikoncha_parents.composeapp.generated.resources.preset_only_selected
import tikoncha_parents.composeapp.generated.resources.preset_what_closed
import tikoncha_parents.composeapp.generated.resources.sayt_qidirish
import tikoncha_parents.composeapp.generated.resources.saytlar
import tikoncha_parents.composeapp.generated.resources.search_normal
import tikoncha_parents.composeapp.generated.resources.targets_add_site
import tikoncha_parents.composeapp.generated.resources.targets_calls_not_counted
import tikoncha_parents.composeapp.generated.resources.targets_except_open
import tikoncha_parents.composeapp.generated.resources.targets_only_counted
import tikoncha_parents.composeapp.generated.resources.targets_only_selected_open
import tikoncha_parents.composeapp.generated.resources.targets_only_selected_open_sub
import tikoncha_parents.composeapp.generated.resources.targets_pick
import tikoncha_parents.composeapp.generated.resources.targets_pick_n
import tikoncha_parents.composeapp.generated.resources.targets_plus
import tikoncha_parents.composeapp.generated.resources.targets_rest_open
import tikoncha_parents.composeapp.generated.resources.targets_selected
import tikoncha_parents.composeapp.generated.resources.targets_selected_closed
import tikoncha_parents.composeapp.generated.resources.targets_selected_closed_sub
import uz.tikoncha_parent.domain.model.apps.AppCategory
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.policy.components.Chevron
import uz.tikoncha_parent.presentation.policy.components.GroupDivider
import uz.tikoncha_parent.presentation.policy.components.PolicyText
import uz.tikoncha_parent.presentation.policy.components.RadioRow
import uz.tikoncha_parent.presentation.policy.components.SectionLabel
import uz.tikoncha_parent.presentation.policy.components.SegmentedTabs
import uz.tikoncha_parent.presentation.policy.components.SettingGroup
import uz.tikoncha_parent.presentation.policy.components.SettingRow
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.NormalIconButtonSize
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

/**
 * Nishon tanlash muharriri — tayyor jadval / o'zim sozlayman ichida to'liq ekran (Student bilan bir xil).
 *
 *  · MODE — rejim va Ilovalar / Kategoriyalar / Saytlar hisoblari
 *  · PICKER — qidiruv, uch segment, ilovalar (tanlanganlar va ko'p ishlatilganlar tepada),
 *    kategoriya kartalari, saytlar + "Sayt qo'shish"
 *
 * Tizim "orqaga": qidiruv → tanlash → rejim → yopish.
 * [allowLocked] — "Faqat tanlanganlar ochiq" (ALLOW) bepul ota-onaga yopiq: Plus belgisi,
 * bosilsa [onLockedClick] (paywall).
 */
@OptIn(InternalVoyagerApi::class)
@Composable
fun TargetsEditor(
    state: TargetsEditorState,
    apps: List<InstalledApp>,
    flavor: TargetsFlavor,
    event: (TargetsEditorEvent) -> Unit,
    onDone: () -> Unit,
    onClose: () -> Unit,
    /** Oxirgi 7 kun: paket → daqiqa; ilovalar tartibi uchun. */
    appUsage: Map<String, Long> = emptyMap(),
    allowLocked: Boolean = false,
    onLockedClick: () -> Unit = {},
) {
    BackHandler(true) {
        if (state.searchOpen || state.page == TargetsPage.PICKER) event(TargetsEditorEvent.Back) else onClose()
    }
    when (state.page) {
        TargetsPage.MODE -> ModePage(
            state = state,
            flavor = flavor,
            event = event,
            onDone = onDone,
            onBack = onClose,
            allowLocked = allowLocked,
            onLockedClick = onLockedClick,
        )
        TargetsPage.PICKER -> PickerPage(state = state, apps = apps, appUsage = appUsage, event = event)
    }
}

// ═══════════════════════════════════════════
//  Rejim va tanlanganlar
// ═══════════════════════════════════════════

@Composable
private fun ModePage(
    state: TargetsEditorState,
    flavor: TargetsFlavor,
    event: (TargetsEditorEvent) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
    allowLocked: Boolean,
    onLockedClick: () -> Unit,
) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    val selected = state.mode == TargetsMode.SELECTED
    val kind = (flavor as? TargetsFlavor.Preset)?.kind
    // Custom: nishonlar ikkala rejimda ham tanlanadi
    val selectionEnabled = selected || flavor == TargetsFlavor.Custom

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg.page).then(systemBars.modifier)) {
        CustomHeader(
            title = stringResource(if (kind == PresetKind.LIMIT) Res.string.preset_counted else Res.string.preset_what_closed),
            showBackButton = true,
            onBackClick = onBack,
        )
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = ContainerPadding),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            SettingGroup {
                if (flavor == TargetsFlavor.Custom) {
                    RadioRow(
                        title = stringResource(Res.string.targets_selected_closed),
                        subtitle = stringResource(Res.string.targets_selected_closed_sub),
                        selected = selected,
                        onClick = { event(TargetsEditorEvent.ModeChanged(TargetsMode.SELECTED)) },
                    )
                    GroupDivider()
                    RadioRow(
                        title = stringResource(Res.string.targets_only_selected_open),
                        subtitle = stringResource(Res.string.targets_only_selected_open_sub),
                        selected = !selected,
                        onClick = {
                            if (allowLocked && selected) onLockedClick()
                            else event(TargetsEditorEvent.ModeChanged(TargetsMode.ALL))
                        },
                        trailingExtra = if (allowLocked) ({ PlusBadge() }) else null,
                    )
                } else {
                    RadioRow(
                        title = stringResource(Res.string.preset_all_apps),
                        subtitle = when (kind) {
                            PresetKind.LIMIT -> stringResource(Res.string.targets_calls_not_counted)
                            else -> stringResource(Res.string.targets_except_open)
                        },
                        selected = !selected,
                        onClick = { event(TargetsEditorEvent.ModeChanged(TargetsMode.ALL)) },
                    )
                    GroupDivider()
                    RadioRow(
                        title = stringResource(Res.string.preset_only_selected),
                        subtitle = stringResource(if (kind == PresetKind.LIMIT) Res.string.targets_only_counted else Res.string.targets_rest_open),
                        selected = selected,
                        onClick = { event(TargetsEditorEvent.ModeChanged(TargetsMode.SELECTED)) },
                    )
                }
            }

            Column(
                modifier = Modifier.alpha(if (selectionEnabled) 1f else 0.45f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SectionLabel(stringResource(Res.string.targets_selected))
                SettingGroup {
                    val rows: List<Triple<StringResource, Int, TargetsTab>> = listOf(
                        Triple(Res.string.ilovalar, state.appCount, TargetsTab.APPS),
                        Triple(Res.string.kategoriyalar, state.categories.size, TargetsTab.CATEGORIES),
                        Triple(Res.string.saytlar, state.sites.size, TargetsTab.SITES),
                    )
                    rows.forEachIndexed { i, (res, count, tab) ->
                        SettingRow(
                            title = stringResource(res),
                            value = count.toString(),
                            onClick = if (selectionEnabled) ({ event(TargetsEditorEvent.OpenPicker(tab)) }) else null,
                            trailing = { Chevron() },
                        )
                        if (i < rows.lastIndex) GroupDivider()
                    }
                }
            }
        }
        CustomButtonNew(
            text = stringResource(Res.string.preset_done),
            modifier = Modifier.fillMaxWidth().padding(horizontal = ContainerPadding, vertical = 12.dp),
            onClick = onDone,
        )
    }
}

/** Pullik imkoniyat belgisi. */
@Composable
private fun PlusBadge() {
    Text(
        text = stringResource(Res.string.targets_plus),
        style = PolicyText.chip,
        color = AppColors.text.inverse,
        modifier = Modifier
            .background(AppColors.bg.primary, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

// ═══════════════════════════════════════════
//  Tanlash
// ═══════════════════════════════════════════

/** Kategoriyalar tabidagi guruh: o'rnatilgan ilovalar + tanlangan, lekin qurilmada ilovasi yo'q turlar. */
private data class CategoryEntry(val id: String, val category: AppCategory, val apps: List<InstalledApp>)

@Composable
private fun PickerPage(
    state: TargetsEditorState,
    apps: List<InstalledApp>,
    appUsage: Map<String, Long>,
    event: (TargetsEditorEvent) -> Unit,
) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.secondary, navigationBarColor = AppColors.bg.secondary)
    val lang = remember { LanguagePrefs.loadOrDefault() }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var toastJob by remember { mutableStateOf<Job?>(null) }
    val coveredText = stringResource(Res.string.bu_ilova_kategoriya_orqali_tanlangan)
    val showCovered: () -> Unit = {
        if (toastJob?.isActive != true) {
            toastJob = scope.launch { snackbar.showSnackbar(coveredText, duration = SnackbarDuration.Short) }
        }
    }

    // Tartib faqat ro'yxat yuklanganda hisoblanadi — tanlaganda qatorlar sakramasin
    val sortedApps = remember(apps, appUsage) {
        val pkgs = state.packages
        val cats = state.categories
        apps.sortedWith(
            compareByDescending<InstalledApp> { it.packageName in pkgs }
                .thenByDescending { it.appCategory.id in cats }
                .thenByDescending { appUsage[it.packageName] ?: 0L }
                .thenBy { it.name.lowercase() }
        )
    }
    val categories = remember(apps) {
        val installed = groupApps(apps).filter { it.category != AppCategory.OTHER }
            .map { CategoryEntry(it.category.id, it.category, it.apps) }
        val present = installed.map { it.id }.toSet()
        // Standart shablondagi tur (masalan GAMES) bolada bo'lmasa ham ko'rinsin — olib tashlash uchun
        val missing = state.categories.filter { it !in present }.map { CategoryEntry(it, AppCategory.from(it), emptyList()) }
        installed + missing
    }
    val sites = remember(state.customSites) {
        val selected = state.sites
        val popular = PopularSites.ALL.toSet()
        (state.customSites + PopularSites.ALL + selected).distinct().sortedWith(
            compareByDescending<String> { it in selected }
                .thenByDescending { it in popular }
                .thenBy { it }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().then(systemBars.modifier).background(AppColors.bg.secondary)) {
            if (state.searchOpen) {
                PickerSearchHeader(
                    query = state.query,
                    placeholder = stringResource(if (state.tab == TargetsTab.SITES) Res.string.sayt_qidirish else Res.string.ilova_qidirish),
                    onQueryChange = { event(TargetsEditorEvent.QueryChanged(it)) },
                    onClose = { event(TargetsEditorEvent.SearchClosed) },
                )
            } else {
                CustomHeader(
                    showBackButton = true,
                    onBackClick = { event(TargetsEditorEvent.Back) },
                    title = stringResource(Res.string.targets_pick),
                    trailingIcon = {
                        IconButton(
                            modifier = Modifier.size(NormalIconButtonSize),
                            onClick = { event(TargetsEditorEvent.SearchOpened) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent, contentColor = AppColors.icon.secondary),
                        ) {
                            Icon(painter = painterResource(Res.drawable.search_normal), contentDescription = null, modifier = Modifier.size(24.dp))
                        }
                    },
                )
            }

            SegmentedTabs(
                labels = listOf(
                    stringResource(Res.string.ilovalar),
                    stringResource(Res.string.kategoriyalar),
                    stringResource(Res.string.saytlar),
                ),
                selectedIndex = state.tab.ordinal,
                onSelect = { event(TargetsEditorEvent.TabChanged(TargetsTab.entries[it])) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), modifier = Modifier.fillMaxSize()) {
                    when (state.tab) {
                        TargetsTab.APPS -> appsTab(state, sortedApps, event, showCovered)
                        TargetsTab.CATEGORIES -> categoriesTab(state, categories, lang, event)
                        TargetsTab.SITES -> sitesTab(state, sites, event)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.modal.primary, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CustomButtonNew(
                    text = if (state.selectedCount == 0) stringResource(Res.string.targets_pick)
                    else stringResource(Res.string.targets_pick_n, state.selectedCount),
                    onClick = { event(TargetsEditorEvent.PickerDone) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
    }

    state.siteDialog?.let { dialog ->
        SiteInputDialog(
            initial = dialog.original.orEmpty(),
            error = dialog.error,
            onConfirm = { event(TargetsEditorEvent.SiteSubmitted(it)) },
            onDismiss = { event(TargetsEditorEvent.SiteDialogDismissed) },
        )
    }
    if (state.siteSheet != null) {
        SiteActionsSheet(
            onEdit = { event(TargetsEditorEvent.EditSiteClicked) },
            onDelete = { event(TargetsEditorEvent.DeleteSiteClicked) },
            onDismiss = { event(TargetsEditorEvent.SiteSheetDismissed) },
        )
    }
}

/** Ilovalar: tekis ro'yxat; YouTube / Instagram ichida Shorts / Reels. */
private fun LazyListScope.appsTab(
    state: TargetsEditorState,
    sortedApps: List<InstalledApp>,
    event: (TargetsEditorEvent) -> Unit,
    showCovered: () -> Unit,
) {
    if (sortedApps.isEmpty()) {
        items(count = 12) { ShimmerAppRow(modifier = Modifier.padding(horizontal = 16.dp)) }
        return
    }
    val query = state.query
    val visible = if (query.isBlank()) sortedApps else sortedApps.filter { PolicyAppFeatures.matchesSearch(it, query) }
    if (visible.isEmpty()) {
        item { PickerEmpty(text = stringResource(Res.string.preset_not_found)) }
        return
    }
    items(items = visible, key = { it.packageName }) { app ->
        val features = PolicyAppFeatures.visibleFeaturesFor(app, query)
        val covered = state.isCovered(app.appCategory.id)
        // Qidiruvda funksiyasi mos kelgan ilova o'zi ochiladi
        val expanded = if (query.isNotBlank()) {
            features.isNotEmpty() && !app.name.contains(query, ignoreCase = true) || state.isExpanded(TargetsTab.APPS, app.packageName)
        } else {
            state.isExpanded(TargetsTab.APPS, app.packageName)
        }
        PickerAppWithFeatures(
            app = app,
            features = features,
            checked = app.packageName in state.packages,
            selectedFeatures = state.features,
            covered = covered,
            expanded = expanded,
            onToggleExpand = { event(TargetsEditorEvent.ToggleExpanded(TargetsTab.APPS, app.packageName)) },
            onAppToggle = { if (covered) showCovered() else event(TargetsEditorEvent.TogglePackage(app.packageName)) },
            onFeatureToggle = { f -> if (covered) showCovered() else event(TargetsEditorEvent.ToggleFeature(f.key)) },
        )
    }
}

/** Kategoriyalar: tur tanlanadi (yangi o'rnatilganlar ham tushadi); ichidagi ilovalar faqat ma'lumot. */
private fun LazyListScope.categoriesTab(
    state: TargetsEditorState,
    categories: List<CategoryEntry>,
    lang: LanguageType,
    event: (TargetsEditorEvent) -> Unit,
) {
    if (categories.isEmpty()) {
        items(count = 8) { ShimmerAppRow(modifier = Modifier.padding(horizontal = 16.dp)) }
        return
    }
    val query = state.query
    val visible = if (query.isBlank()) categories else categories.mapNotNull { c ->
        val localizedName = CategoryLocalizer.localize(c.category, lang).name
        val filtered = c.apps.filter { it.name.contains(query, ignoreCase = true) }
        when {
            filtered.isNotEmpty() -> c.copy(apps = filtered)
            localizedName.contains(query, ignoreCase = true) -> c
            else -> null
        }
    }
    if (visible.isEmpty()) {
        item { PickerEmpty(text = stringResource(Res.string.preset_not_found)) }
        return
    }
    items(items = visible, key = { "cat_" + it.id }) { c ->
        val localized = CategoryLocalizer.localize(c.category, lang)
        PickerCategoryCard(
            name = localized.name,
            emoji = localized.emoji,
            apps = c.apps,
            selected = c.id in state.categories,
            expanded = query.isNotBlank() || state.isExpanded(TargetsTab.CATEGORIES, c.id),
            onToggleExpand = { event(TargetsEditorEvent.ToggleExpanded(TargetsTab.CATEGORIES, c.id)) },
            onToggleSelect = { event(TargetsEditorEvent.ToggleCategory(c.id)) },
        )
    }
}

/** Saytlar: tanlanganlar, mashhurlar, o'zi qo'shganlari; uzoq bosish — tahrirlash / o'chirish. */
private fun LazyListScope.sitesTab(state: TargetsEditorState, sites: List<String>, event: (TargetsEditorEvent) -> Unit) {
    val query = state.query.trim()
    val visible = if (query.isBlank()) sites else sites.filter { it.contains(query, ignoreCase = true) }
    item(key = "site_add") {
        PickerAddRow(
            text = stringResource(Res.string.targets_add_site),
            onClick = { event(TargetsEditorEvent.AddSiteClicked) },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
    if (visible.isEmpty()) {
        item { PickerEmpty(text = stringResource(Res.string.preset_not_found)) }
        return
    }
    items(items = visible, key = { it }) { domain ->
        PickerSiteRow(
            domain = domain,
            checked = domain in state.sites,
            onToggle = { event(TargetsEditorEvent.ToggleSite(domain)) },
            onLongClick = { event(TargetsEditorEvent.SiteLongPressed(domain)) },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}
