package uz.tikoncha_parent.data.repository

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import uz.tikoncha_parent.data.mapper.toAppUsageList
import uz.tikoncha_parent.data.mapper.toChildLinkCode
import uz.tikoncha_parent.data.mapper.toChildLocation
import uz.tikoncha_parent.data.mapper.toUserInfo
import uz.tikoncha_parent.data.remote.ChildApiService
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.data.remote.model.AddChildRequest
import uz.tikoncha_parent.data.remote.model.UnlinkChildRequest
import uz.tikoncha_parent.domain.model.ChildLinkCode
import uz.tikoncha_parent.domain.model.ChildLocation
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_usage.AppUsage
import uz.tikoncha_parent.domain.repository.ChildRepository

class ChildRepositoryImpl(
    private val api: ChildApiService
): ChildRepository {
    override suspend fun addChild(phone: String): Outcome<ChildLinkCode> =
        apiCall(TAG) {
            val r = api.addChild(AddChildRequest(phone_number = phone))
            val body = r.data
            when {
                r.success && body != null -> Outcome.Success(body.toChildLinkCode())
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    override suspend fun children(): Outcome<List<UserInfo>> =
        apiCall(TAG) {
            val r = api.children()
            val body = r.data
            when {
                r.success && body != null -> Outcome.Success(body.children.map { it.toUserInfo() })
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    override suspend fun appUsages(
        childId: String,
        from: LocalDate,
        to: LocalDate,
    ): Outcome<List<AppUsage>> =
        apiCall(TAG) {
            val r = api.appUsages(
                userId = childId,
                dateFrom = from.format(DATE_FORMAT),
                dateTo = to.format(DATE_FORMAT),
                // Busiz server UTC oladi: Toshkentda 00:00–05:00 orasida bugungi kun kesilib qolardi.
                tz = TimeZone.currentSystemDefault().id,
            )
            val body = r.data
            when {
                r.success && body != null -> Outcome.Success(body.toAppUsageList())
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    override suspend fun childrenLocation(): Outcome<List<ChildLocation>> =
        apiCall(TAG) {
            val r = api.childrenLocation()
            val body = r.data
            when {
                r.success && body != null ->
                    Outcome.Success(body.items.mapNotNull { it.toChildLocation() })
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    override suspend fun unlinkChild(
        childUserId: String,
        parentUserId: String
    ): Outcome<String> = apiCall(TAG) {
        val r = api.unlinkChild(
            UnlinkChildRequest(
                child_user_id = childUserId,
                parent_user_id = parentUserId
            )
        )
        if (r.success) Outcome.Success(r.data?.message ?: "")
        else Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
    }

    private companion object {
        const val TAG = "ChildRepository"
        val DATE_FORMAT = LocalDate.Format {
            year(); char('-'); monthNumber(); char('-'); day()
        }
    }
}