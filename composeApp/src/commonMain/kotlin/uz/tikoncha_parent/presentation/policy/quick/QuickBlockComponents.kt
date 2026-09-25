package uz.tikoncha_parent.presentation.policy.quick

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ilova_qidirish
import tikoncha_parents.composeapp.generated.resources.quick_add_count
import tikoncha_parents.composeapp.generated.resources.quick_apps_empty
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.presentation.base.ChildAppIcon
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.SearchField
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.presentation.policy.targets.AppCheckbox
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/** Ilova qatori: ikonka, nom va ixtiyoriy o'ng element. */
@Composable
internal fun AppLine(
    name: String,
    iconUrl: String?,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ChildAppIcon(iconUrl = iconUrl)
        Text(
            text = name,
            style = AppTypography.titleMdMedium,
            color = AppColors.text.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

/**
 * Yashil shaffof izoh qutisi — shu ekran nima qilishini oddiy gaplar bilan aytadi
 * (Student'dagi `PolicyInfoBox`).
 */
@Composable
internal fun InfoBox(lines: List<String>, modifier: Modifier = Modifier) {
    if (lines.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.text.accentSuccess.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        lines.forEach { line ->
            Text(text = line, style = AppTypography.emphasizedXsRegular, color = AppColors.text.primary)
        }
    }
}

/** Qo'shish varag'i: qidiruv, bir nechtasini tanlash, pastda "Qo'shish (N)". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuickAddAppsSheet(
    apps: List<InstalledApp>,
    onAdd: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptics = rememberAppHaptics()
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(emptySet<String>()) }
    val visible = remember(apps, query) {
        val q = query.trim()
        if (q.isEmpty()) apps
        else apps.filter { it.name.contains(q, ignoreCase = true) || it.packageName.contains(q, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.bg.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchField(query = query, label = stringResource(Res.string.ilova_qidirish), onQueryChange = { query = it })

            if (apps.isEmpty()) {
                Text(
                    text = stringResource(Res.string.quick_apps_empty),
                    style = AppTypography.titleSmMedium,
                    color = AppColors.text.tertiary,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(visible, key = { it.packageName }) { app ->
                    val checked = app.packageName in selected
                    val toggle = {
                        selected = if (checked) selected - app.packageName else selected + app.packageName
                        haptics.toggle(!checked)
                    }
                    AppLine(
                        name = app.name,
                        iconUrl = app.iconUrl,
                        modifier = Modifier.clickable { toggle() },
                        trailing = { AppCheckbox(checked = checked, onCheckedChange = { toggle() }) },
                    )
                }
            }

            CustomButtonNew(
                text = stringResource(Res.string.quick_add_count, selected.size),
                enabled = selected.isNotEmpty(),
                onClick = { onAdd(selected) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
        }
    }
}
