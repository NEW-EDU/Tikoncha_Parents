package uz.tikoncha_parent.domain.model.policy

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

@Serializable
enum class PolicyPreset: JavaSerializable {
    SLEEP, APP_LIMIT, CONTENT, PROTECTION;
    companion object {
        fun from(raw: String?): PolicyPreset? = entries.firstOrNull { it.name == raw }
    }
}