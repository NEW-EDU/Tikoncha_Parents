package uz.tikoncha_parent.presentation.profile.personal_information

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.ui.AppIconInnerPadding
import uz.tikoncha_parent.ui.CardCornerRadius
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.SpaceUltraSmall
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ayol
import tikoncha_parents.composeapp.generated.resources.calendar
import tikoncha_parents.composeapp.generated.resources.class_icon
import tikoncha_parents.composeapp.generated.resources.edit_pen
import tikoncha_parents.composeapp.generated.resources.erkak
import tikoncha_parents.composeapp.generated.resources.gender_female
import tikoncha_parents.composeapp.generated.resources.gender_male
import tikoncha_parents.composeapp.generated.resources.ism
import tikoncha_parents.composeapp.generated.resources.jins
import tikoncha_parents.composeapp.generated.resources.maktab
import tikoncha_parents.composeapp.generated.resources.phone
import tikoncha_parents.composeapp.generated.resources.profile
import tikoncha_parents.composeapp.generated.resources.school_icon
import tikoncha_parents.composeapp.generated.resources.shift_clock
import tikoncha_parents.composeapp.generated.resources.sinf
import tikoncha_parents.composeapp.generated.resources.smena
import tikoncha_parents.composeapp.generated.resources.tahrirlash
import tikoncha_parents.composeapp.generated.resources.telefon_nomer
import tikoncha_parents.composeapp.generated.resources.two_users
import tikoncha_parents.composeapp.generated.resources.yosh
import uz.tikoncha_parent.domain.model.GenderType
import uz.tikoncha_parent.presentation.base.CustomOutlinedButton
import uz.tikoncha_parent.ui.NormalIconSize
import uz.tikoncha_parent.ui.PrimaryColor
import uz.tikoncha_parent.ui.SmallButtonHeight
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun PersonalInfoItem(
    userInfo: UserInfo?,
    onEdit: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(AppColors.bg.surface)
            .padding(horizontal = AppIconInnerPadding, vertical = ContainerPadding)
    ) {
        PersonalInformationItemRow(
            icon = Res.drawable.profile,
            title = stringResource(Res.string.ism),
            value = userInfo?.name ?: ""
        )
        SpaceUltraSmall()

        if (userInfo?.age != 0 && userInfo?.age != null) {
            PersonalInformationItemRow(
                icon = Res.drawable.calendar,
                title = stringResource(Res.string.yosh),
                value = "${userInfo.age}"
            )
            SpaceUltraSmall()
        }

        val genderRes = when (userInfo?.genderType) {
            GenderType.MALE -> {
                stringResource(Res.string.erkak)
            }

            GenderType.FEMALE -> {
                stringResource(Res.string.ayol)
            }

            null -> ""
        }

        PersonalInformationItemRow(
            icon = if (userInfo?.genderType == GenderType.FEMALE) {
                Res.drawable.gender_female
            } else {
                Res.drawable.gender_male
            },
            title = stringResource(Res.string.jins),
            value = genderRes
        )
        SpaceUltraSmall()

        PersonalInformationItemRow(
            icon = Res.drawable.phone,
            title = stringResource(Res.string.telefon_nomer),
            value = userInfo?.phoneNumber
        )
        SpaceUltraSmall()

        if (userInfo?.schoolName != null) {
            Column {

                PersonalInformationItemRow(
                    icon = Res.drawable.school_icon,
                    title = stringResource(Res.string.maktab),
                    value = userInfo.schoolName
                )
                SpaceUltraSmall()

                PersonalInformationItemRow(
                    icon = Res.drawable.class_icon,
                    title = stringResource(Res.string.sinf),
                    value = userInfo.schoolClassName ?: ""
                )
                SpaceUltraSmall()

                PersonalInformationItemRow(
                    icon = Res.drawable.shift_clock,
                    title = stringResource(Res.string.smena),
                    value = ShiftType.getShiftByKey(userInfo.shift)
                        ?.let { stringResource(it.resId) }
                        ?: userInfo.shift
                        ?: ""
                )
            }
        }
        SpaceUltraSmall()

        CustomOutlinedButton(
            text = stringResource(Res.string.tahrirlash),
            containerColor = Color.Transparent,
            onClick = { onEdit() },
            leadingIcon = {
                Image(
                    painter = painterResource(Res.drawable.edit_pen),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(PrimaryColor),
                    modifier = Modifier
                        .size(NormalIconSize)
                )
            },
            modifier = Modifier
                .align(Alignment.End)
                .height(SmallButtonHeight)
        )
    }
}

@Preview(name = "Phone", "spec:width=360dp,height=800dp,dpi=420")
//@Preview(name = "Small",  "spec:width=320dp,height=640dp,dpi=420")
//@Preview(name = "Tablet", "spec:width=800dp,height=1280dp,dpi=240")
//@Preview(name = "Landscape", "spec:width=800dp,height=360dp,dpi=420")
@Composable
private fun PreviewPersonalInformationScreen() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        PersonalInfoItem(
            userInfo = UserInfo(
                userId = "",
                phoneNumber = "+998950457405",
                fullName = "",
                name = "Ilhom",
                lastName = "Isomiddinov",
                patronymic = "Islomjon o'g'li",
                genderType = GenderType.MALE,
                age = 12,
                schoolName = "13-Maktab",
                schoolClassName = "11-A",
                shift = "Kunduzgi"
            )
        )
    }
}
