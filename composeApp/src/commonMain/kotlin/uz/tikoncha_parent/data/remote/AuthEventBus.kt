package uz.tikoncha_parent.data.remote

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface AuthEvent {
    data object SessionExpired : AuthEvent
}

object AuthEventBus {
    private val _events = MutableSharedFlow<AuthEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    /**
     * Foydalanuvçi özi çiqayotganda (logout / akkauntni öçiriş) "Sessiya tugadi" oynasi kerak emas —
     * ekran özi kiriş sahifasiga ötadi.
     */
    var muted: Boolean = false

    fun emit(event: AuthEvent) {
        if (muted) return
        _events.tryEmit(event)
    }
}

class SessionExpiredException : Exception("Session expired, please login again.")