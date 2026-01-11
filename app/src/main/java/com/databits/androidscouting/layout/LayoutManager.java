package com.databits.androidscouting.layout;

import android.content.res.Resources;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import com.databits.androidscouting.util.FileUtils;
import java.io.File;

/**
 * Coordinates the entire layout loading process.
 * Orchestrates parser, presenter, and file utilities to load layouts from various sources.
 */
public class LayoutManager {
    private final LayoutParser parser;
    private final LayoutPresenter presenter;
    private final FileUtils fileUtils;

    public LayoutManager(
        LayoutParser parser,
        LayoutPresenter presenter,
        FileUtils fileUtils
    ) {
        this.parser = parser;
        this.presenter = presenter;
        this.fileUtils = fileUtils;
    }

    /**
     * Load layout from file
     * @param layoutFile Layout JSON file
     * @param recyclerView Target RecyclerView
     * @param lifecycleOwner Fragment lifecycle
     * @return true if successful, false otherwise
     */
    public boolean loadLayout(File layoutFile, RecyclerView recyclerView, LifecycleOwner lifecycleOwner) {
        if (!fileUtils.fileExists(layoutFile.toString())) {
            presenter.showError(recyclerView, "Layout file not found: " + layoutFile.getName());
            return false;
        }

        String jsonString = fileUtils.readFile(layoutFile);
        return loadLayoutFromJson(jsonString, recyclerView, lifecycleOwner);
    }

    /**
     * Load layout from raw resource
     * @param rawResourceId Resource ID of the raw layout JSON file
     * @param recyclerView Target RecyclerView
     * @param lifecycleOwner Fragment lifecycle
     * @param resources Android Resources for accessing raw files
     * @return true if successful, false otherwise
     */
    public boolean loadLayout(int rawResourceId, RecyclerView recyclerView, LifecycleOwner lifecycleOwner, Resources resources) {
        try {
            String jsonString = fileUtils.readTextFile(resources.openRawResource(rawResourceId));
            return loadLayoutFromJson(jsonString, recyclerView, lifecycleOwner);
        } catch (Exception e) {
            presenter.showError(recyclerView, "Failed to load layout resource: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load layout from JSON string
     * @param jsonString Raw JSON string
     * @param recyclerView Target RecyclerView
     * @param lifecycleOwner Fragment lifecycle
     * @return true if successful, false otherwise
     */
    public boolean loadLayoutFromJson(String jsonString, RecyclerView recyclerView, LifecycleOwner lifecycleOwner) {
        ParseResult result = parser.parse(jsonString);

        if (result.isSuccess) {
            presenter.applyLayout(recyclerView, result.cells, lifecycleOwner);
            return true;
        } else {
            presenter.showError(recyclerView, result.errorMessage);
            return false;
        }
    }
}
