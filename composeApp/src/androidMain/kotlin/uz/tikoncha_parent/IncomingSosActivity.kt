package uz.tikoncha_parent

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import uz.tikoncha_parent.domain.model.DeepLink
import uz.tikoncha_parent.platform.push.DeepLinks

/**
 * Full-screen incoming-SOS "ring" screen for the parent — woken by a full-screen-intent
 * notification (so it takes over the screen even when locked). "Ko'rish" opens the live
 * viewer (via the DeepLink.Live URI into MainActivity); "Yopish" dismisses.
 */
class IncomingSosActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        val childId = intent.getStringExtra(EXTRA_CHILD_ID)
        val kind = intent.getStringExtra(EXTRA_KIND)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "SOS"
        val body = intent.getStringExtra(EXTRA_BODY) ?: ""
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, 0)
        if (sessionId.isNullOrBlank() || childId.isNullOrBlank()) { finish(); return }

        setContent {
            MaterialTheme {
                SosRingScreen(
                    title = title,
                    body = body,
                    onWatch = {
                        cancelNotif(notifId)
                        val uri = DeepLinks.uri(DeepLink.Live(sessionId, childId, kind))
                        startActivity(
                            Intent(Intent.ACTION_VIEW, uri, this, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                        finishAndRemoveTask()
                    },
                    onDismiss = { cancelNotif(notifId); finishAndRemoveTask() },
                )
            }
        }
    }

    private fun cancelNotif(id: Int) {
        runCatching { NotificationManagerCompat.from(this).cancel(id) }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    companion object {
        const val EXTRA_SESSION_ID = "sos.session_id"
        const val EXTRA_CHILD_ID = "sos.child_id"
        const val EXTRA_KIND = "sos.kind"
        const val EXTRA_TITLE = "sos.title"
        const val EXTRA_BODY = "sos.body"
        const val EXTRA_NOTIF_ID = "sos.notif_id"
    }
}

@Composable
private fun SosRingScreen(
    title: String,
    body: String,
    onWatch: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1B0D0D)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🚨", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(title, color = Color.White, fontSize = 24.sp, textAlign = TextAlign.Center)
        if (body.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(body, color = Color(0xFFBDBDBD), fontSize = 15.sp, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onWatch,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White,
            ),
        ) { Text("Ko'rish") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Yopish", color = Color.White)
        }
    }
}
