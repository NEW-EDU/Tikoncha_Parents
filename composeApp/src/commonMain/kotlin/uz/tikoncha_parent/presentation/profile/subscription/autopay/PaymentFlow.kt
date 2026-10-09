package uz.tikoncha_parent.presentation.profile.subscription.autopay

import cafe.adriel.voyager.navigator.Navigator
import uz.tikoncha_parent.presentation.profile.subscription.info.SubscriptionScreen
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen

/**
 * To'lov oqimidan chiqish: Obuna ekrani stekda bo'lsa — unga, aks holda tarif ekranini ochgan joyga.
 * (Tarif → usul → karta → tasdiqlash → natija bir yo'la yopiladi.)
 */
fun leavePaymentFlow(navigator: Navigator) {
    val items = navigator.items
    when {
        items.any { it is SubscriptionScreen } -> navigator.popUntil { it is SubscriptionScreen }
        items.any { it is SubscriptionPaymentScreen } -> {
            navigator.popUntil { it is SubscriptionPaymentScreen }
            navigator.pop()
        }
        else -> navigator.pop()
    }
}
