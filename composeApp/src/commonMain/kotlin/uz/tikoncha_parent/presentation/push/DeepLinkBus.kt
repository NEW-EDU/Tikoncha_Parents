package uz.tikoncha_parent.presentation.push

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uz.tikoncha_parent.domain.model.DeepLink

object DeepLinkBus {
    private val _events = MutableSharedFlow<DeepLink>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<DeepLink> = _events

    /** UI tayyor (DeepLinkEffect tinglayapti) — link darhol oçiladi; aks holda PendingDeepLinks'ga. */
    val hasListener: Boolean get() = _events.subscriptionCount.value > 0

    fun open(link: DeepLink) { _events.tryEmit(link) }
}