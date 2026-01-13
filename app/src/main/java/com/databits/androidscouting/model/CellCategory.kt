package com.databits.androidscouting.model

import androidx.annotation.ColorRes
import com.databits.androidscouting.R
import com.squareup.moshi.Json

/**
 * Categories for organizing cells into Auto, Teleop, and Endgame phases.
 * Each category has an associated color resource for UI display.
 *
 * Eliminates the switch statement in MultiviewTypeAdapter for category colors.
 */
enum class CellCategory(@ColorRes val colorRes: Int) {
    @Json(name = "Auto")
    AUTO(R.color.auto_category),

    @Json(name = "Teleop")
    TELEOP(R.color.teleop_category),

    @Json(name = "Endgame")
    ENDGAME(R.color.endgame_category),

    @Json(name = "")
    NONE(android.R.color.transparent);

    companion object {
        /**
         * Parse string value to enum.
         * Returns NONE for null or empty strings.
         *
         * @param value Category string from JSON
         * @return CellCategory enum
         */
        fun fromString(value: String?): CellCategory = when (value) {
            "Auto" -> AUTO
            "Teleop" -> TELEOP
            "Endgame" -> ENDGAME
            else -> NONE
        }
    }
}
