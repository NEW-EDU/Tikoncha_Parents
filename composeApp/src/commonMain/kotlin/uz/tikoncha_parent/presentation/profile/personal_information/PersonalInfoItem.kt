package uz.tikoncha_parent.presentation.profile.personal_information

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
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
import tikoncha_parents.composeapp.generated.resources.yosh
import uz.tikoncha_parent.domain.model.GenderType
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.base.CustomButtonDash
import uz.tikoncha_parent.ui.NormalIconSize
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun PersonalInfoItem(
    userInfo: UserInfo?,
    onEdit: () -> Unit = {}
) {
    // M3 Expressive guruhlangan ro'yxat: har bir ma'lumot alohida segment, birgalikda bitta blok.
    val rows = buildList {
        add(InfoRowData(Res.drawable.profile, stringResource(Res.string.ism), userInfo?.name.orEmpty()))
        if (userInfo?.age != 0 && userInfo?.age != null) {
            add(InfoRowData(Res.drawable.calendar, stringResource(Res.string.yosh), "${userInfo.age}"))
        }
        val gender = when (userInfo?.genderType) {
            GenderType.MALE -> stringResource(Res.string.erkak)
            GenderType.FEMALE -> stringResource(Res.string.ayol)
            null -> ""
        }
        add(
            InfoRowData(
                if (userInfo?.genderType == GenderType.FEMALE) Res.drawable.gender_female else Res.drawable.gender_male,
                stringResource(Res.string.jins),
                gender,
            )
        )
        add(InfoRowData(Res.drawable.phone, stringResource(Res.string.telefon_nomer), userInfo?.phoneNumber.orEmpty()))
        if (userInfo?.schoolName != null) {
            add(InfoRowData(Res.drawable.school_icon, stringResource(Res.string.maktab), userInfo.schoolName))
            add(InfoRowData(Res.drawable.class_icon, stringResource(Res.string.sinf), userInfo.schoolClassName.orEmpty()))
            add(
                InfoRowData(
                    Res.drawable.shift_clock,
                    stringResource(Res.string.smena),
                    ShiftType.getShiftByKey(userInfo.shift)?.let { stringResource(it.resId) } ?: userInfo.shift.orEmpty(),
                )
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
            rows.forEachIndexed { index, row ->
                PersonalInformationItemRow(
                    icon = row.icon,
                    title = row.title,
                    value = row.value,
                    segmentIndex = index,
                    segmentCount = rows.size,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        CustomButtonDash(
            text = stringResource(Res.string.tahrirlash),
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.edit_pen),
                    contentDescription = null,
                    tint = AppColors.text.accentEmphasis,
                    modifier = Modifier.size(NormalIconSize)
                )
            },
        )
    }
}

private class InfoRowData(val icon: DrawableResource, val title: String, val value: String)

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
