package com.databits.androidscouting.layout;

import com.databits.androidscouting.model.Cell;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.util.List;

/**
 * Parses layout JSON into cell list with validation and error handling.
 * Handles legacy format with ^ delimiter for backwards compatibility.
 */
public class LayoutParser {
    private final Moshi moshi;
    private final JsonAdapter<LayoutConfig> jsonAdapter;

    public LayoutParser() {
        this.moshi = new Moshi.Builder().build();
        this.jsonAdapter = moshi.adapter(LayoutConfig.class);
    }

    /**
     * Parse layout JSON into cell list
     * @param jsonString Raw JSON string (with or without ^ delimiter)
     * @return ParseResult containing cells on success or error message on failure
     */
    public ParseResult parse(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return ParseResult.error("Layout JSON is empty");
        }

        try {
            // Remove legacy ^ delimiter if present
            String cleanJson = removeDelimiter(jsonString);

            LayoutConfig config = jsonAdapter.fromJson(cleanJson);

            if (config == null) {
                return ParseResult.error("Failed to parse layout: Invalid JSON structure");
            }

            if (config.mCell == null || config.mCell.isEmpty()) {
                return ParseResult.error("Layout file contains no cells");
            }

            return ParseResult.success(config.mCell);

        } catch (IOException e) {
            return ParseResult.error("Failed to parse layout: " + e.getMessage());
        } catch (Exception e) {
            return ParseResult.error("Unexpected error parsing layout: " + e.getMessage());
        }
    }

    /**
     * Remove legacy ^ delimiter from JSON string
     * The ^ delimiter was used in older versions to separate layout data from metadata.
     * This method extracts only the layout portion before the ^ for backwards compatibility.
     *
     * @param jsonString Original JSON string that may contain ^ delimiter
     * @return Clean JSON string with delimiter removed
     */
    private String removeDelimiter(String jsonString) {
        if (jsonString.contains("^")) {
            // Split on ^ and take only the first part (layout data)
            // Second part after ^ is discarded (legacy metadata)
            return jsonString.split("\\^")[0];
        }
        return jsonString;
    }
}
