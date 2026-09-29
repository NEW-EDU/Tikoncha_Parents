package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.setBody
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.contentType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.remote.model.RefreshTokenRequest
import uz.tikoncha_parent.data.remote.model.RefreshTokenResponse
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <reified T> HttpClient.safeRequest(
    method: HttpMethod,
    url: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): T {
    // First request
    val response = this.request {
        this.method = method
        url(url)
        contentType(ContentType.Application.Json)
        block()
    }

    // Check for 401
    val bodyText = response.bodyAsText()
    if (bodyText.contains("\"code\":401") || response.status == HttpStatusCode.Unauthorized) {
        when (val r = refreshAccessToken(this)) {
            RefreshResult.Success -> {
                val retryResponse = this.request {
                    this.method = method
                    url(url)
                    contentType(ContentType.Application.Json)
                    block()
                }
                return retryResponse.body()
            }
            RefreshResult.InvalidToken -> {
                handleSessionExpired()
                throw Exception("Session expired, please login again.")
            }
            is RefreshResult.Temporary -> throw r.cause ?: IOException("Tokenni yangilab bo'lmadi")
        }
    }

    return response.body()
}



suspend inline fun <reified T> HttpClient.safeUploadMultipart(
    url: String,
    formData: List<PartData>
): T {
    // 🔹 1. First attempt
    val response = this.submitFormWithBinaryData(
        url = url,
        formData = formData
    )

    val bodyText = response.bodyAsText()

    // 🔁 2. Token expired (code == 501)
    if (bodyText.contains("\"code\":401")) {
        when (val r = refreshAccessToken(this)) {
            RefreshResult.Success -> {
                val retryResponse = this.submitFormWithBinaryData(
                    url = url,
                    formData = formData
                )
                return retryResponse.body()
            }
            RefreshResult.InvalidToken -> {
                handleSessionExpired()
                throw Exception("Session expired, please login again.")
            }
            is RefreshResult.Temporary -> throw r.cause ?: IOException("Tokenni yangilab bo'lmadi")
        }
    }

    return response.body()
}

/** Token yangilash natijasi. */
sealed interface RefreshResult {
    data object Success : RefreshResult
    data object InvalidToken : RefreshResult
    data class Temporary(val cause: Throwable?) : RefreshResult
}

private val refreshMutex = Mutex()

suspend fun refreshAccessToken(client: HttpClient): RefreshResult {
    // Refresh token yo'q — server 422 qaytaradi, 401 emas. Cheksiz siklga tushmaslik uchun.
    if (AppSettings.refreshToken.isBlank()) return RefreshResult.InvalidToken

    val tokenBefore = AppSettings.accessToken

    // Boshqa korutin allaqachon yangilayapti — kutamiz, o'zimiz 15 s urinmaymiz
    if (refreshMutex.isLocked) {
        refreshMutex.withLock { }
        val after = AppSettings.accessToken
        return if (after.isNotBlank() && after != tokenBefore) RefreshResult.Success
        else RefreshResult.Temporary(null)
    }

    return refreshMutex.withLock {
        // Navbat kutayotganda kimdir yangilab bo'lgan bo'lishi mumkin
        val current = AppSettings.accessToken
        if (current.isNotBlank() && current != tokenBefore) return@withLock RefreshResult.Success

        try {
            val body = client.post("auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(AppSettings.refreshToken))
            }.body<RefreshTokenResponse>()

            val newToken = body.data?.access_token
            when {
                body.success && !newToken.isNullOrBlank() -> {
                    AppSettings.accessToken = newToken
                    RefreshResult.Success
                }
                body.code == 401 || body.code == 403 -> RefreshResult.InvalidToken
                else -> RefreshResult.Temporary(null)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RefreshResult.Temporary(e)
        }
    }
}

fun handleSessionExpired() {
    AppSettings.clearSession()
    AuthEventBus.emit(AuthEvent.SessionExpired)
}