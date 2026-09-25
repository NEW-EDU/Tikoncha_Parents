package uz.tikoncha_parent.presentation.policy.app_site_selection

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.mapper.toAppSelectionUi
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.apps.AppCategory
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.presentation.ui_state.ResponseState

class AppWebViewModel(
    private val policyRepository: PolicyRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(AppWebState())
    val state = _state.asStateFlow()

    fun onEvent(event: AppWebEvent) {
        when (event) {
            is AppWebEvent.LoadApps -> loadChildApps(
                userId = event.userId,
                selectedPkgs = event.selectedPkgs,
                selectedCategories = event.selectedCategories,
            )

            is AppWebEvent.OnTabSelected ->
                _state.update { it.copy(tabIndex = event.tabIndex, searchQuery = "") }

            is AppWebEvent.OnSearchQueryChanged ->
                _state.update { it.copy(searchQuery = event.query) }

            is AppWebEvent.SetServerSites -> mergeServerSites(event.sites)



            is AppWebEvent.SetSiteInput ->
                _state.update { it.copy(siteInput = event.url, siteInputError = null) }

            AppWebEvent.ShowAddSiteDialog ->
                _state.update { it.copy(showAddSiteDialog = true, siteInput = "", siteInputError = null) }

            AppWebEvent.DismissAddSiteDialog ->
                _state.update { it.copy(showAddSiteDialog = false, siteInput = "", siteInputError = null) }

            AppWebEvent.ConfirmAddSite -> {
                if (_state.value.editingSite != null) confirmEditSite()
                else confirmAddSite()
            }

            is AppWebEvent.ShowSiteEditSheet ->
                _state.update {
                    it.copy(showSiteEditSheet = true, editingSite = event.site, siteInput = event.site.url)
                }

            is AppWebEvent.EditSite ->
                _state.update {
                    it.copy(
                        showSiteEditSheet = false,
                        editingSite = event.site,
                        siteInput = event.site.url,
                        showAddSiteDialog = true,
                        siteInputError = null,
                    )
                }

            AppWebEvent.DismissSiteEditSheet ->
                _state.update {
                    it.copy(showAddSiteDialog = false, siteInput = "", siteInputError = null, editingSite = null)
                }

            is AppWebEvent.RemoveSite ->
                _state.update { s ->
                    s.copy(
                        sites = s.sites.filter { it.url != event.url },
                        showSiteEditSheet = false,
                        editingSite = null,
                    )
                }
        }
    }



    private fun loadChildApps(
        userId: String,
        selectedPkgs: Set<String>,
        selectedCategories: Set<String>,
    ) {
        screenModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(loadAppsResponseState = ResponseState.Loading) }

            when (val res = policyRepository.childApps(userId)) {
                is Outcome.Failure -> _state.update {
                    it.copy(loadAppsResponseState = ResponseState.Error(failure = res))
                }
                is Outcome.Success -> {
                    val allApps = res.data.map { it.toAppSelectionUi() }

                    // ════════════════════════════════════════════════════
                    // SORT: selected/covered first → usage → order → name
                    // ViewModel ichida bir marta, screen qayta sortlamaydi
                    // ════════════════════════════════════════════════════

                    val sortedApps = allApps.sortedWith(
                        compareByDescending<AppSelectionUi> { app ->
                            selectedPkgs.any { it.equals(app.packageName, ignoreCase = true) }
                        }
                            // 2-DARAJA: Kategoriya orqali coveredlar
                            .thenByDescending { app ->
                                app.category != null && selectedCategories.any {
                                    it.equals(app.category, ignoreCase = true)
                                }
                            }
                            // 3-DARAJA: Usage bo'yicha
                            .thenByDescending { it.usageMinutes }
                            .thenByDescending { it.order }
                            .thenBy { it.name.lowercase() }
                    )

                    val language = LanguagePrefs.loadOrDefault()
                    val grouped = sortedApps.groupBy { AppCategory.from(it.category) }
                    val hasCategoryData = grouped.keys.any { it != AppCategory.OTHER }

                    val uiGroups = grouped
                        .filterKeys { it != AppCategory.OTHER }
                        .map { (category, apps) ->
                            val localized = CategoryLocalizer.localize(category, language)
                            CategoryGroupUi(
                                id = category.id,
                                displayName = localized.name,
                                emoji = localized.emoji,
                                apps = apps.sortedByDescending { it.order },
                            )
                        }
                        .sortedByDescending { group -> group.apps.sumOf { it.order } }

                    _state.update {
                        it.copy(
                            loadAppsResponseState = ResponseState.Success(),
                            apps = sortedApps,
                            categoryGroups = uiGroups,
                            hasCategoryData = hasCategoryData,
                        )
                    }
                }
            }
        }
    }

    private fun mergeServerSites(serverSites: List<String>) {
        val defaultUrls = DEFAULT_SITES.map { it.url }.toSet()
        val customs = serverSites
            .filter { it !in defaultUrls }
            .map { SiteUi(url = it, isDefault = false) }
        _state.update { it.copy(sites = DEFAULT_SITES + customs) }
    }

    private fun confirmAddSite() {
        val input = normalizeInput(_state.value.siteInput.trim())
        if (input.isBlank()) return

        if (_state.value.sites.any { it.url.equals(input, ignoreCase = true) }) {
            _state.update { it.copy(siteInputError = SiteError.ALREADY_EXISTS) }
            return
        }
        _state.update { s ->
            s.copy(
                sites = s.sites + SiteUi(url = input, isDefault = false),
                showAddSiteDialog = false,
                siteInput = "",
                siteInputError = null,
            )
        }
    }

    private fun confirmEditSite() {
        val old = _state.value.editingSite ?: return
        val newInput = normalizeInput(_state.value.siteInput.trim())
        if (newInput.isBlank()) return

        if (newInput != old.url && _state.value.sites.any { it.url.equals(newInput, ignoreCase = true) }) {
            _state.update { it.copy(siteInputError = SiteError.ALREADY_EXISTS) }
            return
        }
        _state.update { s ->
            s.copy(
                sites = s.sites.map { if (it.url == old.url) it.copy(url = newInput) else it },
                showSiteEditSheet = false,
                showAddSiteDialog = false,
                editingSite = null,
                siteInput = "",
            )
        }
    }

    private fun normalizeInput(input: String): String =
        input.trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .removeSuffix("/")
}