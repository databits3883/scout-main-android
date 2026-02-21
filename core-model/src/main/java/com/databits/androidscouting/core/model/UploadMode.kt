package com.databits.androidscouting.core.model

enum class UploadMode(
    val rawValue: String,
    val queueType: String,
) {
    CROWD("Crowd", "CROWD"),
    SPECIALTY("Specialty", "SPECIALTY"),
    PIT("Pit", "PIT");

    companion object {
        @JvmStatic
        fun fromRaw(raw: String?): UploadMode {
            if (raw.isNullOrBlank()) {
                return CROWD
            }

            val normalized = raw.trim()
            return when {
                normalized.equals(CROWD.rawValue, ignoreCase = true) -> CROWD
                normalized.equals(SPECIALTY.rawValue, ignoreCase = true) -> SPECIALTY
                normalized.equals("Speciality", ignoreCase = true) -> SPECIALTY
                normalized.equals(PIT.rawValue, ignoreCase = true) -> PIT
                else -> CROWD
            }
        }
    }
}
