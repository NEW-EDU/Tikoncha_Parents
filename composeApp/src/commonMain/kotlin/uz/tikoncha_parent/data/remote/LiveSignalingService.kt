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

    private val _incoming = MutableSharedFlow<LiveInFrame>(extraBufferCapacity = 64)
    val incoming: SharedFlow<LiveInFrame> = _incoming.asSharedFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    fun connect() {
        if (runJob?.isActive == true) return
        runJob = scope.launch {
            var attempts = 0
            while (isActive) {
                val token = AppSettings.accessToken
                val url = URLBuilder(baseWs).apply { parameters.append("token", token) }.buildString()
                try {
                    client.webSocket(url) {
                        session = this
                        attempts = 0
                        _connected.value = true
                        for (frame in incoming) {
                            val text = (frame as? Frame.Text)?.readText() ?: continue
                            runCatching { json.decodeFromString<LiveInFrame>(text) }
                                .getOrNull()?.let { _incoming.tryEmit(it) }
                        }
                    }
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Throwable) {
                    Logger.e(TAG, "ws error", e)
                    attempts++
                    delay((attempts * 1000L).coerceAtMost(10_000L))
                } finally {
                    _connected.value = false
                    session = null
                }
            }
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
