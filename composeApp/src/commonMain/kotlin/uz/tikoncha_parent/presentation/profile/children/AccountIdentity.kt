package uz.tikoncha_parent.presentation.profile.children

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.apple_email_hidden_line
import tikoncha_parents.composeapp.generated.resources.google_hisobi
import tikoncha_parents.composeapp.generated.resources.ic_apple_logo
import tikoncha_parents.composeapp.generated.resources.ic_google_g
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.ui.theme.AppColors

/*
 * Farzand hisobini tanish: Google / Apple orqali kirgan farzandda telefon raqami yo'q. Ota-ona shunday farzandni
 * email bo'yicha taniydi; Apple "Hide My Email" bo'lsa — manzil ko'rsatilmaydi (foydasiz va farzandning tanlovi),
 * o'rniga "Apple ID · email yashirilgan".
 */

enum class SignInMethod { PHONE, GOOGLE, APPLE }

val UserInfo.signIn: SignInMethod?
    get() = when (signInMethod) {
        "google" -> SignInMethod.GOOGLE
        "apple" -> SignInMethod.APPLE
        "phone" -> SignInMethod.PHONE
        else -> if (phoneNumber.isNotBlank()) SignInMethod.PHONE else null
    }

/** Ro'yxatdagi "kim" qatori. [brand] — matn oldidagi Google / Apple belgisi (telefonda yo'q). */
class AccountLine(val text: String, val brand: SignInMethod?)

/** Ustuvorlik: telefon → email → Apple (email yashirilgan) → Google hisobi (email hali noma'lum). */
@Composable
fun UserInfo.accountLine(): AccountLine? {
    val method = signIn
    return when {
        phoneNumber.isNotBlank() -> AccountLine(phoneNumber, null)
        !email.isNullOrBlank() -> AccountLine(email, method?.takeIf { it != SignInMethod.PHONE })
        method == SignInMethod.APPLE -> AccountLine(stringResource(Res.string.apple_email_hidden_line), SignInMethod.APPLE)
        method == SignInMethod.GOOGLE -> AccountLine(stringResource(Res.string.google_hisobi), SignInMethod.GOOGLE)
        else -> null
    }
}

/** Google "G" (rangli) yoki Apple belgisi (matn rangida — qorong'i mavzuda ham ko'rinadi). */
@Composable
fun BrandMark(method: SignInMethod, size: Dp, modifier: Modifier = Modifier) {
    when (method) {
        SignInMethod.GOOGLE -> Image(painterResource(Res.drawable.ic_google_g), contentDescription = null, modifier = modifier.size(size))
        SignInMethod.APPLE -> Icon(
            painterResource(Res.drawable.ic_apple_logo),
            contentDescription = null,
            tint = AppColors.text.primary,
            modifier = modifier.size(size),
        )
        SignInMethod.PHONE -> Unit
    }
}
