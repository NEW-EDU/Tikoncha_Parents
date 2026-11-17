package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.dialog_internet
import tikoncha_parents.composeapp.generated.resources.policy_load_failed
import tikoncha_parents.composeapp.generated.resources.policy_off_body
import tikoncha_parents.composeapp.generated.resources.policy_off_cancel
import tikoncha_parents.composeapp.generated.resources.policy_off_confirm
import tikoncha_parents.composeapp.generated.resources.policy_off_title
import tikoncha_parents.composeapp.generated.resources.policy_saved_queued
import tikoncha_parents.composeapp.generated.resources.policy_saved_sent
import tikoncha_parents.composeapp.generated.resources.policy_turned_off
import tikoncha_parents.composeapp.generated.resources.policy_turned_on
import tikoncha_parents.composeapp.generated.resources.policy_unsaved_body
import tikoncha_parents.composeapp.generated.resources.policy_unsaved_discard
import tikoncha_parents.composeapp.generated.resources.policy_unsaved_title
import tikoncha_parents.composeapp.generated.resources.qayta_urinish
import tikoncha_parents.composeapp.generated.resources.quick_unblocked
import tikoncha_parents.composeapp.generated.resources.saqlash
import tikoncha_parents.composeapp.generated.resources.targets_plus
import uz.tikoncha_parent.data.outbox.ParentOutbox
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PolicyName
import uz.tikoncha_parent.presentation.base.ConfirmationBottomSheet
import uz.tikoncha_parent.presentation.base.CustomOutlinedButton
import uz.tikoncha_parent.presentation.base.LocalToastHost
import uz.tikoncha_parent.presentation.base.ToastData
import uz.tikoncha_parent.presentation.base.ToastType
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/*
 * "Qörqmasdan bosiş" — farzand telefonini özgartiradigan amallar uçun umumiy qismlar:
 * natija haqida qisqa xabar (toast), himoyani öçirişdan oldin söraş, saqlanmagan özgarişlar,
 * Plus belgisi va yuklab bölmaganda "Qayta uriniş".
 */

// ═══════════════════════════════════════════
//  Natija xabari (toast)
// ═══════════════════════════════════════════

/** Muvaffaqiyatli amaldan keyingi qisqa xabar. Matn faqat Compose'da hosil böladi. */
sealed interface PolicyToast {
    /** "Saqlandi — farzand telefoniga yuborildi" (offline navbatda bölsa — "internet qaytganda"). */
    data object Saved : PolicyToast
    /** "Uyqu vaqti yoqildi" / "… öçirildi" — röyxatdagi switch'lar. */
    data class Toggled(val name: PolicyName, val on: Boolean) : PolicyToast
    /** "Instagram oçildi" — tezkor blokdan olib taşlandi. */
    data class Unblocked(val app: String) : PolicyToast
}

/** Xabar qaysi ekranda körinadi: saqlab yopilgan ekranning xabari röyxatda körinadi. */
enum class PolicyScreenId { LIST, QUICK, PRESET, EDITOR, PROTECTION, SPECIAL }

/**
 * Ekranlar orasidagi bitta xabar örni. ViewModel muvaffaqiyatdan keyin [post] qiladi; körsatilishi kerak
 * bölgan ekrandagi [PolicyToastHost] uni oladi. Ekran yopilib, ostidagisi ochilsa ham xabar yöqolmaydi.
 */
object PolicyToasts {
    class Entry internal constructor(val toast: PolicyToast, val screen: PolicyScreenId) {
        internal val at = TimeSource.Monotonic.markNow()
    }

    private val _pending = MutableStateFlow<Entry?>(null)
    val pending: StateFlow<Entry?> = _pending.asStateFlow()

    fun post(toast: PolicyToast, screen: PolicyScreenId) {
        _pending.value = Entry(toast, screen)
    }

    internal fun consume(entry: Entry) {
        _pending.compareAndSet(entry, null)
    }
}

/** Kech qolgan xabar (ekran almashib ketgan) körsatilmaydi. */
private val TOAST_STALE = 8.seconds

/** [screen] uçun yuborilgan xabarni körsatadi. `ToastProvider` içida turişi kerak. */
@Composable
fun PolicyToastHost(screen: PolicyScreenId) {
    val host = LocalToastHost.current
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    val pending by PolicyToasts.pending.collectAsState()
    val entry = pending?.takeIf { it.screen == screen }
    val text = entry?.toast?.text()
    LaunchedEffect(entry) {
        if (entry == null || text == null) return@LaunchedEffect
        PolicyToasts.consume(entry)
        if (entry.at.elapsedNow() > TOAST_STALE) return@LaunchedEffect
        job?.cancel()
        val data = ToastData(type = ToastType.Success, title = text)
        job = scope.launch {
            // show() bekor qilinsa (yangi xabar keldi) — eskisi ekranda qotib qolmasin
            try { host.show(data) } finally { if (host.current.value === data) host.dismiss() }
        }
    }
}

@Composable
private fun PolicyToast.text(): String = when (this) {
    PolicyToast.Saved -> stringResource(
        if (ParentOutbox.current().isEmpty()) Res.string.policy_saved_sent else Res.string.policy_saved_queued
    )
    is PolicyToast.Toggled -> stringResource(if (on) Res.string.policy_turned_on else Res.string.policy_turned_off, name.text())
    is PolicyToast.Unblocked -> stringResource(Res.string.quick_unblocked, app)
}

// ═══════════════════════════════════════════
//  Öçirişdan oldin söraş
// ═══════════════════════════════════════════

/**
 * Switch: yoqiş — darhol, öçiriş — avval "… öçirilsinmi?" (nima bölişi oddiy söz bilan).
 * Ekranda bitta: `val guard = rememberTurnOffGuard()` + `TurnOffConfirm(guard)`.
 */
@Stable
class TurnOffGuard internal constructor() {
    internal class Pending(val title: String, val body: StringResource, val apply: () -> Unit)

    internal var pending by mutableStateOf<Pending?>(null)

    fun toggle(title: String, on: Boolean, body: StringResource = Res.string.policy_off_body, apply: (Boolean) -> Unit) {
        if (on) apply(true) else pending = Pending(title, body) { apply(false) }
    }
}

@Composable
fun rememberTurnOffGuard(): TurnOffGuard = remember { TurnOffGuard() }

@Composable
fun TurnOffConfirm(guard: TurnOffGuard) {
    val p = guard.pending ?: return
    ConfirmationBottomSheet(
        title = stringResource(Res.string.policy_off_title, p.title),
        subtitle = stringResource(p.body),
        confirmText = stringResource(Res.string.policy_off_confirm),
        cancelText = stringResource(Res.string.policy_off_cancel),
        onConfirm = {
            guard.pending = null
            p.apply()
        },
        onDismiss = { guard.pending = null },
    )
}

// ═══════════════════════════════════════════
//  Saqlanmagan özgarişlar
// ═══════════════════════════════════════════

/**
 * Orqaga bosilganda: "Özgarişlar saqlansinmi?" — Saqlaş · Saqlamasdan çiqiş · Bekor qiliş.
 * [canSave] false bölsa (masalan, nima yopilişi tanlanmagan) "Saqlaş" körinmaydi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnsavedChangesSheet(
    canSave: Boolean,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.bg.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.policy_unsaved_title),
                style = AppTypography.titleLgSemiBold,
                color = AppColors.text.primary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.policy_unsaved_body),
                style = AppTypography.emphasizedMdMedium,
                color = AppColors.text.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            if (canSave) {
                SheetButton(
                    text = stringResource(Res.string.saqlash),
                    container = AppColors.action.primary,
                    content = AppColors.text.inverse,
                    onClick = onSave,
                )
            }
            SheetButton(
                text = stringResource(Res.string.policy_unsaved_discard),
                container = AppColors.section.secondary,
                content = AppColors.text.accentDanger,
                onClick = onDiscard,
            )
            SheetButton(
                text = stringResource(Res.string.bekor_qilish),
                container = AppColors.section.secondary,
                content = AppColors.text.primary,
                onClick = onDismiss,
            )
        }
    }
}

@Composable
private fun SheetButton(text: String, container: Color, content: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
    ) {
        Text(text = text, style = AppTypography.titleSmMedium)
    }
}

// ═══════════════════════════════════════════
//  Plus belgisi
// ═══════════════════════════════════════════

/** Pullik imkoniyat — bosişdan OLDIN körinadi (paywall kutilmagan "xato" bölib tuyulmasin). */
@Composable
fun PlusBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.targets_plus),
        style = PolicyText.chip,
        color = AppColors.text.inverse,
        maxLines = 1,
        modifier = modifier
            .background(AppColors.bg.primary, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

// ═══════════════════════════════════════════
//  Yuklab bölmadi
// ═══════════════════════════════════════════

/**
 * Yuklaş yiqilganda switch'lar örniga ("Yuklab bölmadi" + sabab + "Qayta uriniş") — standart holatlar
 * haqiqiydek körinib qolmasin. `ErrorRetryState` bilan bir xil körinişda, lekin sarlavha va tugma doim bor.
 */
@Composable
fun PolicyLoadFailed(failure: Outcome.Failure?, onRetry: () -> Unit, modifier: Modifier = Modifier, retrying: Boolean = false) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val offline = failure?.cause == ErrorCause.NoInternet || failure?.cause == ErrorCause.Timeout
        Image(
            painter = painterResource(if (offline) Res.drawable.dialog_internet else Res.drawable.dialog_failed),
            contentDescription = null,
            modifier = Modifier.size(90.dp),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.policy_load_failed),
            style = PolicyText.section,
            color = AppColors.text.primary,
            textAlign = TextAlign.Center,
        )
        if (failure != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = failure.asText(),
                style = PolicyText.dialogText,
                color = AppColors.text.secondary,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(20.dp))
        CustomOutlinedButton(
            text = stringResource(Res.string.qayta_urinish),
            onClick = onRetry,
            enabled = !retrying,
        )
    }
}
