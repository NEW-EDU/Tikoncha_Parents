package uz.tikoncha_parent.data.remote

import io.ktor.http.Url
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.platform.BuildConfig
import uz.tikoncha_parent.platform.KmpLogger
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs

class TikonchaClient(private val engine: HttpClientEngine) {

    companion object {
        private const val _BASE_URL = "api.tikoncha.uz"
        private const val _BASE_URL_TEST = "tikoncha.uz/backend/test"
        val isDevelopment: Boolean = false

        val BASE_URL = if (isDevelopment) _BASE_URL_TEST else _BASE_URL

        private const val LOG_TAG = "TIKONCHA_API"
    }

    val client = HttpClient(engine) {

        install(WebSockets) {
            pingIntervalMillis = 20_000
        }

        install(ContentNegotiation) {
            json(
                Json
                {
                    prettyPrint = BuildConfig.isDebug // release'da trafikni tejaymiz
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    coerceInputValues = true
                }
            )
        }

        // Log faqat debug'da
        if (BuildConfig.isDebug) {
            install(Logging) {
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        val formatted = formatJsonInLog(message)
                        Logger.d(LOG_TAG, formatted)
                    }
                }
                level = LogLevel.ALL
                sanitizeHeader { it.equals("Authorization", ignoreCase = true) }
            }
        }

        install(HttpRedirect) {
            checkHttpMethod = false
            allowHttpsDowngrade = true
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }

        defaultRequest {
            url {
                // Sinov build'ida lokal backend (BuildConfig.apiOverride); aks holda prod
                val override = BuildConfig.apiOverride.takeIf { it.isNotBlank() }?.let { Url(it) }
                if (override != null) {
                    protocol = override.protocol
                    host = override.host
                    port = override.port
                } else {
                    protocol = URLProtocol.HTTPS
                    host = BASE_URL
                }
            }
            header("Accept", "application/json")
            header("Content-Type", "application/json")
            header("X-DEVICE-TYPE", BuildConfig.deviceType) // ANDROID yoki IOS
            header("LANG-CODE", LanguagePrefs.loadOrDefault().languageCode)

            // Token bo'sh bo'lsa "Bearer " yubormaslik uchun
            val token = AppSettings.accessToken
            if (token.isNotBlank()) {
                header("Authorization", "Bearer $token")
            }
        }

        // Production'da network xatolarini silently yutmaslik uchun:
        // expectSuccess = true  // agar kerak bo'lsa
    }


    private val prettyJson = Json {
        prettyPrint = true
        isLenient = true
    }

    private fun formatJsonInLog(message: String): String {
        val startMarker = "BODY START"
        val endMarker = "BODY END"

        val start = message.indexOf(startMarker)
        val end = message.indexOf(endMarker)

        if (start == -1 || end == -1 || end <= start) return message

        val before = message.substring(0, start + startMarker.length)
        val body = message.substring(start + startMarker.length, end).trim()
        val after = message.substring(end)

        if (body.isEmpty()) return message

        val pretty = try {
            val element = prettyJson.parseToJsonElement(body)
            prettyJson.encodeToString(JsonElement.serializer(), element)
        } catch (e: Exception) {
            return message // JSON emas (HTML, text, va h.k.) — original qaytaramiz
        }

        return "$before\n$pretty\n$after"
    }


}