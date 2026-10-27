package uz.tikoncha_parent.presentation.ui_state

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.base.asText

sealed class ResponseState<out T> {
    data object Idle : ResponseState<Nothing>()
    data object Loading : ResponseState<Nothing>()
    data class Success<T>(val data: T? = null) : ResponseState<T>()
    data class Error(
        val res: StringResource? = null,
        val message: String? = null,
        val failure: Outcome.Failure? = null
    ) : ResponseState<Nothing>()

}


@Composable
fun ResponseState<*>.errorText(): String = when (this) {
    is ResponseState.Error -> failure?.asText() ?: message?:res?.let { stringResource(it) } ?:""
    else -> ""
}

/** Server yetib bölmadi (internet, 5xx, texnik ishlar) — offline-first: xato oynasi emas, keşdagi ma'lumot qoladi. */
fun ResponseState<*>.isOffline(): Boolean =
    (this as? ResponseState.Error)?.failure?.cause?.isUnreachable == true
