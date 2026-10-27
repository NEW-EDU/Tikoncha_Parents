package uz.tikoncha_parent.domain.model.protection

enum class ChildMode {
    DEFAULT, GUJANAK, STRICT, UNKNOWN;

    companion object {
        fun from(value: String?): ChildMode =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class StrictMethod {
    TIMER, SECRET_CODE, TEXT, PARENT_REQUEST, UNKNOWN;

    companion object {
        fun from(value: String?): StrictMethod =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

/** Qalqon o'chirish so'rovi statuslari (strict tizimi) */
enum class StrictRequestStatus {
    PENDING, APPROVED, REJECTED, CANCELLED, EXPIRED, UNKNOWN;

    companion object {
        fun from(value: String?): StrictRequestStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

/**
 * Ruxsat key'lari + qaysi rejimdan boshlab zarurligi (tier jadvali).
 * Notanish key kelsa from() null qaytaradi — UI o'tkazib yuboradi (forward compatibility).
 */
enum class ChildPermission(val minMode: ChildMode) {
    NOTIFICATION(ChildMode.DEFAULT),
    USAGE_STATS(ChildMode.DEFAULT),
    OVERLAY(ChildMode.GUJANAK),
    OVERLAY_POPUP(ChildMode.GUJANAK),   // faqat Xiaomi qurilmalardan keladi
    LOCATION(ChildMode.GUJANAK),
    GPS_ENABLED(ChildMode.GUJANAK),
    BATTERY(ChildMode.GUJANAK),
    AUTO_START(ChildMode.GUJANAK),
    ACCESSIBILITY(ChildMode.STRICT),
    DEVICE_ADMIN(ChildMode.STRICT);

    companion object {
        fun from(key: String?): ChildPermission? =
            entries.find { it.name.equals(key, ignoreCase = true) }
    }
}


/** Hisobdan chiqish / ilovani o'chirish so'rovi turi */
enum class AccountRequestAction(val value: String) {
    LOGOUT("logout"),
    DELETE("delete"),
    /** Farzand akkauntini öçiriş — ruxsat berilsa server akkauntni öçiradi. */
    DELETE_ACCOUNT("delete_account"),
    UNKNOWN("");

    companion object {
        fun from(value: String?): AccountRequestAction =
            entries.find { it.value.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

/** Hisobdan chiqish / ilovani o'chirish so'rovi statuslari */
enum class AccountRequestStatus(val value: String) {
    PROCESS("process"),
    ACCESS("access"),
    DENY("deny"),
    UNKNOWN("");

    companion object {
        fun from(value: String?): AccountRequestStatus =
            entries.find { it.value.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}