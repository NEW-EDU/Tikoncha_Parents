package uz.tikoncha_parent.presentation.live

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.IceConnectionState
import com.shepeliev.webrtckmp.IceServer
import com.shepeliev.webrtckmp.OfferAnswerOptions
import com.shepeliev.webrtckmp.PeerConnection
import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.SessionDescriptionType
import com.shepeliev.webrtckmp.VideoStreamTrack
import com.shepeliev.webrtckmp.onDataChannel
import com.shepeliev.webrtckmp.onIceCandidate
import com.shepeliev.webrtckmp.onIceConnectionStateChange
import com.shepeliev.webrtckmp.onTrack
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import uz.tikoncha_parent.core.crypto.LiveSigner
import uz.tikoncha_parent.data.remote.LiveApiService
import uz.tikoncha_parent.data.remote.LiveSignalingService
import uz.tikoncha_parent.data.remote.model.live.GeoPayload
import uz.tikoncha_parent.data.remote.model.live.IcePayload
import uz.tikoncha_parent.data.remote.model.live.IdentityKeyRequest
import uz.tikoncha_parent.data.remote.model.live.LiveInFrame
import uz.tikoncha_parent.data.remote.model.live.LiveOutFrame
import uz.tikoncha_parent.data.remote.model.live.SdpPayload
import uz.tikoncha_parent.data.remote.model.live.TurnCredentialsDto
import uz.tikoncha_parent.platform.Logger

/**
 * Ota-ona (viewer) tomoni: bolani chaqiradi (parent request) yoki SOS qabul qiladi,
 * bola'ning offer'iga javob (answer) beradi, ICE almashadi, video track'ni + GPS'ni oladi.
 * Imzolar Ed25519 bilan uchdan-uchiga tekshiriladi ([LiveSigner]).
 */
class ParentLiveScreenModel(
    private val api: LiveApiService,
    private val signaling: LiveSignalingService,
) : StateScreenModel<ParentLiveState>(ParentLiveState()) {

    private val TAG = "ParentLive"
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; isLenient = true }

    private var pc: PeerConnection? = null
    private var sessionId: String? = null
    private var peerKeys: List<String> = emptyList()

    init { listenSignaling() }

    fun onEvent(event: ParentLiveEvent) {
        when (event) {
            is ParentLiveEvent.SetChildId -> mutableState.update { it.copy(childUserId = event.id) }
            is ParentLiveEvent.RingChild -> ringChild(event.childUserId)
            ParentLiveEvent.End -> end()
        }
    }

    private fun ringChild(childId: String) {
        if (childId.isBlank() || sessionId != null) return
        mutableState.update { it.copy(phase = LivePhase.Ringing, childUserId = childId, error = null) }
        screenModelScope.launch {
            // Identlik: o'z kalitimizni ro'yxatdan o'tkazamiz + bola'ning kalitlarini olamiz
            LiveSigner.ensure()
            runCatching {
                api.registerIdentityKey(IdentityKeyRequest(device_uid = LiveSigner.deviceUid, public_key = LiveSigner.publicKeyB64()))
            }
            peerKeys = runCatching { api.peerKeys(childId).data?.keys?.map { it.public_key } }.getOrNull() ?: emptyList()

            val turn = runCatching { api.turnCredentials() }.getOrNull()?.takeIf { it.success }?.data
                ?: return@launch fail("turn_failed")

            val ring = runCatching { api.ringChild(childId) }.getOrNull()
            val sess = ring?.data?.session ?: return@launch fail(ring?.error ?: "ring_failed")
            sessionId = sess.id

            signaling.connect()
            setupPeer(turn)
            mutableState.update { it.copy(phase = LivePhase.Connecting) }
        }
    }

    private fun setupPeer(turn: TurnCredentialsDto) {
        val iceServers = turn.urls.map { u ->
            if (u.startsWith("turn", ignoreCase = true)) IceServer(listOf(u), turn.username, turn.credential)
            else IceServer(listOf(u))
        }
        val conn = PeerConnection(RtcConfiguration(iceServers = iceServers))
        pc = conn

        screenModelScope.launch { conn.onIceCandidate.collect { sendIce(it) } }
        screenModelScope.launch {
            conn.onTrack.collect { ev ->
                (ev.track as? VideoStreamTrack)?.let { track ->
                    mutableState.update { it.copy(videoTrack = track) }
                }
            }
        }
        screenModelScope.launch {
            conn.onDataChannel.collect { dc ->
                if (dc.label == "geo") screenModelScope.launch {
                    dc.onMessage.collect { bytes ->
                        runCatching { json.decodeFromString<GeoPayload>(bytes.decodeToString()) }
                            .getOrNull()?.let { g -> mutableState.update { it.copy(geo = g) } }
                    }
                }
            }
        }
        screenModelScope.launch {
            conn.onIceConnectionStateChange.collect { st ->
                if (st == IceConnectionState.Connected || st == IceConnectionState.Completed) {
                    mutableState.update { it.copy(phase = LivePhase.Active) }
                }
            }
        }
    }

    private fun listenSignaling() {
        screenModelScope.launch {
            signaling.incoming.collect { f ->
                if (f.session_id != null && sessionId != null && f.session_id != sessionId) return@collect
                when (f.type) {
                    "offer" -> onOffer(f)
                    "ice" -> onRemoteIce(f)
                    "ring" -> if (sessionId == null) sessionId = f.session_id
                    "state" -> when (f.status) {
                        "ENDED", "DECLINED", "TIMED_OUT", "CANCELLED", "FAILED" -> finish(LivePhase.Ended)
                        else -> {}
                    }
                    "error" -> Logger.e(TAG, "signalling error: ${f.error}")
                }
            }
        }
    }

    private suspend fun onOffer(f: LiveInFrame) {
        val sid = sessionId ?: return
        val payload = f.payload ?: return
        if (!LiveSigner.verify("offer", sid, payload, f.signature, peerKeys)) {
            Logger.e(TAG, "offer signature INVALID — dropped"); return
        }
        val sdp = runCatching { json.decodeFromJsonElement<SdpPayload>(payload) }.getOrNull()?.sdp ?: return
        val conn = pc ?: return
        runCatching {
            conn.setRemoteDescription(SessionDescription(SessionDescriptionType.Offer, sdp))
            val answer = conn.createAnswer(OfferAnswerOptions())
            conn.setLocalDescription(answer)
            val outPayload = json.encodeToJsonElement(SdpPayload("answer", answer.sdp))
            val sig = LiveSigner.sign("answer", sid, outPayload)
            signaling.send(LiveOutFrame(type = "answer", session_id = sid, payload = outPayload, signature = sig))
        }.onFailure { Logger.e(TAG, "onOffer failed", it) }
    }

    private suspend fun onRemoteIce(f: LiveInFrame) {
        val sid = sessionId ?: return
        val payload = f.payload ?: return
        if (!LiveSigner.verify("ice", sid, payload, f.signature, peerKeys)) return
        val p = runCatching { json.decodeFromJsonElement<IcePayload>(payload) }.getOrNull() ?: return
        runCatching { pc?.addIceCandidate(IceCandidate(p.sdpMid ?: "", p.sdpMLineIndex, p.candidate)) }
    }

    private suspend fun sendIce(c: IceCandidate) {
        val sid = sessionId ?: return
        val payload = json.encodeToJsonElement(IcePayload(candidate = c.candidate, sdpMid = c.sdpMid, sdpMLineIndex = c.sdpMLineIndex))
        val sig = LiveSigner.sign("ice", sid, payload)
        signaling.send(LiveOutFrame(type = "ice", session_id = sid, payload = payload, signature = sig))
    }

    private fun end() {
        val sid = sessionId
        sessionId = null
        runCatching { pc?.close() }
        pc = null
        screenModelScope.launch {
            if (sid != null) {
                runCatching { signaling.send(LiveOutFrame(type = "end", session_id = sid)) }
                runCatching { api.end(sid) }
            }
            signaling.disconnect()
        }
        finish(LivePhase.Ended)
    }

    private fun fail(reason: String) {
        mutableState.update { it.copy(phase = LivePhase.Error, error = reason) }
    }

    private fun finish(phase: LivePhase) {
        mutableState.update { it.copy(phase = phase, videoTrack = null) }
    }

    override fun onDispose() {
        super.onDispose()
        runCatching { pc?.close() }
        signaling.disconnect()
    }
}
