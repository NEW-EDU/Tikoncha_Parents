@file:OptIn(kotlin.time.ExperimentalTime::class)

package uz.tikoncha_parent.presentation.assistant

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.ai.AssistantEvent
import uz.tikoncha_parent.domain.model.ai.AssistantTurn
import uz.tikoncha_parent.domain.model.ai.ChildReport
import uz.tikoncha_parent.domain.model.ai.DraftApplyError
import uz.tikoncha_parent.domain.model.ai.LocationCard
import uz.tikoncha_parent.domain.model.ai.NavTarget
import uz.tikoncha_parent.domain.model.ai.ProtectionCard
import uz.tikoncha_parent.domain.model.ai.ReportPeriod
import uz.tikoncha_parent.domain.model.ai.RuleDraft
import uz.tikoncha_parent.domain.model.ai.SafetyAlert
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.protection.missingRequiredPermissionCount
import uz.tikoncha_parent.domain.model.protection.pendingRequestCount
import uz.tikoncha_parent.domain.model.todo.TodoFilter
import uz.tikoncha_parent.domain.model.todo.TodosQuery
import uz.tikoncha_parent.domain.repository.AiRepository
import uz.tikoncha_parent.domain.repository.ChildRepository
import uz.tikoncha_parent.domain.repository.ProtectionRepository
import uz.tikoncha_parent.domain.use_case.app_usage.TodayUsageUseCase
import uz.tikoncha_parent.domain.use_case.policy.ObservePoliciesUseCase
import uz.tikoncha_parent.domain.use_case.todo.GetTodosUseCase
import uz.tikoncha_parent.presentation.assistant.voice.AnswerPlayer
import uz.tikoncha_parent.presentation.assistant.voice.VoiceRecorder
import uz.tikoncha_parent.presentation.assistant.voice.createAnswerPlayer
import uz.tikoncha_parent.presentation.assistant.voice.createVoiceRecorder
import uz.tikoncha_parent.presentation.assistant.voice.writeTempAudio
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import kotlin.time.Clock
import kotlin.time.TimeSource

sealed interface AssistantItem {
    val id: Long

    /** [text] — pufakda körinadigani (ovozli savol ilova yozuvida), [sent] — yordamçiga ketgan asl matn. */
    data class Parent(override val id: Long, val text: String, val sent: String = text) : AssistantItem
    data class Reply(override val id: Long, val text: String) : AssistantItem
    data class Draft(override val id: Long, val draft: RuleDraft, val status: DraftStatus) : AssistantItem
    data class Location(override val id: Long, val card: LocationCard) : AssistantItem
    data class Protection(override val id: Long, val card: ProtectionCard) : AssistantItem

    /** Yordamçi ekranni tavsiya qildi — "Oçiş: Xarita" tugmasi. */
    data class Nav(override val id: Long, val target: NavTarget) : AssistantItem

    /** Javob kelmadi. [code]: no_coins · rate_limited · network · server … ; [text] — server matni bo'lsa. */
    data class Problem(override val id: Long, val code: String, val text: String?) : AssistantItem
}

enum class DraftStatus { PENDING, APPLYING, APPLIED, DISCARDED }

/** Ovozli savol: tinglayapti (yozyapti) → tuşunyapti (serverda matnga aylanyapti) → oddiy savol. */
enum class VoicePhase { IDLE, LISTENING, UNDERSTANDING }

/**
 * "Bugun qisqaça" kartalari — Bosh sahifa manbalari (birinçi kadrda keşdagi raqamlar, keyin serverdan).
 * null — hali noma'lum.
 */
data class AssistantGlance(
    val childId: String? = null,
    /** Oxirgi joylaşuv vaqti (epoch ms). */
    val locationAtMs: Long? = null,
    val usage: HourMinute? = null,
    /** DEFAULT · GUJANAK · STRICT. */
    val protectionMode: String? = null,
    val permissionsOff: Int? = null,
    val pendingRequests: Int? = null,
    val activeTasks: Int? = null,
    val rulesOn: Int? = null,
)

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
    /** Qoidani yoqish xatosi (serverning matni va maşina kodi). */
    val draftError: DraftApplyError? = null,
    /** Farzand Bilimdonga yuborgan xavotirli xabarlar (oxirgi 7 kun) — hisobot ostida. */
    val alerts: List<SafetyAlert> = emptyList(),
    val mascot: MascotState = MascotState.WAVE,
    val voice: VoicePhase = VoicePhase.IDLE,
    /** Ovoz tanilmadi — "Eşitmadim, yana bir marta ayting". */
    val voiceMissed: Boolean = false,
    /** Mikrofon ruxsati berilmadi — sozlamalarga yöllaydigan oyna. */
    val micDenied: Boolean = false,
    val glance: AssistantGlance = AssistantGlance(),
    /** Javob faqat bitta "Oçiş" tugmasidan iborat — ekran özi oçiladi (UI bir marta bajaradi). */
    val autoOpen: AssistantItem.Nav? = null,
    /** Server javobni ovozga aylantira oladi (yöq bölsa karnay tugmalari yaşiriladi). */
    val ttsAvailable: Boolean = true,
    /** Hozir ovoz bilan öqilayotgan (yoki yuklanayotgan) javob. */
    val speakingId: Long? = null,
    /** Yangi javoblar avtomatik ovoz bilan öqilsin (sozlama, standart — öçiq). */
    val autoSpeak: Boolean = false,
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

    /** Mikrofon ruxsati bor — yozişni boşlaş. */
    data object StartListening : AssistantEventUi
    /** Mikrofon tugmasi yana bosildi — yozuvni yuboriş. */
    data object StopListening : AssistantEventUi
    /** Ekrandan çiqildi — yozuv taşlanadi. */
    data object CancelListening : AssistantEventUi
    data object MicDenied : AssistantEventUi
    data object DismissMicDenied : AssistantEventUi
    data object AutoOpenHandled : AssistantEventUi
    /** Javobni ovoz bilan öqiş / töxtatiş. */
    data class Speak(val itemId: Long) : AssistantEventUi
    data object StopSpeaking : AssistantEventUi
    data object ToggleAutoSpeak : AssistantEventUi
}

class AssistantViewModel(
    private val ai: AiRepository,
    private val childRepository: ChildRepository,
    private val protectionRepository: ProtectionRepository,
    private val getTodosUseCase: GetTodosUseCase,
    private val todayUsageUseCase: TodayUsageUseCase,
    private val observePoliciesUseCase: ObservePoliciesUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow(
        AssistantState(
            children = AppSettings.children,
            child = AppSettings.selectedChild ?: AppSettings.children.firstOrNull(),
            ttsAvailable = !ai.ttsUnsupported,
            autoSpeak = prefs.getBoolean(KEY_AUTO_SPEAK, false),
        )
    )
    val state = _state.asStateFlow()

    private val recorder: VoiceRecorder by lazy { createVoiceRecorder() }
    private val player: AnswerPlayer by lazy { createAnswerPlayer() }

    /** Mikrofon sathi 0..1 — faqat mikrofon tugmasi va halqalar kuzatadi (butun ekran qayta çizilmasin). */
    val voiceLevel: StateFlow<Float> get() = recorder.level

    private var nextId = 1L
    /** Joriy javob boşlangan joy — kartalar javob matnidan keyin qöyiladi. */
    private var turnStart = 0
    private var askJob: Job? = null
    private var reportJob: Job? = null
    private var mascotJob: Job? = null
    private var listenJob: Job? = null
    private var sttJob: Job? = null
    private var speechJob: Job? = null
    private val glanceJobs = mutableListOf<Job>()

    init {
        loadReport()
        loadAlerts()
        loadGlance()
        // Tab oçilganda tipratikan qöl silkitadi, keyin tinç turadi
        mascot(MascotState.WAVE, holdMs = 2_200)
    }

    override fun onDispose() {
        listenJob?.cancel()
        sttJob?.cancel()
        recorder.cancel()
        speechJob?.cancel()
        player.stop()
    }

    /** "Bilimdon AI" suhbatidan oçilgan bölsa — öqilmagan belgisi tozalansin. */
    fun markChatRead(chatId: String) {
        screenModelScope.launch { ai.markChatRead(chatId) }
    }

    private fun loadAlerts() {
        val child = _state.value.child ?: return
        screenModelScope.launch {
            val res = ai.alerts(child.userId)
            if (res is Outcome.Success) {
                val weekAgo = Clock.System.now() - kotlin.time.Duration.parse("7d")
                val recent = res.data.filter { a ->
                    runCatching { kotlin.time.Instant.parse(a.createdAt) > weekAgo }.getOrDefault(true)
                }
                _state.update { if (it.child?.userId == child.userId) it.copy(alerts = recent) else it }
            }
        }
    }

    fun onEvent(event: AssistantEventUi) {
        when (event) {
            is AssistantEventUi.OnText -> _state.update { it.copy(text = event.text, voiceMissed = false) }
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
                _state.update { it.copy(child = event.child, report = null, alerts = emptyList()) }
                loadReport()
                loadAlerts()
                loadGlance()
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
                stopSpeaking()
                _state.update { it.copy(items = emptyList(), streaming = false, streamingText = "", status = "") }
                mascot(MascotState.WAVE, holdMs = 1_800)
                loadGlance()
            }
            is AssistantEventUi.ShowOnMap -> _state.value.children.firstOrNull { it.userId == event.childId }?.let {
                AppSettings.selectedChild = it
                AppSettings.selectedChildId = it.userId
            }
            AssistantEventUi.StartListening -> startListening()
            AssistantEventUi.StopListening -> {
                listenJob?.cancel()
                listenJob = null
                finishListening()
            }
            AssistantEventUi.CancelListening -> {
                cancelListening()
                stopSpeaking()
            }
            AssistantEventUi.MicDenied -> _state.update { it.copy(micDenied = true) }
            AssistantEventUi.DismissMicDenied -> _state.update { it.copy(micDenied = false) }
            AssistantEventUi.AutoOpenHandled -> _state.update { it.copy(autoOpen = null) }
            is AssistantEventUi.Speak -> {
                if (_state.value.speakingId == event.itemId) stopSpeaking() else speak(event.itemId)
            }
            AssistantEventUi.StopSpeaking -> stopSpeaking()
            AssistantEventUi.ToggleAutoSpeak -> {
                val on = !_state.value.autoSpeak
                prefs[KEY_AUTO_SPEAK] = on
                _state.update { it.copy(autoSpeak = on) }
                if (!on) stopSpeaking()
            }
        }
    }

    // ── tipratikan ──

    /** Holatni qöyadi; [holdMs] berilsa, şuncha vaqtdan keyin (holat özgarmagan bölsa) IDLE ga qaytadi. */
    private fun mascot(m: MascotState, holdMs: Long? = null) {
        mascotJob?.cancel()
        mascotJob = null
        _state.update { it.copy(mascot = m) }
        if (holdMs != null) {
            mascotJob = screenModelScope.launch {
                delay(holdMs)
                _state.update { if (it.mascot == m) it.copy(mascot = MascotState.IDLE) else it }
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
                    r.data.coins?.let { c -> AppSettings.lastCoins = c }
                    // Javob kelguncha boshqa farzand yoki davr tanlangan bo'lsa — eski javobni ko'rsatmaymiz
                    if (it.child?.userId == child.userId && it.period == period) {
                        it.copy(report = r.data, reportLoading = false, coins = r.data.coins ?: it.coins)
                    } else it
                }
                is Outcome.Failure -> _state.update { it.copy(reportLoading = false, reportFailed = true) }
            }
        }
    }

    // ── "Bugun qisqaça" ──

    /** Bosh sahifa bilan bir xil manbalar: keşdagi oxirgi raqamlar darhol, keyin har biri serverdan. */
    private fun loadGlance() {
        glanceJobs.forEach { it.cancel() }
        glanceJobs.clear()
        val childId = _state.value.child?.userId
        if (childId.isNullOrBlank()) {
            _state.update { it.copy(glance = AssistantGlance()) }
            return
        }
        val snap = AppSettings.homeSnapshots[childId]
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        _state.update {
            it.copy(
                glance = AssistantGlance(
                    childId = childId,
                    // Keşda bugungi raqam bölsa; "usageShownDay" töla — bugun hali böş
                    usage = snap?.takeIf { s -> s.usageDay == today }
                        ?.let { s -> if (s.usageShownDay.isBlank()) s.todayUsage else HourMinute(0, 0) },
                    permissionsOff = snap?.takeIf { s -> s.protectionLoaded }?.protectionPermissionOffCount,
                    pendingRequests = snap?.takeIf { s -> s.protectionLoaded }?.protectionPendingRequestCount,
                    activeTasks = snap?.activeTaskCount,
                )
            )
        }

        fun update(block: (AssistantGlance) -> AssistantGlance) =
            _state.update { s -> if (s.glance.childId == childId) s.copy(glance = block(s.glance)) else s }

        glanceJobs += screenModelScope.launch {
            val r = childRepository.childrenLocation()
            if (r is Outcome.Success) {
                val at = r.data.firstOrNull { it.childId == childId && it.latitude != null && it.longitude != null }
                    ?.updatedAt?.let { DateTimeUtil.toMillis(it) }?.takeIf { it > 0 }
                update { it.copy(locationAtMs = at) }
            }
        }
        glanceJobs += screenModelScope.launch {
            val r = todayUsageUseCase(childId)
            if (r is Outcome.Success) update { it.copy(usage = if (r.data.isToday) r.data.total else HourMinute(0, 0)) }
        }
        glanceJobs += screenModelScope.launch {
            val r = protectionRepository.protectionStatus(childId)
            if (r is Outcome.Success) update {
                it.copy(
                    protectionMode = r.data.currentMode,
                    permissionsOff = r.data.missingRequiredPermissionCount(),
                    pendingRequests = r.data.pendingRequestCount(),
                )
            }
        }
        glanceJobs += screenModelScope.launch {
            val r = getTodosUseCase(TodosQuery(targetUserId = childId, filter = TodoFilter(), limit = 500, offset = 0))
            if (r is Outcome.Success) update { it.copy(activeTasks = r.data.items.count { todo -> !todo.isCompleted }) }
        }
        glanceJobs += screenModelScope.launch {
            // Keşni kuzatadi — yordamçi qoida yoqsa / öçirsa raqam özi yangilanadi (Bosh sahifadagi hisob)
            observePoliciesUseCase(childId).collect { policies ->
                val now = Clock.System.now()
                val count = policies.count {
                    it.scope == PolicyType.PARENT_CHILD && it.isStandard && it.deletedAt == null &&
                        it.isActive && it.expiresAt?.let { e -> e <= now } != true
                }
                update { it.copy(rulesOn = count) }
            }
        }
    }

    // ── ovozli savol ──

    private fun appLang(): String = LanguagePrefs.loadOrDefault().languageCode

    private fun startListening() {
        val s = _state.value
        if (s.voice != VoicePhase.IDLE || s.streaming) return
        stopSpeaking()
        if (!recorder.start()) {
            missed()
            return
        }
        _state.update { it.copy(voice = VoicePhase.LISTENING, voiceMissed = false, micDenied = false) }
        mascot(MascotState.LISTENING)
        listenJob = screenModelScope.launch {
            watchSilence()
            listenJob = null
            finishListening()
        }
    }

    /**
     * Nutq eşitilgaç ~1,6 s jimlik bölsa yoki 60 s ötsa — tugaydi. Hech narsa eşitilmasa 10 s'da töxtab, baribir
     * serverga yuboriladi (sath ölçovi xato qilishi mumkin — oxirgi söz serverniki).
     */
    private suspend fun watchSilence() {
        val started = TimeSource.Monotonic.markNow()
        var floor = 1f
        var loudMs = 0L
        var quietMs = 0L
        var heard = false
        while (true) {
            delay(TICK_MS)
            val elapsed = started.elapsedNow().inWholeMilliseconds
            val level = recorder.level.value
            if (elapsed <= CALIBRATE_MS) {
                floor = minOf(floor, level)
                continue
            }
            // Xona şovqini (birinçi 0,3 s) hisobga olinadi, lekin juda baland şovqinda ham gap ajralsin
            val base = floor.coerceAtMost(0.35f)
            when {
                level > maxOf(base + 0.18f, 0.35f) -> {
                    loudMs += TICK_MS
                    quietMs = 0
                    if (loudMs >= MIN_SPEECH_MS) heard = true
                }
                level < maxOf(base + 0.08f, 0.22f) -> quietMs += TICK_MS
                else -> quietMs = 0
            }
            if (heard && quietMs >= SILENCE_MS) return
            if (!heard && elapsed >= NO_SPEECH_MS) return
            if (elapsed >= MAX_LISTEN_MS) return
        }
    }

    private fun finishListening() {
        if (_state.value.voice != VoicePhase.LISTENING) return
        val audio = recorder.stop()
        if (audio == null || audio.durationMs < MIN_AUDIO_MS) {
            missed()
            return
        }
        _state.update { it.copy(voice = VoicePhase.UNDERSTANDING) }
        mascot(MascotState.THINKING)
        val lang = appLang()
        sttJob = screenModelScope.launch {
            val transcript = (ai.transcribe(audio.bytes, audio.fileName, audio.mimeType, lang) as? Outcome.Success)?.data
            val text = transcript?.text?.trim().orEmpty()
            if (text.isEmpty()) {
                missed()
            } else {
                _state.update { it.copy(voice = VoicePhase.IDLE) }
                // Pufakda — ilova yozuvida (kirill / 2026 lotini), yordamçiga — serverning asl matni
                send(text, display = UzbekScript.forDisplay(text, transcript?.language, lang), byVoice = true)
            }
        }
    }

    private fun cancelListening() {
        listenJob?.cancel()
        listenJob = null
        sttJob?.cancel()
        sttJob = null
        recorder.cancel()
        if (_state.value.voice != VoicePhase.IDLE) {
            _state.update { it.copy(voice = VoicePhase.IDLE) }
            if (!_state.value.streaming) mascot(MascotState.IDLE)
        }
    }

    private fun missed() {
        _state.update { it.copy(voice = VoicePhase.IDLE, voiceMissed = true) }
        mascot(MascotState.CONFUSED, holdMs = 2_500)
    }

    // ── javobni ovoz bilan öqiş ──

    private fun speak(itemId: Long) {
        val reply = _state.value.items.firstOrNull { it.id == itemId } as? AssistantItem.Reply ?: return
        stopSpeaking()
        _state.update { it.copy(speakingId = itemId) }
        val lang = appLang()
        speechJob = screenModelScope.launch {
            when (val r = ai.speech(plainText(reply.text).take(TTS_MAX_CHARS), lang)) {
                is Outcome.Success -> {
                    val path = writeTempAudio(r.data, "ai_reply_$itemId.mp3")
                    val started = path != null && player.play(path) {
                        _state.update { s -> if (s.speakingId == itemId) s.copy(speakingId = null) else s }
                        if (_state.value.mascot == MascotState.TALKING && !_state.value.streaming) mascot(MascotState.IDLE)
                    }
                    if (started) {
                        if (_state.value.voice == VoicePhase.IDLE && !_state.value.streaming) mascot(MascotState.TALKING)
                    } else {
                        _state.update { s -> if (s.speakingId == itemId) s.copy(speakingId = null) else s }
                    }
                }
                is Outcome.Failure -> _state.update {
                    it.copy(speakingId = if (it.speakingId == itemId) null else it.speakingId, ttsAvailable = !ai.ttsUnsupported)
                }
            }
        }
    }

    private fun stopSpeaking() {
        speechJob?.cancel()
        speechJob = null
        player.stop()
        if (_state.value.speakingId != null) _state.update { it.copy(speakingId = null) }
        if (_state.value.mascot == MascotState.TALKING && !_state.value.streaming) mascot(MascotState.IDLE)
    }

    /** Ovoz uçun: **qalin** belgilari va ro'yxat nuqtalari olib taşlanadi. */
    private fun plainText(text: String): String =
        text.replace("**", "").lines().joinToString("\n") { it.replace(Regex("^\\s*[*-]\\s+"), "") }.trim()

    // ── suhbat ──

    /** Ota-ona ovoz bilan sörasa — javob ham ovoz bilan öqiladi (yozib sörasa, faqat "avtomatik öqiş" yoqilganda). */
    private var askedByVoice = false

    private fun send(raw: String, display: String? = null, byVoice: Boolean = false) {
        val text = raw.trim()
        if (text.isEmpty() || _state.value.streaming) return
        askedByVoice = byVoice
        if (_state.value.voice != VoicePhase.IDLE) cancelListening()
        stopSpeaking()
        val parent = AssistantItem.Parent(nextId++, display?.trim()?.ifEmpty { null } ?: text, sent = text)
        _state.update {
            it.copy(
                items = it.items + parent,
                text = "",
                streaming = true,
                streamingText = "",
                status = "",
                voiceMissed = false,
            )
        }
        turnStart = _state.value.items.size
        mascot(MascotState.THINKING)

        val turns = historyTurns(_state.value.items)
        askJob?.cancel()
        askJob = screenModelScope.launch {
            ai.ask(turns, _state.value.child?.userId).collect { ev ->
                when (ev) {
                    is AssistantEvent.Status -> {
                        _state.update { it.copy(status = ev.text) }
                        if (_state.value.mascot != MascotState.THINKING) mascot(MascotState.THINKING)
                    }
                    is AssistantEvent.Token -> {
                        _state.update { it.copy(streamingText = it.streamingText + ev.text, status = "") }
                        if (_state.value.mascot != MascotState.TALKING) mascot(MascotState.TALKING)
                    }
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
                    is AssistantEvent.Nav -> _state.update {
                        it.copy(items = it.items + AssistantItem.Nav(nextId++, ev.target))
                    }
                    is AssistantEvent.Done -> finish(coins = ev.coins)
                    is AssistantEvent.Failed -> fail(ev.code, ev.text)
                }
            }
        }
    }

    private fun finish(coins: Int?, stoppedByUser: Boolean = false) {
        // Tangalar ekrani shu sonni darhol ko'rsatadi (keyin serverdan yangilaydi)
        coins?.let { AppSettings.lastCoins = it }
        var replyId: Long? = null
        var answered = false
        _state.update {
            val reply = it.streamingText.trim()
            // Matn — tepada, şu javobdagi kartalar (tasdiqlaş, xarita, himoya) — uning ostida, bosh barmoqqa yaqin
            val start = turnStart.coerceIn(0, it.items.size)
            val before = it.items.take(start)
            val cards = it.items.drop(start)
            val replyItem = if (reply.isNotEmpty()) AssistantItem.Reply(nextId++, reply) else null
            replyId = replyItem?.id
            answered = replyItem != null || cards.isNotEmpty()
            // Javob faqat "Oçiş: …" tugmasi (yoki u va bitta qisqa jumla) — ekran özi oçiladi
            val onlyNav = cards.singleOrNull() as? AssistantItem.Nav
            val autoOpen = if (!stoppedByUser && onlyNav != null && isOneLine(reply)) onlyNav else null
            it.copy(
                items = if (replyItem != null) before + replyItem + cards else it.items,
                streaming = false,
                streamingText = "",
                status = "",
                coins = coins ?: it.coins,
                autoOpen = autoOpen,
            )
        }
        val id = replyId
        if (!stoppedByUser && id != null && (_state.value.autoSpeak || askedByVoice) && _state.value.ttsAvailable && _state.value.autoOpen == null) {
            speak(id)
        } else {
            mascot(if (answered && !stoppedByUser) MascotState.DONE else MascotState.IDLE, holdMs = if (answered) 2_000 else null)
        }
    }

    /**
     * Suhbat konteksti. Kartalar ham kiradi — yordamçi nima qilganini (va ota-ona kartani tasdiqlaganini yoki
     * bekor qilganini) bilsin: aks holda faqat karta bilan javob berilgan savol "javobsiz" körinib, keyingi
     * savolda shu vazifa / qoida qaytadan taklif qilinardi (bekor qilingani ham). Yonma-yon kelgan yordamçi
     * qismlari bitta navbatga qöşiladi.
     */
    private fun historyTurns(items: List<AssistantItem>): List<AssistantTurn> {
        val raw = items.mapNotNull {
            when (it) {
                is AssistantItem.Parent -> AssistantTurn(true, it.sent)
                is AssistantItem.Reply -> AssistantTurn(false, it.text)
                is AssistantItem.Draft -> {
                    val what = (listOf(it.draft.title) + it.draft.lines).filter { l -> l.isNotBlank() }.joinToString("; ")
                    val outcome = when (it.status) {
                        DraftStatus.PENDING -> "not confirmed yet"
                        DraftStatus.APPLYING, DraftStatus.APPLIED -> "the parent confirmed it, it is done"
                        DraftStatus.DISCARDED -> "the parent cancelled it, do not offer it again unless asked"
                    }
                    AssistantTurn(false, "(Confirmation card for ${it.draft.childName}: $what — $outcome.)")
                }
                is AssistantItem.Location -> AssistantTurn(false, "(Showed ${it.card.childName}'s location on a map card.)")
                is AssistantItem.Protection -> AssistantTurn(false, "(Showed ${it.card.childName}'s protection status card.)")
                is AssistantItem.Nav -> AssistantTurn(false, "(Offered a button to open the ${it.target.screen.key} screen.)")
                is AssistantItem.Problem -> AssistantTurn(false, "(No answer: error ${it.code}.)")
            }
        }
        return raw.fold(mutableListOf()) { acc, t ->
            val prev = acc.lastOrNull()
            if (prev != null && !prev.fromParent && !t.fromParent) acc[acc.lastIndex] = prev.copy(text = prev.text + "\n" + t.text)
            else acc += t
            acc
        }
    }

    /** Server: "nav"dan keyin javob köpi bilan bir jumla. */
    private fun isOneLine(reply: String): Boolean =
        reply.isEmpty() || (reply.lines().count { it.isNotBlank() } <= 1 && reply.length <= ONE_LINE_MAX)

    private fun fail(code: String, text: String?) {
        _state.update {
            it.copy(
                items = it.items + AssistantItem.Problem(nextId++, code, text),
                streaming = false,
                streamingText = "",
                status = "",
            )
        }
        mascot(MascotState.CONFUSED, holdMs = 3_000)
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
        send(question.sent, display = question.text)
    }

    private fun stop() {
        if (!_state.value.streaming) return
        askJob?.cancel()
        finish(coins = null, stoppedByUser = true)
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
            when (val error = ai.applyDraft(item.draft.draftId, coin)) {
                null -> {
                    setDraft(itemId, DraftStatus.APPLIED)
                    val s = _state.value
                    if (!s.streaming && s.voice == VoicePhase.IDLE && s.speakingId == null) mascot(MascotState.DONE, holdMs = 2_000)
                    // Vazifa / sörov / himoya raqamlari özgargan bölişi mumkin
                    loadGlance()
                }
                else -> {
                    setDraft(itemId, DraftStatus.PENDING)
                    _state.update { it.copy(draftError = error) }
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

    private companion object {
        val prefs: Settings by lazy { Settings() }
        const val KEY_AUTO_SPEAK = "ai_auto_speak"

        const val TICK_MS = 60L
        const val CALIBRATE_MS = 300L
        const val MIN_SPEECH_MS = 180L
        const val SILENCE_MS = 1_600L
        const val NO_SPEECH_MS = 10_000L
        const val MAX_LISTEN_MS = 60_000L
        const val MIN_AUDIO_MS = 400L

        const val TTS_MAX_CHARS = 3_000
        const val ONE_LINE_MAX = 160
    }
}
