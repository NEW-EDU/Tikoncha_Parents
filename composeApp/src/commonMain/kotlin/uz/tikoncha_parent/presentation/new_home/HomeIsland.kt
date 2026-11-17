package uz.tikoncha_parent.presentation.new_home

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.Icons
import tikoncha_parents.composeapp.generated.resources.parent_avatar_default
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import tikoncha_parents.composeapp.generated.resources.ai_avatar
import tikoncha_parents.composeapp.generated.resources.ai_voice_mic
import tikoncha_parents.composeapp.generated.resources.ai_assistant_title
import tikoncha_parents.composeapp.generated.resources.profil
import uz.tikoncha_parent.ui.theme.AppTypography
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import tikoncha_parents.composeapp.generated.resources.Res
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.base.island.ChildAvatar
import uz.tikoncha_parent.presentation.base.island.ChildPickerSpec
import uz.tikoncha_parent.presentation.base.island.IslandChildPicker
import uz.tikoncha_parent.ui.expressivePress
import uz.tikoncha_parent.ui.theme.AppColors

/*
 * Bosh sahifa oroli — boshqa ekranlar bilan bir xil joylashuv: chapda keng AI tugmasi,
 * o'ng burchakda farzand tanlagich (har ekranda shu joyda, shu o'lchamda — [IslandChildPicker], ro'yxat yuqoriga
 * ochiladi). Yordam (aloqa) — tepada, öngda. Ekran tuşuntirişi — tugmasiz: silkitiş yoki profil pill'ini uzoq bosiş.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RowScope.HomeIslandContent(
    state: HomeState,
    pickerOpen: MutableTransitionState<Boolean>,
    onAssistant: () -> Unit,
    /** Mikrofon: AI yordamçi darhol tinglay boşlaydi. */
    onAssistantVoice: () -> Unit = onAssistant,
    onProfile: () -> Unit,
    onAddChild: () -> Unit,
    onChildSelected: (UserInfo) -> Unit,
) {
    // AI — muhimrog'i: orolning keng tugmasi (profil tepaga, o'ng burçakka ötdi)
    val aiSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .expressivePress(aiSource, pressedScale = 0.96f)
            .clip(CircleShape)
            .background(AppColors.action.primary.copy(alpha = 0.14f))
            .clickable(interactionSource = aiSource, indication = null, onClick = onAssistant)
            .padding(start = 6.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.ai_avatar),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(36.dp).clip(CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        // Yozuv yöq: farzand tanlagich yonida "Yordamçi" kesilib qolardi — avatar (quloqçinli tipratikon) + mikrofon yetarli
        Spacer(Modifier.weight(1f))
        // Mikrofon — eng tez yöl: bosilişi bilan yordamçi oçiladi va darhol tinglaydi (egasining talabi)
        val micSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(40.dp)
                .expressivePress(micSource, pressedScale = 0.9f)
                .clip(CircleShape)
                .background(AppColors.action.primary)
                .clickable(interactionSource = micSource, indication = null, onClick = onAssistantVoice),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = stringResource(Res.string.ai_voice_mic),
                tint = AppColors.text.inverse,
                modifier = Modifier.size(24.dp),
            )
        }
    }

    IslandChildPicker(
        spec = ChildPickerSpec(state.childrenList, state.selectedChild, onChildSelected, onAddChild),
        // Farzand yo'q — "+ Farzand qo'shish" to'liq sig'sin (katta matn o'lchamida ham); AI tugmasi qolgan joyni oladi
        modifier = Modifier.widthIn(max = if (state.childrenList.isEmpty()) 208.dp else 164.dp),
        open = pickerOpen,
    )
}

private val MIN_NAME_WIDTH = 64.dp
