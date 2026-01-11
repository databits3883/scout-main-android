package com.databits.androidscouting.factory;

import com.preference.Preference;

/**
 * Configuration object for RecyclerView creation.
 * Encapsulates all settings needed to configure a RecyclerView.
 */
public class RecyclerViewConfig {
    public final boolean useGridLayout;
    public final boolean enableReordering;
    public final int gridColumnCount;

    /**
     * Create a RecyclerView configuration
     * @param useGridLayout Whether to use GridLayoutManager (true) or LinearLayoutManager (false)
     * @param enableReordering Whether to enable drag-and-drop reordering
     * @param gridColumnCount Number of columns for grid layout (ignored if useGridLayout is false)
     */
    public RecyclerViewConfig(boolean useGridLayout, boolean enableReordering, int gridColumnCount) {
        this.useGridLayout = useGridLayout;
        this.enableReordering = enableReordering;
        this.gridColumnCount = gridColumnCount;
    }

    /**
     * Create configuration from preference store
     * @param prefs Preference store containing configuration values
     * @return RecyclerViewConfig built from preferences
     */
    public static RecyclerViewConfig fromPreferences(Preference prefs) {
        return new RecyclerViewConfig(
            prefs.getBoolean("grid_toggle", true),
            prefs.getBoolean("reorder_cells_toggle", false),
            2  // Default grid column count
        );
    }
}
