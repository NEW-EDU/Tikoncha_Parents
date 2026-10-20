package uz.tikoncha_parent.data.repository

import uz.tikoncha_parent.data.remote.TikonchagramApiService
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.data.remote.model.tikonchagram.TgPolicyDto
import uz.tikoncha_parent.data.remote.model.tikonchagram.TgPolicyResponse
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.tikonchagram.TgRules
import uz.tikoncha_parent.domain.model.tikonchagram.TgRulesState
import uz.tikoncha_parent.domain.repository.TikonchagramRepository

class TikonchagramRepositoryImpl(
    private val api: TikonchagramApiService,
) : TikonchagramRepository {

    override suspend fun rules(childId: String): Outcome<TgRulesState> =
        apiCall(TAG) { api.policy(childId).toOutcome() }

    override suspend fun save(childId: String, rules: TgRules): Outcome<TgRulesState> =
        apiCall(TAG) { api.savePolicy(childId, rules.toDto()).toOutcome() }

    private fun TgPolicyResponse.toOutcome(): Outcome<TgRulesState> {
        val body = data
        return when {
            success && body != null -> Outcome.Success(TgRulesState(body.toRules(), body.isDefault ?: false))
            success -> Outcome.Failure(ErrorCause.InvalidResponse)
            else -> Outcome.Failure(ApiErrorMapper.fromCode(code), error)
        }
    }

    private companion object { const val TAG = "TikonchagramRepository" }
}

internal fun TgPolicyDto.toRules() = TgRules(
    enabled = enabled,
    hideChannels = hideChannels, hideGroups = hideGroups, hideBots = hideBots,
    contactsOnly = contactsOnly, allowNewChats = allowNewChats,
    allowStickers = allowStickers, allowGifs = allowGifs, allowPhotos = allowPhotos,
    allowVideos = allowVideos, allowVoice = allowVoice, allowVideoNotes = allowVideoNotes,
    allowDocuments = allowDocuments, allowLinks = allowLinks, allowForwards = allowForwards,
    showBlockedPlaceholder = showBlockedPlaceholder,
    maxChats = maxChats,
    dailyMaxMinutes = dailyMaxMinutes,
)

internal fun TgRules.toDto() = TgPolicyDto(
    enabled = enabled,
    hideChannels = hideChannels, hideGroups = hideGroups, hideBots = hideBots,
    contactsOnly = contactsOnly, allowNewChats = allowNewChats,
    allowStickers = allowStickers, allowGifs = allowGifs, allowPhotos = allowPhotos,
    allowVideos = allowVideos, allowVoice = allowVoice, allowVideoNotes = allowVideoNotes,
    allowDocuments = allowDocuments, allowLinks = allowLinks, allowForwards = allowForwards,
    showBlockedPlaceholder = showBlockedPlaceholder,
    maxChats = maxChats,
    dailyMaxMinutes = dailyMaxMinutes,
)
