package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import uz.tikoncha_parent.data.remote.model.AddChildRequest
import uz.tikoncha_parent.data.remote.model.AddChildResponse
import uz.tikoncha_parent.data.remote.model.ApiEnvelope
import uz.tikoncha_parent.data.remote.model.AppUsageDataDto
import uz.tikoncha_parent.data.remote.model.ChildrenLocationResponse
import uz.tikoncha_parent.data.remote.model.ChildrenResponse
import uz.tikoncha_parent.data.remote.model.UnlinkChildRequest
import uz.tikoncha_parent.data.remote.model.UnlinkChildResponse

class ChildApiService(private val client: HttpClient) {

    suspend fun addChild(request: AddChildRequest): AddChildResponse =
        client.safeRequest(
            method = HttpMethod.Post,
            url = "users/add-child",
            block = {
                setBody(request)
            }
        )

    suspend fun children(): ChildrenResponse =
        client.safeRequest(
            method = HttpMethod.Get,
            url = "users/children",
            block = {

            }
        )
    /**
     * Soatlik foydalanish. Sanalar — bolaning mahalliy sanasi (YYYY-MM-DD, ikkalasi ham kiradi).
     * [tz] — server "bugun"ni va obuna bo'yicha ruxsat etilgan kunlarni shu mintaqada hisoblaydi.
     */
    suspend fun appUsages(userId: String, dateFrom: String, dateTo: String, tz: String): ApiEnvelope<AppUsageDataDto> =
        client.safeRequest(
            method = HttpMethod.Get,
            url = "data-exchange",
            block = {
                parameter("user_id", userId)
                parameter("date_from", dateFrom)
                parameter("date_to", dateTo)
                parameter("tz", tz)
            }
        )

    suspend fun childrenLocation(): ChildrenLocationResponse =
        client.safeRequest(
            method = HttpMethod.Get,
            url = "data-exchange/children/locations",
            block = {}
        )

    suspend fun unlinkChild(request: UnlinkChildRequest): UnlinkChildResponse =
        client.safeRequest(
            method = HttpMethod.Post,
            url = "users/unlink-parent",
            block = {
                setBody(request)
            }
        )
}