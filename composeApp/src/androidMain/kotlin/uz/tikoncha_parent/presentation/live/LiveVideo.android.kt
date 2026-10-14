package uz.tikoncha_parent.presentation.live

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.shepeliev.webrtckmp.VideoTrack
import com.shepeliev.webrtckmp.WebRtc
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

@Composable
actual fun LiveVideo(videoTrack: VideoTrack, modifier: Modifier) {
    var renderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    val observer = remember(renderer, videoTrack) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> renderer?.also {
                    it.init(WebRtc.rootEglBase.eglBaseContext, null)
                    runCatching { videoTrack.addSink(it) }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    renderer?.also { r -> runCatching { videoTrack.removeSink(r) } }
                    renderer?.release()
                }
                else -> {}
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, observer) {
        lifecycle.addObserver(observer)
        onDispose {
            renderer?.let { runCatching { videoTrack.removeSink(it) } }
            renderer?.release()
            lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                setScalingType(
                    RendererCommon.ScalingType.SCALE_ASPECT_BALANCED,
                    RendererCommon.ScalingType.SCALE_ASPECT_FIT,
                )
                renderer = this
            }
        },
    )
}
