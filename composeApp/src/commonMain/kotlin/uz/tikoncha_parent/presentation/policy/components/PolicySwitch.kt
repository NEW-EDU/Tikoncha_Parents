package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import uz.tikoncha_parent.presentation.base.CustomSwitch
import uz.tikoncha_parent.presentation.base.haptics.rememberToggleHaptic
import uz.tikoncha_parent.ui.Haptics
import uz.tikoncha_parent.ui.theme.AppColors
import androidx.compose.ui.graphics.Color

/**
 * Jadval ekranlaridagi switch — Samsung One UI uslubi (Student bilan bir xil, brend rangida):
 * yoqiq — iz `action.primary`, oq thumb, ichida ✓; o'chiq — kulrang iz (`field.switchTrack`),
 * oq thumb. So'rov ketayotganda o'rnida aylanuvchi. [checked] — server tasdiqlagan holat;
 * tebranish holat haqiqatan o'zgarganda.
 *
 * Körinişi 48×28, lekin bosiş maydoni 56×48: titroq barmoq biroz yuqoriroq yoki pastroq bossa ham
 * switch bosiladi (karta ochilib ketmaydi). Qator balandligi özgarmaydi — maydon qator içida çiqib turadi.
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
    val change: (Boolean) -> Unit = {
        armHaptic()
        onCheckedChange(it)
    }
    Box(modifier = modifier.size(48.dp, 28.dp), contentAlignment = Alignment.Center) {
        if (busy) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = AppColors.action.primary, strokeWidth = 3.dp)
        } else {
            // Kengaytirilgan bosiş maydoni (ekran öquvçisi uçun alohida element emas — switch'ning özi bor)
            Box(
                modifier = Modifier
                    .requiredSize(56.dp, 48.dp)
                    .clearAndSetSemantics { }
                    .clickable(
                        enabled = enabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        Haptics.toggle()
                        change(!checked)
                    },
            )
            CustomSwitch(
                checked = checked,
                onCheckedChange = change,
                enabled = enabled,
                width = 48.dp,
                padding = 3.dp,
                showCheckIcon = true,
                trackOnColor = AppColors.action.primary,
                trackOffColor = AppColors.field.switchTrack,
                trackBorderOnColor = Color.Transparent,
                trackBorderOffColor = Color.Transparent,
                thumbOnColor = AppColors.icon.inverse,
                thumbOffColor = AppColors.icon.inverse,
                checkIconColor = AppColors.action.primary,
            )
        }
    }
}
