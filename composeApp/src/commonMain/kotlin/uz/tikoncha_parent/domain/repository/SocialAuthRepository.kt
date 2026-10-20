package uz.tikoncha_parent.domain.repository

import kotlinx.coroutines.flow.StateFlow
import uz.tikoncha_parent.domain.model.auth.SocialAuthResult
import uz.tikoncha_parent.domain.model.auth.SocialProvider

interface SocialAuthRepository {
    val pendingResult: StateFlow<SocialAuthResult?>
    fun isAppleAvailable(): Boolean
    fun start(provider: SocialProvider): Boolean
    fun consumePending()
}
