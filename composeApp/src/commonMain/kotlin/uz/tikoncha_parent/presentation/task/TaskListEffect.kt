package uz.tikoncha_parent.presentation.task

import uz.tikoncha_parent.domain.model.app_error.Outcome

sealed interface TaskListEffect {
    data class ShowFailure(val failure: Outcome.Failure) : TaskListEffect
    /** Ota-ona qabul qildi: "Qabul qilindi. Malika 25 ta tanga oldi." (tanga yöq bölsa — faqat birinçi qism). */
    data class TaskMarkedAsCompleted(val taskId: String, val childName: String, val coin: Int) : TaskListEffect
    data object TaskDeleted : TaskListEffect
}