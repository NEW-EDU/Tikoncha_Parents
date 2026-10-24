package uz.tikoncha_parent.presentation.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.tracking_checking_settings
import tikoncha_parents.composeapp.generated.resources.tracking_last_update
import tikoncha_parents.composeapp.generated.resources.tracking_location_unavailable
import tikoncha_parents.composeapp.generated.resources.tracking_watch_video
import tikoncha_parents.composeapp.generated.resources.ulashish
import tikoncha_parents.composeapp.generated.resources.xaritadan_ochish
import uz.tikoncha_parent.domain.model.permission_status.PermissionIssue
import uz.tikoncha_parent.presentation.base.island.ChildAvatar
import uz.tikoncha_parent.ui.expressiveButtonShapes
import uz.tikoncha_parent.ui.expressivePress
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/*
 * Xarita ustidagi pastki qatlam (orol ustida, bosh barmoq yetadigan joyda):
 *  • farzandlar qatori — har biri avatar + ism + oxirgi yangilanish; PLUS'siz farzandda qulf;
 *  • farzand tanlansa — qator o'rniga o'sha farzand kartasi (oxirgi yangilanish, sozlama muammolari,
 *    "Xaritadan ochish" / "Ulashish"). Modal oyna yo'q — xarita ko'rinib turadi, karta xaritaga bosilganda yopiladi.
 * Soya yo'q — xaritadan ingichka chegara bilan ajraladi (orol bilan bir xil).
 */

private val CardShape = RoundedCornerShape(28.dp)

/** Farzandlar qatori (pastda, gorizontal). */
@Composable
fun ChildrenRail(
    people: List<Person>,
    selectedId: String?,
    isLocked: (Person) -> Boolean,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(people, key = { it.id }) { person ->
            ChildPill(
                person = person,
                selected = person.id == selectedId,
                locked = isLocked(person),
                onClick = { onClick(person.id) },
            )
        }
    }
}

@Composable
private fun ChildPill(person: Person, selected: Boolean, locked: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .heightIn(min = 60.dp)
            .expressivePress(source, pressedScale = 0.96f)
            .clip(CircleShape)
            .background(AppColors.bg.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) AppColors.action.primary else AppColors.border.secondarySubtle,
                shape = CircleShape,
            )
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .padding(start = 8.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChildAvatar(person.avatarUrl, Modifier.size(44.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = person.name,
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (person.location != null) formatLastSeen(person.lastSeenEpochMs)
                else stringResource(Res.string.tracking_location_unavailable),
                style = AppTypography.bodySmMedium,
                color = AppColors.text.tertiary,
                maxLines = 1,
            )
        }
        if (locked) {
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Rounded.Lock, contentDescription = null, tint = AppColors.icon.tertiary, modifier = Modifier.size(16.dp))
        }
    }
}

/** Tanlangan farzand kartasi (avvalgi pastki oyna o'rnida). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PersonLocationCard(
    person: Person,
    issues: List<PermissionIssue>,
    isCheckingStatus: Boolean,
    maxHeight: Dp,
    onClose: () -> Unit,
    onWatchVideo: (String) -> Unit,
    onOpenInMaps: (Person) -> Unit,
    onShare: (Person) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasLocation = person.location != null
    Column(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .clip(CardShape)
            .background(AppColors.bg.surface)
            .border(1.dp, AppColors.border.secondarySubtle, CardShape)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChildAvatar(person.avatarUrl, Modifier.size(52.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = person.name,
                    style = AppTypography.titleLgSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                    color = AppColors.text.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (hasLocation) {
                    val label = stringResource(Res.string.tracking_last_update)
                    val value = formatLastSeen(person.lastSeenEpochMs)
                    Text(
                        text = buildAnnotatedString {
                            append(label)
                            append(" ")
                            withStyle(SpanStyle(color = AppColors.text.accentEmphasis, fontWeight = FontWeight.SemiBold)) {
                                append(value)
                            }
                        },
                        style = AppTypography.bodyMdMedium,
                        color = AppColors.text.tertiary,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            FilledTonalIconButton(
                onClick = onClose,
                modifier = Modifier.size(40.dp),
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = AppColors.text.primary.copy(alpha = 0.06f),
                    contentColor = AppColors.icon.secondary,
                ),
            ) {
                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }

        // Joylashuv ham, muammo ham yo'q va tekshirilmayapti
        if (!hasLocation && issues.isEmpty() && !isCheckingStatus) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(Res.string.tracking_location_unavailable),
                style = AppTypography.titleSmSemiBold,
                color = AppColors.text.primary,
            )
        }

        if (isCheckingStatus) {
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = AppColors.text.tertiary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.tracking_checking_settings),
                    style = AppTypography.bodySmMedium,
                    color = AppColors.text.tertiary,
                )
            }
        }

        issues.forEach { issue ->
            Spacer(Modifier.height(12.dp))
            IssueBlock(issue = issue, onWatchVideo = { onWatchVideo(issue.videoUrl.orEmpty()) })
        }

        if (hasLocation) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CardAction(
                    text = stringResource(Res.string.xaritadan_ochish),
                    icon = Icons.Rounded.Map,
                    onClick = { onOpenInMaps(person) },
                    modifier = Modifier.weight(1f),
                )
                CardAction(
                    text = stringResource(Res.string.ulashish),
                    icon = Icons.Rounded.Share,
                    onClick = { onShare(person) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Sozlama muammosi: sarlavha, izoh, (bo'lsa) video. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IssueBlock(issue: PermissionIssue, onWatchVideo: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.bg.accentWarningContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = issue.title, style = AppTypography.titleSmSemiBold, color = AppColors.text.primary)
        Text(text = issue.body, style = AppTypography.bodyMdMedium, color = AppColors.text.secondary)
        if (!issue.videoUrl.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onWatchVideo,
                shapes = expressiveButtonShapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.button.primary,
                    contentColor = AppColors.text.inverse,
                ),
                contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
                modifier = Modifier.height(44.dp),
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.tracking_watch_video), style = AppTypography.bodyMdSemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CardAction(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shapes = expressiveButtonShapes(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.action.primary.copy(alpha = 0.14f),
            contentColor = AppColors.text.primary,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = modifier.height(52.dp),
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.icon.accentPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = text, style = AppTypography.titleSmSemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
