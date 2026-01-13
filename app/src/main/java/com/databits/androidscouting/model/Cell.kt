package com.databits.androidscouting.model

import com.squareup.moshi.JsonClass

/**
 * Kotlin version of Cell with Moshi codegen.
 * Replaces Cell.java with type-safe immutable data class.
 */
@JsonClass(generateAdapter = true)
data class Cell(
    val id: Int,
    val title: String,
    val typeString: String,
    val config: CellConfig
) {
    /**
     * Get type as enum.
     * Computed property for type-safe access.
     */
    val type: CellType
        get() = CellType.fromString(typeString) ?: CellType.TEXT

    /**
     * Validate this cell's configuration.
     * Returns validation result with any errors.
     */
    fun validate(): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult.error("Title cannot be blank")
        }

        val cellType = type
        return config.validate(cellType)
    }
}
