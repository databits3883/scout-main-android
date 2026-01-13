package com.databits.androidscouting.model

import com.squareup.moshi.JsonClass

/**
 * Configuration parameters for a cell.
 * Replaces Java CellParam class with Kotlin data class + Moshi codegen.
 */
@JsonClass(generateAdapter = true)
data class CellConfig(
    // Type (redundant with Cell.type but kept for consistency)
    val type: String? = null,

    // Counter configuration
    val defaultValue: Int = 0,
    val max: Int = 0,
    val min: Int = 0,
    val unit: Int = 1,

    // Segment configuration
    val segments: Int = 0,
    val segmentLabels: List<String> = emptyList(),

    // List configuration
    val totalEntries: Int = 0,
    val entryLabels: List<String> = emptyList(),

    // Text configuration
    val isTextHidden: Boolean = false,
    val textHint: String? = null,

    // Category
    val categoryString: String? = null,

    // Help configuration
    val helpTitle: String? = null,
    val helpPictureSelector: String? = null,
    val helpText: String? = null,

    // Special configuration
    val special: Int = 0,
    val specialTeamColor: String? = null
) {
    /**
     * Get category as enum.
     * Computed property for type-safe access.
     */
    val category: CellCategory
        get() = CellCategory.fromString(categoryString)

    /**
     * Get help picture type as enum.
     * Computed property for type-safe access.
     */
    val helpPicture: HelpPictureType
        get() = HelpPictureType.fromString(helpPictureSelector)

    /**
     * Validate configuration for a specific cell type.
     * Returns validation result with error message if invalid.
     */
    fun validate(cellType: CellType): ValidationResult {
        return when (cellType) {
            CellType.COUNTER, CellType.DOUBLE_COUNTER, CellType.DUAL_COUNTER -> {
                if (max < min) {
                    ValidationResult.error("Max ($max) must be >= min ($min)")
                } else if (unit <= 0) {
                    ValidationResult.error("Unit must be positive")
                } else if (defaultValue < min || defaultValue > max) {
                    ValidationResult.error("Default ($defaultValue) must be between $min and $max")
                } else {
                    ValidationResult.success()
                }
            }
            CellType.SEGMENT -> {
                if (segments <= 0) {
                    ValidationResult.error("Segment count must be positive")
                } else if (segments > 6) {
                    ValidationResult.error("Segment count cannot exceed 6")
                } else if (segmentLabels.size != segments) {
                    ValidationResult.error("Expected $segments labels, got ${segmentLabels.size}")
                } else {
                    ValidationResult.success()
                }
            }
            CellType.LIST -> {
                if (totalEntries <= 0) {
                    ValidationResult.error("Total entries must be positive")
                } else if (entryLabels.size != totalEntries) {
                    ValidationResult.error("Expected $totalEntries labels, got ${entryLabels.size}")
                } else {
                    ValidationResult.success()
                }
            }
            CellType.TEXT -> {
                // Text fields are always valid - empty hints are allowed
                ValidationResult.success()
            }
            else -> ValidationResult.success()
        }
    }
}

/**
 * Result of validation operation.
 * Sealed class provides type-safe success/error handling.
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    companion object {
        fun success() = Success
        fun error(message: String) = Error(message)
    }
}
