package uz.tikoncha_parent.data.repository

import uz.tikoncha_parent.data.mapper.toRequest
import uz.tikoncha_parent.data.mapper.toSession
import uz.tikoncha_parent.data.mapper.toUserInfo
import uz.tikoncha_parent.data.remote.LoginApiService
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.data.remote.model.SendOtpRequest
import uz.tikoncha_parent.data.remote.model.VerifyOtpRequest
import uz.tikoncha_parent.common.AppCode
import uz.tikoncha_parent.data.remote.model.auth.AppleLoginRequest
import uz.tikoncha_parent.data.remote.model.auth.GoogleLoginRequest
import uz.tikoncha_parent.data.remote.model.auth.TelegramLoginRequest
import uz.tikoncha_parent.data.remote.model.auth.TelegramLoginResponse
import uz.tikoncha_parent.domain.model.RegistrationData
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.AuthRepository
import uz.tikoncha_parent.domain.model.Session
import uz.tikoncha_parent.domain.model.UserInfo

class AuthRepositoryImpl(
    private val api: LoginApiService
): AuthRepository {
    override suspend fun telegramLogin(idToken: String): Outcome<Session> =
        apiCall(TAG) {
            val r = api.telegramLogin(TelegramLoginRequest(token = idToken))
            val data = r.data
            when {
                r.success && data != null -> Outcome.Success(data.toSession())
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    override suspend fun appleLogin(identityToken: String, fullName: String?): Outcome<Session> =
        apiCall(TAG) {
            api.appleLogin(AppleLoginRequest(identityToken, AppCode.currentAppCode, fullName)).toSessionOutcome()
        }

    override suspend fun googleLogin(idToken: String): Outcome<Session> =
        apiCall(TAG) {
            api.googleLogin(GoogleLoginRequest(idToken, AppCode.currentAppCode)).toSessionOutcome()
        }

    private fun TelegramLoginResponse.toSessionOutcome(): Outcome<Session> {
        val d = data
        return when {
            success && d != null -> Outcome.Success(d.toSession())
            success -> Outcome.Failure(ErrorCause.InvalidResponse)
            else -> Outcome.Failure(ApiErrorMapper.fromCode(code), error)
        }
    }

    override suspend fun sendOtp(phone: String): Outcome<Unit> =
        apiCall(TAG) {
            val r = api.sendOtp(SendOtpRequest(phone = phone))
            val url = r.url
            when {
                r.success -> Outcome.Success(Unit)
                !url.isNullOrBlank() -> Outcome.Failure(ErrorCause.AccountDeletionRequired(url), r.error)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    override suspend fun verifyOtp(
        phone: String,
        code: String
    ): Outcome<Session> =
        apiCall(TAG) {
            val r = api.verifyOtp(VerifyOtpRequest(phone = phone, otp_code = code))
            val data = r.data
            when {
                r.success && data != null -> Outcome.Success(data.toSession())
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    override suspend fun register(data: RegistrationData): Outcome<UserInfo> =
        apiCall(TAG) {
            val r = api.registerUser(data.toRequest())
            val body = r.data
            when {
                r.success && body != null -> Outcome.Success(body.toUserInfo())
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    private companion object {const val TAG = "AuthRepository"}
}