package uz.tikoncha_parent.presentation.base

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** "Saqlaş" tugmalari uçun yagona belgi (✓) — tugma matni rangida, matndan oldin. */
@Composable
fun SaveIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Rounded.Check,
        contentDescription = null,
        tint = LocalContentColor.current,
        modifier = modifier.padding(end = 6.dp).size(20.dp),
    )
}
