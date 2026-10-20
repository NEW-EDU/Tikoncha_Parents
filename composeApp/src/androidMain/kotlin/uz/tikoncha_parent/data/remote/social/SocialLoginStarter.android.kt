package uz.tikoncha_parent.data.remote.social

// Sign in with Apple Android'da ko'rsatilmaydi (Google bor)
actual fun isAppleLoginAvailable(): Boolean = false

actual fun startAppleLogin(): Boolean = false

actual fun startGoogleLogin(): Boolean = GoogleSignInCoordinator.start()
