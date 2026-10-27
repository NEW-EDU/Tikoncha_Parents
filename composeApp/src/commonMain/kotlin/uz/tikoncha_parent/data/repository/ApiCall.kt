package uz.tikoncha_parent.data.repository

import uz.tikoncha_parent.data.remote.ServerReachability

import uz.tikoncha_parent.platform.Logger
import kotlinx.coroutines.CancellationException
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.domain.model.app_error.Outcome

/**
 * Tarmoq chaqiruvi uchun yagona try/catch.
 *
 * Blok ichida Outcome qaytaring — muvaffaqiyat ham, server xatosi ham.
 * Bu funksiya faqat KUTILMAGAN istisnolarni ushlaydi.
 */

suspend inline fun <T> apiCall(
    tag: String,
    block: () -> Outcome<T>,
): Outcome<T> = try {
    block()
} catch (t: Throwable) {
    if (t is CancellationException) throw t
    Logger.e(tag, t.message ?: "api error", t)
    Outcome.Failure(ApiErrorMapper.from(t))
}.also { ServerReachability.report(it) }