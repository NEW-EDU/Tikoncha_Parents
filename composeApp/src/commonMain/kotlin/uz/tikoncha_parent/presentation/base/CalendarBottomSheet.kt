package uz.tikoncha_parent.presentation.base

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import uz.tikoncha_parent.common.Util
import uz.tikoncha_parent.ui.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.platform.formatMonthYear
import uz.tikoncha_parent.platform.getWeekDays
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarBottomSheet(
    show: Boolean,
    selectedDate: LocalDate?,
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    if (!show) return

    fun LocalDate.withDayOfMonth(day: Int): LocalDate {
        return LocalDate(this.year, month.number, day)
    }

    fun LocalDate.lengthOfMonth(): Int {
        val nextMonth = this.plus(DatePeriod(months = 1)).withDayOfMonth(1)
        return nextMonth.minus(DatePeriod(days = 1)).day
    }

    val today = remember { Util.getCurrentDate() }

    var currentMonth by remember {
        mutableStateOf(selectedDate ?: today.withDayOfMonth(1))
    }
    var tempSelectedDate by remember { mutableStateOf(selectedDate ?: today) }

    val isInPreview = LocalInspectionMode.current
    val language = remember {
        if (isInPreview) {
            LanguageType.UZ
        } else {
            LanguageType.getLangType(LanguagePrefs.loadOrDefault().languageCode)
        }
    }
    val daysOfWeek = listOf(
        stringResource(Res.string.weekday_monday),
        stringResource(Res.string.weekday_tuesday),
        stringResource(Res.string.weekday_wednesday),
        stringResource(Res.string.weekday_thursday),
        stringResource(Res.string.weekday_friday),
        stringResource(Res.string.weekday_saturday),
        stringResource(Res.string.weekday_sunday)
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = AppColors.bg.surface,
        shape = RoundedCornerShape(24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ContainerPadding)
                .padding(bottom = 24.dp)
        ) {

            // 1) Oy navigatsiyasi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    currentMonth = currentMonth.minus(DatePeriod(months = 1))
                }) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_right_rounded),
                        contentDescription = "Previous month",
                        tint = AppColors.text.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(180f)
                    )
                }

                Text(
                    text = currentMonth.formatMonthYear(language),
                    style = AppTypography.titleMdSemiBold,
                    color = AppColors.text.primary
                )

                IconButton(onClick = {
                    currentMonth = currentMonth.plus(DatePeriod(months = 1))
                }) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_right_rounded),
                        contentDescription = "Next month",
                        tint = AppColors.text.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2) Hafta kunlari sarlavhasi (Sh, Ya — qizil)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysOfWeek.forEachIndexed { index, dayName ->
                    val isWeekend = index >= 5
                    Text(
                        text = dayName.take(2),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = AppTypography.titleSmMedium,
                        color = if (isWeekend) AppColors.text.accentDanger
                        else AppColors.text.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 3) Kalendar to'ri
            val firstDayOfMonth = currentMonth.withDayOfMonth(1)
            val lastDay = currentMonth.lengthOfMonth()
            val firstDayIso = firstDayOfMonth.dayOfWeek.isoDayNumber
            val startDayOfWeek = (firstDayIso - 1) % 7
            val totalRows = (startDayOfWeek + lastDay + 6) / 7

            val prevMonth = currentMonth.minus(DatePeriod(months = 1))
            val prevMonthLastDay = prevMonth.lengthOfMonth()
            val nextMonth = currentMonth.plus(DatePeriod(months = 1))

            Column {
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col

                            // Sana va joriy oyga tegishliligini aniqlash
                            val (date, isCurrentMonth) = when {
                                cellIndex < startDayOfWeek -> {
                                    val day = prevMonthLastDay - (startDayOfWeek - cellIndex - 1)
                                    prevMonth.withDayOfMonth(day) to false
                                }
                                cellIndex >= startDayOfWeek + lastDay -> {
                                    val day = cellIndex - startDayOfWeek - lastDay + 1
                                    nextMonth.withDayOfMonth(day) to false
                                }
                                else -> {
                                    val day = cellIndex - startDayOfWeek + 1
                                    currentMonth.withDayOfMonth(day) to true
                                }
                            }

                            val isSelected = isCurrentMonth && date == tempSelectedDate
                            val isToday = isCurrentMonth && date == today && !isSelected

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        enabled = isCurrentMonth
                                    ) {
                                        if (isCurrentMonth) tempSelectedDate = date
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val textColor = when {
                                    !isCurrentMonth -> AppColors.text.tertiary
                                    isSelected -> AppColors.text.inverse
                                    else -> AppColors.text.primary
                                }

                                val cellBg = when {
                                    isSelected -> AppColors.bg.primary
                                    isToday -> AppColors.bg.primaryContainer
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(cellBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = date.day.toString(),
                                        color = textColor,
                                        style = if (isSelected) AppTypography.bodyMdSemiBold
                                        else AppTypography.titleSmMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // 4) Tugmalar — avval Bajarildi, keyin Bekor qilish
            CustomButtonNew(
                enabled = tempSelectedDate != null,
                contentColor = AppColors.text.inverse,
                containerColor = AppColors.button.primary,
                text = stringResource(Res.string.bajarildi),
                onClick = {
                    tempSelectedDate?.let { onDateSelected(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Bekor qilish — fonli kulrang tugma
            CustomButtonNew(
                onClick = onDismissRequest,
                text = stringResource(Res.string.bekor_qilish),
                containerColor = AppColors.section.secondary,
                contentColor = AppColors.text.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            )
        }
    }
}
@Preview
@Composable
private fun CalendarContentPreview_NoSelection() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            CalendarBottomSheet(
                show = true,
                selectedDate = null,
                onDismissRequest = {},
                onDateSelected = {}
            )
        }
    }
}
