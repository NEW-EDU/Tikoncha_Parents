package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.boshqa
import tikoncha_parents.composeapp.generated.resources.daqiqa
import tikoncha_parents.composeapp.generated.resources.ilova_qidirish
import tikoncha_parents.composeapp.generated.resources.kunlar
import tikoncha_parents.composeapp.generated.resources.policy_day_fri
import tikoncha_parents.composeapp.generated.resources.policy_day_mon
import tikoncha_parents.composeapp.generated.resources.policy_day_sat
import tikoncha_parents.composeapp.generated.resources.policy_day_sun
import tikoncha_parents.composeapp.generated.resources.policy_day_thu
import tikoncha_parents.composeapp.generated.resources.policy_day_tue
import tikoncha_parents.composeapp.generated.resources.policy_day_wed
import tikoncha_parents.composeapp.generated.resources.policy_every_day
import tikoncha_parents.composeapp.generated.resources.policy_weekend
import tikoncha_parents.composeapp.generated.resources.policy_workdays
import tikoncha_parents.composeapp.generated.resources.preset_add
import tikoncha_parents.composeapp.generated.resources.preset_add_app
import tikoncha_parents.composeapp.generated.resources.preset_add_n
import tikoncha_parents.composeapp.generated.resources.preset_done
import tikoncha_parents.composeapp.generated.resources.preset_hours_n
import tikoncha_parents.composeapp.generated.resources.preset_minutes_n
import tikoncha_parents.composeapp.generated.resources.preset_not_found
import tikoncha_parents.composeapp.generated.resources.preset_pause_action
import tikoncha_parents.composeapp.generated.resources.preset_per_day_label
import tikoncha_parents.composeapp.generated.resources.preset_per_hour_label
import tikoncha_parents.composeapp.generated.resources.soat
import tikoncha_parents.composeapp.generated.resources.vaqtincha_toxtatish
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.presentation.base.ChildAppIcon
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.WheelTimePicker
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.presentation.policy.app_site_selection.AppCheckbox
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors

/*
 * Umumiy varaqlar — tayyor jadval va "Jadval yaratish" ekranlari birga ishlatadi
 * (Student `PresetPolicySheets` bilan bir xil). Har biri sof parametrlar oladi.
 */

/** Ilova uslubidagi ModalBottomSheet qobig'i. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicySheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.bg.elevated,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { SheetHandle() },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = ContainerPadding, end = ContainerPadding, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

/** Vaqt g'ildiragi (Boshlanadi / Tugaydi). */
@Composable
fun TimeWheelSheetContent(title: String, initialMinute: Int, onDone: (Int) -> Unit) {
    var picked by remember { mutableIntStateOf(initialMinute) }
    SheetTitle(title)
    WheelTimePicker(
        modifier = Modifier.fillMaxWidth(),
        initialHour = (initialMinute / 60).coerceIn(0, 23),
        initialMinute = initialMinute % 60,
        hourLabel = stringResource(Res.string.soat),
        minuteLabel = stringResource(Res.string.daqiqa),
        onTimeChanged = { h, m -> picked = h * 60 + m },
    )
    CustomButtonNew(text = stringResource(Res.string.preset_done), modifier = Modifier.fillMaxWidth(), onClick = { onDone(picked) })
}

/** Limit g'ildiragi: kunlik — soat:daqiqa, soatlik — faqat daqiqa (1–59). */
@Composable
fun LimitWheelSheetContent(hourly: Boolean, initialMinutes: Int, onDone: (Int) -> Unit) {
    var picked by remember { mutableIntStateOf(initialMinutes) }
    SheetTitle(stringResource(if (hourly) Res.string.preset_per_hour_label else Res.string.preset_per_day_label))
    if (hourly) {
        WheelTimePicker(
            modifier = Modifier.fillMaxWidth(),
            initialMinute = picked.coerceIn(HOURLY_MINUTES),
            showHours = false,
            minuteRange = HOURLY_MINUTES,
            minuteLabel = stringResource(Res.string.daqiqa),
            onTimeChanged = { _, m -> picked = m },
        )
    } else {
        WheelTimePicker(
            modifier = Modifier.fillMaxWidth(),
            initialHour = (picked / 60).coerceIn(0, 12),
            initialMinute = picked % 60,
            hourRange = 0..12,
            hourLabel = stringResource(Res.string.soat),
            minuteLabel = stringResource(Res.string.daqiqa),
            onTimeChanged = { h, m -> picked = h * 60 + m },
        )
    }
    CustomButtonNew(
        text = stringResource(Res.string.preset_done),
        modifier = Modifier.fillMaxWidth(),
        enabled = picked >= if (hourly) HOURLY_MINUTES.first else DAILY_MIN_MINUTES,
        onClick = { onDone(picked) },
    )
}

private val HOURLY_MINUTES = 1..59
private const val DAILY_MIN_MINUTES = 5
private val ALL_DAYS = (1..7).toSet()
private val WORK_DAYS = (1..5).toSet()
private val WEEKEND = setOf(6, 7)

/** Kunlar: Har kuni / Ish kunlari / Dam olish / Boshqa + hafta chiplari. */
@Composable
fun DaysSheetContent(initialDays: Set<Int>, onDone: (List<Int>) -> Unit) {
    var days by remember { mutableStateOf(initialDays) }
    val haptics = rememberAppHaptics()
    val presets = listOf(
        stringResource(Res.string.policy_every_day) to ALL_DAYS,
        stringResource(Res.string.policy_workdays) to WORK_DAYS,
        stringResource(Res.string.policy_weekend) to WEEKEND,
        stringResource(Res.string.boshqa) to null,
    )
    val selected = when (days) {
        ALL_DAYS -> 0
        WORK_DAYS -> 1
        WEEKEND -> 2
        else -> 3
    }
    val short = listOf(
        Res.string.policy_day_mon, Res.string.policy_day_tue, Res.string.policy_day_wed, Res.string.policy_day_thu,
        Res.string.policy_day_fri, Res.string.policy_day_sat, Res.string.policy_day_sun,
    ).map { stringResource(it) }

    SheetTitle(stringResource(Res.string.kunlar))
    SettingGroup {
        presets.forEachIndexed { i, (label, set) ->
            OptionRow(text = label, selected = selected == i, onClick = { if (set != null) days = set })
            if (i < presets.lastIndex) GroupDivider()
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (1..7).forEach { d ->
            val on = d in days
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (on) AppColors.action.primary else AppColors.bg.tertiary)
                    .clickable {
                        haptics.tick()
                        days = if (on) days - d else days + d
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = short[d - 1], style = PolicyText.tab, color = if (on) AppColors.text.inverse else AppColors.text.secondary)
            }
        }
    }
    CustomButtonNew(
        text = stringResource(Res.string.preset_done),
        modifier = Modifier.fillMaxWidth(),
        enabled = days.isNotEmpty(),
        onClick = { onDone(days.sorted()) },
    )
}

/** Vaqtincha to'xtatish: 15 / 30 / 60 / 180 daqiqa. */
@Composable
fun PauseSheetContent(onPause: (Int) -> Unit) {
    val options = listOf(15, 30, 60, 180)
    var picked by remember { mutableIntStateOf(30) }
    SheetTitle(stringResource(Res.string.vaqtincha_toxtatish))
    SettingGroup {
        options.forEachIndexed { i, minutes ->
            OptionRow(
                text = if (minutes < 60) stringResource(Res.string.preset_minutes_n, minutes) else stringResource(Res.string.preset_hours_n, minutes / 60),
                selected = picked == minutes,
                onClick = { picked = minutes },
            )
            if (i < options.lastIndex) GroupDivider()
        }
    }
    CustomButtonNew(text = stringResource(Res.string.preset_pause_action), modifier = Modifier.fillMaxWidth(), onClick = { onPause(picked) })
}

/** Ilova qo'shish: qidiruv + belgilash. [excluded] — allaqachon ro'yxatdagilar. */
@Composable
fun AppsSheetContent(installedApps: List<InstalledApp>, excluded: Set<String>, onAdd: (List<String>) -> Unit) {
    val haptics = rememberAppHaptics()
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(emptySet<String>()) }
    val apps = remember(installedApps, query, excluded) {
        installedApps.filter { it.packageName !in excluded && it.name.contains(query, ignoreCase = true) }
    }

    SheetTitle(stringResource(Res.string.preset_add_app))
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(Res.string.ilova_qidirish), style = PolicyText.input, color = AppColors.text.placeholder) },
        textStyle = PolicyText.input,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.border.accentEmphasis,
            unfocusedBorderColor = AppColors.border.primary,
            focusedTextColor = AppColors.text.primary,
            unfocusedTextColor = AppColors.text.primary,
            cursorColor = AppColors.action.primary,
        ),
    )
    if (apps.isEmpty()) {
        Text(
            text = stringResource(Res.string.preset_not_found),
            style = PolicyText.empty,
            color = AppColors.text.tertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f).clip(RoundedCornerShape(24.dp)).background(AppColors.bg.section)) {
            items(apps, key = { it.packageName }) { app ->
                val on = app.packageName in selected
                val toggle = {
                    selected = if (on) selected - app.packageName else selected + app.packageName
                    haptics.toggle(!on)
                }
                SettingRow(
                    title = app.name,
                    leading = { ChildAppIcon(iconUrl = app.iconUrl) },
                    onClick = toggle,
                    trailing = { AppCheckbox(checked = on, onCheckedChange = { toggle() }) },
                )
            }
        }
    }
    CustomButtonNew(
        text = if (selected.isEmpty()) stringResource(Res.string.preset_add) else stringResource(Res.string.preset_add_n, selected.size),
        modifier = Modifier.fillMaxWidth(),
        enabled = selected.isNotEmpty(),
        onClick = { onAdd(selected.toList()) },
    )
}

@Composable
private fun SheetHandle() {
    Box(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp).width(36.dp).height(4.dp).background(AppColors.border.primary, CircleShape))
}

@Composable
fun SheetTitle(text: String) {
    Text(text = text, style = PolicyText.sheetTitle, color = AppColors.text.primary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
}

/** Ro'yxat qatori: tanlanganda o'ngda belgi. `enabled=false` — xira, bosilmaydi. */
@Composable
fun OptionRow(text: String, selected: Boolean, onClick: () -> Unit, subtitle: String? = null, value: String? = null, enabled: Boolean = true) {
    val haptics = rememberAppHaptics()
    SettingRow(
        title = text,
        subtitle = subtitle,
        value = value,
        onClick = if (enabled) ({
            if (!selected) haptics.tick()
            onClick()
        }) else null,
        modifier = Modifier.alpha(if (enabled) 1f else 0.45f),
        trailing = {
            if (selected) Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp), tint = AppColors.icon.accentPrimary)
            else Box(modifier = Modifier.size(20.dp))
        },
    )
}

/** Radio qatori: sarlavha + izoh + o'ngda radio. */
@Composable
fun RadioRow(title: String, subtitle: String?, selected: Boolean, onClick: () -> Unit, trailingExtra: (@Composable () -> Unit)? = null) {
    val haptics = rememberAppHaptics()
    val select = {
        if (!selected) haptics.tick()
        onClick()
    }
    SettingRow(
        title = title,
        subtitle = subtitle,
        onClick = select,
        trailing = {
            trailingExtra?.invoke()
            RadioButton(
                selected = selected,
                onClick = select,
                colors = RadioButtonDefaults.colors(selectedColor = AppColors.action.primary, unselectedColor = AppColors.border.primary),
            )
        },
    )
}
