package uz.tikoncha_parent.presentation.new_home

import uz.tikoncha_parent.ui.rememberHapticSource
import tikoncha_parents.composeapp.generated.resources.parent_avatar_default
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.profil
import uz.tikoncha_parent.ui.theme.AppTypography
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.support_icon
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.base.island.ChildAvatar
import uz.tikoncha_parent.presentation.base.island.ChildPickerSpec
import uz.tikoncha_parent.presentation.base.island.IslandChildPicker
import uz.tikoncha_parent.ui.expressivePress
import uz.tikoncha_parent.ui.theme.AppColors

/*
 * Bosh sahifa oroli — boshqa ekranlar bilan bir xil joylashuv: chapda keng "kim" qismi (profil: avatar + ism),
 * o'ng burchakda farzand tanlagich (har ekranda shu joyda, shu o'lchamda — [IslandChildPicker], ro'yxat yuqoriga
 * ochiladi), ular orasida yordam tugmasi.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RowScope.HomeIslandContent(
    state: HomeState,
    pickerOpen: MutableTransitionState<Boolean>,
    onSupport: () -> Unit,
    onProfile: () -> Unit,
    onAddChild: () -> Unit,
    onChildSelected: (UserInfo) -> Unit,
) {
    val profileSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .expressivePress(profileSource, pressedScale = 0.96f)
            .clip(CircleShape)
            .clickable(interactionSource = profileSource, indication = null, onClick = onProfile)
            .padding(start = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Ota-onaning standart rasmi — farzandlarnikidan farqli (qaysi tugma qayerga olib borishi adashmasin)
        ChildAvatar(
            state.userImageUrl,
            Modifier.size(40.dp),
            fallback = Res.drawable.parent_avatar_default,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = state.userName.substringBefore(' ').ifBlank { stringResource(Res.string.profil) },
            style = AppTypography.titleMdSemiBold.copy(fontWeight = FontWeight.ExtraBold),
            color = AppColors.text.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    FilledTonalIconButton(
        onClick = onSupport,
        interactionSource = rememberHapticSource(),
        modifier = Modifier.size(48.dp),
        shapes = IconButtonDefaults.shapes(),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = AppColors.action.primary.copy(alpha = 0.14f),
            contentColor = AppColors.icon.accentPrimary,
        ),
    ) {
        Icon(painterResource(Res.drawable.support_icon), contentDescription = null, modifier = Modifier.size(22.dp))
    }

    IslandChildPicker(
        spec = ChildPickerSpec(state.childrenList, state.selectedChild, onChildSelected, onAddChild),
        modifier = Modifier.widthIn(max = 164.dp),
        open = pickerOpen,
    )
}
