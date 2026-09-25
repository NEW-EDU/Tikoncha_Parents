package uz.tikoncha_parent.presentation.statistic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.foydalanilmagan
import tikoncha_parents.composeapp.generated.resources.jami
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.ui.CardCornerRadius
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.DividerHorizontal
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import kotlin.math.min

@Composable
fun UsageDetailsDialog(
    details: UsageDetailsUi?,
    show: Boolean,
    onDismiss: () -> Unit,
) {
    if (!show || details == null) return

    val rowHeight = 44.dp
    val maxVisible = 8
    val visibleCount = min(details.items.size, maxVisible).coerceAtLeast(1)
    val listHeight = rowHeight * visibleCount

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.modal.primary, RoundedCornerShape(CardCornerRadius))
                .padding(ContainerPadding)
        ) {
            Text(
                text = usageDetailsTitleString(details.title),
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
                modifier = Modifier.fillMaxWidth(),
            )
            SpaceMedium()

            if (details.items.isEmpty()) {
                Text(
                    text = stringResource(Res.string.foydalanilmagan),
                    style = AppTypography.bodyMdRegular,
                    color = AppColors.text.secondary,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(listHeight)) {
                    items(items = details.items, key = { it.packageName }) { item ->
                        UsageDetailRow(item, rowHeight)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            DividerHorizontal()
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.jami),
                    style = AppTypography.titleMdMedium,
                    color = AppColors.text.primary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = durationString(details.total),
                    style = AppTypography.titleMdSemiBold,
                    color = AppColors.text.primary,
                )
            }
        }
    }
}

@Composable
private fun UsageDetailRow(item: UsageDetailItem, rowHeight: androidx.compose.ui.unit.Dp) {
    Row(
        modifier = Modifier.fillMaxWidth().height(rowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(AppColors.bg.tertiary),
            contentAlignment = Alignment.Center
        ) {
            if (!item.iconUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.iconUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = item.name,
            style = AppTypography.bodyMdRegular,
            color = AppColors.text.primary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = rowDuration(item.usageMillis),
            style = AppTypography.bodyMdMedium,
            color = AppColors.text.secondary,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun UsageDetailsDialogPreview_HourRange() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        UsageDetailsDialog(
            details = UsageDetailsUi(
                title = UsageDetailsTitle.HourRange(
                    day = 19, monthIndex = 4, hourFrom = 14, hourToExclusive = 16
                ),
                total = HourMinute(3, 14),
                items = previewDetailItems(),
            ),
            show = true,
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun UsageDetailsDialogPreview_Weekday() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        UsageDetailsDialog(
            details = UsageDetailsUi(
                title = UsageDetailsTitle.WeekdayDate(
                    weekdayIndex = 0, day = 12, monthIndex = 4
                ),
                total = HourMinute(5, 42),
                items = previewDetailItems(),
            ),
            show = true,
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun UsageDetailsDialogPreview_Empty() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        UsageDetailsDialog(
            details = UsageDetailsUi(
                title = UsageDetailsTitle.HourRange(
                    day = 19, monthIndex = 4, hourFrom = 10, hourToExclusive = 12
                ),
                total = HourMinute(0, 0),
                items = emptyList(),
            ),
            show = true,
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun UsageDetailsDialogPreview_Dark() {
    TikonchaParentTheme(ThemeMode.DARK) {
        UsageDetailsDialog(
            details = UsageDetailsUi(
                title = UsageDetailsTitle.HourRange(
                    day = 19, monthIndex = 4, hourFrom = 14, hourToExclusive = 16
                ),
                total = HourMinute(3, 14),
                items = previewDetailItems(),
            ),
            show = true,
            onDismiss = {},
        )
    }
}