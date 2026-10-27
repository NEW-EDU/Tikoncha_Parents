package uz.tikoncha_parent.data.remote

import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.URLBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import uz.tikoncha_parent.data.local.AppSettings

/**
 * Zaxira server (Tikoncha_API `standby/RUNBOOK.md`): asosiy API (api.tikoncha.uz) yiqilsa, sayt serveridagi nusxa
 * `https://tikoncha.uz/api-standby/` qölda "promoted" qilinadi. Ilova unga faqat zaxiraning öz `/role` javobi
 * `promoted` bölganda öçadi va u shunday deb turguncha qoladi — eski asosiy server eskirgan ma'lumot bilan qaytib
 * kelsa ham ilovani özi tomon tortmaydi. Tekşiriş: asosiy server tarmoq/502–504 xatosida, eng köpi 2 daqiqada bir.
 */
object Failover {
    const val PRIMARY_HOST = "api.tikoncha.uz"
    const val STANDBY_HOST = "tikoncha.uz"
    const val STANDBY_PREFIX = "api-standby"
    const val STANDBY_ROLE_URL = "https://$STANDBY_HOST/$STANDBY_PREFIX/role"
    private val CHECK_EVERY = 2.minutes
    private const val KEY_ACTIVE = "failover_active"

    var active: Boolean = false
        private set
    private var checkedAt: TimeMark? = null
    private var checking = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Zaxiraning `/role` javobi; [TikonchaClient] alohida (bu plaginsiz) mijoz bilan beradi. */
    var fetchStandbyRole: suspend () -> String? = { null }

    /** Ilova qayta ochilganda oxirgi holat (zaxirada edikmi); birinçi so'rov uni qayta tekşiradi. */
    fun restore() {
        active = runCatching { AppSettings.offlineValue(KEY_ACTIVE) == "1" }.getOrDefault(false)
    }

    /** Asosiy serverga yetib bölmadi: zaxira "promoted" bölsa, unga öçamiz. */
    fun primaryLooksDown() = checkSoon()

    private fun checkSoon() {
        val last = checkedAt
        if (checking || (last != null && last.elapsedNow() < CHECK_EVERY)) return
        checking = true
        checkedAt = TimeSource.Monotonic.markNow()
        scope.launch {
            try { check() } finally { checking = false }
        }
    }

    internal suspend fun check() {
        val body = try { fetchStandbyRole() } catch (e: CancellationException) { throw e } catch (_: Throwable) { null }
        val promoted = isPromoted(body)
        if (promoted != active) {
            active = promoted
            runCatching { AppSettings.setOfflineValue(KEY_ACTIVE, if (promoted) "1" else null) }
        }
    }

    /** `{"success":true,"data":{"role":"promoted"},...}` → true. Javob yöq / boşqa rol → false. */
    internal fun isPromoted(body: String?): Boolean =
        body != null && Regex("\"role\"\\s*:\\s*\"promoted\"").containsMatchIn(body)

    /** Zaxira faol bölsa, api.tikoncha.uz/<path> → tikoncha.uz/api-standby/<path>. */
    internal fun rewrite(url: URLBuilder) {
        if (!active || url.host != PRIMARY_HOST) return
        url.host = STANDBY_HOST
        url.encodedPathSegments = listOf(STANDBY_PREFIX) + url.encodedPathSegments.dropWhile { it.isEmpty() }
    }

    internal fun resetForTest() { active = false; checkedAt = null; checking = false }

    val Plugin = createClientPlugin("Failover") {
        onRequest { request, _ ->
            if (active) checkSoon()   // zaxirada: hali ham "promoted"mi — yöq bölsa asosiyga qaytamiz
            rewrite(request.url)
        }
        on(Send) { request ->
            val onStandby = request.url.host == STANDBY_HOST
            try {
                val call = proceed(request)
                if (!onStandby && call.response.status.value in 502..504) primaryLooksDown()
                call
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                if (!onStandby) primaryLooksDown()
                throw e
            }
        }
    }
}
