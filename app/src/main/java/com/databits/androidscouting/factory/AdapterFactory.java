package com.databits.androidscouting.factory;

import com.databits.androidscouting.adapter.MultiviewTypeAdapter;
import com.databits.androidscouting.model.Cell;
import java.util.Collections;
import java.util.List;

/**
 * Factory for creating MultiviewTypeAdapter instances.
 * Provides a clean interface for adapter creation without unnecessary serialization.
 */
public class AdapterFactory {

    /**
     * Create adapter from cell list
     * @param cells List of cells to display in the adapter
     * @return Configured MultiviewTypeAdapter
     */
    public MultiviewTypeAdapter create(List<Cell> cells) {
        if (cells == null || cells.isEmpty()) {
            return createEmptyAdapter();
        }
        return new MultiviewTypeAdapter(cells);
    }

    /**
     * Create empty adapter for initial state
     * @return MultiviewTypeAdapter with no cells
     */
    public MultiviewTypeAdapter createEmptyAdapter() {
        return new MultiviewTypeAdapter(Collections.emptyList());
    }
}
