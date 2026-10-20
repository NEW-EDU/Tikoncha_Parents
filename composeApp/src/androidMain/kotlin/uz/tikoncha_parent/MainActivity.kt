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

        TelegramLoginCoordinator.bindActivity(this)
        GoogleSignInCoordinator.bindActivity(this)
        handleTelegramIntent(intent)

        PushPlatform.bind(this)
        PushPlatform.initialize()
        PushPlatform.requestNotificationPermissionIfNeeded()

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d(TAG, "token=$token")
            AppSettings.fcmToken = token
            FcmTokenRegister.submit(token)
        }

        // Cold start: UI hali tayyor emas -> enqueue (App() drain qiladi)
        AndroidDeepLinkParser.parse(intent)?.let { coordinator.onTapped(it, uiReady = false) }

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
    }

    override fun onDestroy() {
        TelegramLoginCoordinator.unbindActivity(this)
        GoogleSignInCoordinator.unbindActivity(this)
        super.onDestroy()
    }

    private fun handleTelegramIntent(intent: Intent?) {
        val uri: Uri = intent?.data ?: return
        if (TelegramConfig.isTelegramHost(uri.host)) {
            TelegramLoginCoordinator.handleResponse(uri)
        }
    }
}