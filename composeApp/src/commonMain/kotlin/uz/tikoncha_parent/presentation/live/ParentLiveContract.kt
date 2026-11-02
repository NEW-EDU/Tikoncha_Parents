package uz.tikoncha_parent.presentation.live

import androidx.compose.runtime.Immutable
import com.shepeliev.webrtckmp.VideoStreamTrack
import uz.tikoncha_parent.data.remote.model.live.GeoPayload

enum class LivePhase { Idle, Ringing, Connecting, Active, Ended, Error }

@Immutable
data class ParentLiveState(
    val phase: LivePhase = LivePhase.Idle,
    val childUserId: String = "",
    val geo: GeoPayload? = null,
    val videoTrack: VideoStreamTrack? = null,
    val error: String? = null,
)

sealed interface ParentLiveEvent {
    data class SetChildId(val id: String) : ParentLiveEvent
    data class RingChild(val childUserId: String) : ParentLiveEvent
    data class JoinSession(val sessionId: String, val childUserId: String) : ParentLiveEvent
    data object End : ParentLiveEvent
}
