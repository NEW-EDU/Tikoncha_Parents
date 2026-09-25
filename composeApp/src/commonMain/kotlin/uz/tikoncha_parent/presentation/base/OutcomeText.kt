package uz.tikoncha_parent.presentation.base

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.farzand_tanlanmagan
import tikoncha_parents.composeapp.generated.resources.iltimos_internetga_ulang
import tikoncha_parents.composeapp.generated.resources.kutilmagan_xatolik_qayta_urining
import tikoncha_parents.composeapp.generated.resources.muhimlilikni_tanlang
import tikoncha_parents.composeapp.generated.resources.sarlavha_bosh
import tikoncha_parents.composeapp.generated.resources.server_connection_error
import tikoncha_parents.composeapp.generated.resources.tanga_balansi_yetarli_emas
import tikoncha_parents.composeapp.generated.resources.tanga_manfiy_bolmasin
import tikoncha_parents.composeapp.generated.resources.token_eskirgan
import tikoncha_parents.composeapp.generated.resources.tugatish_sanasini_tanlang
import tikoncha_parents.composeapp.generated.resources.tugatish_vaqtini_tanlang
import tikoncha_parents.composeapp.generated.resources.vazifa_id_topilmadi
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import tikoncha_parents.composeapp.generated.resources.jadval_topilmadi
import tikoncha_parents.composeapp.generated.resources.obuna_dialog_message

@Composable
fun Outcome.Failure.asText(): String {
    serverMessage?.takeIf { it.isNotBlank() }?.let { return it }
    return cause.asFallbackText()
}

@Composable
fun ErrorCause.asFallbackText(): String = when (this) {
    ErrorCause.NoInternet,
    ErrorCause.Timeout -> stringResource(Res.string.iltimos_internetga_ulang)
    ErrorCause.SessionExpired -> stringResource(Res.string.token_eskirgan)
    ErrorCause.Forbidden,
    is ErrorCause.Server -> stringResource(Res.string.server_connection_error)
    is ErrorCause.PremiumRequired -> stringResource(Res.string.obuna_dialog_message)
    ErrorCause.NotFound -> stringResource(Res.string.jadval_topilmadi)
    ErrorCause.Validation -> stringResource(Res.string.kutilmagan_xatolik_qayta_urining)
    is ErrorCause.AccountDeletionRequired -> stringResource(Res.string.server_connection_error)
    ErrorCause.InvalidResponse,
    ErrorCause.Unknown -> stringResource(Res.string.kutilmagan_xatolik_qayta_urining)
    ErrorCause.EmptyTitle -> stringResource(Res.string.sarlavha_bosh)
    ErrorCause.NegativeCoin -> stringResource(Res.string.tanga_manfiy_bolmasin)
    ErrorCause.ImportanceNotSelected -> stringResource(Res.string.muhimlilikni_tanlang)
    ErrorCause.DueDateNotSelected -> stringResource(Res.string.tugatish_sanasini_tanlang)
    ErrorCause.DueTimeNotSelected -> stringResource(Res.string.tugatish_vaqtini_tanlang)
    ErrorCause.InsufficientCoins -> stringResource(Res.string.tanga_balansi_yetarli_emas)
    ErrorCause.ChildNotSelected -> stringResource(Res.string.farzand_tanlanmagan)
    ErrorCause.TaskIdMissing -> stringResource(Res.string.vazifa_id_topilmadi)
}