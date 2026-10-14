@file:OptIn(ExperimentalForeignApi::class)

package uz.tikoncha_parent.presentation.live

import WebRTC.RTCMTLVideoView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.shepeliev.webrtckmp.VideoTrack
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewContentMode

/** iOS renderer — Metal `RTCMTLVideoView` (WebRTC pod) hosted in Compose via UIKitView. */
@Composable
actual fun LiveVideo(videoTrack: VideoTrack, modifier: Modifier) {
    UIKitView(
        factory = {
            RTCMTLVideoView().apply {
                videoContentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                videoTrack.addRenderer(this)
            }
        },
        modifier = modifier,
        onRelease = { videoTrack.removeRenderer(it) },
    )
}
