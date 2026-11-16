package uz.tikoncha_parent.presentation.profile.subscription.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ap_done
import tikoncha_parents.composeapp.generated.resources.ap_for_child
import tikoncha_parents.composeapp.generated.resources.ap_result_paid_title
import tikoncha_parents.composeapp.generated.resources.sub_success_body
import tikoncha_parents.composeapp.generated.resources.sub_success_until
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.island.ChildAvatar
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

/**
 * To'lov o'tdi — sokin tasdiq (avvalgi yashil gradientli "TABRIKLAYMIZ!" oynasi o'rniga): bitta urg'u rangi,
 * aniq sarlavha, nima ochilgani bir qatorda, kim uchun va qachongacha (ma'lum bo'lsa), bitta tugma.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionSuccessSheet(
    show: Boolean,
    /** Obuna kim uchun olindi; null — farzand hali ulanmagan (telefon raqami bo'yicha olingan). */
    childName: String?,
    childAvatarUrl: String?,
    /** Tayyor matn ("16-dekabr 2026"); null — server hali qaytarmadi, qator ko'rsatilmaydi. */
    validUntil: String?,
    onDone: () -> Unit,
) {
    if (!show) return
    ModalBottomSheet(
        onDismissRequest = onDone,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.bg.elevated,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box {
                if (childName != null) {
                    ChildAvatar(childAvatarUrl, Modifier.size(72.dp))
                    CheckBadge(Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(28.dp), iconSize = 16)
                } else {
                    CheckBadge(Modifier.size(64.dp), iconSize = 32)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.ap_result_paid_title),
                style = AppTypography.headlineSmSemiBold,
                color = AppColors.text.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.sub_success_body),
                style = AppTypography.bodyLgRegular,
                color = AppColors.text.secondary,
                textAlign = TextAlign.Center,
            )
            val forWhom = listOfNotNull(
                childName?.let { stringResource(Res.string.ap_for_child, it) },
                validUntil?.let { stringResource(Res.string.sub_success_until, it) },
            ).joinToString(" · ")
            if (forWhom.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = forWhom,
                    style = AppTypography.titleSmSemiBold,
                    color = AppColors.text.primary,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(28.dp))
            CustomButtonNew(
                text = stringResource(Res.string.ap_done),
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CheckBadge(modifier: Modifier, iconSize: Int) {
    Box(
        modifier = modifier
            .background(AppColors.bg.elevated, CircleShape)
            .padding(2.dp)
            .background(AppColors.action.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Check, contentDescription = null, tint = AppColors.icon.inverse, modifier = Modifier.size(iconSize.dp))
    }
}

@Preview
@Composable
private fun SubscriptionSuccessSheetPreview() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        SubscriptionSuccessSheet(
            show = true,
            childName = "Dilnoza",
            childAvatarUrl = null,
            validUntil = "16-dekabr 2026",
            onDone = {},
        )
    }
}
