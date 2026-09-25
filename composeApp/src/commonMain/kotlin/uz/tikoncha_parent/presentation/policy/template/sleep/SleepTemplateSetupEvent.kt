package uz.tikoncha_parent.presentation.policy.template.sleep

import kotlinx.datetime.LocalTime
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedState

sealed interface SleepTemplateSetupEvent {
    data class Init(val sharedState: PolicySharedState) : SleepTemplateSetupEvent

    data class SetStartTime(val time: LocalTime) : SleepTemplateSetupEvent
    data class SetEndTime(val time: LocalTime) : SleepTemplateSetupEvent
    data class SetTimeRange(val start: LocalTime, val end: LocalTime) : SleepTemplateSetupEvent

    data class Save(val sharedState: PolicySharedState) : SleepTemplateSetupEvent
    data class Delete(val policyId: String) : SleepTemplateSetupEvent
    data object ResetResponseState : SleepTemplateSetupEvent
}