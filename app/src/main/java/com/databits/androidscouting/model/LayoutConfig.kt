package com.databits.androidscouting.model

import com.squareup.moshi.JsonClass

/**
 * Kotlin version of LayoutConfig with Moshi codegen.
 * Top-level wrapper for layout JSON structure.
 */
@JsonClass(generateAdapter = true)
data class LayoutConfig(
    val cells: List<Cell>,
    val version: Int = 1
) {
    /**
     * Validate all cells in the layout.
     * Returns list of validation errors (empty if all valid).
     */
    fun validate(): List<ValidationResult.Error> {
        return cells.mapIndexedNotNull { index, cell ->
            val result = cell.validate()
            if (result is ValidationResult.Error) {
                ValidationResult.Error("Cell $index (${cell.title}): ${result.message}")
            } else {
                null
            }
        }
    }
}
