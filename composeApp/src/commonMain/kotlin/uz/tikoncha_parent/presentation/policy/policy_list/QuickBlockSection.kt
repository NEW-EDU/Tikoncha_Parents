package uz.tikoncha_parent.presentation.policy.policy_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.android
import tikoncha_parents.composeapp.generated.resources.boshqa_ota_ona
import tikoncha_parents.composeapp.generated.resources.close_remove
import tikoncha_parents.composeapp.generated.resources.farzandingiz
import tikoncha_parents.composeapp.generated.resources.siz
import tikoncha_parents.composeapp.generated.resources.tezkor_bloklar
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.presentation.policy.app_site_selection.AppSelectionUi
import uz.tikoncha_parent.ui.AppIconSize
import uz.tikoncha_parent.ui.ContainerCornerRadius
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.DividerHorizontal
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

@Composable
fun QuickBlockSection(
    entries: List<QuickBlockEntry>,
    myUserId: String,
    apps: Map<String, AppSelectionUi>,
    isBusy: (String) -> Boolean,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.tezkor_bloklar),
            style = AppTypography.titleLgSemiBold,
            color = AppColors.text.primary,
        )
        Spacer(Modifier.height(12.dp))

        entries.forEachIndexed { index, entry ->
            QuickBlockCard(
                entry = entry,
                myUserId = myUserId,
                apps = apps,
                isBusy = isBusy,
                onRemove = onRemove,
            )
            if (index < entries.lastIndex) Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuickBlockCard(
    entry: QuickBlockEntry,
    myUserId: String,
    apps: Map<String, AppSelectionUi>,
    isBusy: (String) -> Boolean,
    onRemove: (String) -> Unit,
) {
    val mine = entry.isMine(myUserId)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .simpleShadow(shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.bg.surface)
            .padding(horizontal = ContainerPadding, vertical = 12.dp),
    ) {
        Text(
            text = entry.ownerLabel(myUserId),
            style = AppTypography.bodySmMedium,
            color = AppColors.text.secondary,
        )

        entry.targets.packages.forEachIndexed { index, pkg ->
            val app = apps[pkg]
            QuickBlockAppRow(
                name = app?.name?.takeIf { it.isNotBlank() } ?: pkg,
                iconUrl = app?.iconUrl,
                // Faqat o'zingiz qo'ygan blokni olib tashlay olasiz — server ham shunday talab qiladi.
                removable = mine,
                busy = isBusy(pkg),
                onRemove = { onRemove(pkg) },
            )
            if (index < entry.targets.packages.lastIndex) {
                DividerHorizontal(
                    color = AppColors.border.secondarySubtle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun QuickBlockAppRow(
    name: String,
    iconUrl: String?,
    removable: Boolean,
    busy: Boolean,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(AppIconSize)
                .clip(RoundedCornerShape(ContainerCornerRadius))
                .background(AppColors.bg.tertiary),
            contentAlignment = Alignment.Center,
        ) {
            if (!iconUrl.isNullOrBlank()) {
                AsyncImage(
                    model = iconUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    error = painterResource(Res.drawable.android),
                    placeholder = painterResource(Res.drawable.android),
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.android),
                    contentDescription = null,
                    tint = AppColors.icon.accentPrimary,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = name,
            style = AppTypography.titleSmMedium,
            color = AppColors.text.primary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (removable) {
            Icon(
                painter = painterResource(Res.drawable.close_remove),
                contentDescription = null,
                tint = AppColors.icon.secondary,
                modifier = Modifier
                    .size(36.dp)
                    .clickable(
                        enabled = !busy,
                        interactionSource = null,
                        indication = null,
                        onClick = onRemove,
                    )
                    .padding(8.dp)
                    .alpha(if (busy) 0.4f else 1f),
            )
        }
    }
}

@Composable
private fun QuickBlockEntry.ownerLabel(myUserId: String): String = when {
    isMine(myUserId) -> stringResource(Res.string.siz)
    isChildOwner -> stringResource(Res.string.farzandingiz)
    else -> stringResource(Res.string.boshqa_ota_ona)
}