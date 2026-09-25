package uz.tikoncha_parent.domain.model.policy

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

/** Jadval turi. QUICK_BLOCK — statistikadan bir bosishda qo'yilgan blok. */
@Serializable
enum class PolicyKind : JavaSerializable {
    STANDARD, QUICK_BLOCK;

    companion object {
        fun from(raw: String?): PolicyKind = entries.firstOrNull { it.name == raw } ?: STANDARD
    }
}