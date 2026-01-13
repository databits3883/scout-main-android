package com.databits.androidscouting.layout;

import com.databits.androidscouting.model.Cell;
import com.databits.androidscouting.model.LayoutConfig;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.List;

/**
 * Parses layout JSON into cell list with validation and error handling.
 * Uses Moshi to parse modern Kotlin data classes directly.
 */
public class LayoutParser {
    private final JsonAdapter<LayoutConfig> adapter;

    public LayoutParser() {
        Moshi moshi = MoshiProvider.INSTANCE.getMoshi();
        this.adapter = moshi.adapter(LayoutConfig.class);
    }

    /**
     * Parse layout JSON into cell list.
     *
     * @param jsonString Raw JSON string (with or without ^ delimiter)
     * @return ParseResult containing cells on success or error message on failure
     */
    public ParseResult parse(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return ParseResult.error("Layout JSON is empty");
        }

        // Remove legacy ^ delimiter if present
        String cleanJson = jsonString.contains("^")
            ? jsonString.split("\\^")[0]
            : jsonString;

        try {
            LayoutConfig config = adapter.fromJson(cleanJson);
            if (config == null) {
                return ParseResult.error("Failed to parse layout: null result");
            }

            return ParseResult.success(config.getCells());
        } catch (Exception e) {
            return ParseResult.error("Failed to parse layout: " + e.getMessage());
        }
    }
}
