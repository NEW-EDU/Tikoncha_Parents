package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random
import uz.tikoncha_parent.domain.model.app_error.Outcome

/**
 * Server yetib bormoqdami — har bir API natijasidan (apiCall) bilinadi. Offline-first UI shu bilan xotirjam
 * "offline" belgisini körsatadi va xato oynalarini çiqarmaydi; [probeUntilOnline] internet qaytişini kutadi.
 */
object ServerReachability {
    private val _online = MutableStateFlow(true)
    val online: StateFlow<Boolean> = _online.asStateFlow()

    fun report(outcome: Outcome<*>) {
        when (outcome) {
            is Outcome.Success -> if (!_online.value) _online.value = true
            is Outcome.Failure -> if (outcome.cause.isUnreachable && _online.value) _online.value = false
        }
    }

    fun markOnline() { if (!_online.value) _online.value = true }

    /** Offline paytda har [periodMs] da `GET /health` — javob kelsa online. */
    suspend fun probeUntilOnline(client: HttpClient, periodMs: Long = 20_000) {
        while (!_online.value) {
            delay(periodMs)
            val ok = runCatching { client.get("health").status.isSuccess() }.getOrDefault(false)
            if (ok) markOnline()
        }
    }
}

const val IDEMPOTENCY_KEY = "Idempotency-Key"

private val WRITE_METHODS = setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch, HttpMethod.Delete)

fun newIdempotencyKey(): String = buildString {
    repeat(32) { append("0123456789abcdef"[Random.nextInt(16)]) }
}

/** Har bir yoziş so'roviga kalit (agar yöq bölsa): javob yöqolib qayta yuborilsa, server uni bir marta bajaradi. */
val IdempotencyKeyPlugin = createClientPlugin("IdempotencyKey") {
    onRequest { request, _ ->
        if (request.method in WRITE_METHODS && request.headers[IDEMPOTENCY_KEY] == null) {
            request.headers.append(IDEMPOTENCY_KEY, newIdempotencyKey())
        }
    }
}
