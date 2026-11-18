package uz.tikoncha_parent.platform

import uz.tikoncha_parent.platform.KmpLogger

/**
 * NSLog işlatilmaydi: birinçi argumenti — FORMAT satri. Matnni şu yerga qöysak, undagi "%" ("15%", URL'dagi
 * "%D0%B0", yordamçi javobi) format belgisi deb öqiladi va ilova yiqiladi (SIGBUS, 2026-11-18, iPhone);
 * Kotlin satrini "%@" ga argument qilib berish ham işlamaydi (NSString emas — SIGTRAP). println — formatsiz,
 * Xcode / devicectl konsolida körinadi.
 */
actual object Logger : KmpLogger {
    actual override fun d(tag: String, message: String) {
        println("DEBUG: $tag: $message")
    }

    actual override fun e(
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        // Turi va stek ham — xabarsiz istisnoning sababi körinsin
        println("ERROR: $tag: $message ${throwable?.stackTraceToString()?.take(4000) ?: ""}")
    }
}
