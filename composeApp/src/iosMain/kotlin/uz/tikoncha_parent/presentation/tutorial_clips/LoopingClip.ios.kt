package uz.tikoncha_parent.presentation.tutorial_clips

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerLooper
import platform.AVFoundation.AVQueuePlayer
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.setMuted
import platform.Foundation.NSURL
import uz.tikoncha_parent.presentation.player.PlayerUIView

private class ClipLoop(uri: String) {
    val player = AVQueuePlayer()
    // AVPlayerLooper o'zi navbatni to'ldirib turadi — klip uzluksiz takrorlanadi. Havolasi saqlanishi şart.
    val looper: AVPlayerLooper? = NSURL.URLWithString(uri)
        ?.let { AVPlayerLooper.playerLooperWithPlayer(player, AVPlayerItem(uRL = it)) }

    init {
        player.setMuted(true)
        player.play()
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LoopingClip(uri: String, modifier: Modifier) {
    val loop = remember(uri) { ClipLoop(uri) }
    DisposableEffect(loop) { onDispose { loop.player.pause(); loop.looper?.disableLooping() } }
    UIKitView(
        modifier = modifier,
        factory = { PlayerUIView(loop.player) },
        update = { },
    )
}
