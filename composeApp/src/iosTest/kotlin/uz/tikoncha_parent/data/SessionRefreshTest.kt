package uz.tikoncha_parent.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.remote.SessionExpiredException
import uz.tikoncha_parent.data.remote.safeRequest

/**
 * The server answers an EXPIRED access token (every hour) with HTTP 401 and `"error_code": "session_ended"`.
 * The app must refresh and carry on, and sign out only when the refresh itself is rejected.
 * iOS-only: AppSettings needs the no-arg Settings, which has no Android context in JVM unit tests.
 */
class SessionRefreshTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val sessionEnded =
        """{"success":false,"data":null,"error":"Your session has ended","code":401,"error_code":"session_ended"}"""

    private fun client(refreshOk: Boolean, calls: MutableList<String>) = HttpClient(MockEngine { req ->
        val auth = req.headers[HttpHeaders.Authorization].orEmpty()
        calls += "${req.url.encodedPath} $auth"
        when {
            req.url.encodedPath.endsWith("auth/refresh") ->
                if (refreshOk) respond("""{"success":true,"data":{"access_token":"NEW"},"error":null,"code":200}""", HttpStatusCode.OK, jsonHeaders)
                else respond(sessionEnded, HttpStatusCode.Unauthorized, jsonHeaders)
            auth == "Bearer NEW" -> respond("""{"ok":1}""", HttpStatusCode.OK, jsonHeaders)
            else -> respond(sessionEnded, HttpStatusCode.Unauthorized, jsonHeaders)
        }
    }) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        defaultRequest {
            url("https://api.test/")
            AppSettings.accessToken.takeIf { it.isNotBlank() }?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }
    }

    private fun signIn() {
        AppSettings.accessToken = "EXPIRED"
        AppSettings.refreshToken = "REFRESH"
        AppSettings.hasUserLogin = true
    }

    @AfterTest
    fun cleanUp() = AppSettings.clearSession()

    @Test
    fun expiredAccessTokenIsRefreshedNotSignedOut() = runTest {
        signIn()
        val calls = mutableListOf<String>()

        val result: JsonObject = client(refreshOk = true, calls).safeRequest(HttpMethod.Get, "children")

        assertEquals("1", result["ok"].toString())
        assertEquals("NEW", AppSettings.accessToken)
        assertTrue(AppSettings.hasUserLogin)
        assertEquals(3, calls.size, "request, refresh, retry: $calls")
    }

    @Test
    fun revokedSessionStillSignsOut() = runTest {
        signIn()
        val calls = mutableListOf<String>()

        assertFailsWith<SessionExpiredException> {
            client(refreshOk = false, calls).safeRequest<JsonObject>(HttpMethod.Get, "children")
        }
        assertFalse(AppSettings.hasUserLogin)
        assertEquals("", AppSettings.accessToken)
        assertTrue(calls.any { it.contains("auth/refresh") }, "refresh was attempted first: $calls")
    }
}
