package uz.tikoncha_parent.presentation.policy.protection_packs

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.SubscriptionType
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.ProtectionPackStatus
import uz.tikoncha_parent.domain.use_case.policy.GetProtectionPackStatusesUseCase
import uz.tikoncha_parent.domain.use_case.policy.RefreshPoliciesUseCase
import uz.tikoncha_parent.domain.use_case.policy.ToggleProtectionPackUseCase
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.presentation.ui_state.ResponseState

class ProtectionPacksViewModel(
    private val getStatuses: GetProtectionPackStatusesUseCase,
    private val togglePack: ToggleProtectionPackUseCase,
    private val refreshPolicies: RefreshPoliciesUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow(ProtectionPacksState())
    val state = _state.asStateFlow()

    private val _effect = Channel<ProtectionPacksEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val myUserId: String = AppSettings.userId
    private var loadJob: Job? = null

    init {
        val child = AppSettings.selectedChild
        _state.update {
            it.copy(
                selectedChild = child,
                childrenList = AppSettings.children,
                isPaid = isPaid(child?.userId),
            )
        }
    }

    fun onEvent(event: ProtectionPacksEvent) {
        when (event) {
            ProtectionPacksEvent.Load -> load(refresh = true)

            is ProtectionPacksEvent.OnChildSelected -> {
                if (event.child.userId == _state.value.selectedChild?.userId) return
                AppSettings.selectedChildId = event.child.userId
                AppSettings.selectedChild = event.child
                _state.update {
                    it.copy(
                        selectedChild = event.child,
                        packs = emptyList(),
                        isPaid = isPaid(event.child.userId),
                    )
                }
                load(refresh = true)
            }

            is ProtectionPacksEvent.Toggle -> toggle(event.code, event.enabled)

            ProtectionPacksEvent.DismissPremium ->
                _state.update { it.copy(premiumFailure = null) }
        }
    }

    /**
     * Paket holati PROTECTION jadvallaridan yig'iladi, ular esa jadvallar keshida.
     * [refresh] — keshni avval serverdan yangilash (ekran ochilganda va bola almashganda).
     */
    private fun load(refresh: Boolean) {
        val childId = _state.value.selectedChild?.userId
        if (childId.isNullOrBlank()) return

        loadJob?.cancel()
        loadJob = screenModelScope.launch {
            if (_state.value.packs.isEmpty()) {
                _state.update { it.copy(responseState = ResponseState.Loading) }
            }

            if (refresh) {
                val refreshed = refreshPolicies(childId)
                if (refreshed is Outcome.Failure) {
                    _state.update { it.copy(responseState = ResponseState.Error(failure = refreshed)) }
                    return@launch
                }
            }

            when (val res = getStatuses(childId, myUserId)) {
                is Outcome.Failure -> _state.update {
                    it.copy(responseState = ResponseState.Error(failure = res))
                }

                is Outcome.Success -> {
                    val lang = LanguagePrefs.loadOrDefault().languageCode
                    val busy = _state.value.packs.filter { it.inProgress }.map { it.code }.toSet()
                    _state.update {
                        it.copy(
                            responseState = ResponseState.Success(),
                            packs = res.data
                                .filter { status -> status.pack.isActive }
                                .map { status -> status.toUi(lang, status.pack.code in busy) },
                        )
                    }
                }
            }
        }
    }

    private fun toggle(code: String, enabled: Boolean) {
        val s = _state.value
        val childId = s.selectedChild?.userId
        if (childId.isNullOrBlank()) return
        val pack = s.packs.firstOrNull { it.code == code } ?: return
        if (pack.inProgress) return

        screenModelScope.launch {
            setInProgress(code, true)
            val res = togglePack(childId, myUserId, code, enabled, pack.title)
            setInProgress(code, false)

            when {
                // create/patch keshni yangiladi — tarmoqsiz qayta yig'amiz.
                res is Outcome.Success -> load(refresh = false)

                res is Outcome.Failure && res.cause is ErrorCause.PremiumRequired ->
                    _state.update { it.copy(premiumFailure = res) }

                // Jadval boshqa joyda o'chirilgan — keshni yangilab qayta chizamiz.
                res is Outcome.Failure && res.cause == ErrorCause.NotFound -> load(refresh = true)

                res is Outcome.Failure -> _effect.trySend(ProtectionPacksEffect.ShowError(res))
            }
        }
    }

    private fun setInProgress(code: String, busy: Boolean) {
        _state.update { st ->
            st.copy(packs = st.packs.map { if (it.code == code) it.copy(inProgress = busy) else it })
        }
    }

    private fun isPaid(childId: String?): Boolean =
        AppSettings.subscriptionLimitList
            .firstOrNull { it.childId == childId }
            ?.subscriptionType
            ?.let { it != SubscriptionType.FREE }
            ?: false
}

private fun ProtectionPackStatus.toUi(lang: String, inProgress: Boolean) = ProtectionPackUi(
    code = pack.code,
    title = pack.title(lang),
    description = pack.desc(lang),
    packageCount = pack.packageCount,
    siteCount = pack.siteCount,
    enabledByMe = enabledByMe,
    enabledByCoParent = enabledByCoParent,
    enabledByChild = enabledByChild,
    inProgress = inProgress,
)