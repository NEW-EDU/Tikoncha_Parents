package uz.tikoncha_parent.data.mapper

import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.data.remote.model.UserInfoDto
import uz.tikoncha_parent.domain.model.GenderType
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs

fun UserInfoDto.toUserInfo(): UserInfo{

    val languageCode = LanguageType.getLangType(LanguagePrefs.loadOrDefault().languageCode)
    val dateMillis = DateTimeUtil.toMillisForChild(last_seen)
    val last_seen = DateTimeUtil.formatDateTimeMonthlyForMap(dateMillis, languageCode)

    return UserInfo(
        userId = user_id?:"",
        phoneNumber = phone_number?:"",
        name = first_name?:"",
        lastName = last_name?:"",
        patronymic = patronymic?:"",
        genderType = GenderType.getGenderByKey(gender)?: GenderType.MALE,
        passportId = passport_id?:"",
        age = age,
        schoolId = school_id,
        schoolName = school_name,
        schoolClassName = school_class_name,
        schoolClassId = school_class_id,
        shift = shift,
        fullName = "$last_name $first_name $patronymic",
        avatarUrl = avatar_url,
        last_seen = last_seen,
        subscription = subscription,
        subscription_end_date = subscription_end_date,
        signInMethod = sign_in_method,
        email = email,
        emailHidden = email_hidden == true,
    )
}