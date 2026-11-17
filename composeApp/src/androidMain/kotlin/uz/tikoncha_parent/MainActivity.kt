// composeApp/src/androidMain/kotlin/uz/tikoncha_parent/MainActivity.kt
package uz.tikoncha_parent

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.android.inject
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.remote.telegram.TelegramConfig
import uz.tikoncha_parent.domain.model.DeepLink
import uz.tikoncha_parent.data.remote.social.GoogleSignInCoordinator
import uz.tikoncha_parent.data.remote.telegram.TelegramLoginCoordinator
import uz.tikoncha_parent.platform.push.AndroidDeepLinkParser
import uz.tikoncha_parent.presentation.push.FcmTokenRegister
import uz.tikoncha_parent.presentation.push.PushCoordinator
import uz.tikoncha_parent.presentation.push.PushPlatform

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val coordinator: PushCoordinator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)
        // Faqat debug: "server öçdi" sinovi —
        //   adb shell am start -n uz.tikoncha.parent/uz.tikoncha_parent.MainActivity --es simulate_server_down 1   (öçiriş: 0)
        if (uz.tikoncha_parent.platform.BuildConfig.isDebug) {
            intent?.getStringExtra("simulate_server_down")?.let {
                uz.tikoncha_parent.data.local.AppSettings.setOfflineValue("simulate_server_down", it.takeIf { v -> v == "1" })
            }
            // Ekran yordami: hamma ekran yana "birinçi kiriş" (avto-tuşuntiriş, "silkiting" maslahati) —
            //   adb shell am start -S -n uz.tikoncha.parent/uz.tikoncha_parent.MainActivity --es reset_help 1
            if (intent?.getStringExtra("reset_help") != null) {
                uz.tikoncha_parent.presentation.voice_help.VoiceHelpPrefs.resetFirstVisit()
            }
        }

        TelegramLoginCoordinator.bindActivity(this)
        GoogleSignInCoordinator.bindActivity(this)
        handleTelegramIntent(intent)

        PushPlatform.bind(this)
        PushPlatform.initialize()
        // Bildirishnoma ruxsati bu yerda so'ralmaydi — tanishtiruvdagi alohida ekranda (NotificationPermissionScreen)

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d(TAG, "token=$token")
            AppSettings.fcmToken = token
            FcmTokenRegister.submit(token)
        }

        // Cold start: UI hali tayyor emas -> enqueue (App() drain qiladi)
        AndroidDeepLinkParser.parse(intent)?.let { coordinator.onTapped(it, uiReady = false) }
        permissionOffLinkFromExtras(intent)?.let { coordinator.onTapped(it, uiReady = false) }

        intent?.data = null
        setIntent(intent)

        setContent { App() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleTelegramIntent(intent)
        // Warm start: UI tayyor -> darhol ochish
        AndroidDeepLinkParser.parse(intent)?.let { coordinator.onTapped(it, uiReady = true) }
        permissionOffLinkFromExtras(intent)?.let { coordinator.onTapped(it, uiReady = true) }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PushPlatform.onPermissionResult(requestCode, grantResults)
    }

    override fun onDestroy() {
        TelegramLoginCoordinator.unbindActivity(this)
        GoogleSignInCoordinator.unbindActivity(this)
        super.onDestroy()
    }

    /**
     * PERMISSION_OFF push `notification` bloki bilan keladi: ilova fonda bölsa bannerni tizim özi körsatadi va
     * bosilganda FCM `data` kalitlari intent extras'ida keladi (deep-link URI yöq). Bir marta işlatiladi.
     */
    private fun permissionOffLinkFromExtras(intent: Intent?): DeepLink? {
        intent ?: return null
        if (intent.getStringExtra("type") != "PERMISSION_OFF") return null
        val childId = intent.getStringExtra("child_user_id")?.takeIf { it.isNotBlank() }
        intent.removeExtra("type")
        return DeepLink.PermissionOff(childUserId = childId)
    }

    private fun handleTelegramIntent(intent: Intent?) {
        val uri: Uri = intent?.data ?: return
        if (TelegramConfig.isTelegramHost(uri.host)) {
            TelegramLoginCoordinator.handleResponse(uri)
        }
    }
}