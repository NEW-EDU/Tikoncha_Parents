package uz.tikoncha_parent.presentation.policy.policy_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.dot
import tikoncha_parents.composeapp.generated.resources.faol
import tikoncha_parents.composeapp.generated.resources.faol_emas
import tikoncha_parents.composeapp.generated.resources.faqat_jadval_egasi_ozgartira_oladi
import tikoncha_parents.composeapp.generated.resources.jadval_toxtatilgan_gacha
import tikoncha_parents.composeapp.generated.resources.muddati_tugagan
import uz.tikoncha_parent.domain.model.policy.PolicyEffectiveState
import uz.tikoncha_parent.presentation.policy.common.toHhMm
import uz.tikoncha_parent.presentation.policy.policy_list.PolicyItemUi
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * Tahrir ekrani sarlavhasi ostidagi holat (§5.2): ro'yxat kartasidagi belgi bilan bir xil.
 * [canEdit] = false — jadval boshqa ota-onaniki yoki farzandniki, egasi haqida izoh chiqadi.
 */
@Composable
fun PolicySetupStatus(
    policy: PolicyItemUi,
    canEdit: Boolean,
    modifier: Modifier = Modifier,
) {
    val pausedUntilText = policy.pausedUntil
        ?.toLocalDateTime(TimeZone.currentSystemDefault())
        ?.time
        ?.toHhMm()
        .orEmpty()

    val stateText = when (policy.effectiveState) {
        PolicyEffectiveState.ACTIVE -> stringResource(Res.string.faol)
        PolicyEffectiveState.PAUSED -> stringResource(Res.string.jadval_toxtatilgan_gacha, pausedUntilText)
        PolicyEffectiveState.EXPIRED -> stringResource(Res.string.muddati_tugagan)
        PolicyEffectiveState.OFF -> stringResource(Res.string.faol_emas)
    }

    val isLive = policy.effectiveState == PolicyEffectiveState.ACTIVE
    val bgColor = if (isLive) AppColors.bg.primaryContainer else AppColors.action.disabledTertiary
    val textColor = if (isLive) AppColors.text.accentEmphasis else AppColors.text.disabledTertiary

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .background(bgColor, CircleShape)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.dot),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(8.dp),
            )
            SpaceUltraSmall()
            Text(
                text = stateText,
                style = AppTypography.bodyMdMedium,
                color = textColor,
                maxLines = 1,
            )
        }

        if (!canEdit) {
            Space(8.dp)
            Text(
                text = stringResource(Res.string.faqat_jadval_egasi_ozgartira_oladi),
                style = AppTypography.bodySmMedium,
                color = AppColors.text.secondary,
            )
        }
    }
}