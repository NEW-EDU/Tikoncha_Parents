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
import tikoncha_parents.composeapp.generated.resources.ruxsat_yoq
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
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs

@Composable
fun Outcome.Failure.asText(): String {
    serverMessage?.takeIf { it.isNotBlank() && !looksTechnical(it, LanguagePrefs.loadOrDefault()) }?.let { return it }
    return cause.asFallbackText()
}

/**
 * Server matni ota-onaga körsatilmaydigan holatlar (ehtiyotkor: şubha bölsa — server matni qoladi):
 * texnik iz (Exception, HTTP kod, JSON/HTML, `snake_case` kod) yoki ilova tilida emas
 * (özbek / rus interfeysida inglizça matn; kirill interfeysida kirill harfisiz matn).
 */
internal fun looksTechnical(message: String, lang: LanguageType): Boolean {
    val m = message.trim()
    if (m.isEmpty()) return true
    // JSON / HTML / stack trace
    if (m.first() == '{' || m.first() == '[' || m.first() == '<') return true
    // Faqat kod: "live_bad_state", "child.not_found"
    if (CODE_ONLY.matches(m)) return true
    val lower = m.lowercase()
    if (TECH_MARKERS.any { it in lower }) return true
    // "Request failed with status 500"
    if (HTTP_STATUS.containsMatchIn(m) && (STATUS_WORDS.any { it in lower })) return true

    // Til mos emas — faqat asosiy tillar uçun (boşqa lotin tillarida "error" kabi sözlar özining sözi bölishi mumkin).
    // Rus interfeysida kirill harfi umuman yöq matn — rus tilida emas. (Kirill özbek interfeysida lotin özbekça
    // matn tuşunarli — u qoladi.)
    if (lang == LanguageType.RU && m.none { it in 'Ѐ'..'ӿ' } && m.count { it in 'a'..'z' || it in 'A'..'Z' } >= 3) return true
    val uzOrRuUi = lang == LanguageType.UZ || lang == LanguageType.UZ_CYRL || lang == LanguageType.KAA || lang == LanguageType.RU
    if (uzOrRuUi && ENGLISH_WORD.containsMatchIn(lower)) return true
    return false
}

private val CODE_ONLY = Regex("""^[A-Za-z0-9]+(?:[_.:][A-Za-z0-9]+)+$""")
private val HTTP_STATUS = Regex("""\b[45]\d\d\b""")
private val STATUS_WORDS = listOf("status", "error", "http", "code")
private val TECH_MARKERS = listOf(
    "exception", "traceback", "stacktrace", "stack trace", "internal server error",
    "sqlalchemy", "errno", "nullpointer", "http/1.",
)
/** Özbek va rus matnlarida uçramaydigan inglizça sözlar ("server" ataylab yöq — özbekçada ham işlatiladi). */
private val ENGLISH_WORD = Regex(
    """\b(the|please|error|failed|failure|invalid|unable|could not|cannot|not found|unauthorized|forbidden|""" +
        """already exists|is required|exceeded|denied|unexpected)\b"""
)

@Composable
fun ErrorCause.asFallbackText(): String = when (this) {
    ErrorCause.NoInternet,
    ErrorCause.Timeout -> stringResource(Res.string.iltimos_internetga_ulang)
    ErrorCause.SessionExpired -> stringResource(Res.string.token_eskirgan)
    // 403 — "ruxsat yöq", "ulanib bölmadi" emas (audit 2026-11-17: ota-ona internetni tekşirib övora bölardi).
    ErrorCause.Forbidden -> stringResource(Res.string.ruxsat_yoq)
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
