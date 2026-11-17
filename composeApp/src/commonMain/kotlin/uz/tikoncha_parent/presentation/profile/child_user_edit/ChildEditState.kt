package uz.tikoncha_parent.presentation.profile.child_user_edit

import uz.tikoncha_parent.domain.model.GenderType
import uz.tikoncha_parent.presentation.ui_state.ResponseState

data class ChildEditState(
    val firstName: String = "",
    val lastName: String = "",
    val patronymic: String = "",
    val age: String = "",
    val genderType: GenderType = GenderType.MALE,

    // Dirty check uchun original qiymatlar
    val originalFirstName: String = "",
    val originalLastName: String = "",
    val originalPatronymic: String = "",
    val originalGenderType: GenderType = GenderType.MALE,

    val saveState: ResponseState<Unit> = ResponseState.Idle
) {
    // Majburiy maydon
    private val isFirstNameValid: Boolean
        get() = firstName.trim().length >= 2

    // Ixtiyoriy: bo'sh OK, lekin to'ldirilsa kamida 2 ta belgi
    private val isLastNameValid: Boolean
        get() = lastName.isEmpty() || lastName.trim().length >= 2

    private val isPatronymicValid: Boolean
        get() = patronymic.isEmpty() || patronymic.trim().length >= 2

    // Foydalanuvchi biror narsani o'zgartirganmi?
    private val isDirty: Boolean
        get() = firstName.trim() != originalFirstName.trim() ||
                lastName.trim() != originalLastName.trim() ||
                patronymic.trim() != originalPatronymic.trim() ||
                genderType != originalGenderType

    val isFormValid: Boolean
        get() = isFirstNameValid &&
                isLastNameValid &&
                isPatronymicValid &&
                isDirty &&
                saveState !is ResponseState.Loading &&
                saveState !is ResponseState.Success // "Saqlandi" körinib turganda qayta yuborilmasin
}