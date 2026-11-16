package uz.tikoncha_parent.presentation.profile.language

import androidx.compose.runtime.remember
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.ui.DividerHorizontal
import uz.tikoncha_parent.ui.NormalIconButtonPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun LanguageSelection(
    selectedLanguage: LanguageType,
    onLanguageSelected: (LanguageType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Ўзбекча (kirill) — birinçi: özbek tili uçun standart. Qolganlari enum tartibida.
        val languages = remember {
            listOf(LanguageType.UZ_CYRL) + LanguageType.entries.filter { it != LanguageType.UZ_CYRL }
        }
        languages.forEachIndexed { index, language ->
            val isLast = index == languages.lastIndex
            val isSelected = selectedLanguage == language

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLanguageSelected(language) }
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            AppColors.bg.surfaceTertiary,
                            RoundedCornerShape(NormalIconButtonPadding)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    LanguageFlag(language, 30.dp)
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = language.languageName,
                        color = AppColors.text.primary,
                        style = AppTypography.titleMdMedium
                    )
                }

                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AppColors.action.primary,
                        unselectedColor = AppColors.icon.secondary
                    )
                )
            }

            if (!isLast) {
                DividerHorizontal(modifier = Modifier.padding(start = 72.dp))
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        LanguageSelection(
            selectedLanguage = LanguageType.UZ,
            onLanguageSelected = {}
        )
    }
}