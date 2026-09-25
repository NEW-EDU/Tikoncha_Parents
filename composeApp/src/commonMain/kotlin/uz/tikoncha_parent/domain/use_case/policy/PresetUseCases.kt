package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.map
import uz.tikoncha_parent.domain.model.policy.Patch
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.model.policy.ProtectionPackStatus
import uz.tikoncha_parent.domain.policy.PresetDefaults
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

/**
 * Tayyor jadval switch'i: bazada yo'q bo'lsa [PresetDefaults] bilan yaratadi,
 * bor bo'lsa faqat `is_active`. Yo'q narsani o'chirish — hech narsa qilmaydi.
 */
class TogglePresetUseCase(
    private val repository: PolicyRepository,
) {
    suspend operator fun invoke(
        childId: String,
        preset: PolicyPreset,
        existing: Policy?,
        enabled: Boolean,
        title: String,
    ): Outcome<Unit> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)
        return when {
            existing != null -> repository.patchPolicy(existing.id, PolicyPatch.toggle(enabled)).map { }
            enabled -> {
                val draft = PresetDefaults.of(preset, title) ?: return Outcome.Failure(ErrorCause.Validation)
                repository.createPolicy(childId, draft).map { }
            }
            else -> Outcome.Success(Unit)
        }
    }
}

/**
 * "Kontent himoya" — bitta switch hamma paket uchun (Student bilan bir xil).
 * Yoqish: men yoqmagan paketlarning hammasi. O'chirish: faqat o'zim yoqqanlarim —
 * farzand yoki ikkinchi ota-ona yoqqanini o'chira olmayman.
 */
class ToggleContentProtectionUseCase(
    private val togglePack: ToggleProtectionPackUseCase,
) {
    suspend operator fun invoke(
        childId: String,
        myUserId: String,
        statuses: List<ProtectionPackStatus>,
        enabled: Boolean,
        titleOf: (ProtectionPackStatus) -> String,
    ): Outcome<Unit> {
        val targets = if (enabled) statuses.filter { !it.enabledByMe } else statuses.filter { it.enabledByMe }
        for (s in targets) {
            val res = togglePack(childId, myUserId, s.pack.code, enabled, titleOf(s))
            if (res is Outcome.Failure) return res
        }
        return Outcome.Success(Unit)
    }
}

/** Serverdagi jadvaldan tahrir nusxasi (passthrough maydonlar — wifi, launch, iOS — saqlanadi). */
fun Policy.toDraft(): PolicyDraft = PolicyDraft(
    name = name,
    action = action,
    targets = targets,
    conditions = conditions,
    limits = limits,
    preset = preset,
    priority = priority,
    isActive = isActive,
    expiresAt = expiresAt,
    pausedUntil = pausedUntil,
)

/**
 * Tayyor jadval / o'zim yaratgan jadvalni saqlash: bazada yo'q bo'lsa yaratadi, bor bo'lsa
 * PATCH. Server `targets`/`conditions`/`limits` bo'limlarini BUTUNLAY almashtiradi —
 * qoralama serverdagidan olingani uchun passthrough maydonlar yo'qolmaydi.
 */
class SavePolicyDraftUseCase(
    private val create: CreatePolicyUseCase,
    private val update: UpdatePolicyUseCase,
) {
    suspend operator fun invoke(childId: String, policyId: String?, draft: PolicyDraft, saved: PolicyDraft?): Outcome<Policy> {
        if (policyId == null) return create(childId, draft)
        if (draft.name.isBlank()) return Outcome.Failure(ErrorCause.EmptyTitle)
        if (draft.targets.isEmpty) return Outcome.Failure(ErrorCause.Validation)
        val patch = PolicyPatch(
            name = if (saved?.name != draft.name) Patch.Value(draft.name.trim()) else Patch.Unset,
            action = if (saved?.action != draft.action) Patch.Value(draft.action) else Patch.Unset,
            targets = if (saved?.targets != draft.targets) Patch.Value(draft.targets) else Patch.Unset,
            conditions = if (saved?.conditions != draft.conditions) Patch.Value(draft.conditions) else Patch.Unset,
            limits = if (saved?.limits != draft.limits) Patch.Value(draft.limits) else Patch.Unset,
        )
        if (patch.isEmpty) return Outcome.Failure(ErrorCause.Validation)
        return update(policyId, patch)
    }
}
