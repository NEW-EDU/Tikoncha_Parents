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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun CalendarDialog(
    selectedDate: LocalDate?,
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {

    fun LocalDate.withDayOfMonth(day: Int): LocalDate {
        return LocalDate(this.year, month.number, day)
    }

    fun LocalDate.lengthOfMonth(): Int {
        val nextMonth = this.plus(DatePeriod(months = 1)).withDayOfMonth(1)
        return nextMonth.minus(DatePeriod(days = 1)).day
    }

    var currentMonth by remember {
        mutableStateOf(selectedDate ?: Util.getCurrentDate().withDayOfMonth(1))
    }
    var tempSelectedDate by remember { mutableStateOf(selectedDate ?: Util.getCurrentDate()) }

    val language = LanguageType.getLangType(LanguagePrefs.loadOrDefault().languageCode)
    val daysOfWeek = remember(language) { getWeekDays(language) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        containerColor = Color.Transparent,
        text = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .coverShadow(
                    radius = CardCornerRadius
                )
                .background(MaterialTheme.extendedColor.backgroundColor, RoundedCornerShape(CardCornerRadius))
                .padding(ContainerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minus(DatePeriod(months = 1)) }) {

                        Icon(
                            painter = painterResource(Res.drawable.arrow_previous),
                            contentDescription = "Back"
                        )
                    }

                    CustomText(
                        text = currentMonth.formatMonthYear(language),
                        fontSize = LargeTextSize,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(onClick = { currentMonth = currentMonth.plus(DatePeriod(months = 1)) }) {

                       Icon(
                            painter = painterResource(Res.drawable.arrow_right),
                            null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.extendedColor.borderColor)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    daysOfWeek.forEach {
                        CustomText(
                            text = it,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val firstDayOfMonth = currentMonth.withDayOfMonth(1)
                val lastDay = currentMonth.lengthOfMonth()
                val firstDayISo = firstDayOfMonth.dayOfWeek.isoDayNumber
                val startDayOfWeek = (firstDayISo - 1) % 7
                val totalCells = lastDay + startDayOfWeek
                val rows = (totalCells + 6) / 7

                Column {
                    var dayCounter = 1
                    for (i in 0 until rows) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (j in 0..6) {
                                val index = i * 7 + j
                                if (index < startDayOfWeek || dayCounter > lastDay) {
                                    Spacer(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    )
                                } else {
                                    val date = currentMonth.withDayOfMonth(dayCounter)
                                    val isSelected = date == tempSelectedDate

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { tempSelectedDate = date },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(PrimaryColor, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CustomText(
                                                    text = dayCounter.toString(),
                                                    color = MaterialTheme.extendedColor.backgroundColor
                                                )
                                            }
                                        } else {
                                            CustomText(
                                                text = dayCounter.toString(),
                                            )
                                        }
                                    }
                                    dayCounter++
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                CustomOutlinedButton(
                    text = stringResource(Res.string.bekor_qilish),
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .height(DialogButtonHeight)
                        .fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomButton(
                    onClick = {
                        tempSelectedDate?.let { onDateSelected(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DialogButtonHeight),
                    enabled = tempSelectedDate != null,
                    text = stringResource(Res.string.saqlash),
                )
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ){
        Column(
            modifier = Modifier.fillMaxSize().background(OnPrimaryColor)
        ) {
            CalendarDialog(
                onDateSelected = {},
                onDismissRequest = {},
                selectedDate = null
            )
        }
    }
}

