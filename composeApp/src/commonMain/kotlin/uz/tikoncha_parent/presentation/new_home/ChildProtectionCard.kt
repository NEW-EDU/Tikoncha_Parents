package uz.tikoncha_parent.presentation.new_home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.farzand_himoyasi
import tikoncha_parents.composeapp.generated.resources.farzand_qoshilmagan
import tikoncha_parents.composeapp.generated.resources.guard
import tikoncha_parents.composeapp.generated.resources.guard_danger
import tikoncha_parents.composeapp.generated.resources.guard_neutral
import tikoncha_parents.composeapp.generated.resources.guard_warning
import tikoncha_parents.composeapp.generated.resources.ta_ruxsat_ochirilgan
import tikoncha_parents.composeapp.generated.resources.ta_yangi_sorov_kutilmoqda
import tikoncha_parents.composeapp.generated.resources.tekshirilmoqda
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.CardCornerPadding
import uz.tikoncha_parent.ui.CardCornerRadius
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

/** Rang va belgi qalqonda, sonlar matnda. Hammasi joyida bo'lsa — faqat ko'k ✓, matnsiz. */
private enum class ProtectionLevel(val shield: DrawableResource) {
    Ok(Res.drawable.guard),
    Warning(Res.drawable.guard_warning),
    Danger(Res.drawable.guard_danger),
    Unknown(Res.drawable.guard_neutral),
}

@Composable
fun ChildProtectionCard(
    permissionOffCount: Int,
    pendingRequestCount: Int,
    hasChild: Boolean,
    isLoaded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Farzand yo'q, holat yuklanmagan yoki xato — "hammasi joyida" deb ko'rsatmaymiz
    val level = when {
        !hasChild || !isLoaded -> ProtectionLevel.Unknown
        permissionOffCount > 0 -> ProtectionLevel.Danger
        pendingRequestCount > 0 -> ProtectionLevel.Warning
        else -> ProtectionLevel.Ok
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .simpleShadow(RoundedCornerShape(CardCornerRadius))
            .background(AppColors.bg.surface, RoundedCornerShape(CardCornerRadius))
            .clip(RoundedCornerShape(CardCornerRadius))
            .singleClick { onClick() }
            .padding(CardCornerPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.farzand_himoyasi),
                style = AppTypography.displaySmRegular,
                color = AppColors.text.primary,
            )
            when (level) {
                ProtectionLevel.Unknown -> ProtectionSubtitle(
                    text = stringResource(
                        if (hasChild) Res.string.tekshirilmoqda else Res.string.farzand_qoshilmagan
                    ),
                    color = AppColors.text.tertiary,
                )

                // Hammasi joyida — ko'k ✓ qalqonning o'zi yetarli, pastki matn yo'q
                ProtectionLevel.Ok -> Unit

                // Sariq matn oq fonda o'qilmaydi — sariqlikni qalqon beradi, matn oddiy rangda
                ProtectionLevel.Warning -> ProtectionSubtitle(
                    text = pluralStringResource(
                        Res.plurals.ta_yangi_sorov_kutilmoqda, pendingRequestCount, pendingRequestCount
                    ),
                    color = AppColors.text.secondary,
                )

                // Ikkalasi bo'lsa — ikki qator: bitta qatorga sig'maydi
                ProtectionLevel.Danger -> {
                    ProtectionSubtitle(
                        text = pluralStringResource(
                            Res.plurals.ta_ruxsat_ochirilgan, permissionOffCount, permissionOffCount
                        ),
                        color = AppColors.text.accentDanger,
                    )
                    if (pendingRequestCount > 0) {
                        ProtectionSubtitle(
                            text = pluralStringResource(
                                Res.plurals.ta_yangi_sorov_kutilmoqda, pendingRequestCount, pendingRequestCount
                            ),
                            color = AppColors.text.secondary,
                            topPadding = 0.dp,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Image(
            painter = painterResource(level.shield),
            // Holatni qalqonning rangi va belgisi ifodalaydi
            contentDescription = null,
            modifier = Modifier
                .padding(end = 5.6.dp)
                .size(80.dp),
        )
    }
}

@Composable
private fun ProtectionSubtitle(
    text: String,
    color: Color,
    topPadding: Dp = 2.dp,
) {
    Text(
        text = text,
        style = AppTypography.titleSmMedium,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = topPadding),
    )
}

@Preview
@Composable
private fun ChildProtectionCardPreview() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Column(
            modifier = Modifier
                .background(AppColors.bg.page)
                .padding(16.dp)
        ) {
            // Hammasi joyida
            ChildProtectionCard(
                permissionOffCount = 0, pendingRequestCount = 0,
                hasChild = true, isLoaded = true,
                onClick = {}, modifier = Modifier.fillMaxWidth(),
            )
            Space(12.dp)
            // So'rov kutilmoqda
            ChildProtectionCard(
                permissionOffCount = 0, pendingRequestCount = 1,
                hasChild = true, isLoaded = true,
                onClick = {}, modifier = Modifier.fillMaxWidth(),
            )
            Space(12.dp)
            // Ruxsat o'chirilgan + so'rov
            ChildProtectionCard(
                permissionOffCount = 2, pendingRequestCount = 1,
                hasChild = true, isLoaded = true,
                onClick = {}, modifier = Modifier.fillMaxWidth(),
            )
            Space(12.dp)
            // Yuklanmoqda
            ChildProtectionCard(
                permissionOffCount = 0, pendingRequestCount = 0,
                hasChild = true, isLoaded = false,
                onClick = {}, modifier = Modifier.fillMaxWidth(),
            )
            Space(12.dp)
            // Farzand yo'q
            ChildProtectionCard(
                permissionOffCount = 0, pendingRequestCount = 0,
                hasChild = false, isLoaded = false,
                onClick = {}, modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}