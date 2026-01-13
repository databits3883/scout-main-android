package com.databits.androidscouting.model

import com.squareup.moshi.Json

/**
 * Type-safe enum for all cell types in the scouting system.
 * Replaces string-based type checking with compile-time type safety.
 *
 * @Json annotations ensure backward compatibility with existing JSON files.
 */
enum class CellType {
    @Json(name = "YesNo")
    YES_NO,

    @Json(name = "Text")
    TEXT,

    @Json(name = "Counter")
    COUNTER,

    @Json(name = "DoubleCounter")
    DOUBLE_COUNTER,

    @Json(name = "DualCounter")
    DUAL_COUNTER,

    @Json(name = "Segment")
    SEGMENT,

    @Json(name = "List")
    LIST,

    @Json(name = "TeamSelect")
    TEAM_SELECT,

    @Json(name = "Special")
    SPECIAL,

    @Json(name = "Title")
    TITLE;

    companion object {
        /**
         * Parse string value to enum.
         *
         * @param value String value from JSON
         * @return CellType enum or null if invalid
         */
        fun fromString(value: String?): CellType? = when (value) {
            "YesNo" -> YES_NO
            "Text" -> TEXT
            "Counter" -> COUNTER
            "DoubleCounter" -> DOUBLE_COUNTER
            "DualCounter" -> DUAL_COUNTER
            "Segment" -> SEGMENT
            "List" -> LIST
            "TeamSelect" -> TEAM_SELECT
            "Special" -> SPECIAL
            "Title" -> TITLE
            else -> null
        }
    }
}
