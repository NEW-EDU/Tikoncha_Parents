package uz.tikoncha_parent.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.tikoncha_parent.domain.model.ai.AssistantEvent
import uz.tikoncha_parent.domain.model.ai.AssistantTurn
import uz.tikoncha_parent.domain.model.ai.ChildReport
import uz.tikoncha_parent.domain.model.ai.ReportPeriod
import uz.tikoncha_parent.domain.model.app_error.Outcome

interface AiRepository {
    suspend fun report(childId: String, period: ReportPeriod): Outcome<ChildReport>

    /** Javob oqimi; har doim [AssistantEvent.Done] yoki [AssistantEvent.Failed] bilan tugaydi. */
    fun ask(turns: List<AssistantTurn>, childId: String?): Flow<AssistantEvent>

    suspend fun applyDraft(draftId: String, coin: Int? = null): Outcome<Unit>

    suspend fun remind(childId: String, kind: String): Outcome<Unit>
    suspend fun discardDraft(draftId: String)
}
