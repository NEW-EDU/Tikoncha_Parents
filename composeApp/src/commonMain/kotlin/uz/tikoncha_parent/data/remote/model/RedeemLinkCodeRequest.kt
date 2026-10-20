package uz.tikoncha_parent.data.remote.model

import kotlinx.serialization.Serializable

/** POST users/link-code/redeem — farzand yaratgan 8 xonali telefonsiz ulanish kodi. */
@Serializable
data class RedeemLinkCodeRequest(
    val code: String
)
