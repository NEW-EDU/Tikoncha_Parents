package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.URLBuilder
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.remote.model.live.LiveInFrame
import uz.tikoncha_parent.data.remote.model.live.LiveOutFrame
import uz.tikoncha_parent.platform.Logger

/**
 * Live signalizatsiya soketi (ota-ona tomoni) — SDP/ICE + boshqaruv ramkalarini uzatadi.
 * Media (P2P WebRTC) va joylashuv (P2P data-kanal) bu yerdan O'TMAYDI. ChatSocketService
 * bilan bir xil naqsh: qayta ulanish + heartbeat (HttpClient'da pingInterval ham bor).
 */
class LiveSignalingService(
    private val client: HttpClient,
) {
    private val TAG = "LiveSignaling"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var session: DefaultClientWebSocketSession? = null
    private var runJob: Job? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private val baseWs = "wss://${TikonchaClient.BASE_URL}/live/ws"

    private companion object {
        /** Server: token yaroqsiz — ulanişni qabul qilib, şu kod bilan yopadi. */
        const val CLOSE_AUTH_FAILED: Short = 4401
    }

    private val _incoming = MutableSharedFlow<LiveInFrame>(extraBufferCapacity = 64)
    val incoming: SharedFlow<LiveInFrame> = _incoming.asSharedFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    fun connect() {
        if (runJob?.isActive == true) return
        // Server tokenni ulanişdan KEYIN tekşiradi: noto'g'ri bölsa qabul qilib, 4401 bilan yopadi. Ilgari bu
        // "muvaffaqiyatli ulanish" deb hisoblanib (attempts = 0) darhol qayta ulanardi — soatiga ~4000 urinish
        // (2026-11-17/18). Endi: token yöq — ulanmaymiz; 4401 — bir marta yangilaymiz, yana 4401 — töxtaymiz;
        // urinişlar soni faqat haqiqiy ramka kelgaç nolga tushadi; har qayta ulaniş — kutib (1, 2, 4 … 30 s).
        runJob = scope.launch {
            var attempts = 0
            var refreshed = false
            while (isActive) {
                val token = AppSettings.accessToken
                if (token.isBlank()) {
                    Logger.d(TAG, "no access token — not connecting")
                    break
                }
                val url = URLBuilder(baseWs).apply { parameters.append("token", token) }.buildString()
                var gotFrame = false
                var closeCode: Short? = null
                try {
                    client.webSocket(url) {
                        session = this
                        _connected.value = true
                        for (frame in incoming) {
                            gotFrame = true
                            val text = (frame as? Frame.Text)?.readText() ?: continue
                            runCatching { json.decodeFromString<LiveInFrame>(text) }
                                .getOrNull()?.let { _incoming.tryEmit(it) }
                        }
                        closeCode = runCatching { closeReason.await()?.code }.getOrNull()
                    }
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Throwable) {
                    Logger.e(TAG, "ws error", e)
                } finally {
                    _connected.value = false
                    session = null
                }
                if (gotFrame) {
                    attempts = 0
                    refreshed = false
                }
                if (closeCode == CLOSE_AUTH_FAILED) {
                    if (refreshed) {
                        Logger.d(TAG, "auth failed after token refresh — stopping")
                        break
                    }
                    refreshed = true
                    if (refreshAccessToken(client) !is RefreshResult.Success) {
                        Logger.d(TAG, "auth failed, token refresh failed — stopping")
                        break
                    }
                }
                attempts++
                delay((1000L shl (attempts - 1).coerceAtMost(5)).coerceAtMost(30_000L))
            }
            _connected.value = false
        }
    }

    suspend fun send(frame: LiveOutFrame): Boolean {
        val s = session ?: return false
        val text = json.encodeToString(frame)
        return runCatching { s.send(Frame.Text(text)) }.isSuccess
    }

    fun disconnect() {
        runJob?.cancel()
        runJob = null
        session = null
        _connected.value = false
    }
}
