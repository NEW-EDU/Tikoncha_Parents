package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import uz.tikoncha_parent.ui.theme.AppColors

/** Sarlavhadagi ⋮ menyusi: 14dp burchak, soya, ingichka chegara (Student bilan bir xil). */
@Composable
fun PolicyMenu(expanded: Boolean, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 180.dp),
        offset = DpOffset(x = (-8).dp, y = 4.dp),
        shape = RoundedCornerShape(14.dp),
        containerColor = AppColors.modal.primary,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, AppColors.border.secondary.copy(alpha = 0.5f)),
        content = content,
    )
}

/** Menyu bandi: ikonka · bir qatorli matn. [destructive] — qizil (O'chirish). */
@Composable
fun PolicyMenuItem(text: String, icon: ImageVector, onClick: () -> Unit, destructive: Boolean = false) {
    val color = if (destructive) AppColors.text.accentDanger else AppColors.text.primary
    Row(
        modifier = Modifier.clickable(onClick = onClick).height(44.dp).padding(start = 14.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (destructive) AppColors.icon.accentDanger else AppColors.icon.primary,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = PolicyText.menu, color = color, maxLines = 1, softWrap = false)
    }
}
