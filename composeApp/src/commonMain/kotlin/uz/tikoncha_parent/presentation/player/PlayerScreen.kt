@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package uz.tikoncha_parent.presentation.player

import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

/**
 * Qayta ishlatiladigan video player Composable.
 *
 * [screenModel]'ni caller (Voyager Screen) yaratadi:
 * `val playerModel = koinScreenModel<PlayerScreenModel>()`
 * va shu yerga uzatadi. Lifecycle parent Voyager Screen'iga bog'lanadi.
 */
@Composable
fun PlayerScreen(
    screenModel: PlayerScreenModel,
    videoUrl: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    startPositionMs: Long = 0L,
    primaryColor: Color = Color(0xFFFFFFFF),
    showCloseButton: Boolean = false,
    enableImmersive: Boolean = false,
    onClose: (() -> Unit)? = null,
) {
    val state by screenModel.uiState.collectAsState()
    val immersive = rememberImmersiveController()

    var controlsVisible by remember { mutableStateOf(true) }
    var interactionTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.isPlaying, interactionTick) {
        if (state.isPlaying && controlsVisible) {
            delay(5000)
            controlsVisible = false
        }
    }

    LaunchedEffect(videoUrl) {
        if (videoUrl.isNotBlank()) {
            screenModel.onEvent(
                PlayerEvent.Load(
                    url = videoUrl,
                    autoPlay = true,
                    startPositionMs = startPositionMs
                )
            )
        }
    }

    LaunchedEffect(state.shouldClose) {
        if (state.shouldClose) {
            screenModel.consumeClose()
            onClose?.invoke()
        }
    }

    DisposableEffect(enableImmersive) {
        if (enableImmersive) immersive.enter()
        onDispose {
            if (enableImmersive) immersive.exit()
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        controlsVisible = !controlsVisible
                        if (controlsVisible) interactionTick++
                    }
                )
            }
    ) {
        VideoSurface(
            engine = screenModel.engine,
            modifier = Modifier.fillMaxSize()
        )

        PlayerControlsOverlay(
            modifier = Modifier.fillMaxSize(),
            state = state,
            event = screenModel::onEvent,
            controlsVisible = controlsVisible,
            primaryColor = primaryColor,
            showCloseButton = showCloseButton && onClose != null,
            onUserInteracted = {
                controlsVisible = true
                interactionTick++
            },
            onClose = {
                if (state.isPlaying) screenModel.onEvent(PlayerEvent.TogglePlayPause)
                onClose?.invoke()
            }
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(color = Color.White)
            }
        }
    }
}