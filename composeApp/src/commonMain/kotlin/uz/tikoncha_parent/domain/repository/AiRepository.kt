package uz.tikoncha_parent.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.tikoncha_parent.domain.model.ai.AssistantEvent
import uz.tikoncha_parent.domain.model.ai.AssistantTurn
import uz.tikoncha_parent.domain.model.ai.ChildReport
import uz.tikoncha_parent.domain.model.ai.DraftApplyError
import uz.tikoncha_parent.domain.model.ai.Transcript
import uz.tikoncha_parent.domain.model.ai.ReportPeriod
import uz.tikoncha_parent.domain.model.ai.SafetyAlert
import uz.tikoncha_parent.domain.model.app_error.Outcome

interface AiRepository {
    suspend fun report(childId: String, period: ReportPeriod): Outcome<ChildReport>

    /** Javob oqimi; har doim [AssistantEvent.Done] yoki [AssistantEvent.Failed] bilan tugaydi. */
    /** [inputLang] — ovozli savolning STT aniqlagan tili (javob tili uçun); yozilgan savolda null. */
    fun ask(turns: List<AssistantTurn>, childId: String?, inputLang: String? = null): Flow<AssistantEvent>

    /** Muvaffaqiyat — null; xato — server matni va maşina kodi. */
    suspend fun applyDraft(draftId: String, coin: Int? = null): DraftApplyError?

    suspend fun remind(childId: String, kind: String): Outcome<Unit>
    suspend fun discardDraft(draftId: String)

    suspend fun alerts(childId: String?): Outcome<List<SafetyAlert>>

    /** Ro'yxatdagi "Bilimdon AI" suhbatini öqilgan deb belgilaş (ogohlantiriş xabarlari shu yerda). */
    suspend fun markChatRead(chatId: String)

    /** Ovozli savol → matn (`POST /ai/stt`). Nutq topilmasa — böş matn. [lang] — ilova tili (masalan "uz-Cyrl"). */
    suspend fun transcribe(audio: ByteArray, fileName: String, mimeType: String, lang: String): Outcome<Transcript>

    /** Javobni ovoz bilan öqiş (MP3 baytlari). Server buni qöllamasa — [Outcome.Failure] va [ttsUnsupported] = true. */
    suspend fun speech(text: String, lang: String): Outcome<ByteArray>

    /** Server matnni ovozga aylantira olmaydi (404 / 503) — tugma yaşiriladi. */
    val ttsUnsupported: Boolean
}
