@file:OptIn(cafe.adriel.voyager.core.annotation.InternalVoyagerApi::class)

package uz.tikoncha_parent.presentation.live

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.live_call_button
import tikoncha_parents.composeapp.generated.resources.live_child_fallback
import tikoncha_parents.composeapp.generated.resources.live_confirm_body
import tikoncha_parents.composeapp.generated.resources.live_confirm_title
import tikoncha_parents.composeapp.generated.resources.live_connecting
import tikoncha_parents.composeapp.generated.resources.live_declined
import tikoncha_parents.composeapp.generated.resources.live_end
import tikoncha_parents.composeapp.generated.resources.live_error_connect
import tikoncha_parents.composeapp.generated.resources.live_no_answer
import tikoncha_parents.composeapp.generated.resources.live_ringing
import tikoncha_parents.composeapp.generated.resources.live_show_on_map
import tikoncha_parents.composeapp.generated.resources.yopish
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.platform.BuildConfig
import uz.tikoncha_parent.platform.openInExternalMaps
import uz.tikoncha_parent.presentation.base.CustomBottomDialog
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * Ota-ona jonli ko'rish ekrani — bolani chaqiradi (parent request) va bola rozi bo'lgach
 * jonli video + ovoz + GPS'ni oladi (P2P WebRTC). Veb-viewer o'rniga native variant.
 *
 * Qönğiroq faqat tasdiqdan keyin ketadi; har bosqichda katta "Bekor qiliş"/"Tugatiş" bor;
 * rad etilsa / javob bölmasa — natija oddiy söz bilan körsatiladi, ekran jim yopilmaydi.
 */
class ParentLiveScreen(
    private val childUserId: String? = null,
    private val joinSessionId: String? = null,
) : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ParentLiveScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.current
        val close: () -> Unit = { navigator?.let { if (it.canPop) it.pop() } }

        val childName = remember(childUserId) {
            AppSettings.children.firstOrNull { it.userId == childUserId }?.name?.takeIf { it.isNotBlank() }
        } ?: stringResource(Res.string.live_child_fallback)

        // Xaritadan kirilgan (farzand tanlangan) — qönğiroqdan oldin bir marta söraladi.
        // Push'dan (SOS / bola özi chaqirgan) kirilganda söralmaydi — bola allaqaçon kutyapti.
        var askToCall by remember { mutableStateOf(joinSessionId.isNullOrBlank() && !childUserId.isNullOrBlank()) }

        LaunchedEffect(childUserId, joinSessionId) {
            when {
                // Opened from an incoming SOS/ring push → join the existing session
                // (the child is already capturing and re-offering).
                !joinSessionId.isNullOrBlank() && !childUserId.isNullOrBlank() ->
                    screenModel.onEvent(ParentLiveEvent.JoinSession(joinSessionId, childUserId))
                // Entered from the map with a child chosen → ring only after the confirmation sheet.
                !childUserId.isNullOrBlank() -> screenModel.onEvent(ParentLiveEvent.SetChildId(childUserId))
            }
        }

        // Ota-ona özi bekor qildi / video tugadi → ekrandan chiqiladi.
        LaunchedEffect(state.phase) {
            if (state.phase == LivePhase.Ended) close()
        }

        val inCall = state.phase == LivePhase.Ringing || state.phase == LivePhase.Connecting ||
            state.phase == LivePhase.Active || state.videoTrack != null
        // Orqaga = bekor qiliş (bola telefoni jiringlab qolmasin)
        BackHandler(inCall) { screenModel.onEvent(ParentLiveEvent.End) }

        CustomBottomDialog(
            show = askToCall && state.phase == LivePhase.Idle,
            title = stringResource(Res.string.live_confirm_title),
            message = stringResource(Res.string.live_confirm_body, childName),
            confirmButtonText = stringResource(Res.string.live_call_button),
            dismissButtonText = stringResource(Res.string.bekor_qilish),
            onDismiss = {
                askToCall = false
                close()
            },
            onConfirm = {
                askToCall = false
                screenModel.onEvent(ParentLiveEvent.RingChild(state.childUserId.ifBlank { childUserId.orEmpty() }))
            },
        )

        // Video foni doim qora (kamera kadri atrofida) — mavzu rangiga bog'liq emas
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            val track = state.videoTrack
            if (track != null) {
                LiveVideo(track, modifier = Modifier.fillMaxSize())
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (state.phase) {
                        LivePhase.Ringing, LivePhase.Connecting, LivePhase.Active -> {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = if (state.phase == LivePhase.Ringing) stringResource(Res.string.live_ringing, childName)
                                else stringResource(Res.string.live_connecting),
                                style = AppTypography.titleLgSemiBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                        }
                        LivePhase.Error -> Text(
                            text = when (state.failure) {
                                LiveFailure.DECLINED -> stringResource(Res.string.live_declined)
                                LiveFailure.NO_ANSWER -> stringResource(Res.string.live_no_answer)
                                else -> stringResource(Res.string.live_error_connect)
                            },
                            style = AppTypography.titleLgSemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                        LivePhase.Idle -> if (BuildConfig.isDebug && childUserId.isNullOrBlank()) {
                            // Faqat debug: farzand tanlanmagan holda qöl bilan id kiritib sinash
                            OutlinedTextField(
                                value = state.childUserId,
                                onValueChange = { screenModel.onEvent(ParentLiveEvent.SetChildId(it)) },
                                label = { Text("child user_id (debug)", color = Color.White) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Button(
                                onClick = { screenModel.onEvent(ParentLiveEvent.RingChild(state.childUserId)) },
                                enabled = state.childUserId.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            ) { Text("Ring (debug)") }
                        }
                        LivePhase.Ended -> Unit
                    }
                }
            }

            // Joylaşuv — raqamlar emas, xaritada oçiladigan tugma (status bar / Dynamic Island'dan pastda)
            state.geo?.let { g ->
                val cancelText = stringResource(Res.string.bekor_qilish)
                Row(
                    modifier = Modifier.align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { openInExternalMaps(g.lat, g.lng, childName, cancelText) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.live_show_on_map), style = AppTypography.titleSmSemiBold, color = Color.White)
                }
            }

            // Har bosqichda katta tugma: jiringlayotganda / ulanayotganda — Bekor qiliş, videoda — Tugatiş,
            // natijada — Yopiş
            val bottomButton: Pair<String, Boolean>? = when {
                state.phase == LivePhase.Error -> stringResource(Res.string.yopish) to false
                state.phase == LivePhase.Active || track != null -> stringResource(Res.string.live_end) to true
                state.phase == LivePhase.Ringing || state.phase == LivePhase.Connecting ->
                    stringResource(Res.string.bekor_qilish) to true
                // Tasdiq oynasi yöq (farzand tanlanmagan) — boş qora ekranda qolib ketmasin
                state.phase == LivePhase.Idle && !askToCall -> stringResource(Res.string.yopish) to false
                else -> null
            }
            bottomButton?.let { (label, endsCall) ->
                CustomButtonNew(
                    text = label,
                    onClick = { if (endsCall) screenModel.onEvent(ParentLiveEvent.End) else close() },
                    containerColor = if (endsCall) AppColors.button.accentDanger else AppColors.button.primary,
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp),
                )
            }
        }
    }
}
