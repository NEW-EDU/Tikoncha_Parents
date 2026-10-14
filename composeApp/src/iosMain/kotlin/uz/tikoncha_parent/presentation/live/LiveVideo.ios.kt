package uz.tikoncha_parent.presentation.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.shepeliev.webrtckmp.VideoTrack

/**
 * iOS renderer — STUB (follow-up). Real impl: RTCMTLVideoView via UIKitView once the
 * WebRTC cocoapods pod is wired. The Android parent viewer is functional now.
 */
@Composable
actual fun LiveVideo(videoTrack: VideoTrack, modifier: Modifier) {
    Box(modifier.fillMaxSize().background(Color.Black))
}
