package uz.tikoncha_parent.domain.model.policy

import cafe.adriel.voyager.core.lifecycle.JavaSerializable

enum class TargetType: JavaSerializable {
    APP, SITE, FEATURE;

    companion object {
        fun from(raw: String?): TargetType = entries.firstOrNull {it.name == raw} ?: APP
    }
}