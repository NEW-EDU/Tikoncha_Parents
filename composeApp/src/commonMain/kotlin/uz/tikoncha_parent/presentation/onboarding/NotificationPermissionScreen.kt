package uz.tikoncha_parent.presentation.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.davom_etish
import tikoncha_parents.composeapp.generated.resources.mascot_guide
import tikoncha_parents.composeapp.generated.resources.ob_notif_body
import tikoncha_parents.composeapp.generated.resources.ob_notif_title
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.presentation.login.LoginScreen
import uz.tikoncha_parent.presentation.push.PushPlatform

/**
 * Bildirishnoma ruxsatidan oldingi alohida ekran. App Store 5.1.1(iv): tugma faqat "Davom etish",
 * "Ruxsat berish"/"Keyinroq" yo'q — tizim oynasi har doim ko'rsatiladi, javobdan qat'i nazar kirishga o'tiladi.
 */
class NotificationPermissionScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var busy by remember { mutableStateOf(false) }
        val play = rememberScenePlay(active = true, durationMs = 2200)

        ForceLightSystemIcons()

        NightBackdrop(glowX = 0.5f) {
            BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                val mascot = min(maxHeight * 0.18f, 150.dp)
                Column(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(40.dp))
                    NotificationStack(play, Modifier.fillMaxWidth())
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        MascotActor(pose = Res.drawable.mascot_guide, size = mascot)
                    }
                    KineticTitle(stringResource(Res.string.ob_notif_title), play)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(Res.string.ob_notif_body),
                        style = bodyStyle(),
                        modifier = Modifier.padding(horizontal = 4.dp).graphicsLayer {
                            val t = easeOut(stage(play, 0.22f, 0.55f))
                            alpha = t
                            translationY = (1f - t) * 10.dp.toPx()
                        },
                    )
                    Spacer(Modifier.height(28.dp))
                    CtaButton(
                        text = stringResource(Res.string.davom_etish),
                        enabled = !busy,
                        onClick = {
                            busy = true
                            scope.launch {
                                PushPlatform.requestNotificationPermission()
                                AppSettings.onboardingDone = true
                                navigator.replaceAll(LoginScreen())
                            }
                        },
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
