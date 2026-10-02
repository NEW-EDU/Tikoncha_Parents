package uz.tikoncha_parent.presentation.protection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
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
import tikoncha_parents.composeapp.generated.resources.qalqonni_ochirish
import tikoncha_parents.composeapp.generated.resources.rad_etildi
import tikoncha_parents.composeapp.generated.resources.rad_etish
import tikoncha_parents.composeapp.generated.resources.ruxsat_berildi
import tikoncha_parents.composeapp.generated.resources.ruxsat_berish
import tikoncha_parents.composeapp.generated.resources.ruxsatlar
import tikoncha_parents.composeapp.generated.resources.sorov_rad_etildi_desc
import tikoncha_parents.composeapp.generated.resources.tasdiqlandi
import tikoncha_parents.composeapp.generated.resources.tasdiqlash
import tikoncha_parents.composeapp.generated.resources.x_dan_y_berilgan
import tikoncha_parents.composeapp.generated.resources.zarur_ruxsat_banner_desc
import uz.tikoncha_parent.domain.model.protection.AccountRequest
import uz.tikoncha_parent.domain.model.protection.ChildMode
import uz.tikoncha_parent.domain.model.protection.ChildPermission
import uz.tikoncha_parent.domain.model.protection.ChildRequest
import uz.tikoncha_parent.domain.model.protection.StrictMethod
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.ui.DialogButtonHeight
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

// ═══════════════════════ Umumiy oq karta ═══════════════════════

@Composable
private fun ProtectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.bg.surface)
            .padding(16.dp),
        content = content,
    )
}

/** Kalit-qiymat qatori (hero ichidagi detallar) */
@Composable
private fun KeyValueRow(
    label: String,
    modifier: Modifier = Modifier,
    valueContent: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = AppTypography.emphasizedSmRegular,
            color = AppColors.text.secondary,
            modifier = Modifier.weight(1f),
        )
        valueContent()
    }
}

/** Farzand ismini accent rang bilan ajratadi */
@Composable
private fun highlightedChildText(fullText: String, childName: String): AnnotatedString =
    buildAnnotatedString {
        append(fullText)
        if (childName.isNotEmpty()) {
            val start = fullText.indexOf(childName)
            if (start >= 0) {
                addStyle(
                    style = AppTypography.emphasizedSmSemiBold
                        .copy(color = AppColors.text.accentEmphasis)
                        .toSpanStyle(),
                    start = start,
                    end = start + childName.length,
                )
            }
        }
    }

// ═══════════════════════ HERO KARTA ═══════════════════════

@Composable
fun ProtectionHeroCard(
    state: ProtectionState,
    event: (ProtectionEvent) -> Unit,
) {
    ProtectionCard {
        // Sarlavha qatori: ikona + nom/tavsif + daraja pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.bg.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = state.mode.icon(),
                    contentDescription = null,
                    tint = AppColors.icon.accentPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(state.mode.title()),
                    style = AppTypography.titleLgSemiBold,
                    color = AppColors.text.primary,
                )
                Text(
                    text = stringResource(state.mode.shortDesc()),
                    style = AppTypography.bodyLgRegular,
                    color = AppColors.text.secondary,
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(AppColors.bg.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = stringResource(state.mode.levelLabel()),
                    style = AppTypography.emphasizedXsMedium,
                    color = AppColors.text.accentEmphasis,
                )
            }
        }

        Space(14.dp)
        HorizontalDivider(color = AppColors.border.secondary)
        Space(14.dp)

        // Oxirgi sinxron
        KeyValueRow(label = stringResource(Res.string.oxirgi_sinxron)) {
            Text(
                text = relativeTimeText(state.lastSyncAt),
                style = AppTypography.emphasizedSmSemiBold,
                color = AppColors.text.primary,
            )
        }

        // Jadvallar telefonga yetib bordimi (farzand ilovasining oxirgi hisoboti bo'yicha)
        val upToDate = state.policiesUpToDate
        if (upToDate != null) {
            Space(10.dp)
            KeyValueRow(label = stringResource(Res.string.protection_policies_label)) {
                Text(
                    text = stringResource(
                        if (upToDate) Res.string.protection_policies_delivered
                        else Res.string.protection_policies_pending
                    ),
                    style = AppTypography.emphasizedSmSemiBold,
                    color = if (upToDate) AppColors.text.accentSuccess else AppColors.text.accentWarning,
                )
            }
        }

        // G'ujanak saytlar va Shorts/Reels'ni ko'rmaydi — ota-ona buni bilishi kerak
        if (state.mode == ChildMode.GUJANAK) {
            Space(10.dp)
            Text(
                text = stringResource(Res.string.protection_gujanak_limits_note),
                style = AppTypography.bodyMdRegular,
                color = AppColors.text.accentWarning,
            )
        }

        // O'chirish usuli — faqat Qalqon faol bo'lganda
        val method = state.strictMethod
        if (state.mode == ChildMode.STRICT && method != null) {
            Space(10.dp)
            KeyValueRow(label = stringResource(Res.string.ochirish_usuli)) {
                Text(
                    text = stringResource(method.title()),
                    style = AppTypography.emphasizedSmSemiBold,
                    color = AppColors.text.primary,
                )
            }

            // Maxfiy kod — yashirin/ko'rsatish
            val code = state.unlockData
            if (method == StrictMethod.SECRET_CODE && !code.isNullOrEmpty()) {
                Space(10.dp)
                KeyValueRow(label = stringResource(Res.string.kod)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.isCodeVisible) code else "••••",
                            style = AppTypography.titleMdSemiBold,
                            color = AppColors.text.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(
                                if (state.isCodeVisible) Res.drawable.eye_slash
                                else Res.drawable.eye
                            ),
                            contentDescription = null,
                            tint = AppColors.icon.accentPrimary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickableNoRipple { event(ProtectionEvent.ToggleCodeVisibility) },
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════ SO'ROVLAR ═══════════════════════
// Har bir so'rov — ALOHIDA oq karta (karta ichida karta yo'q)

@Composable
fun ProtectionStrictRequestCard(
    request: ChildRequest,
    childName: String,
    isPending: Boolean,
    remainingSeconds: Int,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    val approved = request.status.equals("approved", ignoreCase = true)
    val rejected = request.status.equals("rejected", ignoreCase = true)

    ProtectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (isPending) AppColors.icon.accentDanger
                else AppColors.icon.secondary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.qalqonni_ochirish),
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
                modifier = Modifier.weight(1f),
            )
            when {
                isPending && remainingSeconds > 0 -> Text(
                    text = formatCountdown(remainingSeconds),
                    style = AppTypography.emphasizedSmSemiBold,
                    color = if (remainingSeconds < 60) AppColors.text.accentDanger
                    else AppColors.text.accentWarning,
                )
                approved -> ResultStatusPill(
                    text = stringResource(Res.string.tasdiqlandi),
                    color = AppColors.text.accentSuccess,
                )
                rejected -> ResultStatusPill(
                    text = stringResource(Res.string.rad_etildi),
                    color = AppColors.text.accentDanger,
                )
                else -> {}
            }
        }

        Space(8.dp)
        val strictDescRes = when {
            isPending -> Res.string.farzand_qalqon_ochirish_sorovi
            approved -> Res.string.qalqon_ochirish_tasdiqlandi_desc
            rejected -> Res.string.qalqon_ochirish_rad_etildi_desc
            else -> Res.string.farzand_qalqon_ochirish_sorovi
        }
        Text(
            text = highlightedChildText(
                fullText = stringResource(strictDescRes, childName),
                childName = childName,
            ),
            style = AppTypography.emphasizedSmRegular,
            color = AppColors.text.secondary,
        )

        if (isPending) {
            Space(12.dp)
            RequestActionButtons(
                isProcessing = isProcessing,
                allowText = stringResource(Res.string.tasdiqlash),
                denyText = stringResource(Res.string.rad_etish),
                onAllow = onApprove,
                onDeny = onReject,
            )
        }
    }
}

@Composable
fun ProtectionAccountRequestCard(
    request: AccountRequest?,
    childName: String,
    title: String,
    isProcessing: Boolean,
    onAllow: () -> Unit,
    onDeny: () -> Unit,
) {
    val isPending = request?.status.equals("process", ignoreCase = true)
    val accessed = request?.status.equals("access", ignoreCase = true)
    val denied = request?.status.equals("deny", ignoreCase = true)
    val isDelete = request?.action == "delete"

    // Holatga mos matn — "so'ramoqda" faqat pending'da
    val descRes = when {
        isPending && isDelete -> Res.string.farzand_ilovani_ochirish_sorovi
        isPending -> Res.string.farzand_ilovadan_chiqish_sorovi
        accessed && isDelete -> Res.string.ilovani_ochirish_ruxsat_berildi_desc
        accessed -> Res.string.hisobdan_chiqish_ruxsat_berildi_desc
        else -> Res.string.sorov_rad_etildi_desc
    }

    ProtectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isDelete) Icons.Default.Delete
                else Icons.Default.Logout,
                contentDescription = null,
                tint = AppColors.icon.accentWarning,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
                modifier = Modifier.weight(1f),
            )
            when {
                isPending -> Text(
                    text = relativeTimeText(parseInstantOrNull(request?.createdAt)),
                    style = AppTypography.bodyMdRegular,
                    color = AppColors.text.tertiary,
                )
                accessed -> ResultStatusPill(
                    text = stringResource(Res.string.ruxsat_berildi),
                    color = AppColors.text.accentSuccess,
                )
                denied -> ResultStatusPill(
                    text = stringResource(Res.string.rad_etildi),
                    color = AppColors.text.accentDanger,
                )
                else -> {}
            }
        }

        Space(8.dp)
        Text(
            text = highlightedChildText(
                fullText = stringResource(descRes, childName),
                childName = childName,
            ),
            style = AppTypography.emphasizedSmRegular,
            color = AppColors.text.secondary,
        )

        if (isPending) {
            Space(12.dp)
            RequestActionButtons(
                isProcessing = isProcessing,
                allowText = stringResource(Res.string.ruxsat_berish),
                denyText = stringResource(Res.string.rad_etish),
                onAllow = onAllow,
                onDeny = onDeny,
            )
        }
    }
}

/** Ikkala tugma ham CustomButtonNew — rad etish = section fon (dialogdagi cancel kabi) */
@Composable
private fun RequestActionButtons(
    isProcessing: Boolean,
    allowText: String,
    denyText: String,
    onAllow: () -> Unit,
    onDeny: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CustomButtonNew(
            text = denyText,
            onClick = onDeny,
            containerColor = AppColors.section.section,
            contentColor = AppColors.text.primary,
            modifier = Modifier
                .weight(1f)
                .height(DialogButtonHeight),
        )
        Box(modifier = Modifier.weight(1f)) {
            CustomButtonNew(
                text = allowText,
                onClick = onAllow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DialogButtonHeight),
            )
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(16.dp),
                    color = AppColors.icon.inverse,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

// ═══════════════════ RUXSAT YO'Q — WARNING BANNER ═══════════════════

@Composable
fun MissingPermissionsBanner(missing: List<ChildPermission>) {
    val names = ArrayList<String>(missing.size)
    for (permission in missing) {
        names.add(stringResource(permission.label()))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.bg.accentWarningContainer)
            .border(1.dp, AppColors.border.accentWarning, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = AppColors.icon.accentWarning,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = stringResource(Res.string.n_ta_zarur_ruxsat_ochiq, missing.size),
                style = AppTypography.emphasizedSmSemiBold,
                color = AppColors.text.primary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = names.joinToString(", ") + ". " +
                        stringResource(Res.string.zarur_ruxsat_banner_desc),
                style = AppTypography.emphasizedXsRegular,
                color = AppColors.text.secondary,
            )
        }
    }
}

// ═══════════════════════ RUXSATLAR KARTASI ═══════════════════════

@Composable
fun ProtectionPermissionsCard(state: ProtectionState) {
    ProtectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.ruxsatlar),
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
                modifier = Modifier.weight(1f),
            )
            if (state.requiredPermissions.isNotEmpty()) {
                Text(
                    text = stringResource(
                        Res.string.x_dan_y_berilgan,
                        state.grantedRequiredCount,
                        state.requiredPermissions.size,
                    ),
                    style = AppTypography.emphasizedSmMedium,
                    color = if (state.missingRequiredPermissions.isEmpty())
                        AppColors.text.accentSuccess
                    else AppColors.text.accentWarning,
                )
            }
        }

        Space(4.dp)

        for (permission in state.requiredPermissions) {
            PermissionRow(
                permission = permission,
                isGranted = permission in state.enabledPermissions,
            )
        }

        if (state.higherTierPermissions.isNotEmpty()) {
            Space(8.dp)
            HorizontalDivider(color = AppColors.border.secondary)
            Space(8.dp)
            Text(
                text = stringResource(Res.string.boshqa_rejimlar_uchun),
                style = AppTypography.emphasizedXsRegular,
                color = AppColors.text.tertiary,
            )
            Space(4.dp)

            for (permission in state.higherTierPermissions) {
                HigherTierPermissionRow(permission = permission)
            }
        }
    }
}


@Composable
private fun ResultStatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = AppTypography.emphasizedXsSemiBold,
            color = color,
        )
    }
}

@Composable
private fun PermissionRow(permission: ChildPermission, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(permission.label()),
            style = AppTypography.emphasizedSmMedium,
            color = AppColors.text.primary,
            modifier = Modifier.weight(1f),
        )
        StatusPill(
            text = stringResource(
                if (isGranted) Res.string.berilgan else Res.string.berilmagan
            ),
            color = if (isGranted) AppColors.text.accentSuccess
            else AppColors.text.accentWarning,
        )
    }
}

@Composable
private fun HigherTierPermissionRow(permission: ChildPermission) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(permission.label()),
            style = AppTypography.emphasizedSmMedium,
            color = AppColors.text.tertiary,
            modifier = Modifier.weight(1f),
        )
        StatusPill(
            text = stringResource(
                if (permission.minMode == ChildMode.STRICT) Res.string.qalqon_uchun
                else Res.string.gujanak_uchun
            ),
            color = AppColors.text.tertiary,
        )
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = AppTypography.emphasizedXsMedium,
            color = color,
        )
    }
}