package uz.tikoncha_parent.presentation.protection

import uz.tikoncha_parent.ui.rememberHapticSource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.berilgan
import tikoncha_parents.composeapp.generated.resources.berilmagan
import tikoncha_parents.composeapp.generated.resources.boshqa_rejimlar_uchun
import tikoncha_parents.composeapp.generated.resources.eye
import tikoncha_parents.composeapp.generated.resources.eye_slash
import tikoncha_parents.composeapp.generated.resources.farzand_ilovadan_chiqish_sorovi
import tikoncha_parents.composeapp.generated.resources.farzand_ilovani_ochirish_sorovi
import tikoncha_parents.composeapp.generated.resources.farzand_qalqon_ochirish_sorovi
import tikoncha_parents.composeapp.generated.resources.guard
import tikoncha_parents.composeapp.generated.resources.guard_danger
import tikoncha_parents.composeapp.generated.resources.guard_neutral
import tikoncha_parents.composeapp.generated.resources.guard_warning
import tikoncha_parents.composeapp.generated.resources.gujanak_uchun
import tikoncha_parents.composeapp.generated.resources.hisobdan_chiqish_ruxsat_berildi_desc
import tikoncha_parents.composeapp.generated.resources.ilovani_ochirish_ruxsat_berildi_desc
import tikoncha_parents.composeapp.generated.resources.kod
import tikoncha_parents.composeapp.generated.resources.n_ta_zarur_ruxsat_ochiq
import tikoncha_parents.composeapp.generated.resources.ochirish_usuli
import tikoncha_parents.composeapp.generated.resources.oxirgi_sinxron
import tikoncha_parents.composeapp.generated.resources.protection_gujanak_limits_note
import tikoncha_parents.composeapp.generated.resources.protection_policies_delivered
import tikoncha_parents.composeapp.generated.resources.protection_policies_label
import tikoncha_parents.composeapp.generated.resources.protection_policies_pending
import tikoncha_parents.composeapp.generated.resources.qalqon_ochirish_rad_etildi_desc
import tikoncha_parents.composeapp.generated.resources.qalqon_ochirish_tasdiqlandi_desc
import tikoncha_parents.composeapp.generated.resources.qalqon_uchun
import tikoncha_parents.composeapp.generated.resources.rad_etildi
import tikoncha_parents.composeapp.generated.resources.ruxsat_berildi
import tikoncha_parents.composeapp.generated.resources.ruxsatlar
import tikoncha_parents.composeapp.generated.resources.sorov_rad_etildi_desc
import tikoncha_parents.composeapp.generated.resources.tasdiqlandi
import tikoncha_parents.composeapp.generated.resources.x_dan_y_berilgan
import tikoncha_parents.composeapp.generated.resources.zarur_ruxsat_banner_desc
import uz.tikoncha_parent.domain.model.protection.AccountRequest
import uz.tikoncha_parent.domain.model.protection.ChildMode
import uz.tikoncha_parent.domain.model.protection.ChildPermission
import uz.tikoncha_parent.domain.model.protection.ChildRequest
import uz.tikoncha_parent.domain.model.protection.StrictMethod
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.expressiveButtonShapes
import uz.tikoncha_parent.ui.expressiveSegment
import uz.tikoncha_parent.ui.segmentShape
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/*
 * Farzand himoyasi — M3 Expressive guruhlangan bloklar (Android 16 sozlamalari uslubi), kartalar ichida karta yo'q:
 *  • holat guruhi: katta qalqon + rejim nomi + 3 bosqichli daraja, ostida sinxron / jadvallar / usul / kod qatorlari;
 *  • ruxsatlar — guruhlangan ro'yxat, berilmaganlari tepada;
 *  • kutilayotgan so'rovlar — ekran pastidagi dokda (bosh barmoq yetadigan joyda), hal bo'lganlari ro'yxatda.
 * Rang faqat holat uchun (qalqon, xavf/ogohlantirish matni) — qolgani bitta urg'u rangi.
 */

private val RowPaddingH = 20.dp

// ═══════════════════════ Umumiy ═══════════════════════

/** Qalqon rasmi Bosh sahifadagi "Farzand himoyasi" bilan bir xil ma'noda. */
private enum class ShieldLevel(val drawable: DrawableResource) {
    Ok(Res.drawable.guard),
    Warning(Res.drawable.guard_warning),
    Danger(Res.drawable.guard_danger),
    Unknown(Res.drawable.guard_neutral),
}

private fun ProtectionState.shieldLevel(): ShieldLevel = when {
    mode == ChildMode.UNKNOWN -> ShieldLevel.Unknown
    missingRequiredPermissions.isNotEmpty() -> ShieldLevel.Danger
    pendingRequestCount > 0 -> ShieldLevel.Warning
    else -> ShieldLevel.Ok
}

/** Rejim darajasi: 0 — noma'lum, 1..3 — Default / G'ujanak / Qalqon. */
private fun ChildMode.levelStep(): Int = when (this) {
    ChildMode.DEFAULT -> 1
    ChildMode.GUJANAK -> 2
    ChildMode.STRICT -> 3
    ChildMode.UNKNOWN -> 0
}

/** Guruh sarlavhasi: kichik, xira; o'ngda ixtiyoriy qisqa holat. */
@Composable
fun ProtectionSectionLabel(text: String, trailing: String? = null, trailingColor: Color = AppColors.text.tertiary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = AppTypography.titleSmMedium,
            color = AppColors.text.tertiary,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(text = trailing, style = AppTypography.titleSmMedium, color = trailingColor)
        }
    }
}

/** Guruh ostidagi izoh (xira, kichik). */
@Composable
fun ProtectionFootnote(text: String, color: Color = AppColors.text.secondary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            tint = AppColors.text.tertiary,
            modifier = Modifier.padding(top = 1.dp).size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(text = text, style = AppTypography.bodyMdRegular, color = color)
    }
}

/** Guruh elementi: bosilmaydigan bo'lsa oddiy shakl, bosiladigan bo'lsa — M3E shakl morfi bilan. */
@Composable
private fun Segment(
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val base = modifier.fillMaxWidth()
    Box(
        modifier = if (onClick != null) {
            base.expressiveSegment(index, count, color = AppColors.bg.surface, debounceMs = 250L, onClick = onClick)
        } else {
            val shape = segmentShape(index, count)
            base.clip(shape).background(AppColors.bg.surface, shape)
        },
    ) { content() }
}

/** Kalit — qiymat qatori (holat guruhida). */
@Composable
private fun ValueRow(label: String, value: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = RowPaddingH, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = AppTypography.titleMdMedium,
            color = AppColors.text.primary,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        value()
    }
}

@Composable
private fun ValueText(text: String, color: Color = AppColors.text.secondary) {
    Text(text = text, style = AppTypography.titleMdMedium, color = color)
}

/** Farzand ismini urg'u rangi bilan ajratadi. */
@Composable
private fun highlightedChildText(fullText: String, childName: String): AnnotatedString =
    buildAnnotatedString {
        append(fullText)
        if (childName.isNotEmpty()) {
            val start = fullText.indexOf(childName)
            if (start >= 0) {
                addStyle(
                    style = AppTypography.bodyMdSemiBold
                        .copy(color = AppColors.text.accentEmphasis)
                        .toSpanStyle(),
                    start = start,
                    end = start + childName.length,
                )
            }
        }
    }

// ═══════════════════════ HOLAT GURUHI ═══════════════════════

private sealed interface StatusRow {
    data object Sync : StatusRow
    data class Policies(val upToDate: Boolean) : StatusRow
    data class Method(val method: StrictMethod) : StatusRow
    data class Code(val code: String) : StatusRow
}

@Composable
fun ProtectionStatusGroup(
    state: ProtectionState,
    event: (ProtectionEvent) -> Unit,
) {
    val rows = buildList {
        add(StatusRow.Sync)
        state.policiesUpToDate?.let { add(StatusRow.Policies(it)) }
        val method = state.strictMethod
        if (state.mode == ChildMode.STRICT && method != null) {
            add(StatusRow.Method(method))
            val code = state.unlockData
            if (method == StrictMethod.SECRET_CODE && !code.isNullOrEmpty()) add(StatusRow.Code(code))
        }
    }
    val count = rows.size + 1

    Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
        Segment(index = 0, count = count) { StatusHero(state) }

        rows.forEachIndexed { i, row ->
            val index = i + 1
            when (row) {
                StatusRow.Sync -> Segment(index, count) {
                    ValueRow(stringResource(Res.string.oxirgi_sinxron)) {
                        ValueText(relativeTimeText(state.lastSyncAt))
                    }
                }

                // Jadvallar telefonga yetib bordimi (farzand ilovasining oxirgi hisoboti bo'yicha)
                is StatusRow.Policies -> Segment(index, count) {
                    ValueRow(stringResource(Res.string.protection_policies_label)) {
                        if (row.upToDate) {
                            ValueText(stringResource(Res.string.protection_policies_delivered))
                        } else {
                            ValueText(
                                stringResource(Res.string.protection_policies_pending),
                                color = AppColors.text.accentWarning,
                            )
                        }
                    }
                }

                is StatusRow.Method -> Segment(index, count) {
                    ValueRow(stringResource(Res.string.ochirish_usuli)) {
                        ValueText(stringResource(row.method.title()))
                    }
                }

                // Maxfiy kod — butun qator bosiladi (ko'rsatish / yashirish)
                is StatusRow.Code -> Segment(index, count, onClick = { event(ProtectionEvent.ToggleCodeVisibility) }) {
                    ValueRow(stringResource(Res.string.kod)) {
                        Text(
                            text = if (state.isCodeVisible) row.code else "• • • •",
                            style = AppTypography.titleLgSemiBold,
                            color = AppColors.text.primary,
                        )
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            painter = painterResource(
                                if (state.isCodeVisible) Res.drawable.eye_slash else Res.drawable.eye
                            ),
                            contentDescription = null,
                            tint = AppColors.icon.accentPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }

    // G'ujanak saytlar va Shorts/Reels'ni ko'rmaydi — ota-ona buni bilishi kerak
    if (state.mode == ChildMode.GUJANAK) {
        ProtectionFootnote(stringResource(Res.string.protection_gujanak_limits_note))
    }
}

/** Holat bloki: rejim nomi (katta), qisqa holat, daraja chizig'i; o'ngda qalqon. */
@Composable
private fun StatusHero(state: ProtectionState) {
    val step = state.mode.levelStep()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = RowPaddingH, end = 14.dp, top = 22.dp, bottom = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(state.mode.title()),
                style = AppTypography.displaySmRegular.copy(fontWeight = FontWeight.Bold),
                color = AppColors.text.primary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(state.mode.shortDesc()),
                style = AppTypography.bodyLgRegular,
                color = AppColors.text.secondary,
            )
            if (step > 0) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelTrack(step = step, modifier = Modifier.width(84.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(state.mode.levelLabel()),
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.tertiary,
                    )
                }
            }
            val missing = state.missingRequiredPermissions.size
            if (missing > 0) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.n_ta_zarur_ruxsat_ochiq, missing),
                    style = AppTypography.titleSmMedium,
                    color = AppColors.text.accentDanger,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Image(
            painter = painterResource(state.shieldLevel().drawable),
            // Holatni yuqoridagi matn aytadi; qalqon — uning rangli belgisi
            contentDescription = null,
            modifier = Modifier.size(84.dp),
        )
    }
}

/** 3 bosqichli daraja: Default → G'ujanak → Qalqon. */
@Composable
private fun LevelTrack(step: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (i < step) AppColors.action.primary
                        else AppColors.text.primary.copy(alpha = 0.08f)
                    ),
            )
        }
    }
}

// ═══════════════════════ RUXSATLAR ═══════════════════════

@Composable
fun ProtectionPermissionsGroup(state: ProtectionState) {
    val required = state.requiredPermissions
    val missing = state.missingRequiredPermissions
    if (required.isNotEmpty()) {
        ProtectionSectionLabel(
            text = stringResource(Res.string.ruxsatlar),
            trailing = stringResource(Res.string.x_dan_y_berilgan, state.grantedRequiredCount, required.size),
            trailingColor = if (missing.isEmpty()) AppColors.text.tertiary else AppColors.text.accentDanger,
        )
        // Berilmaganlari tepada — ota-ona birinchi shularni ko'radi
        val ordered = required.sortedBy { it in state.enabledPermissions }
        Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
            ordered.forEachIndexed { i, permission ->
                Segment(i, ordered.size) {
                    PermissionRow(permission = permission, granted = permission in state.enabledPermissions)
                }
            }
        }
        if (missing.isNotEmpty()) {
            ProtectionFootnote(stringResource(Res.string.zarur_ruxsat_banner_desc))
        }
    }

    val higher = state.higherTierPermissions
    if (higher.isNotEmpty()) {
        ProtectionSectionLabel(
            text = if (required.isEmpty()) stringResource(Res.string.ruxsatlar)
            else stringResource(Res.string.boshqa_rejimlar_uchun),
        )
        Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
            higher.forEachIndexed { i, permission ->
                Segment(i, higher.size) { HigherTierPermissionRow(permission) }
            }
        }
    }
}

@Composable
private fun PermissionRow(permission: ChildPermission, granted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = RowPaddingH, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(permission.label()),
            style = AppTypography.titleMdMedium,
            color = AppColors.text.primary,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(if (granted) Res.string.berilgan else Res.string.berilmagan),
            style = AppTypography.bodyMdMedium,
            color = if (granted) AppColors.text.tertiary else AppColors.text.accentDanger,
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
            contentDescription = null,
            tint = if (granted) AppColors.icon.accentPrimary else AppColors.icon.accentDanger,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun HigherTierPermissionRow(permission: ChildPermission) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = RowPaddingH, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(permission.label()),
            style = AppTypography.titleMdMedium,
            color = AppColors.text.tertiary,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(
                if (permission.minMode == ChildMode.STRICT) Res.string.qalqon_uchun else Res.string.gujanak_uchun
            ),
            style = AppTypography.bodyMdMedium,
            color = AppColors.text.tertiary,
        )
    }
}

// ═══════════════════════ SO'ROVLAR ═══════════════════════

/** Bitta so'rov — ekranda ko'rsatish uchun tayyor ma'lumot (kutilayotgan ham, hal bo'lgan ham). */
class ProtectionRequestUi(
    val key: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val pending: Boolean,
    /** Kutilayotganda: qolgan vaqt yoki yuborilgan vaqt; hal bo'lganda — natija. */
    val trailing: String?,
    val trailingColor: Color,
    val allowText: String,
    val denyText: String,
    val isProcessing: Boolean,
    val onAllow: () -> Unit,
    val onDeny: () -> Unit,
)

@Composable
fun strictRequestUi(
    request: ChildRequest,
    childName: String,
    isPending: Boolean,
    remainingSeconds: Int,
    isProcessing: Boolean,
    title: String,
    allowText: String,
    denyText: String,
    onApprove: () -> Unit,
    onReject: () -> Unit,
): ProtectionRequestUi {
    val approved = request.status.equals("approved", ignoreCase = true)
    val rejected = request.status.equals("rejected", ignoreCase = true)
    val descRes = when {
        isPending -> Res.string.farzand_qalqon_ochirish_sorovi
        approved -> Res.string.qalqon_ochirish_tasdiqlandi_desc
        rejected -> Res.string.qalqon_ochirish_rad_etildi_desc
        else -> Res.string.farzand_qalqon_ochirish_sorovi
    }
    val (trailing, color) = when {
        isPending && remainingSeconds > 0 -> formatCountdown(remainingSeconds) to
            (if (remainingSeconds < 60) AppColors.text.accentDanger else AppColors.text.accentWarning)
        approved -> stringResource(Res.string.tasdiqlandi) to AppColors.text.accentSuccess
        rejected -> stringResource(Res.string.rad_etildi) to AppColors.text.accentDanger
        else -> null to AppColors.text.tertiary
    }
    return ProtectionRequestUi(
        key = "strict",
        icon = Icons.Rounded.Lock,
        title = title,
        description = stringResource(descRes, childName),
        pending = isPending,
        trailing = trailing,
        trailingColor = color,
        allowText = allowText,
        denyText = denyText,
        isProcessing = isProcessing,
        onAllow = onApprove,
        onDeny = onReject,
    )
}

@Composable
fun accountRequestUi(
    request: AccountRequest,
    childName: String,
    title: String,
    isProcessing: Boolean,
    allowText: String,
    denyText: String,
    onAllow: () -> Unit,
    onDeny: () -> Unit,
): ProtectionRequestUi {
    val isPending = request.status.equals("process", ignoreCase = true)
    val accessed = request.status.equals("access", ignoreCase = true)
    val denied = request.status.equals("deny", ignoreCase = true)
    val isDelete = request.action == "delete"
    // Holatga mos matn — "so'ramoqda" faqat kutilayotganda
    val descRes = when {
        isPending && isDelete -> Res.string.farzand_ilovani_ochirish_sorovi
        isPending -> Res.string.farzand_ilovadan_chiqish_sorovi
        accessed && isDelete -> Res.string.ilovani_ochirish_ruxsat_berildi_desc
        accessed -> Res.string.hisobdan_chiqish_ruxsat_berildi_desc
        else -> Res.string.sorov_rad_etildi_desc
    }
    val (trailing, color) = when {
        isPending -> relativeTimeText(parseInstantOrNull(request.createdAt)) to AppColors.text.tertiary
        accessed -> stringResource(Res.string.ruxsat_berildi) to AppColors.text.accentSuccess
        denied -> stringResource(Res.string.rad_etildi) to AppColors.text.accentDanger
        else -> null to AppColors.text.tertiary
    }
    return ProtectionRequestUi(
        key = if (isDelete) "delete" else "logout",
        icon = if (isDelete) Icons.Rounded.Delete else Icons.AutoMirrored.Rounded.Logout,
        title = title,
        description = stringResource(descRes, childName),
        pending = isPending,
        trailing = trailing,
        trailingColor = color,
        allowText = allowText,
        denyText = denyText,
        isProcessing = isProcessing,
        onAllow = onAllow,
        onDeny = onDeny,
    )
}

/** Hal bo'lgan so'rovlar — ro'yxatdagi guruh (tugmasiz, natija o'ngda). */
@Composable
fun ProtectionResolvedRequests(requests: List<ProtectionRequestUi>, childName: String) {
    Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
        requests.forEachIndexed { i, r ->
            Segment(i, requests.size) {
                RequestHeader(r, childName, modifier = Modifier.padding(horizontal = RowPaddingH, vertical = 16.dp))
            }
        }
    }
}

/**
 * Kutilayotgan so'rovlar — ekran pastida, orol ustida (bosh barmoq yetadigan joyda). Bir nechta bo'lsa,
 * bitta guruh bo'lib turadi.
 */
@Composable
fun ProtectionPendingDock(requests: List<ProtectionRequestUi>, childName: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
        requests.forEachIndexed { i, r ->
            Segment(i, requests.size) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp)) {
                    RequestHeader(r, childName, modifier = Modifier.padding(horizontal = 4.dp))
                    Spacer(Modifier.height(16.dp))
                    RequestButtons(r)
                }
            }
        }
    }
}

@Composable
private fun RequestHeader(r: ProtectionRequestUi, childName: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (r.pending) AppColors.action.primary.copy(alpha = 0.14f)
                    else AppColors.text.primary.copy(alpha = 0.06f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = r.icon,
                contentDescription = null,
                tint = if (r.pending) AppColors.icon.accentPrimary else AppColors.icon.secondary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = r.title,
                    style = AppTypography.titleMdSemiBold,
                    color = AppColors.text.primary,
                    modifier = Modifier.weight(1f),
                )
                r.trailing?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(text = it, style = AppTypography.bodyMdMedium, color = r.trailingColor)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = highlightedChildText(r.description, childName),
                style = AppTypography.bodyMdRegular,
                color = AppColors.text.secondary,
            )
        }
    }
}

/** Rad etish (tonal) | Ruxsat berish (asosiy) — M3E tugmalari, bosilganda shakl morfi. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RequestButtons(r: ProtectionRequestUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = r.onDeny,
            interactionSource = rememberHapticSource(),
            enabled = !r.isProcessing,
            shapes = expressiveButtonShapes(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.action.primary.copy(alpha = 0.14f),
                contentColor = AppColors.text.primary,
                disabledContainerColor = AppColors.action.primary.copy(alpha = 0.08f),
                disabledContentColor = AppColors.text.tertiary,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.weight(1f).height(52.dp),
        ) {
            Text(text = r.denyText, style = AppTypography.titleMdSemiBold, maxLines = 1)
        }
        Button(
            onClick = r.onAllow,
            interactionSource = rememberHapticSource(),
            enabled = !r.isProcessing,
            shapes = expressiveButtonShapes(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.button.primary,
                contentColor = AppColors.text.inverse,
                disabledContainerColor = AppColors.button.primary.copy(alpha = 0.6f),
                disabledContentColor = AppColors.text.inverse,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.weight(1f).height(52.dp),
        ) {
            if (r.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AppColors.icon.inverse,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(text = r.allowText, style = AppTypography.titleMdSemiBold, maxLines = 1)
            }
        }
    }
}
