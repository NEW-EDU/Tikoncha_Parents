package uz.tikoncha_parent.presentation.protection

import androidx.compose.ui.text.style.TextAlign
import uz.tikoncha_parent.ui.homeSubtitleColor
import uz.tikoncha_parent.ui.tileBlobColor
import uz.tikoncha_parent.ui.homeTileColor
import uz.tikoncha_parent.ui.HomeItemHeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.StringResource
import tikoncha_parents.composeapp.generated.resources.perm_why_accessibility
import tikoncha_parents.composeapp.generated.resources.perm_why_auto_start
import tikoncha_parents.composeapp.generated.resources.perm_why_battery
import tikoncha_parents.composeapp.generated.resources.perm_why_device_admin
import tikoncha_parents.composeapp.generated.resources.perm_why_gps
import tikoncha_parents.composeapp.generated.resources.perm_why_location
import tikoncha_parents.composeapp.generated.resources.perm_why_notification
import tikoncha_parents.composeapp.generated.resources.perm_why_overlay
import tikoncha_parents.composeapp.generated.resources.perm_why_overlay_popup
import tikoncha_parents.composeapp.generated.resources.perm_why_usage_stats
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import tikoncha_parents.composeapp.generated.resources.acc_delete_req_done
import tikoncha_parents.composeapp.generated.resources.acc_delete_req_pending
import tikoncha_parents.composeapp.generated.resources.boshqa_rejimlar_uchun
import tikoncha_parents.composeapp.generated.resources.eye
import tikoncha_parents.composeapp.generated.resources.eye_slash
import tikoncha_parents.composeapp.generated.resources.farzand_ilovadan_chiqish_sorovi
import tikoncha_parents.composeapp.generated.resources.farzand_ilovani_ochirish_sorovi
import tikoncha_parents.composeapp.generated.resources.farzand_qalqon_ochirish_sorovi
import tikoncha_parents.composeapp.generated.resources.guard_danger
import tikoncha_parents.composeapp.generated.resources.guard_neutral
import tikoncha_parents.composeapp.generated.resources.guard_warning
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
import tikoncha_parents.composeapp.generated.resources.rad_etildi
import tikoncha_parents.composeapp.generated.resources.ruxsat_berildi
import tikoncha_parents.composeapp.generated.resources.sorov_rad_etildi_desc
import tikoncha_parents.composeapp.generated.resources.tasdiqlandi
import tikoncha_parents.composeapp.generated.resources.zarur_ruxsat_banner_desc
import tikoncha_parents.composeapp.generated.resources.prot_phone_settings
import tikoncha_parents.composeapp.generated.resources.prot_phone_settings_hint
import tikoncha_parents.composeapp.generated.resources.prot_x_of_y_on
import tikoncha_parents.composeapp.generated.resources.prot_on
import tikoncha_parents.composeapp.generated.resources.prot_off
import tikoncha_parents.composeapp.generated.resources.prot_not_needed_now
import tikoncha_parents.composeapp.generated.resources.prot_why_needed
import tikoncha_parents.composeapp.generated.resources.prot_where_to_turn_on
import tikoncha_parents.composeapp.generated.resources.prot_stronger_group
import tikoncha_parents.composeapp.generated.resources.prot_strength
import tikoncha_parents.composeapp.generated.resources.prot_default_note
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

/**
 * Holat rasmi: muammo bölsa — qizil/sariq qalqon (Bosh sahifadagi "Farzand himoyasi" bilan bir xil ma'noda),
 * hammasi joyida bölsa — joriy himoya darajasining 3D rasmi (köz / soat / qulf).
 */
private fun ProtectionState.heroImage(): DrawableResource = when {
    mode == ChildMode.UNKNOWN -> Res.drawable.guard_neutral
    missingRequiredPermissions.isNotEmpty() -> Res.drawable.guard_danger
    pendingRequestCount > 0 -> Res.drawable.guard_warning
    else -> mode.icon3d() ?: Res.drawable.guard_neutral
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
        // Qiymat uzun bölsa ham nom söz örtasidan bölinmasin: ikkalasi joyni teng bölişadi, qiymat ö'ngga tekislanadi
        Row(
            modifier = Modifier.weight(1f, fill = false),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) { value() }
    }
}

@Composable
private fun ValueText(text: String, color: Color = AppColors.text.secondary) {
    Text(text = text, style = AppTypography.titleMdMedium, color = color, textAlign = TextAlign.End)
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

    // Ota-ona qöygan qoidalar nimani yopişi (yoki yopmasligi) va kuçliroq himoya qayerda yoqilişi
    when (state.mode) {
        ChildMode.DEFAULT -> ProtectionFootnote(stringResource(Res.string.prot_default_note))
        ChildMode.GUJANAK -> ProtectionFootnote(stringResource(Res.string.protection_gujanak_limits_note))
        else -> Unit
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
                // Nomi tepada yozilgan — bu yerda faqat "qança kuçli" (3 tadan)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.prot_strength),
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.tertiary,
                    )
                    Spacer(Modifier.width(10.dp))
                    LevelTrack(step = step, modifier = Modifier.width(84.dp))
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
            painter = painterResource(state.heroImage()),
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

/**
 * Muhimlik tartibi: eng nozik ruxsatlar tepada. "Maxsus imkoniyatlar" doim birinçi —
 * ekran oçilişi bilan körinadi (ilgari Qalqondan boşqa rejimda pastdagi holatsiz röyxatda edi).
 */
private val PermissionPriority = listOf(
    ChildPermission.ACCESSIBILITY,
    ChildPermission.USAGE_STATS,
    ChildPermission.OVERLAY,
    ChildPermission.OVERLAY_POPUP,
    ChildPermission.AUTO_START,
    ChildPermission.BATTERY,
    ChildPermission.DEVICE_ADMIN,
    ChildPermission.LOCATION,
    ChildPermission.GPS_ENABLED,
    ChildPermission.NOTIFICATION,
)

/** "Nega xavfli?" — ruxsat öçiq bölsa nima işlamay qolişi (bir-ikki gap). */
private fun ChildPermission.whyRes(): StringResource = when (this) {
    ChildPermission.NOTIFICATION -> Res.string.perm_why_notification
    ChildPermission.USAGE_STATS -> Res.string.perm_why_usage_stats
    ChildPermission.OVERLAY -> Res.string.perm_why_overlay
    ChildPermission.OVERLAY_POPUP -> Res.string.perm_why_overlay_popup
    ChildPermission.LOCATION -> Res.string.perm_why_location
    ChildPermission.GPS_ENABLED -> Res.string.perm_why_gps
    ChildPermission.BATTERY -> Res.string.perm_why_battery
    ChildPermission.AUTO_START -> Res.string.perm_why_auto_start
    ChildPermission.ACCESSIBILITY -> Res.string.perm_why_accessibility
    ChildPermission.DEVICE_ADMIN -> Res.string.perm_why_device_admin
}

/**
 * Farzand telefonidagi sozlamalar. Ota-ona (ko'pincha iPhone'da) ularni öz telefonida topa olmaydi, şuning uçun:
 *  • sarlavha va izoh — bular FARZAND telefonida yoqiladi;
 *  • har qator nomi — sozlama ota-onaga nima beradi (Android'dagi nomi faqat "qayerda yoqiladi" izohida);
 *  • joriy himoyada kerak bölmaganlari — yopiq guruhda, ogohlantiriş belgisisiz.
 */
@Composable
fun ProtectionPermissionsGroup(state: ProtectionState) {
    val required = state.requiredPermissions
    val missing = state.missingRequiredPermissions
    val higher = state.higherTierPermissions
    if (required.isEmpty() && higher.isEmpty()) return

    ProtectionSectionLabel(
        text = stringResource(Res.string.prot_phone_settings),
        trailing = if (required.isEmpty()) null
        else stringResource(Res.string.prot_x_of_y_on, state.grantedRequiredCount, required.size),
        trailingColor = if (missing.isEmpty()) AppColors.text.tertiary else AppColors.text.accentDanger,
    )
    Text(
        text = stringResource(Res.string.prot_phone_settings_hint),
        style = AppTypography.bodyMdRegular,
        color = AppColors.text.secondary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
    )

    var explained by rememberSaveable { mutableStateOf<String?>(null) }
    val toggle: (ChildPermission) -> Unit = { p -> explained = if (explained == p.name) null else p.name }

    if (required.isNotEmpty()) {
        // Öçiq turganlari tepada, qolgani muhimlik tartibida
        val ordered = required.sortedWith(
            compareBy<ChildPermission>({ it !in missing }, { PermissionPriority.indexOf(it) })
        )
        PermissionList(ordered, state, required = true, explained = explained, onToggle = toggle)
        if (missing.isNotEmpty()) {
            ProtectionFootnote(stringResource(Res.string.zarur_ruxsat_banner_desc))
        }
    }

    if (higher.isNotEmpty()) {
        var showHigher by rememberSaveable { mutableStateOf(false) }
        Spacer(Modifier.height(if (required.isEmpty()) 0.dp else 16.dp))
        Segment(index = 0, count = 1, onClick = { showHigher = !showHigher }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = RowPaddingH, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.prot_stronger_group, higher.size),
                        style = AppTypography.titleMdMedium,
                        color = AppColors.text.primary,
                    )
                    Text(
                        text = stringResource(Res.string.prot_not_needed_now),
                        style = AppTypography.bodyMdRegular,
                        color = AppColors.text.tertiary,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Icon(
                    imageVector = if (showHigher) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AppColors.text.tertiary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        AnimatedVisibility(visible = showHigher) {
            Column {
                Spacer(Modifier.height(SegmentGap))
                val ordered = higher.sortedBy { PermissionPriority.indexOf(it) }
                PermissionList(ordered, state, required = false, explained = explained, onToggle = toggle)
            }
        }
    }
}

@Composable
private fun PermissionList(
    items: List<ChildPermission>,
    state: ProtectionState,
    required: Boolean,
    explained: String?,
    onToggle: (ChildPermission) -> Unit,
) {
    // Bosh sahifa bloklaridek: katta plitka, 3D rasm ortida yumşoq doira (3D rasmga joy kerak — egasining qoidasi)
    val tile = homeTileColor()
    Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
        items.forEachIndexed { i, permission ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveSegment(i, items.size, color = tile, debounceMs = 250L) { onToggle(permission) },
            ) {
                PermissionRow(
                    permission = permission,
                    granted = permission in state.enabledPermissions,
                    required = required,
                    expanded = explained == permission.name,
                )
            }
        }
    }
}

/** Rasm ortidagi doira — rasm markazida, plitka balandligiga bog'liq emas (izoh oçilganda ham joyida turadi). */
private fun Modifier.iconBlob(color: Color, iconSize: Dp, endPad: Dp, topPad: Dp): Modifier = drawBehind {
    val r = (iconSize * 0.62f).toPx()
    drawCircle(
        color = color,
        radius = r,
        center = Offset(size.width - (endPad + iconSize / 2).toPx(), (topPad + iconSize / 2).toPx()),
    )
}

private val TileIcon = 72.dp
private val TileEndPad = 14.dp
private val TileTopPad = 16.dp

@Composable
private fun PermissionRow(permission: ChildPermission, granted: Boolean, required: Boolean, expanded: Boolean) {
    val dimmed = !granted && !required
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HomeItemHeight)
            .padding(start = RowPaddingH, end = TileEndPad, top = TileTopPad, bottom = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f).padding(top = 2.dp)) {
                Text(
                    text = stringResource(permission.title()),
                    style = AppTypography.titleLgSemiBold,
                    color = AppColors.text.primary,
                )
                Spacer(Modifier.height(8.dp))
                // Holat: yoqilgan / öçiq / hozir şart emas
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (granted || required) {
                        Icon(
                            imageVector = if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                            contentDescription = null,
                            tint = if (granted) AppColors.icon.accentPrimary else AppColors.icon.accentDanger,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = when {
                            granted -> stringResource(Res.string.prot_on)
                            required -> stringResource(Res.string.prot_off)
                            else -> stringResource(Res.string.prot_not_needed_now)
                        },
                        style = AppTypography.titleSmMedium,
                        color = if (!granted && required) AppColors.text.accentDanger else homeSubtitleColor(),
                    )
                }
                if (!granted && required && !expanded) {
                    Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = AppColors.icon.accentPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.prot_why_needed),
                            style = AppTypography.bodyMdMedium,
                            color = AppColors.text.accentEmphasis,
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Image(
                painter = painterResource(permission.icon3d()),
                contentDescription = null,
                modifier = Modifier.size(TileIcon).alpha(if (dimmed) 0.5f else 1f),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = 12.dp, end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(permission.whyRes()),
                    style = AppTypography.bodyLgRegular,
                    color = AppColors.text.secondary,
                )
                // GPS — telefonning tezkor panelidagi tugma, sozlamalar röyxatida emas
                if (permission != ChildPermission.GPS_ENABLED) {
                    Text(
                        text = stringResource(Res.string.prot_where_to_turn_on, stringResource(permission.label())),
                        style = AppTypography.bodyMdRegular,
                        color = AppColors.text.tertiary,
                    )
                }
            }
        }
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
    val isAccountDelete = request.action == "delete_account"
    // Holatga mos matn — "so'ramoqda" faqat kutilayotganda
    val descRes = when {
        isPending && isAccountDelete -> Res.string.acc_delete_req_pending
        accessed && isAccountDelete -> Res.string.acc_delete_req_done
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
        key = request.action ?: "logout",
        icon = if (isDelete || isAccountDelete) Icons.Rounded.Delete else Icons.AutoMirrored.Rounded.Logout,
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
