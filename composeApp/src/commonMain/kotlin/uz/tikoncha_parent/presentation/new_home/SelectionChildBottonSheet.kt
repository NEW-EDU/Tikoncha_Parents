@file:OptIn(ExperimentalMaterial3Api::class)

package uz.tikoncha_parent.presentation.new_home

import uz.tikoncha_parent.ui.rememberCachedImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.add
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.ohirgi_faollik
import tikoncha_parents.composeapp.generated.resources.plus_home_sheet_subscribe
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomRadio
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.DividerHorizontal
import uz.tikoncha_parent.ui.SuccessColor
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme


@Composable
fun SelectionChildBottomSheet(
    navigator: Navigator?,
    modifier: Modifier = Modifier,
    title: String,
    items: List<UserInfo>,
    onDismiss: () -> Unit,
    selectedItem: UserInfo? = null,
    onItemSelected: (UserInfo) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = AppColors.bg.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.bg.surface)
                .padding(horizontal = 16.dp),

        ) {
            Text(
                text = title,
                style = AppTypography.titleLgSemiBold,
                color = AppColors.text.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                items.forEachIndexed { index, item ->
                    val userInfo = item as? UserInfo

                    if (userInfo != null) {
                        ChildListItem(
                            userInfo = userInfo,
                            isSelected = selectedItem == item,
                            onClick = {
                                onItemSelected(item)
                                onDismiss()
                            },
                            onCheckClick = {
                                onItemSelected(item)
                                onDismiss()
                            }
                        )
                    }

                    if (index < items.lastIndex)
                    DividerHorizontal(
                        color = AppColors.border.secondarySubtle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            CustomButton(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                text = stringResource(Res.string.farzand_qo_shish),
                onClick = {
                    navigator?.push(AddChildScreen())
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.add),
                        contentDescription = "",
                        tint = AppColors.icon.inverse,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ChildListItem(
    modifier: Modifier = Modifier,
    userInfo: UserInfo,
    onClick: () -> Unit,
    onCheckClick: () -> Unit,
    isSelected: Boolean = false,
) {
    val hasSubscription = !userInfo.subscription.isNullOrBlank() && userInfo.subscription != "FREE"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .singleClick { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .then(
                        if (hasSubscription) {
                            Modifier.border(2.dp, SuccessColor, CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .background(AppColors.bg.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = rememberCachedImage(userInfo.avatarUrl),
                    contentDescription = "",
                    error = painterResource(Res.drawable.profile_hedgehog_img),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            if (hasSubscription) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-8).dp)
                ) {
                    Image(
                        painter = painterResource(Res.drawable.plus_home_sheet_subscribe),
                        contentDescription = "",
                        modifier = Modifier
                            .height(22.dp)
                            .width(36.dp)
                    )
                }
            }
        }
        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = userInfo.toString(),
                style = AppTypography.titleMdMedium,
                color = AppColors.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.ohirgi_faollik),
                    style = AppTypography.titleSmMedium,
                    color = AppColors.text.tertiary,
                )
                Spacer(Modifier.width(4.dp))

                Text(
                    text = userInfo.last_seen.orEmpty(),
                    style = AppTypography.titleSmMedium,
                    color = AppColors.text.secondary,
                )
            }
        }

        CustomRadio(
            checked = isSelected,
            onChecked = { onCheckClick()}
        )
    }
}


@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ) {
        SelectionChildBottomSheet(
            navigator = null,
            title = "Farzandlaringiz",
            items = listOf(),
            onDismiss = {},
            onItemSelected = {}
        )
        ChildListItem(
            userInfo = UserInfo(
                userId = "1",
                phoneNumber = "+99890 123 45 67",
                fullName = "Tikoncha",
                name = "Tikoncha",
                lastName = "Tikoncha",
                patronymic = "Tikoncha",
                genderType = uz.tikoncha_parent.domain.model.GenderType.MALE,
            ),
            onClick = {},
            onCheckClick = {},
            isSelected = true
        )
    }
}