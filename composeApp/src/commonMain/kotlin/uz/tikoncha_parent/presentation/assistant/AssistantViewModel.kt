package uz.tikoncha_parent.presentation.assistant

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.ai.AssistantEvent
import uz.tikoncha_parent.domain.model.ai.AssistantTurn
import uz.tikoncha_parent.domain.model.ai.ChildReport
import uz.tikoncha_parent.domain.model.ai.LocationCard
import uz.tikoncha_parent.domain.model.ai.ProtectionCard
import uz.tikoncha_parent.domain.model.ai.ReportPeriod
import uz.tikoncha_parent.domain.model.ai.RuleDraft
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.AiRepository

sealed interface AssistantItem {
    val id: Long

    data class Parent(override val id: Long, val text: String) : AssistantItem
    data class Reply(override val id: Long, val text: String) : AssistantItem
    data class Draft(override val id: Long, val draft: RuleDraft, val status: DraftStatus) : AssistantItem
    data class Location(override val id: Long, val card: LocationCard) : AssistantItem
    data class Protection(override val id: Long, val card: ProtectionCard) : AssistantItem

    /** Javob kelmadi. [code]: no_coins · rate_limited · network · server … ; [text] — server matni bo'lsa. */
    data class Problem(override val id: Long, val code: String, val text: String?) : AssistantItem
}

enum class DraftStatus { PENDING, APPLYING, APPLIED, DISCARDED }

data class AssistantState(
    val children: List<UserInfo> = emptyList(),
    val child: UserInfo? = null,
    val period: ReportPeriod = ReportPeriod.TODAY,
    val report: ChildReport? = null,
    val reportLoading: Boolean = false,
    val reportFailed: Boolean = false,
    val items: List<AssistantItem> = emptyList(),
    val streaming: Boolean = false,
    val streamingText: String = "",
    val status: String = "",
    val text: String = "",
    val coins: Int? = null,
    /** Eslatma yuborilgan kartalar (id) — tugma "Yuborildi" ga aylanadi. */
    val reminded: Set<Long> = emptySet(),
    /** Qoidani yoqish xatosi (serverning tarjima qilingan matni). */
    val draftError: Outcome.Failure? = null,
)

sealed interface AssistantEventUi {
    data class OnText(val text: String) : AssistantEventUi
    data object Send : AssistantEventUi
    data class Ask(val text: String) : AssistantEventUi
    data object Stop : AssistantEventUi
    data class SelectPeriod(val period: ReportPeriod) : AssistantEventUi
    data class SelectChild(val child: UserInfo) : AssistantEventUi
    data object RetryReport : AssistantEventUi
    data class Apply(val itemId: Long, val coin: Int? = null) : AssistantEventUi
    data class Discard(val itemId: Long) : AssistantEventUi
    data object DismissDraftError : AssistantEventUi
    /** Javob kelmagan savolni qayta yuboriş (muammo kartasi). */
    data class Retry(val itemId: Long) : AssistantEventUi
    /** Suhbatni tozalaş — hisobot qoladi. */
    data object NewChat : AssistantEventUi
    /** Xarita kartasi bosildi — xarita şu farzand bilan oçilsin. */
    data class ShowOnMap(val childId: String) : AssistantEventUi
    /** Farzandga eslatma: GPS yoki ruxsatlarni yoqiş. */
    data class Remind(val itemId: Long, val childId: String, val kind: String) : AssistantEventUi
}

class AssistantViewModel(private val ai: AiRepository) : ScreenModel {

    private val _state = MutableStateFlow(
        AssistantState(
            children = AppSettings.children,
            child = AppSettings.selectedChild ?: AppSettings.children.firstOrNull(),
        )
    )
    val state = _state.asStateFlow()

    private var nextId = 1L
    /** Joriy javob boşlangan joy — kartalar javob matnidan keyin qöyiladi. */
    private var turnStart = 0
    private var askJob: Job? = null
    private var reportJob: Job? = null

    init {
        loadReport()
    }

    fun onEvent(event: AssistantEventUi) {
        when (event) {
            is AssistantEventUi.OnText -> _state.update { it.copy(text = event.text) }
            AssistantEventUi.Send -> send(_state.value.text)
            is AssistantEventUi.Ask -> send(event.text)
            AssistantEventUi.Stop -> stop()
            is AssistantEventUi.SelectPeriod -> {
                if (event.period == _state.value.period) return
                _state.update { it.copy(period = event.period) }
                loadReport()
            }
            is AssistantEventUi.SelectChild -> {
                if (event.child.userId == _state.value.child?.userId) return
                AppSettings.selectedChild = event.child
                AppSettings.selectedChildId = event.child.userId
                _state.update { it.copy(child = event.child, report = null) }
                loadReport()
            }
            AssistantEventUi.RetryReport -> loadReport()
            is AssistantEventUi.Apply -> apply(event.itemId, event.coin)
            is AssistantEventUi.Discard -> discard(event.itemId)
            AssistantEventUi.DismissDraftError -> _state.update { it.copy(draftError = null) }
            is AssistantEventUi.Retry -> retry(event.itemId)
            is AssistantEventUi.Remind -> {
                if (event.itemId in _state.value.reminded) return
                _state.update { it.copy(reminded = it.reminded + event.itemId) }
                screenModelScope.launch {
                    if (ai.remind(event.childId, event.kind) is Outcome.Failure) {
                        _state.update { it.copy(reminded = it.reminded - event.itemId) }
                    }
                }
            }
            AssistantEventUi.NewChat -> {
                askJob?.cancel()
                _state.update { it.copy(items = emptyList(), streaming = false, streamingText = "", status = "") }
            }
            is AssistantEventUi.ShowOnMap -> _state.value.children.firstOrNull { it.userId == event.childId }?.let {
                AppSettings.selectedChild = it
                AppSettings.selectedChildId = it.userId
            }
        }
    }

    // ── hisobot ──

    private fun loadReport() {
        val child = _state.value.child ?: return
        val period = _state.value.period
        reportJob?.cancel()
        reportJob = screenModelScope.launch {
            _state.update { it.copy(reportLoading = true, reportFailed = false) }
            when (val r = ai.report(child.userId, period)) {
                is Outcome.Success -> _state.update {
                    // Javob kelguncha boshqa farzand yoki davr tanlangan bo'lsa — eski javobni ko'rsatmaymiz
                    if (it.child?.userId == child.userId && it.period == period) {
                        it.copy(report = r.data, reportLoading = false, coins = r.data.coins ?: it.coins)
                    } else it
                }
                is Outcome.Failure -> _state.update { it.copy(reportLoading = false, reportFailed = true) }
            }
        }
    }

    // ── suhbat ──

    private fun send(raw: String) {
        val text = raw.trim()
        if (text.isEmpty() || _state.value.streaming) return
        val parent = AssistantItem.Parent(nextId++, text)
        _state.update { it.copy(items = it.items + parent, text = "", streaming = true, streamingText = "", status = "") }
        turnStart = _state.value.items.size

        // Suhbat konteksti: ota-onaning va yordamchining matnlari (kartalar va xatolar kirmaydi)
        val turns = _state.value.items.mapNotNull {
            when (it) {
                is AssistantItem.Parent -> AssistantTurn(true, it.text)
                is AssistantItem.Reply -> AssistantTurn(false, it.text)
                else -> null
            }
        }
        askJob?.cancel()
        askJob = screenModelScope.launch {
            ai.ask(turns, _state.value.child?.userId).collect { ev ->
                when (ev) {
                    is AssistantEvent.Status -> _state.update { it.copy(status = ev.text) }
                    is AssistantEvent.Token -> _state.update { it.copy(streamingText = it.streamingText + ev.text, status = "") }
                    is AssistantEvent.Draft -> _state.update {
                        // Karta javob matnidan oldin chiqadi — matn karta haqida gapiradi
                        it.copy(items = it.items + AssistantItem.Draft(nextId++, ev.draft, DraftStatus.PENDING))
                    }
                    is AssistantEvent.Location -> _state.update {
                        it.copy(items = it.items + AssistantItem.Location(nextId++, ev.card))
                    }
                    is AssistantEvent.Protection -> _state.update {
                        it.copy(items = it.items + AssistantItem.Protection(nextId++, ev.card))
                    }
                    is AssistantEvent.Done -> finish(coins = ev.coins)
                    is AssistantEvent.Failed -> fail(ev.code, ev.text)
                }
            }
        }
    }

    private fun finish(coins: Int?) {
        _state.update {
            val reply = it.streamingText.trim()
            // Matn — tepada, şu javobdagi kartalar (tasdiqlaş, xarita, himoya) — uning ostida, bosh barmoqqa yaqin
            val start = turnStart.coerceIn(0, it.items.size)
            val before = it.items.take(start)
            val cards = it.items.drop(start)
            it.copy(
                items = if (reply.isNotEmpty()) before + AssistantItem.Reply(nextId++, reply) + cards else it.items,
                streaming = false,
                streamingText = "",
                status = "",
                coins = coins ?: it.coins,
            )
        }
    }

    private fun fail(code: String, text: String?) {
        _state.update {
            it.copy(
                items = it.items + AssistantItem.Problem(nextId++, code, text),
                streaming = false,
                streamingText = "",
                status = "",
            )
        }
    }

    /** Muammo kartasini olib, undan oldingi savolni qayta yuboradi. */
    private fun retry(problemId: Long) {
        if (_state.value.streaming) return
        val items = _state.value.items
        val idx = items.indexOfFirst { it.id == problemId }
        if (idx < 0) return
        val qIdx = items.subList(0, idx).indexOfLast { it is AssistantItem.Parent }
        val question = items.getOrNull(qIdx) as? AssistantItem.Parent ?: return
        // Muvaffaqiyatsiz javobning hammasi (savol, kartalar, xato) olib taşlanadi — savol qaytadan yuboriladi
        _state.update { s -> s.copy(items = s.items.take(qIdx) + s.items.drop(idx + 1)) }
        send(question.text)
    }

    private fun stop() {
        if (!_state.value.streaming) return
        askJob?.cancel()
        finish(coins = null)
    }

    // ── qoida kartalari ──

    private fun draftItem(itemId: Long) = _state.value.items.firstOrNull { it.id == itemId } as? AssistantItem.Draft

    private fun setDraft(itemId: Long, status: DraftStatus) = _state.update { s ->
        s.copy(items = s.items.map { if (it is AssistantItem.Draft && it.id == itemId) it.copy(status = status) else it })
    }

    private fun apply(itemId: Long, coin: Int?) {
        val item = draftItem(itemId) ?: return
        if (item.status != DraftStatus.PENDING) return
        setDraft(itemId, DraftStatus.APPLYING)
        screenModelScope.launch {
            when (val r = ai.applyDraft(item.draft.draftId, coin)) {
                is Outcome.Success -> setDraft(itemId, DraftStatus.APPLIED)
                is Outcome.Failure -> {
                    setDraft(itemId, DraftStatus.PENDING)
                    _state.update { it.copy(draftError = r) }
                }
            }
        }
    }

    private fun discard(itemId: Long) {
        val item = draftItem(itemId) ?: return
        if (item.status != DraftStatus.PENDING) return
        setDraft(itemId, DraftStatus.DISCARDED)
        screenModelScope.launch { ai.discardDraft(item.draft.draftId) }
    }
}
