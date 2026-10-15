// composeApp/src/androidMain/kotlin/uz/tikoncha_parent/platform/push/DeepLinks.kt
package uz.tikoncha_parent.platform.push

import android.net.Uri
import uz.tikoncha_parent.domain.model.DeepLink

object DeepLinks {
    const val SCHEME = "tikoncha_parent"

    object Host {
        const val CHAT = "chat"
        const val PARENTAL_REQUEST = "parental_request"
        const val STRICT_DISABLE = "strict_disable"
        const val LIVE = "live"
        const val HOME = "home"
    }

    object Param {
        const val CHAT_ID = "chatId"
        const val CHAT_TITLE = "chatTitle"
        const val SESSION_ID = "sessionId"
        const val CHILD_ID = "childId"
        const val KIND = "kind"
    }

    fun uri(link: DeepLink): Uri = when (link) {
        is DeepLink.Chat -> build(Host.CHAT) {
            appendQueryParameter(Param.CHAT_ID, link.chatId)
            appendQueryParameter(Param.CHAT_TITLE, link.chatTitle ?: "")
        }
        DeepLink.ParentalRequest -> build(Host.PARENTAL_REQUEST)
        DeepLink.StrictDisable -> build(Host.STRICT_DISABLE)
        is DeepLink.Live -> build(Host.LIVE) {
            appendQueryParameter(Param.SESSION_ID, link.sessionId)
            appendQueryParameter(Param.CHILD_ID, link.childId)
            link.kind?.let { appendQueryParameter(Param.KIND, it) }
        }
        is DeepLink.General -> build(Host.HOME)
    }

    private fun build(host: String, params: Uri.Builder.() -> Unit = {}): Uri =
        Uri.Builder().scheme(SCHEME).authority(host).apply(params).build()
}