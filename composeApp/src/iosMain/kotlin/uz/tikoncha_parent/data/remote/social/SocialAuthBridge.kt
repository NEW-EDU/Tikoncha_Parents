package uz.tikoncha_parent.data.remote.social

import uz.tikoncha_parent.domain.model.auth.SocialAuthResult
import uz.tikoncha_parent.domain.model.auth.SocialProvider

/** Swift'da (iosApp/SocialAuthBridgeImpl.swift) amalga oshiriladi: AuthenticationServices — Apple va Google. */
interface SocialAuthBridge {
    fun startApple(callback: SocialAuthBridgeCallback)
    fun startGoogle(clientId: String, callback: SocialAuthBridgeCallback)
}

interface SocialAuthBridgeCallback {
    fun onSuccess(token: String, fullName: String?)
    fun onError(message: String)
    fun onCancel()
}

object SocialAuthBridgeProvider {
    var bridge: SocialAuthBridge? = null
}

private fun callback(provider: SocialProvider) = object : SocialAuthBridgeCallback {
    override fun onSuccess(token: String, fullName: String?) =
        SocialAuthBus.emitResult(SocialAuthResult.Success(provider, token, fullName))
    override fun onError(message: String) = SocialAuthBus.emitResult(SocialAuthResult.Error(provider, message))
    override fun onCancel() = SocialAuthBus.emitResult(SocialAuthResult.Cancelled(provider))
}

actual fun isAppleLoginAvailable(): Boolean = SocialAuthBridgeProvider.bridge != null

actual fun startAppleLogin(): Boolean {
    val bridge = SocialAuthBridgeProvider.bridge ?: return false
    bridge.startApple(callback(SocialProvider.APPLE))
    return true
}

actual fun startGoogleLogin(): Boolean {
    val bridge = SocialAuthBridgeProvider.bridge ?: return false
    bridge.startGoogle(GoogleOAuthConfig.IOS_CLIENT_ID, callback(SocialProvider.GOOGLE))
    return true
}
