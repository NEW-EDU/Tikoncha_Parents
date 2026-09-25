package uz.tikoncha_parent.domain.repository

import kotlinx.datetime.LocalDate
import uz.tikoncha_parent.domain.model.ChildLinkCode
import uz.tikoncha_parent.domain.model.ChildLocation
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_usage.AppUsage

interface ChildRepository {

    suspend fun addChild(phone: String): Outcome<ChildLinkCode>
    suspend fun children(): Outcome<List<UserInfo>>

    suspend fun appUsages(
        childId: String,
        from: LocalDate,
        to: LocalDate,
    ): Outcome<List<AppUsage>>

    suspend fun childrenLocation(): Outcome<List<ChildLocation>>

    suspend fun unlinkChild(childUserId: String, parentUserId: String): Outcome<String>
}