package uz.tikoncha_parent.domain.repository

import uz.tikoncha_parent.domain.model.RegistrationData
import uz.tikoncha_parent.domain.model.Session
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome

interface AuthRepository {
    suspend fun telegramLogin(idToken: String): Outcome<Session>
    suspend fun appleLogin(identityToken: String, fullName: String?): Outcome<Session>
    suspend fun googleLogin(idToken: String): Outcome<Session>
    suspend fun sendOtp(phone: String): Outcome<Unit>
    suspend fun verifyOtp(phone: String, code: String): Outcome<Session>
    suspend fun register(data: RegistrationData): Outcome<UserInfo>
}