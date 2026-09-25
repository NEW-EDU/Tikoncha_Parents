package uz.tikoncha_parent.domain.model

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

/**
 * Jadval qamrovi (scope). [order] — ustuvorlik, katta = kuchli:
 * SCHOOL > PARENT_CHILD > STUDENT > ALL.
 */
@Serializable
enum class PolicyType(val order: Int) : JavaSerializable {
    SCHOOL(4), PARENT_CHILD(3), STUDENT(2), ALL(1);

    companion object {
        /** Noma'lum qiymat → ALL (eng past). Eski koddagi SCHOOL fallback'i xato edi. */
        fun from(raw: String?): PolicyType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: ALL
    }
}