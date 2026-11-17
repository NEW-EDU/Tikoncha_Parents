package uz.tikoncha_parent.presentation.live

import androidx.compose.runtime.Immutable
import com.shepeliev.webrtckmp.VideoStreamTrack
import uz.tikoncha_parent.data.remote.model.live.GeoPayload

/**
 * Idle — hali qönğiroq qilinmagan (tasdiq oynasi); Ringing — bola telefoni jiringlayapti (u hali rozi bölmagan);
 * Connecting — bola rozi böldi, video ulanmoqda; Active — video bor; Ended — ekran yopiladi; Error — [ParentLiveState.failure].
 */
enum class LivePhase { Idle, Ringing, Connecting, Active, Ended, Error }

/** Nega körinmadi — oddiy söz bilan körsatiladi, keyin ota-ona özi yopadi. */
enum class LiveFailure { CONNECT, DECLINED, NO_ANSWER }

@Immutable
data class ParentLiveState(
    val phase: LivePhase = LivePhase.Idle,
    val childUserId: String = "",
    val geo: GeoPayload? = null,
    val videoTrack: VideoStreamTrack? = null,
    /** Texnik sabab (log uçun) — ekranda körsatilmaydi. */
    val error: String? = null,
    val failure: LiveFailure? = null,
)

sealed interface ParentLiveEvent {
    data class SetChildId(val id: String) : ParentLiveEvent
    data class RingChild(val childUserId: String) : ParentLiveEvent
    data class JoinSession(val sessionId: String, val childUserId: String) : ParentLiveEvent
    data object End : ParentLiveEvent
}
