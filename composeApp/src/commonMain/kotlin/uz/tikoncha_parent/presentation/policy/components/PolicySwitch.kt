package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uz.tikoncha_parent.presentation.base.CustomSwitch
import uz.tikoncha_parent.presentation.base.haptics.rememberToggleHaptic
import uz.tikoncha_parent.ui.theme.AppColors

/**
 * Jadval ekranlaridagi switch: yoqilganda thumb ichida ✓, so'rov ketayotganda o'rnida
 * aylanuvchi. [checked] — server tasdiqlagan holat; tebranish holat haqiqatan o'zgarganda.
 */
@Composable
fun PolicySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
    enabled: Boolean = true,
) {
    val armHaptic = rememberToggleHaptic(checked)
    Box(modifier = modifier.size(48.dp, 28.dp), contentAlignment = Alignment.Center) {
        if (busy) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = AppColors.action.primary, strokeWidth = 3.dp)
        } else {
            CustomSwitch(
                checked = checked,
                onCheckedChange = {
                    armHaptic()
                    onCheckedChange(it)
                },
                enabled = enabled,
                width = 48.dp,
                showCheckIcon = true,
            )
        }
    }
}
