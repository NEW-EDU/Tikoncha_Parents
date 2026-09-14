package uz.tikoncha_parent.presentation.policy.policy_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.apps_play
import tikoncha_parents.composeapp.generated.resources.boshqa_ota_ona
import tikoncha_parents.composeapp.generated.resources.category_point
import tikoncha_parents.composeapp.generated.resources.dot
import tikoncha_parents.composeapp.generated.resources.faol
import tikoncha_parents.composeapp.generated.resources.faol_emas
import tikoncha_parents.composeapp.generated.resources.farzandingiz
import tikoncha_parents.composeapp.generated.resources.global
import tikoncha_parents.composeapp.generated.resources.ilovalar
import tikoncha_parents.composeapp.generated.resources.jadval_toxtatilgan_gacha
import tikoncha_parents.composeapp.generated.resources.kategoriyalar
import tikoncha_parents.composeapp.generated.resources.maktab
import tikoncha_parents.composeapp.generated.resources.muddati_tugagan
import tikoncha_parents.composeapp.generated.resources.ta
import tikoncha_parents.composeapp.generated.resources.umumiy
import tikoncha_parents.composeapp.generated.resources.vebsaytlar
import tikoncha_parents.composeapp.generated.resources.vertical_menu
import uz.tikoncha_parent.domain.model.GeoType
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.WeekDay
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyEffectiveState
import uz.tikoncha_parent.domain.model.policy.PolicyKind
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.presentation.base.CustomSwitch
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.presentation.policy.common.toHhMm
import uz.tikoncha_parent.presentation.policy.limit_rule.LimitRuleUi
import uz.tikoncha_parent.presentation.policy.time_rule.TimeRuleUi
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.SpaceSmall
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun PolicyListItem(
    modifier: Modifier = Modifier,
    policy: PolicyItemUi,
    /** Server javobini kutyapti — switch bloklanadi. */
    busy: Boolean = false,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit = {},
    onMoreClick: () -> Unit = {},
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

    // Rang faqat ACTIVE da "yoqilgan" ko'rinishda — pauza va muddat tugashi ham o'chiq holat.
    val isLive = policy.effectiveState == PolicyEffectiveState.ACTIVE

    val statusBgColor = if (isLive) AppColors.bg.primaryContainer
    else AppColors.action.disabledTertiary

    val statusTextColor = if (isLive) AppColors.text.accentEmphasis
    else AppColors.text.disabledTertiary

    /** Tahrirlash mumkin bo'lmagan jadval kimga tegishli ekani. */
    val scopeLabel = when (policy.policyType) {
        PolicyType.PARENT_CHILD -> stringResource(Res.string.boshqa_ota_ona)
        PolicyType.STUDENT -> stringResource(Res.string.farzandingiz)
        PolicyType.SCHOOL -> stringResource(Res.string.maktab)
        PolicyType.ALL -> stringResource(Res.string.umumiy)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onClick
            )
            .simpleShadow(shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.bg.surface)
            .padding(top = 12.dp, bottom = 18.dp),
    ) {
        // ── Sarlavha va faollik ──────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ContainerPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = policy.policyName,
                    style = AppTypography.titleLgSemiBold,
                    color = AppColors.text.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
                if (!policy.canEdit) {
                    Text(
                        text = scopeLabel,
                        style = AppTypography.bodySmMedium,
                        color = AppColors.text.secondary,
                        maxLines = 1,
                    )
                }
            }

            SpaceSmall()

            if (policy.canEdit) {
                Text(
                    maxLines = 1,
                    text = stateText,
                    style = AppTypography.bodySmMedium,
                    color = statusTextColor,
                )
                SpaceSmall()
                CustomSwitch(
                    checked = policy.isActive,
                    enabled = !busy,
                    onCheckedChange = onToggle,
                )
                Icon(
                    painter = painterResource(Res.drawable.vertical_menu),
                    contentDescription = null,
                    tint = AppColors.icon.secondary,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(20.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = onMoreClick,
                        ),
                )
            } else {
                Row(
                    modifier = Modifier
                        .background(statusBgColor, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.dot),
                        contentDescription = null,
                        tint = statusTextColor,
                        modifier = Modifier.size(8.dp),
                    )
                    SpaceUltraSmall()
                    Text(
                        maxLines = 1,
                        text = stateText,
                        style = AppTypography.bodyMdMedium,
                        color = statusTextColor,
                    )
                }
            }
        }

        Space(12.dp)
        HorizontalDivider(
            color = AppColors.border.secondarySubtle,
            thickness = 1.dp
        )
        if (policy.appCount > 0){
            PolicyListRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                icon = painterResource(Res.drawable.apps_play),
                title = stringResource(Res.string.ilovalar),
                count = policy.appCount
            )
            if (policy.categoryCount > 0 || policy.webCount > 0){
                HorizontalDivider(
                    color = AppColors.border.secondarySubtle,
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            }
        }
        if (policy.webCount > 0){
            PolicyListRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                icon = painterResource(Res.drawable.global),
                title = stringResource(Res.string.vebsaytlar),
                count = policy.webCount
            )
            if (policy.categoryCount > 0){
                HorizontalDivider(
                    color = AppColors.border.secondarySubtle,
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            }
        }
        if (policy.categoryCount > 0){
            PolicyListRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                icon = painterResource(Res.drawable.category_point),
                title = stringResource(Res.string.kategoriyalar),
                count = policy.categoryCount
            )
        }





    }
}

@Composable
private fun PolicyListRow(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    count: Int
){
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            painter = icon,
            contentDescription = "",
            tint = AppColors.icon.secondary,
            modifier = Modifier
                .size(20.dp)
        )
        Space(8.dp)
        Text(
            text = title,
            modifier = Modifier
                .weight(1f),
            color = AppColors.text.primary,
            style = AppTypography.titleSmMedium
        )
        Space(8.dp)
        Text(
            text = "$count ${stringResource(Res.string.ta)}",
            modifier = Modifier,
            color = AppColors.text.primary,
            style = AppTypography.titleSmSemiBold
        )

    }
}

@Preview
@Composable
private fun PolicyListItemPreview() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        PolicyListItem(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            policy = PolicyItemUi(
                policyId = "preview",
                policyName = "Maktab",
                action = PolicyAction.ALLOW,
                kind = PolicyKind.STANDARD,
                preset = null,
                packCode = null,
                policyType = PolicyType.STUDENT,
                actorUserId = null,
                isMine = true,
                canEdit = true,
                isActive = false,
                effectiveState = PolicyEffectiveState.OFF,
                pausedUntil = null,
                expiresAt = null,
                targets = PolicyTargets(),
                timeRule = listOf(TimeRuleUi(id = 1, weekDays = WeekDay.entries.toSet())),
                limitRule = listOf(LimitRuleUi(id = 1, time = HourMinute(1, 30))),
                locationRule = LocationRule(
                    geoType = GeoType.CIRCLE,
                    centerLat = 41.31,
                    centerLng = 69.24,
                    radiusMeters = 150,
                    reverse = false
                ),
            )
        )
    }
}