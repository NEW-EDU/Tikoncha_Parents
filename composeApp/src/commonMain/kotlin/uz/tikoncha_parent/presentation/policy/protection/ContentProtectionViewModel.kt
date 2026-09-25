package uz.tikoncha_parent.presentation.policy.protection

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.use_case.policy.ChildPaidStatusUseCase
import uz.tikoncha_parent.domain.use_case.policy.GetProtectionPackStatusesUseCase
import uz.tikoncha_parent.domain.use_case.policy.RefreshPoliciesUseCase
import uz.tikoncha_parent.domain.use_case.policy.ToggleContentProtectionUseCase
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs

/**
 * Kontent himoya: yoqish — men yoqmagan hamma paket, o'chirish — faqat o'zim yoqqanlarim
 * (ro'yxatdagi karta bilan bir xil `ToggleContentProtectionUseCase`).
 */
class ContentProtectionViewModel(
    private val getStatuses: GetProtectionPackStatusesUseCase,
    private val refreshPolicies: RefreshPoliciesUseCase,
    private val toggleProtection: ToggleContentProtectionUseCase,
    private val paidStatus: ChildPaidStatusUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow(ContentProtectionState())
    val state = _state.asStateFlow()

    private val myUserId: String get() = AppSettings.userId
    private var started = false

    fun onEvent(e: ContentProtectionEvent) {
        when (e) {
            is ContentProtectionEvent.Init -> start(e.childId)
            is ContentProtectionEvent.Toggled -> toggle(e.enabled)
            is ContentProtectionEvent.PackClicked -> _state.update {
                it.copy(expanded = if (e.code in it.expanded) it.expanded - e.code else it.expanded + e.code)
            }
            ContentProtectionEvent.PayWallDismissed -> _state.update { it.copy(showPayWall = false) }
            ContentProtectionEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun start(childId: String) {
        if (started) return
        started = true
        _state.update { it.copy(childId = childId, paid = paidStatus(childId)) }
        screenModelScope.launch {
            load(refresh = false)                 // keshdan darhol
            load(refresh = true)                  // keyin serverdan
        }
        screenModelScope.launch {
            val paid = paidStatus.refresh(childId)
            _state.update { it.copy(paid = paid ?: it.paid) }
        }
    }

    /** Paket holati PROTECTION jadvallaridan yig'iladi; [refresh] — jadvallar keshini avval yangilash. */
    private suspend fun load(refresh: Boolean) {
        val childId = _state.value.childId
        if (refresh) {
            val r = refreshPolicies(childId, force = true)
            if (r is Outcome.Failure) {
                _state.update { it.copy(loaded = true, error = if (it.packs.isEmpty()) r else it.error) }
                return
            }
        }
        when (val res = getStatuses(childId, myUserId)) {
            is Outcome.Success -> _state.update { it.copy(loaded = true, packs = res.data.filter { s -> s.pack.isActive }) }
            is Outcome.Failure -> _state.update { it.copy(loaded = true, error = if (it.packs.isEmpty()) res else it.error) }
        }
    }

    private fun toggle(enabled: Boolean) {
        val s = _state.value
        if (s.busy || !s.isAvailable) return
        if (enabled && s.paid == false) {
            _state.update { it.copy(showPayWall = true) }
            return
        }
        val lang = LanguagePrefs.loadOrDefault().languageCode
        screenModelScope.launch {
            _state.update { it.copy(busy = true) }
            val result = toggleProtection(s.childId, myUserId, s.packs, enabled) { it.pack.title(lang) }
            if (result is Outcome.Failure) {
                _state.update {
                    if (result.cause is ErrorCause.PremiumRequired) it.copy(busy = false, showPayWall = true)
                    else it.copy(busy = false, error = result)
                }
                load(refresh = result.cause == ErrorCause.NotFound)
                return@launch
            }
            load(refresh = false)                 // create/patch keshni yangiladi
            _state.update { it.copy(busy = false) }
        }
    }
}
