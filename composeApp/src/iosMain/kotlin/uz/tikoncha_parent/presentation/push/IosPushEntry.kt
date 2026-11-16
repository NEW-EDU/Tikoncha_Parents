package uz.tikoncha_parent.presentation.push

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.use_case.push.ParseFcmPayloadUseCase

/** Swift (AppDelegate) shu obyektni chaqiradi (eski Kmp*Bridge'lar o'rniga) */
object IosPushEntry : KoinComponent {
    private val coordinator: PushCoordinator by inject()
    private val parse: ParseFcmPayloadUseCase by inject()

    fun submitToken(token: String) {
        AppSettings.fcmToken = token
        FcmTokenRegister.submit(token)
    }

    fun onMessage(payloadRaw: String?) = coordinator.onMessage(payloadRaw)

    fun onTap(payloadRaw: String?) {
        val link = parse(payloadRaw)?.toDeepLink()
        // Sovuq start: UI hali yöq → navbatga (App() oladi). Ilova oçiq/fonda: DeepLinkEffect tinglayapti → darhol.
        coordinator.onTapped(link, uiReady = DeepLinkBus.hasListener)
    }
}