package uz.tikoncha_parent.presentation.live

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.VideoTrack

/** Remote video renderer for a webrtc-kmp [VideoTrack] (platform surface). */
@Composable
expect fun LiveVideo(videoTrack: VideoTrack, modifier: Modifier = Modifier)
