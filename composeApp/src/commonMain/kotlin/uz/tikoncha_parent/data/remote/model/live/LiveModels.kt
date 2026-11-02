package uz.tikoncha_parent.data.remote.model.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Live modul — ota-ona (viewer) tomoni. Roziliksiz bo'lmaydigan, uchdan-uchiga jonli
 * A/V + GPS sessiyalari. Server media/joylashuvni ko'rmaydi (P2P WebRTC DTLS-SRTP);
 * bu modellar faqat signalizatsiya (SDP/ICE) + sessiya metama'lumotini tashiydi.
 */

// ── TURN credentials (GET /live/turn-credentials) ──
@Serializable
data class TurnCredentialsDto(
    val username: String,
    val credential: String,
    val ttl_seconds: Int = 0,
    val urls: List<String> = emptyList(),
)

@Serializable
data class TurnCredentialsResponse(
    val success: Boolean = false,
    val data: TurnCredentialsDto? = null,
    val error: String? = null,
    val code: Int = 0,
)

// ── Session (POST /live/sessions, /respond, /cancel, /end) ──
@Serializable
data class LiveSessionDto(
    val id: String,
    val kind: String,                 // "SOS" | "PARENT_REQUEST"
    val status: String,
    val initiator_user_id: String = "",
    val target_user_id: String = "",
    val child_user_id: String = "",
    val ended_at: String? = null,
    val end_reason: String? = null,
    val duration_seconds: Int? = null,
)

@Serializable
data class SessionInitiateRequest(
    val kind: String,
    val child_user_id: String? = null,
    val target_user_id: String? = null,
)

@Serializable
data class SessionInitiateData(
    val session: LiveSessionDto,
    val target_online: Boolean = false,
)

@Serializable
data class SessionInitiateResponse(
    val success: Boolean = false,
    val data: SessionInitiateData? = null,
    val error: String? = null,
    val code: Int = 0,
)

@Serializable
data class SessionResponse(
    val success: Boolean = false,
    val data: LiveSessionDto? = null,
    val error: String? = null,
    val code: Int = 0,
)

// ── Identity keys (POST /live/identity-keys, GET /live/identity-keys/{peer}) ──
@Serializable
data class IdentityKeyRequest(
    val device_uid: String,
    val public_key: String,
    val algorithm: String = "ed25519",
)

@Serializable
data class IdentityKeyData(
    val public_key: String? = null,
    val device_uid: String? = null,
    val algorithm: String? = null,
    val active: Boolean? = null,
)

@Serializable
data class IdentityKeyResponse(
    val success: Boolean = false,
    val data: IdentityKeyData? = null,
    val error: String? = null,
    val code: Int = 0,
)

@Serializable
data class PeerKeyDto(
    val public_key: String,
    val device_uid: String? = null,
    val algorithm: String? = null,
    val active: Boolean? = null,
    val user_id: String? = null,
)

@Serializable
data class PeerKeysData(
    val peer_user_id: String? = null,
    val keys: List<PeerKeyDto> = emptyList(),
)

@Serializable
data class PeerKeysResponse(
    val success: Boolean = false,
    val data: PeerKeysData? = null,
    val error: String? = null,
    val code: Int = 0,
)

// ── Signalling frames (WS /live/ws) ──
@Serializable
data class LiveOutFrame(
    val type: String,
    val session_id: String? = null,
    val payload: JsonElement? = null,
    val signature: String? = null,
)

@Serializable
data class LiveInFrame(
    val type: String,
    val session_id: String? = null,
    val status: String? = null,
    val kind: String? = null,
    val from: String? = null,
    @SerialName("child_user_id") val childUserId: String? = null,
    val online: Boolean? = null,
    val error: String? = null,
    val detail: String? = null,
    val payload: JsonElement? = null,
    val signature: String? = null,
)

@Serializable
data class SdpPayload(val type: String, val sdp: String)

@Serializable
data class IcePayload(
    val candidate: String,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int = 0,
)

/** Location received over the P2P "geo" data channel (never the server). */
@Serializable
data class GeoPayload(
    val lat: Double,
    val lng: Double,
    val accuracy: Float? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val ts: Long = 0,
)
