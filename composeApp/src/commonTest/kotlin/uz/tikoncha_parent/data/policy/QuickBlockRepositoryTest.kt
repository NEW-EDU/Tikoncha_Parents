package uz.tikoncha_parent.data.policy

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import uz.tikoncha_parent.data.remote.policy.PolicyApiService
import uz.tikoncha_parent.data.repository.policy.QuickBlockRepositoryImpl
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.QuickBlockResult
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuickBlockRepositoryTest {

    private val requests = mutableListOf<HttpMethod>()

    private fun repo(handler: suspend MockRequestHandleScope.(HttpRequestData) -> io.ktor.client.request.HttpResponseData): QuickBlockRepositoryImpl {
        val client = HttpClient(MockEngine { req -> requests += req.method; handler(req) }) {
            install(ContentNegotiation) { json(wireJson) }
        }
        return QuickBlockRepositoryImpl(PolicyApiService(client))
    }

    private fun MockRequestHandleScope.ok(data: String) =
        respond(Wire.envelope(data), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))

    /** Javobdagi jadval keshdagi o'z yozuvini almashtiradi; boshqalarniki tegilmaydi, qayta GET yo'q. */
    @Test
    fun addAppliesResponsePolicyWithoutRelisting() = runTest {
        val repo = repo { req ->
            if (req.method == HttpMethod.Get) ok(Wire.QUICK_BLOCK_LIST) else ok(Wire.QUICK_BLOCK_ADDED)
        }
        repo.refresh(Wire.CHILD_ID)
        val before = repo.observeQuickBlocks(Wire.CHILD_ID).first()
        assertEquals(listOf(false, true), before.map { it.isActive })

        val res = repo.add(Wire.CHILD_ID, QuickBlockTarget.app("com.instagram.android"))

        assertEquals(Outcome.Success(QuickBlockResult.ADDED), res)
        val after = repo.observeQuickBlocks(Wire.CHILD_ID).first()
        assertEquals(listOf(true, true), after.map { it.isActive })            // qayta yoqildi
        assertEquals(before[1], after[1])                                         // bolaniki o'zgarmadi
        assertEquals(listOf(HttpMethod.Get, HttpMethod.Post), requests)
    }

    /** Ikkinchi bosish birinchisining javobini kutadi — so'rovlar ustma-ust ketmaydi. */
    @Test
    fun requestsRunOneAtATime() = runTest {
        val firstArrived = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        var inFlight = 0
        var maxInFlight = 0
        val repo = repo {
            inFlight++; maxInFlight = maxOf(maxInFlight, inFlight)
            if (!firstArrived.isCompleted) { firstArrived.complete(Unit); releaseFirst.await() }
            inFlight--
            ok(Wire.QUICK_BLOCK_ADDED)
        }

        val a = async { repo.add(Wire.CHILD_ID, QuickBlockTarget.app("a.one")) }
        firstArrived.await()
        val b = async { repo.add(Wire.CHILD_ID, QuickBlockTarget.app("b.two")) }
        repeat(5) { yield() }
        assertEquals(1, requests.size)                                           // ikkinchisi navbatda

        releaseFirst.complete(Unit)
        a.await(); b.await()
        assertEquals(2, requests.size)
        assertEquals(1, maxInFlight)
    }

    /** O'chiq blokni yoqish: PATCH javobidagi jadval keshdagi yozuvni almashtiradi. */
    @Test
    fun setEnabledAppliesPatchResponse() = runTest {
        val repo = repo { req ->
            if (req.method == HttpMethod.Get) ok(Wire.QUICK_BLOCK_LIST) else ok(Wire.QUICK_BLOCK_POLICY)
        }
        repo.refresh(Wire.CHILD_ID)
        val res = repo.update(Wire.CHILD_ID, "44444444-4444-4444-4444-444444444444", PolicyPatch.toggle(true))

        assertTrue((res as Outcome.Success).data.isActive)
        assertEquals(listOf(true, true), repo.observeQuickBlocks(Wire.CHILD_ID).first().map { it.isActive })
        assertEquals(listOf(HttpMethod.Get, HttpMethod.Patch), requests)
    }

    @Test
    fun removeWithoutOwnBlockIsAbsent() = runTest {
        val repo = repo {
            respond(Wire.envelope("null", success = false, code = 404, error = "Jadval topilmadi."),
                HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        assertEquals(Outcome.Success(QuickBlockResult.ABSENT), repo.remove(Wire.CHILD_ID, QuickBlockTarget.app("x.y")))
    }

    @Test
    fun addBeforeFirstListDoesNotInventAList() = runTest {
        val repo = repo { ok(Wire.QUICK_BLOCK_ADDED) }
        repo.add(Wire.CHILD_ID, QuickBlockTarget.app("com.instagram.android"))
        assertTrue(repo.observeQuickBlocks(Wire.CHILD_ID).first().isEmpty())     // refresh olib keladi
    }
}
