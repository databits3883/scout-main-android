package com.databits.androidscouting.model

import androidx.annotation.DrawableRes
import com.databits.androidscouting.R
import com.squareup.moshi.Json

/**
 * Help picture types for cell help tooltips.
 * Each type maps to a drawable resource.
 *
 * Eliminates the switch statement in MultiviewTypeAdapter for help pictures.
 */
enum class HelpPictureType(@DrawableRes val drawableRes: Int) {
    @Json(name = "Speaker")
    SPEAKER(R.drawable.speaker),

    @Json(name = "Amp")
    AMP(R.drawable.amp),

    @Json(name = "Stage")
    STAGE(R.drawable.stage),

    @Json(name = "Endgame")
    ENDGAME(R.drawable.endgame),

    @Json(name = "Feeder")
    FEEDER(R.drawable.feeder),

    @Json(name = "Auto")
    AUTO(R.drawable.auto),

    @Json(name = "None")
    NONE(android.R.color.transparent);

    companion object {
        /**
         * Parse string value to enum.
         * Returns NONE for null or unrecognized values.
         *
         * @param value Help picture selector string from JSON
         * @return HelpPictureType enum
         */
        fun fromString(value: String?): HelpPictureType = when (value) {
            "Speaker" -> SPEAKER
            "Amp" -> AMP
            "Stage" -> STAGE
            "Endgame" -> ENDGAME
            "Feeder" -> FEEDER
            "Auto" -> AUTO
            else -> NONE
        }
    }
}
