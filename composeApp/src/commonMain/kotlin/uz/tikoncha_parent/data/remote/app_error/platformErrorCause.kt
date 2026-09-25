package uz.tikoncha_parent.data.remote.app_error

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import uz.tikoncha_parent.domain.model.app_error.ErrorCause

/**
 * Platformaga xos tarmoq istisnolari.
 * Tanimasa null qaytaradi — u holda umumiy mapper Unknown beradi.
 */
expect fun platformErrorCause(t: Throwable): ErrorCause?

object ApiErrorMapper {

    /**
     * ⚠️ TARTIB MUHIM.
     *
     * SocketTimeoutException va ConnectTimeoutException — ikkalasi ham
     * IOException avlodi. Agar IOException yuqorida tursa, timeout'lar
     * hech qachon "Timeout" deb tanilmaydi va hammasi "NoInternet" bo'ladi.
     */
    fun from(t: Throwable): ErrorCause = when (t) {
        is HttpRequestTimeoutException,
        is SocketTimeoutException,
        is ConnectTimeoutException -> ErrorCause.Timeout

        // Ktor `body()` parse xatosini JsonConvertException'ga o'raydi — u SerializationException
        // avlodi EMAS. Busiz server bilan format mos kelmasligi "noma'lum xato" bo'lib qolardi.
        is SerializationException,
        is ContentConvertException -> ErrorCause.InvalidResponse

        is IOException -> ErrorCause.NoInternet

        else -> platformErrorCause(t) ?: ErrorCause.Unknown
    }

    /** HTTP yoki envelope kodini sababga aylantiradi. */
    fun fromCode(code: Int?): ErrorCause = when (code) {
        401 -> ErrorCause.SessionExpired
        403 -> ErrorCause.Forbidden
        else -> ErrorCause.Server(code)
    }
}