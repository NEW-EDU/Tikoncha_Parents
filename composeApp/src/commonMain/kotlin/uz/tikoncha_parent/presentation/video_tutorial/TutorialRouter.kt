package uz.tikoncha_parent.presentation.video_tutorial

import cafe.adriel.voyager.navigator.Navigator
import uz.tikoncha_parent.presentation.onboarding.OnboardingScreen
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs

/** Qo'llanma videolari hozircha faqat o'zbek tilida yozilgan. */
fun hasTutorialVideo(): Boolean = LanguagePrefs.loadOrDefault().languageCode == "uz"

/**
 * "Hoglt nima?" — barcha tillarda (o'zbekchada ham) ilova ichidagi tanishtiruv sahnalari (foydalanuvchi qarori,
 * 2026-10-23: YouTube videosi emas). Qolgan qo'llanmalar: o'zbek tilida — video. "Farzand qo'shish" sahifasining
 * o'zi qadam-baqadam qo'llanma, shuning uchun u yerda boshqa tillarda video tugmasi ko'rsatilmaydi.
 */
fun Navigator.openTutorial(type: TutorialType) {
    when {
        type == TutorialType.TIKONCHA -> push(OnboardingScreen(tourOnly = true))
        else -> push(VideoTutorialYoutubeScreen(type))
    }
}
