package uz.tikoncha_parent.data.repository

import kotlinx.coroutines.flow.StateFlow
import uz.tikoncha_parent.data.remote.social.SocialAuthBus
import uz.tikoncha_parent.data.remote.social.isAppleLoginAvailable
import uz.tikoncha_parent.data.remote.social.startAppleLogin
import uz.tikoncha_parent.data.remote.social.startGoogleLogin
import uz.tikoncha_parent.domain.model.auth.SocialAuthResult
import uz.tikoncha_parent.domain.model.auth.SocialProvider
import uz.tikoncha_parent.domain.repository.SocialAuthRepository

class SocialAuthRepositoryImpl : SocialAuthRepository {
    override val pendingResult: StateFlow<SocialAuthResult?> get() = SocialAuthBus.pendingResult

    override fun isAppleAvailable(): Boolean = isAppleLoginAvailable()

    override fun start(provider: SocialProvider): Boolean {
        SocialAuthBus.clearPending()
        return when (provider) {
            SocialProvider.APPLE -> startAppleLogin()
            SocialProvider.GOOGLE -> startGoogleLogin()
        }
    }

    override fun consumePending() = SocialAuthBus.clearPending()
}
