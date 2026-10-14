package uz.tikoncha_parent.presentation.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel

/**
 * Ota-ona jonli ko'rish ekrani — bolani chaqiradi (parent request) va bola rozi bo'lgach
 * jonli video + ovoz + GPS'ni oladi (P2P WebRTC). Veb-viewer o'rniga native variant.
 */
class ParentLiveScreen(private val childUserId: String? = null) : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ParentLiveScreenModel>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(childUserId) {
            if (!childUserId.isNullOrBlank()) screenModel.onEvent(ParentLiveEvent.SetChildId(childUserId))
        }

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
                        LivePhase.Ringing, LivePhase.Connecting -> {
                            CircularProgressIndicator(color = Color.White)
                            Text(
                                text = if (state.phase == LivePhase.Ringing) "Chaqirilmoqda…" else "Ulanmoqda…",
                                color = Color.White, modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                        LivePhase.Error -> Text(
                            text = "Ulanib bo'lmadi" + (state.error?.let { " ($it)" } ?: ""),
                            color = Color(0xFFE53935), textAlign = TextAlign.Center,
                        )
                        else -> {
                            OutlinedTextField(
                                value = state.childUserId,
                                onValueChange = { screenModel.onEvent(ParentLiveEvent.SetChildId(it)) },
                                label = { Text("Bola user_id", color = Color.White) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Button(
                                onClick = { screenModel.onEvent(ParentLiveEvent.RingChild(state.childUserId)) },
                                enabled = state.childUserId.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            ) { Text("Bolani chaqirish") }
                        }
                    }
                }
            }

            // GPS overlay
            state.geo?.let { g ->
                Box(
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
                        .background(Color(0xCC000000), RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("📍 ${fmt(g.lat)}, ${fmt(g.lng)}", color = Color.White)
                }
            }

            // End button while a session is live/connecting
            if (state.phase == LivePhase.Connecting || state.phase == LivePhase.Active || track != null) {
                Button(
                    onClick = { screenModel.onEvent(ParentLiveEvent.End) },
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                ) { Text("To'xtatish") }
            }
        }
    }

    private fun fmt(v: Double): String {
        val scaled = kotlin.math.round(v * 1_000_000) / 1_000_000
        return scaled.toString()
    }
}
