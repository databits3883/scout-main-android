package com.databits.androidscouting.core.model

enum class DeviceRole(val rawValue: String) {
    MASTER("master"),
    CROWD("crowd"),
    PIT("pit"),
    SPECIAL("special"),
    UNKNOWN("unknown");

    companion object {
        @JvmStatic
        fun fromRaw(raw: String?): DeviceRole {
            if (raw.isNullOrBlank()) {
                return UNKNOWN
            }

            return entries.firstOrNull { it.rawValue.equals(raw.trim(), ignoreCase = true) }
                ?: UNKNOWN
        }
    }
}
