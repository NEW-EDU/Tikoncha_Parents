package uz.tikoncha_parent.platform

expect fun isLocationServiceEnabled(): Boolean
expect fun openLocationSettings()

expect fun openAppSettings()

/** Joylashuvni xarita ilovasida ochadi. iOS: o'rnatilganlaridan tanlash (Yandex / Google / Apple). */
expect fun openInExternalMaps(lat: Double, lng: Double, label: String, cancelText: String = "Cancel")
expect fun shareLocation(lat: Double, lng: Double, name: String)