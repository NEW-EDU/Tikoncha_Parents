package uz.tikoncha_parent.presentation.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.profile.language.LocalLanguageController
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * Kirişgaça (tanişuv, kiriş, kod, röyxatdan ötiş) har ekranda turadigan til tanlagiç: globus + til nomi + ▾.
 * Globus — tilni öqiy olmagan odam ham "bu til" ekanini tanisin. Til darhol almaşadi, kiritilgan matn saqlanadi.
 */
@Composable
fun LanguagePill(
    modifier: Modifier = Modifier,
    container: Color = AppColors.bg.surface,
    content: Color = AppColors.text.primary,
    subtle: Color = AppColors.text.tertiary,
) {
    val controller = LocalLanguageController.current
    val current by controller.current.collectAsState()
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(onClick = { open = true }, shape = CircleShape, color = container) {
            Row(Modifier.padding(start = 10.dp, end = 10.dp, top = 7.dp, bottom = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Language, contentDescription = null, tint = subtle, modifier = Modifier.size(17.dp))
                Spacer(Modifier.size(6.dp))
                Text(current.languageName, style = AppTypography.bodyMdMedium.copy(fontWeight = FontWeight.Bold), color = content, maxLines = 1)
                Spacer(Modifier.size(2.dp))
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = subtle, modifier = Modifier.size(18.dp))
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            LanguageType.entries.forEach { lang ->
                DropdownMenuItem(
                    text = {
                        Text(
                            lang.languageName,
                            style = AppTypography.bodyLgMedium.copy(fontWeight = if (lang == current) FontWeight.ExtraBold else FontWeight.Medium),
                        )
                    },
                    onClick = {
                        open = false
                        controller.select(lang)
                    },
                )
            }
        }
    }
}
