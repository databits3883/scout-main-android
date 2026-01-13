package com.databits.androidscouting.layout

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Singleton provider for Moshi instance.
 * Replaces per-parser Moshi instances with shared singleton for better performance.
 *
 * The Moshi instance is configured with:
 * - Generated adapters (KSP-generated, automatically registered)
 * - Kotlin reflection as fallback for classes without codegen
 */
object MoshiProvider {
    /**
     * Shared Moshi instance with Kotlin support and generated adapters.
     * Lazy initialization ensures thread-safe creation.
     *
     * Performance note: Generated adapters (from KSP) are automatically
     * added and used by default. Kotlin reflection is only used as a fallback
     * for classes without @JsonClass(generateAdapter = true).
     */
    val moshi: Moshi by lazy {
        Moshi.Builder()
            // Generated adapters are auto-registered by Moshi codegen
            // They take precedence over reflection adapters for performance

            // Add Kotlin reflection as fallback
            // Only used if codegen adapter doesn't exist
            .addLast(KotlinJsonAdapterFactory())

            .build()
    }
}
