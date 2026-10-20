package uz.tikoncha_parent.data.remote.social

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import uz.tikoncha_parent.domain.model.auth.SocialAuthResult
import uz.tikoncha_parent.domain.model.auth.SocialProvider
import java.lang.ref.WeakReference

/**
 * "Sign in with Google" tugmasi oqimi (Credential Manager). ID token `aud` — Web client
 * ([GoogleOAuthConfig.WEB_CLIENT_ID]); backend uni tekshiradi. Android OAuth mijozlari Firebase'da
 * paket nomi + SHA-1 (Play imzo, upload, debug) bilan ro'yxatdan o'tgan.
 */
object GoogleSignInCoordinator {
    private var activityRef: WeakReference<Activity>? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun bindActivity(activity: Activity) { activityRef = WeakReference(activity) }

    fun unbindActivity(activity: Activity) {
        if (activityRef?.get() === activity) activityRef = null
    }

    fun start(): Boolean {
        val activity = activityRef?.get() ?: return false
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(GoogleOAuthConfig.WEB_CLIENT_ID).build())
            .build()
        scope.launch {
            val result = try {
                val credential = CredentialManager.create(activity).getCredential(activity, request).credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    SocialAuthResult.Success(SocialProvider.GOOGLE, GoogleIdTokenCredential.createFrom(credential.data).idToken)
                } else {
                    SocialAuthResult.Error(SocialProvider.GOOGLE, "Unexpected credential")
                }
            } catch (_: GetCredentialCancellationException) {
                SocialAuthResult.Cancelled(SocialProvider.GOOGLE)
            } catch (e: GetCredentialException) {
                SocialAuthResult.Error(SocialProvider.GOOGLE, e.message ?: e.type)
            } catch (e: Exception) {
                SocialAuthResult.Error(SocialProvider.GOOGLE, e.message ?: "Google sign-in failed")
            }
            SocialAuthBus.emitResult(result)
        }
        return true
    }
}
