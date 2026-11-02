package uz.tikoncha_parent.domain.repository

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.tikonchagram.TgRules
import uz.tikoncha_parent.domain.model.tikonchagram.TgRulesState

interface TikonchagramRepository {
    suspend fun rules(childId: String): Outcome<TgRulesState>
    suspend fun save(childId: String, rules: TgRules): Outcome<TgRulesState>
}
