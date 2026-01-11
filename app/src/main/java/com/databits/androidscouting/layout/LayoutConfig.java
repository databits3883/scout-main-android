package com.databits.androidscouting.layout;

import com.databits.androidscouting.model.Cell;
import java.util.List;

/**
 * Simple wrapper class for JSON layout deserialization.
 * Matches the structure of MultiviewTypeAdapter for JSON compatibility.
 */
public class LayoutConfig {
    public List<Cell> mCell;

    public LayoutConfig() {
        // Default constructor for Moshi
    }

    public LayoutConfig(List<Cell> cells) {
        this.mCell = cells;
    }
}
