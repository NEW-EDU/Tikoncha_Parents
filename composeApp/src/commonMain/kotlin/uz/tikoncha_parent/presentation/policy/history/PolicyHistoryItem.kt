package uz.tikoncha_parent.presentation.policy.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.boshqa_ota_ona
import tikoncha_parents.composeapp.generated.resources.bugun
import tikoncha_parents.composeapp.generated.resources.circle_clock
import tikoncha_parents.composeapp.generated.resources.close_circle
import tikoncha_parents.composeapp.generated.resources.edit_pen
import tikoncha_parents.composeapp.generated.resources.farzandingiz
import tikoncha_parents.composeapp.generated.resources.jadval_toxtatilgan_gacha
import tikoncha_parents.composeapp.generated.resources.kecha
import tikoncha_parents.composeapp.generated.resources.locked
import tikoncha_parents.composeapp.generated.resources.message_delete
import tikoncha_parents.composeapp.generated.resources.pause
import tikoncha_parents.composeapp.generated.resources.play
import tikoncha_parents.composeapp.generated.resources.plus_symbol
import tikoncha_parents.composeapp.generated.resources.siz
import tikoncha_parents.composeapp.generated.resources.status_nomalum
import tikoncha_parents.composeapp.generated.resources.tahrirlangan
import tikoncha_parents.composeapp.generated.resources.tarix_created
import tikoncha_parents.composeapp.generated.resources.tarix_deleted
import tikoncha_parents.composeapp.generated.resources.tarix_disabled
import tikoncha_parents.composeapp.generated.resources.tarix_enabled
import tikoncha_parents.composeapp.generated.resources.tarix_paused
import tikoncha_parents.composeapp.generated.resources.tarix_quick_block_add
import tikoncha_parents.composeapp.generated.resources.tarix_quick_block_remove
import tikoncha_parents.composeapp.generated.resources.tarix_resumed
import tikoncha_parents.composeapp.generated.resources.unlocked
import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.domain.model.policy.PolicyEventType
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.policy.common.toHhMm
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

@Composable
fun PolicyHistoryItem(
    item: PolicyHistoryItemUi,
    modifier: Modifier = Modifier,
) {
    val lang = remember { LanguagePrefs.loadOrDefault() }
    val today = stringResource(Res.string.bugun)
    val yesterday = stringResource(Res.string.kecha)

    val createdText = item.createdAt.toHistoryText(lang, today, yesterday)

    // Pauza yozilgan kunning o'zida tugasa — faqat soat, aks holda sana bilan.
    val pausedText = item.pausedUntil?.let {
        if (it.date == item.createdAt.date) it.time.toHhMm()
        else DateTimeUtil.formatDayMonthLocal(it.date, lang) + ", " + it.time.toHhMm()
    }

    // Stringlar shartsiz olinadi, keyin tanlanadi.
    val eventLabel = stringResource(item.event.labelRes()).replaceFirstChar { it.uppercase() }
    val pausedLabel = stringResource(Res.string.jadval_toxtatilgan_gacha, pausedText.orEmpty())
    val resumedLabel = stringResource(Res.string.tarix_resumed)
    val title = when {
        item.isResume -> resumedLabel
        item.event == PolicyEventType.PAUSED && pausedText != null -> pausedLabel
        else -> eventLabel
    }

    val actorText = when (item.actor) {
        HistoryActor.ME -> stringResource(Res.string.siz)
        HistoryActor.CHILD -> stringResource(Res.string.farzandingiz)
        HistoryActor.OTHER_PARENT -> stringResource(Res.string.boshqa_ota_ona)
        HistoryActor.UNKNOWN -> null
    }
    val meta = listOfNotNull(actorText, createdText).joinToString(" · ")

    // Nom o'zgargan bo'lsa "eski → yangi", tezkor blokda ilova, aks holda jadval nomi.
    val secondLine = when {
        item.renamedFrom != null && item.renamedTo != null -> "${item.renamedFrom} → ${item.renamedTo}"
        item.quickBlockTarget != null -> item.quickBlockTarget
        else -> item.policyName?.takeIf { it.isNotBlank() }
    }

    val tint = when (item.event) {
        PolicyEventType.DELETED, PolicyEventType.QUICK_BLOCK_ADD -> AppColors.icon.accentDanger
        PolicyEventType.DISABLED, PolicyEventType.PAUSED -> AppColors.icon.accentWarning
        PolicyEventType.UNKNOWN -> AppColors.icon.secondary
        else -> AppColors.icon.accentPrimary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .simpleShadow(shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.bg.surface)
            .padding(horizontal = ContainerPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AppColors.bg.secondary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(if (item.isResume) Res.drawable.play else item.event.iconRes()),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = AppTypography.titleSmMedium,
                color = AppColors.text.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            secondLine?.let {
                Text(
                    text = it,
                    style = AppTypography.bodyMdRegular,
                    color = AppColors.text.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = meta,
                style = AppTypography.bodySmMedium,
                color = AppColors.text.tertiary,
            )
        }
    }
}

private fun LocalDateTime.toHistoryText(lang: LanguageType, today: String, yesterday: String): String =
    DateTimeUtil.formatDayMonthLocalized(date, lang, today, yesterday) + ", " + time.toHhMm()

private fun PolicyEventType.labelRes(): StringResource = when (this) {
    PolicyEventType.CREATED -> Res.string.tarix_created
    PolicyEventType.UPDATED -> Res.string.tahrirlangan
    PolicyEventType.ENABLED -> Res.string.tarix_enabled
    PolicyEventType.DISABLED -> Res.string.tarix_disabled
    PolicyEventType.PAUSED -> Res.string.tarix_paused
    PolicyEventType.DELETED -> Res.string.tarix_deleted
    PolicyEventType.QUICK_BLOCK_ADD -> Res.string.tarix_quick_block_add
    PolicyEventType.QUICK_BLOCK_REMOVE -> Res.string.tarix_quick_block_remove
    PolicyEventType.UNKNOWN -> Res.string.status_nomalum
}

private fun PolicyEventType.iconRes(): DrawableResource = when (this) {
    PolicyEventType.CREATED -> Res.drawable.plus_symbol
    PolicyEventType.UPDATED -> Res.drawable.edit_pen
    PolicyEventType.ENABLED -> Res.drawable.play
    PolicyEventType.DISABLED -> Res.drawable.close_circle
    PolicyEventType.PAUSED -> Res.drawable.pause
    PolicyEventType.DELETED -> Res.drawable.message_delete
    PolicyEventType.QUICK_BLOCK_ADD -> Res.drawable.locked
    PolicyEventType.QUICK_BLOCK_REMOVE -> Res.drawable.unlocked
    PolicyEventType.UNKNOWN -> Res.drawable.circle_clock
}