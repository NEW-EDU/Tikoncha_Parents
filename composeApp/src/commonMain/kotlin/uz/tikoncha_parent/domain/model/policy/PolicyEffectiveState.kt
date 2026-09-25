package uz.tikoncha_parent.domain.model.policy

import cafe.adriel.voyager.core.lifecycle.JavaSerializable

enum class PolicyEffectiveState: JavaSerializable {
    /** Ishlayapti. */
    ACTIVE,
    /** paused_until hali kelmagan — vaqtincha to'xtatilgan. */
    PAUSED,
    /** expires_at o'tib ketgan. */
    EXPIRED,
    /** is_active = false yoki o'chirilgan. */
    OFF
}