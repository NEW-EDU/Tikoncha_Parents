package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import uz.tikoncha_parent.data.remote.model.autopay.AutopayCardRequest
import uz.tikoncha_parent.data.remote.model.autopay.AutopayChildRequest
import uz.tikoncha_parent.data.remote.model.autopay.AutopayEnableRequest
import uz.tikoncha_parent.data.remote.model.autopay.AutopayResponse
import uz.tikoncha_parent.data.remote.model.autopay.CardConfirmRequest
import uz.tikoncha_parent.data.remote.model.autopay.CardListResponse
import uz.tikoncha_parent.data.remote.model.autopay.CardResponse
import uz.tikoncha_parent.data.remote.model.autopay.CardStartRequest
import uz.tikoncha_parent.data.remote.model.autopay.CardStartResponse
import uz.tikoncha_parent.data.remote.model.autopay.ChargeResultResponse
import uz.tikoncha_parent.data.remote.model.autopay.ChargeStatusResponse
import uz.tikoncha_parent.data.remote.model.autopay.OkResponse

/**
 * `/autopay` — karta orqali avto-to'lov (Paylov). Kartalar ota-onaniki (bir karta — bir nechta farzand),
 * avto-to'lov esa har farzand uchun alohida: `childId` farzandning user id'si.
 */
class AutopayApiService(private val client: HttpClient) {

    suspend fun cards(): CardListResponse =
        client.safeRequest(HttpMethod.Get, "/autopay/cards")

    suspend fun startCard(body: CardStartRequest): CardStartResponse =
        client.safeRequest(HttpMethod.Post, "/autopay/cards") { setBody(body) }

    suspend fun confirmCard(cardId: String, body: CardConfirmRequest): CardResponse =
        client.safeRequest(HttpMethod.Post, "/autopay/cards/$cardId/confirm") { setBody(body) }

    /** "Asosiy qilish" — yangilangan ro'yxat; ota-onaning barcha avto-to'lovlari shu kartaga o'tadi. */
    suspend fun makePrimary(cardId: String): CardListResponse =
        client.safeRequest(HttpMethod.Post, "/autopay/cards/$cardId/primary")

    suspend fun removeCard(cardId: String): OkResponse =
        client.safeRequest(HttpMethod.Delete, "/autopay/cards/$cardId")

    suspend fun state(childId: String): AutopayResponse =
        client.safeRequest(HttpMethod.Get, "/autopay") { parameter("child_user_id", childId) }

    suspend fun enable(body: AutopayEnableRequest): ChargeResultResponse =
        client.safeRequest(HttpMethod.Post, "/autopay") { setBody(body) }

    suspend fun changeCard(body: AutopayCardRequest): AutopayResponse =
        client.safeRequest(HttpMethod.Put, "/autopay/card") { setBody(body) }

    suspend fun retry(childId: String): ChargeResultResponse =
        client.safeRequest(HttpMethod.Post, "/autopay/retry") { setBody(AutopayChildRequest(childId)) }

    suspend fun disable(childId: String): AutopayResponse =
        client.safeRequest(HttpMethod.Delete, "/autopay") { parameter("child_user_id", childId) }

    suspend fun chargeStatus(transactionId: String): ChargeStatusResponse =
        client.safeRequest(HttpMethod.Get, "/autopay/charges/$transactionId")
}
