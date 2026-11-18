package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.remote.model.ai.SttDto
import uz.tikoncha_parent.data.remote.model.ai.TtsRequest
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import uz.tikoncha_parent.data.remote.model.ApiEnvelope
import uz.tikoncha_parent.data.remote.model.ai.ChildReportDto
import uz.tikoncha_parent.data.remote.model.ai.SafetyAlertDto
import uz.tikoncha_parent.data.remote.model.ai.ParentChatRequest

/**
 * Tikoncha API `/ai/parent` — farzand hisoboti va ota-ona yordamçisi (docs/ai-platform-plan.md).
 * Suhbat javobi Server-Sent Events: status · token · draft · error · done.
 * Xato bo'lsa server SSE o'rniga envelope JSON qaytaradi (HTTP 200, `code` tanada) — 401 da token yangilanadi.
 */
class AiApiService(private val client: HttpClient) {

    /** Envelope xatosi: `code` — haqiqiy kod, `serverMessage` — server tarjima qilgan matn. */
    class EnvelopeException(val code: Int?, val serverMessage: String?) : Exception("code=$code")

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** Raqamlar bazadan; matn — AI. Matn kesh qilingan bo'lmasa server bir necha soniya yozadi. */
    suspend fun report(childId: String, period: String): ApiEnvelope<ChildReportDto> =
        client.safeRequest(
            method = HttpMethod.Get,
            url = "ai/parent/children/$childId/report",
            block = {
                parameter("period", period)
                timeout {
                    requestTimeoutMillis = 60_000
                    socketTimeoutMillis = 60_000
                }
            }
        )

    /**
     * SSE oqimi. channelFlow — oddiy flow emas: iOS'da (Darwin) Ktor `execute {}` blokini Dispatchers.IO'da
     * ishlatadi, flow {} esa boşqa kontekstdan emit'ni taqiqlaydi ("Flow invariant is violated") — iPhone'da
     * har bir javob şu sababli tashlab yuborilardi (2026-11-18). send() har qanday kontekstdan xavfsiz.
     */
    fun chat(request: ParentChatRequest): Flow<JsonObject> = channelFlow {
        var refreshed = false
        while (true) {
            var unauthorized = false
            client.preparePost("ai/parent/chat") {
                contentType(ContentType.Application.Json)
                setBody(request)
                timeout {
                    // Asboblar (hisobot, ilovalar) ishlaganda bo'laklar orasida jimlik bo'ladi
                    requestTimeoutMillis = 180_000
                    socketTimeoutMillis = 90_000
                }
            }.execute { response ->
                if (!response.status.isSuccess()) throw EnvelopeException(response.status.value, null)
                if (isEnvelope(response)) {
                    val failure = envelopeFailure(response)
                    if (failure.code == 401 && !refreshed) {
                        unauthorized = true
                        return@execute
                    }
                    throw failure
                }
                val channel = response.bodyAsChannel()
                while (true) {
                    val line = channel.readUTF8Line() ?: break
                    if (!line.startsWith("data:")) continue
                    val payload = line.removePrefix("data:").trim()
                    if (payload.isEmpty()) continue
                    val event = runCatching { json.parseToJsonElement(payload).jsonObject }.getOrNull() ?: continue
                    send(event)
                }
            }
            if (!unauthorized) return@channelFlow
            refreshed = true
            when (val r = refreshAccessToken(client)) {
                RefreshResult.Success -> Unit
                RefreshResult.InvalidToken -> {
                    handleSessionExpired()
                    throw EnvelopeException(401, null)
                }
                is RefreshResult.Temporary -> throw r.cause ?: IOException("Tokenni yangilab bo'lmadi")
            }
        }
    }

    /**
     * Ekran holati ovozda (masalan, Himoya): server qisqa matn tuzadi, ovozga aylantiradi va keşlaydi —
     * `{text, audio_url}`. Bepul (tangasiz).
     */
    suspend fun statusVoice(childId: String, screen: String, lang: String): ApiEnvelope<JsonElement> =
        client.safeRequest(
            method = HttpMethod.Post,
            url = "ai/parent/status-voice",
            block = {
                contentType(ContentType.Application.Json)
                setBody(kotlinx.serialization.json.buildJsonObject {
                    put("child_id", kotlinx.serialization.json.JsonPrimitive(childId))
                    put("screen", kotlinx.serialization.json.JsonPrimitive(screen))
                    put("lang", kotlinx.serialization.json.JsonPrimitive(lang))
                })
            },
        )

    /** [coin] — topşiriq kartasida ota-ona özgartirgan mukofot. */
    suspend fun applyDraft(draftId: String, coin: Int? = null): ApiEnvelope<JsonElement> =
        client.safeRequest(
            method = HttpMethod.Post,
            url = "ai/parent/drafts/$draftId/apply",
            block = {
                contentType(ContentType.Application.Json)
                setBody(if (coin != null) """{"coin":$coin}""" else "{}")
            },
        )

    /** Farzandga körinadigan eslatma: [kind] = gps · permissions. */
    suspend fun remind(childId: String, kind: String): ApiEnvelope<JsonElement> =
        client.safeRequest(
            method = HttpMethod.Post,
            url = "ai/parent/children/$childId/remind",
            block = {
                contentType(ContentType.Application.Json)
                setBody("""{"kind":"$kind"}""")
            },
        )

    /** Farzand(lar) Bilimdonga yuborgan xavotirli xabarlar (yangisi birinçi). */
    suspend fun alerts(childId: String?): ApiEnvelope<List<SafetyAlertDto>> =
        client.safeRequest(
            method = HttpMethod.Get,
            url = "ai/parent/alerts",
            block = { childId?.let { parameter("child_id", it) } },
        )

    /** AI suhbati oçildi — ro'yxatdagi öqilmagan belgisi tozalanadi. */
    suspend fun markChatRead(chatId: String, readAtIso: String): ApiEnvelope<JsonElement> =
        client.safeRequest(
            method = HttpMethod.Post,
            url = "chat/read",
            block = {
                contentType(ContentType.Application.Json)
                setBody("""{"chat_id":"$chatId","read_at":"$readAtIso"}""")
            },
        )

    suspend fun discardDraft(draftId: String): ApiEnvelope<JsonElement> =
        client.safeRequest(method = HttpMethod.Delete, url = "ai/parent/drafts/$draftId")

    /**
     * Ovozli savol → matn: multipart `file` (m4a) + `lang` (ilova tili; server bilmasa e'tiborsiz qoldiradi).
     * Tanib oliş bir necha soniya — umumiy 15 s chegarasi uzaytiriladi. 401 bölsa token yangilanib qayta yuboriladi.
     */
    suspend fun transcribe(audio: ByteArray, fileName: String, mimeType: String, lang: String): ApiEnvelope<SttDto> {
        val parts = formData {
            append(
                key = "file",
                value = audio,
                Headers.build {
                    append(HttpHeaders.ContentType, mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                },
            )
            append("lang", lang)
        }
        suspend fun post(): HttpResponse = client.submitFormWithBinaryData(url = "ai/stt", formData = parts) {
            timeout {
                requestTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }
        }
        var response = post()
        if (isUnauthorized(response.status, response.bodyAsText()) && AppSettings.accessToken.isNotBlank()) {
            when (val r = refreshAccessToken(client)) {
                RefreshResult.Success -> response = post()
                RefreshResult.InvalidToken -> {
                    handleSessionExpired()
                    throw SessionExpiredException()
                }
                is RefreshResult.Temporary -> throw r.cause ?: IOException("Tokenni yangilab bo'lmadi")
            }
        }
        return response.body()
    }

    /** Matn → ovoz natijasi: [audio] (MP3) yoki server kodi ([code]: 429 — kunlik chegara, 503 — sozlanmagan …). */
    class SpeechResult(val audio: ByteArray?, val code: Int?)

    /**
     * Javobni ovoz bilan öqiş: `POST /ai/tts {text, lang}` → audio/mpeg. Audio boşlanmasidan oldingi xato —
     * JSON envelope (ai_text_empty, ai_rate_limited, ai_voice_failed, ai_voice_unavailable).
     */
    suspend fun speech(text: String, lang: String): SpeechResult {
        suspend fun post() = readSpeech(
            client.post("ai/tts") {
                contentType(ContentType.Application.Json)
                setBody(TtsRequest(text = text, lang = lang))
                timeout {
                    requestTimeoutMillis = 60_000
                    socketTimeoutMillis = 30_000
                }
            }
        )
        var result = post()
        if (result.code == 401 && AppSettings.accessToken.isNotBlank() && refreshAccessToken(client) == RefreshResult.Success) {
            result = post()
        }
        return result
    }

    private suspend fun readSpeech(response: HttpResponse): SpeechResult {
        val type = response.contentType()
        if (response.status.isSuccess() && type?.contentType == "audio") {
            return SpeechResult(response.bodyAsBytes(), response.status.value)
        }
        if (!response.status.isSuccess()) return SpeechResult(null, response.status.value)
        // Xato — envelope (HTTP 200, haqiqiy kod tanada)
        val code = runCatching { json.parseToJsonElement(response.bodyAsText()).jsonObject["code"]?.jsonPrimitive?.intOrNull }
            .getOrNull()
        return SpeechResult(null, code ?: 500)
    }

    private fun isEnvelope(response: HttpResponse): Boolean =
        response.contentType()?.match(ContentType.Application.Json) == true

    private suspend fun envelopeFailure(response: HttpResponse): EnvelopeException {
        val env = runCatching { json.parseToJsonElement(response.bodyAsText()).jsonObject }.getOrNull()
        return EnvelopeException(
            code = env?.get("code")?.jsonPrimitive?.intOrNull,
            serverMessage = env?.get("error")?.jsonPrimitive?.contentOrNull,
        )
    }
}
