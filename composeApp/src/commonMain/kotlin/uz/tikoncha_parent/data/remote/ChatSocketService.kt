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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.remote.model.ChatWsEvent
import uz.tikoncha_parent.data.remote.model.WSEditMessage
import uz.tikoncha_parent.data.remote.model.WSError
import uz.tikoncha_parent.data.remote.model.WSMessageCreated
import uz.tikoncha_parent.data.remote.model.WSMessageUpdated
import uz.tikoncha_parent.data.remote.model.WSReadMessage
import uz.tikoncha_parent.data.remote.model.WSReadUpdate
import uz.tikoncha_parent.data.remote.model.WSRequest
import uz.tikoncha_parent.data.remote.model.WSResponse
import uz.tikoncha_parent.data.remote.model.WSSendMessage
import uz.tikoncha_parent.platform.Logger
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalAtomicApi::class)
class ChatSocketService(
    private val client: HttpClient
) {

    private val TAG = "ChatSocketService"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var session: DefaultClientWebSocketSession? = null
    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null

    private val _events = MutableSharedFlow<ChatWsEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ChatWsEvent> = _events.asSharedFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private val connecting = AtomicBoolean(false)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }
    private val baseWs = "wss://${TikonchaClient.BASE_URL}/chat/ws"

    fun connect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            var attempts = 0
            while (isActive) {
                val token = AppSettings.accessToken
                Logger.d(TAG, "connect: 1")

                val url = URLBuilder(baseWs).apply {
                    parameters.append(
                        "token",
                        token
                    ) // "Bearer xxx" bo‘lsa ham to‘g‘ri encode qiladi
                }.buildString()

                // Bir vaqtning o'zida parallel connect bo‘lishini oldini olamiz
                if (!connecting.compareAndSet(expectedValue = false, newValue = true)) {
                    delay(300)
                    continue
                }

                try {
                    Logger.d(TAG, "connect: token=$token")
                    client.webSocket(url) {
                        session = this
                        attempts = 0
                        _connected.value = true

                        startHeartbeat()
                        listenLoop()
                    }
                } catch (ce: CancellationException) {
                    Logger.e(TAG, "connect: cancelled", ce)
                    throw ce               // ← cancel normal chiqish
                } catch (e: Throwable) {
                    Logger.e(TAG, "connect error", e)
                    attempts++
                    val base = (attempts * 1000L).coerceAtMost(15_000L)
                    val jitter = (0..400).random().toLong()
                    delay(base + jitter)

                } finally {
                    Logger.e(TAG, "connect: finaly")
                    stopHeartbeat()
                    _connected.value = false
                    session = null
                    connecting.store(false)
                }

            }
        }
    }

    fun disconnect() {
        stopHeartbeat()
        reconnectJob?.cancel()
        reconnectJob = null
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(25_000)
                runCatching { sendPing() }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private suspend fun listenLoop() {
        for (frame in session?.incoming ?: return) {
            val text = (frame as? Frame.Text)?.readText() ?: continue
            Logger.d(TAG, "WS-IN: $text")
            runCatching { handleIncoming(text) }
                .onFailure { _events.tryEmit(ChatWsEvent.Error("parse error: ${it.message}")) }
        }
    }

    private suspend fun sendText(payLoad: String) {
        val current = session
        if (current == null) {
            // Avval bu holatda ham "WS-OUT" yozilardi va xabar jimgina yo'qolardi.
            Logger.e(TAG, "WS-OUT yuborilmadi (ulanish yo'q): $payLoad")
            return
        }
        Logger.d(TAG, "WS-OUT: $payLoad")
        current.send(Frame.Text(payLoad))
    }

    private fun handleIncoming(raw: String) {
        val env = json.decodeFromString<WSResponse<JsonElement>>(raw)
        when (env.type) {
            "pong" -> {
                _events.tryEmit(ChatWsEvent.Pong)
            }

            "message_created" -> {
                val data = env.data?.let { json.decodeFromJsonElement<WSMessageCreated>(it) }

                val message = data?.message
                if (message != null) {
                    _events.tryEmit(ChatWsEvent.MessageCreated(message))
                }
            }

            "message_updated" -> {
                val data = env.data?.let { json.decodeFromJsonElement<WSMessageUpdated>(it) }
                val message = data?.message
                if (message != null) {
                    _events.tryEmit(ChatWsEvent.MessageUpdated(message))
                }
            }

            "read_update" -> {
                val data = env.data?.let { json.decodeFromJsonElement<WSReadUpdate>(it) }
                if (data != null) {
                    _events.tryEmit(
                        ChatWsEvent.ReadUpdate(
                            chatId = data.chat_id,
                            messageId = data.message_id,
                            readerId = data.reader_id
                        )
                    )
                }
            }

            "error" -> {
                val data = env.data?.let { json.decodeFromJsonElement<WSError>(it) }
                _events.tryEmit(ChatWsEvent.Error(data?.error ?: "Unknown WS error"))
            }

            else -> _events.tryEmit(ChatWsEvent.Raw(env.type))
        }
    }

//    suspend fun sendPing() {
//        val env = WSRequest<Any>(
//            type = "ping",
//            payload = null
//        )
//        sendText(json.encodeToString(env))
//    }

    suspend fun sendPing() {
        // WSRequest<Any> seriyalanmaydi ("Serializer for class 'Any' is not found"),
        // runCatching xatoni yutgani uchun ping umuman ketmas edi. JsonElement'ning serializeri bor.
        val env = WSRequest<JsonElement>(
            type = "ping",
            payload = null
        )
        sendText(json.encodeToString(env))
    }

    suspend fun sendMessage(message: WSSendMessage) {
        val env = WSRequest(
            type = "send_message",
            payload = message
        )
        sendText(json.encodeToString(env))
    }

    suspend fun editMessage(messageId: String, newText: String) {
        val editMessage = WSEditMessage(
            message_id = messageId,
            text = newText
        )
        val env = WSRequest(
            type = "edit_message",
            payload = editMessage
        )
        sendText(json.encodeToString(env))
    }

    suspend fun markRead(chatId: String, messageId: String) {
        // Ilova fondan qaytganda socket qayta ulanayotgan bo'ladi — "o'qildi" yo'qolmasin,
        // ulanish tiklanishini kutamiz. Ulanmasa xato qaytadi (repository Outcome.Failure beradi).
        withTimeoutOrNull(10_000) { connected.first { it } }
            ?: error("Chat socket ulanmagan — read yuborilmadi")

        val readMessage = WSReadMessage(
            chat_id = chatId,
            message_id = messageId
        )
        val env = WSRequest(
            type = "read",
            payload = readMessage
        )
        sendText(json.encodeToString(env))
    }
}