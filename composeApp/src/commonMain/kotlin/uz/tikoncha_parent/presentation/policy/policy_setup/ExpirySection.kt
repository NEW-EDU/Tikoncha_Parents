@file:OptIn(ExperimentalMaterial3Api::class)

package uz.tikoncha_parent.presentation.policy.policy_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.arrow_right_rounded
import tikoncha_parents.composeapp.generated.resources.bir_soatga
import tikoncha_parents.composeapp.generated.resources.bugun
import tikoncha_parents.composeapp.generated.resources.dushanbagacha
import tikoncha_parents.composeapp.generated.resources.ertagacha
import tikoncha_parents.composeapp.generated.resources.ikki_soatga
import tikoncha_parents.composeapp.generated.resources.kecha
import tikoncha_parents.composeapp.generated.resources.muddat
import tikoncha_parents.composeapp.generated.resources.muddat_tavsif
import tikoncha_parents.composeapp.generated.resources.muddati_tugagan
import tikoncha_parents.composeapp.generated.resources.muddatsiz
import tikoncha_parents.composeapp.generated.resources.tugaydi_format
import tikoncha_parents.composeapp.generated.resources.uch_soatga
import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.presentation.policy.common.toHhMm
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.ui.DividerHorizontal
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * "Muddat" kartasi — jadval qachongacha amal qilishi. Faqat qora ro'yxatda ko'rsatiladi (§5.7).
 *
 * Qiymat: shu sessiyada tanlangan variant ("2 soatga") → aks holda serverdagi aniq vaqt
 * ("Tugaydi: Bugun, 14:30") → muddat yo'q bo'lsa "Muddatsiz".
 */
@Composable
fun ExpirySection(
    expiryOption: ExpiryOption?,
    expiresAt: Instant?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = remember { LanguagePrefs.loadOrDefault() }
    val today = stringResource(Res.string.bugun)
    val yesterday = stringResource(Res.string.kecha)
    val noExpiryText = stringResource(Res.string.muddatsiz)
    val expiredText = stringResource(Res.string.muddati_tugagan)
    val optionText = expiryOption?.label()

    val absoluteText = expiresAt?.let { at ->
        val local = at.toLocalDateTime(TimeZone.currentSystemDefault())
        DateTimeUtil.formatDayMonthLocalized(local.date, lang, today, yesterday) + ", " + local.time.toHhMm()
    }
    val endsText = stringResource(Res.string.tugaydi_format, absoluteText.orEmpty())

    val valueText = when {
        optionText != null -> optionText
        expiresAt == null -> noExpiryText
        expiresAt <= Clock.System.now() -> expiredText
        else -> endsText
    }
    val hasExpiry = expiryOption != null || expiresAt != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .simpleShadow(shape = RoundedCornerShape(20.dp))
            .background(AppColors.bg.surface, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .singleClick { if (enabled) onClick() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.muddat),
                style = AppTypography.titleLgSemiBold,
                color = AppColors.text.primary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueText,
                style = AppTypography.titleSmMedium,
                color = if (hasExpiry) AppColors.text.accentEmphasis else AppColors.text.secondary,
            )
            if (enabled) {
                SpaceUltraSmall()
                Icon(
                    painter = painterResource(Res.drawable.arrow_right_rounded),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.icon.secondary,
                )
            }
        }

        Space(6.dp)

        Text(
            text = stringResource(Res.string.muddat_tavsif),
            style = AppTypography.bodySmMedium,
            color = AppColors.text.tertiary,
        )
    }
}

/** Muddat variantlari. [onSelect] ga `null` — "Muddatsiz". */
@Composable
fun ExpiryOptionSheet(
    selected: ExpiryOption?,
    hasExpiry: Boolean,
    onSelect: (ExpiryOption?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.bg.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(Res.string.muddat),
                style = AppTypography.titleLgSemiBold,
                color = AppColors.text.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(16.dp))

            ExpirySheetRow(
                text = stringResource(Res.string.muddatsiz),
                selected = !hasExpiry,
                onClick = { onSelect(null) },
            )

            ExpiryOption.entries.forEach { option ->
                DividerHorizontal(
                    color = AppColors.border.secondarySubtle,
                    modifier = Modifier.fillMaxWidth(),
                )
                ExpirySheetRow(
                    text = option.label(),
                    selected = option == selected,
                    onClick = { onSelect(option) },
                )
            }
        }
    }
}

@Composable
private fun ExpiryOption.label(): String = when (this) {
    ExpiryOption.ONE_HOUR -> stringResource(Res.string.bir_soatga)
    ExpiryOption.TWO_HOURS -> stringResource(Res.string.ikki_soatga)
    ExpiryOption.THREE_HOURS -> stringResource(Res.string.uch_soatga)
    ExpiryOption.UNTIL_TOMORROW -> stringResource(Res.string.ertagacha)
    ExpiryOption.UNTIL_MONDAY -> stringResource(Res.string.dushanbagacha)
}

@Composable
private fun ExpirySheetRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style = AppTypography.bodyLgMedium,
        color = if (selected) AppColors.text.accentEmphasis else AppColors.text.primary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
    )
}