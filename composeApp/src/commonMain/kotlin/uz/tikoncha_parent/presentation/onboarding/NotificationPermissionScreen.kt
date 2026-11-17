package uz.tikoncha_parent.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalDensity
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
import tikoncha_parents.composeapp.generated.resources.ob_notif_enable
import tikoncha_parents.composeapp.generated.resources.ob_notif_title
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.platform.isIos
import uz.tikoncha_parent.presentation.login.LoginScreen
import uz.tikoncha_parent.presentation.push.PushPlatform

/**
 * Bildirishnoma ruxsatidan oldingi alohida ekran. "Keyinroq" yo'q — tizim oynasi har doim ko'rsatiladi, javobdan
 * qat'i nazar kirishga o'tiladi.
 * Tugma: Android'da aniq "Bildirişnomalarni yoqiş" (bosilganda nima bölişini aytadi). iPhone'da "Davom etiş" qoladi —
 * App Store 5.1.1(iv): tizim oynasidan oldingi öz ekranimizdagi tugma "Continue/Next" kabi bölişi kerak, "yoqiş/ruxsat
 * beriş" emas (aks holda rad etiladi).
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
                // Katta şriftda tipratikon kiçrayadi; baribir sığmasa ekran aylantiriladi (matn va tugma kesilmaydi)
                val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
                val mascot = (min(maxHeight * 0.18f, 150.dp) / fontScale).coerceAtLeast(64.dp)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // Joy yetsa — bildirişnomalar tepada, tipratikon örtada, matn va tugma pastda (avvalgidek)
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.height(40.dp))
                        ArtFontScale { NotificationStack(play, Modifier.fillMaxWidth()) }
                    }
                    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        MascotActor(pose = Res.drawable.mascot_guide, size = mascot)
                    }
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
                            text = stringResource(if (isIos()) Res.string.davom_etish else Res.string.ob_notif_enable),
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
}
