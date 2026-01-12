package com.databits.androidscouting.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.databits.androidscouting.R;
import com.databits.androidscouting.data.repository.PowerPreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.databits.androidscouting.factory.RecyclerViewConfig;
import com.databits.androidscouting.factory.RecyclerViewFactory;
import com.databits.androidscouting.layout.LayoutManager;
import com.databits.androidscouting.layout.LayoutParser;
import com.databits.androidscouting.layout.LayoutPresenter;
import com.databits.androidscouting.util.FileUtils;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.ScoutUtils;
import com.databits.androidscouting.util.TeamInfo;
import java.io.File;
import java.util.Objects;

/**
 * Base class for scout fragments (Crowd, Pit, Special).
 * Eliminates duplication by extracting common layout management functionality.
 */
public abstract class BaseScoutFragment extends Fragment {
    protected RecyclerView mRecyclerView;
    protected PreferenceRepository repository;

    // Dependencies
    protected LayoutManager layoutManager;
    protected RecyclerViewFactory recyclerViewFactory;
    protected FileUtils fileUtils;
    protected ScoutUtils scoutUtils;
    protected MatchInfo matchInfo;
    protected TeamInfo teamInfo;

    // UI Components
    protected Button loadButton;
    protected Button importButton;
    protected CheckBox autoLoadCheckBox;

    /**
     * Initialize all dependencies and utilities.
     * Called from onViewCreated before setupRecyclerView.
     */
    protected void initializeDependencies() {
        // Repository
        repository = PowerPreferenceRepository.getInstance();

        // Utilities
        fileUtils = new FileUtils(requireContext());
        scoutUtils = new ScoutUtils(requireContext());
        matchInfo = new MatchInfo();
        teamInfo = new TeamInfo(requireContext());

        // Set grid toggle based on fragment requirements
        repository.setGridToggle(useGridLayout());

        // Create layout system
        LayoutParser parser = new LayoutParser();
        LayoutPresenter presenter = new LayoutPresenter(requireContext(), matchInfo, teamInfo);
        layoutManager = new LayoutManager(parser, presenter, fileUtils);

        // Create RecyclerView factory
        RecyclerViewConfig rvConfig = RecyclerViewConfig.fromRepository(repository);
        recyclerViewFactory = new RecyclerViewFactory(requireContext(), rvConfig);
    }

    /**
     * Setup RecyclerView with factory and load existing layout if present.
     * Called from onViewCreated after initializeDependencies.
     */
    protected void setupRecyclerView(View view) {
        mRecyclerView = recyclerViewFactory.create(view, R.id.recycler_view);

        // Try to load existing layout file
        File layoutFile = new File(requireContext().getFilesDir(), getLayoutFileName());
        if (fileUtils.fileExists(layoutFile.toString())) {
            boolean loaded = layoutManager.loadLayout(layoutFile, mRecyclerView, getViewLifecycleOwner());
            if (loaded) {
                hideLayoutButtons();
            }
        }
    }

    /**
     * Setup button click listeners and auto-load logic.
     * Called from onViewCreated after setupRecyclerView.
     */
    protected void setupButtons() {
        // Find buttons from the concrete fragment's binding/layout
        loadButton = requireView().findViewById(R.id.loadButton);
        importButton = requireView().findViewById(R.id.importButton);
        autoLoadCheckBox = requireView().findViewById(R.id.auto_load_checkBox);

        if (loadButton != null) {
            loadButton.setOnClickListener(v -> handleLoad());
        }

        if (importButton != null) {
            importButton.setOnClickListener(v -> handleImport());
        }

        if (autoLoadCheckBox != null) {
            autoLoadCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Use synchronous set since this is a critical UI state
                if ("auto_load_crowd_layout_toggle".equals(getAutoLoadPreferenceKey())) {
                    repository.setAutoLoadCrowdLayout(isChecked);
                }
            });
        }

        // Auto-load if configured
        if (shouldAutoLoad()) {
            if (loadButton != null) {
                loadButton.performClick();
            }
            hideLayoutButtons();
        }
    }

    /**
     * Handle load button click - loads default layout from raw resources
     */
    protected void handleLoad() {
        boolean loaded = layoutManager.loadLayout(
            getDefaultLayoutResourceId(),
            mRecyclerView,
            getViewLifecycleOwner(),
            getResources()
        );

        if (loaded) {
            hideLayoutButtons();
        }
    }

    /**
     * Handle import button click - launches file picker
     */
    protected void handleImport() {
        Intent data = fileUtils.intentFileDialog();
        Intent.createChooser(data, "Select a File to Import");
        importLauncher.launch(data);
    }

    /**
     * Hide layout selection buttons after layout is loaded
     */
    protected void hideLayoutButtons() {
        if (loadButton != null) {
            loadButton.setVisibility(View.INVISIBLE);
        }
        if (importButton != null) {
            importButton.setVisibility(View.INVISIBLE);
        }
        if (autoLoadCheckBox != null) {
            autoLoadCheckBox.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Check if layout should be auto-loaded on fragment creation
     */
    protected boolean shouldAutoLoad() {
        boolean roleLocked = repository.isRoleLocked();
        boolean autoLoadEnabled = false;

        // Check specific auto-load preference based on fragment type
        if ("auto_load_crowd_layout_toggle".equals(getAutoLoadPreferenceKey())) {
            autoLoadEnabled = repository.isAutoLoadCrowdLayoutEnabled();
        }

        return roleLocked || autoLoadEnabled;
    }

    /**
     * File import result handler
     */
    protected ActivityResultLauncher<Intent> importLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    Uri uri = data.getData();
                    File file = new File(
                        Objects.requireNonNull(FileUtils.copyFileToInternal(requireContext(), uri, getLayoutFileName())));

                    // Save current data before loading new layout
                    scoutUtils.saveData(requireView(), shouldSaveWithSpecialFlag());

                    // Load new layout
                    boolean loaded = layoutManager.loadLayout(file, mRecyclerView, getViewLifecycleOwner());
                    if (loaded) {
                        hideLayoutButtons();
                    }
                }
            }
        }
    );

    @Override
    public void onResume() {
        super.onResume();
        if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
            mRecyclerView.post(() -> {
                LayoutPresenter presenter = new LayoutPresenter(requireContext(), matchInfo, teamInfo);
                presenter.updateTitleCells(mRecyclerView);
            });
        }
        refreshActionBar();
    }

    // Abstract methods for subclasses to implement

    /**
     * @return Layout filename (e.g., "crowd_layout.json")
     */
    protected abstract String getLayoutFileName();

    /**
     * @return Raw resource ID for default layout (e.g., R.raw.crowd_layout)
     */
    protected abstract int getDefaultLayoutResourceId();

    /**
     * @return Preference key for auto-load setting (e.g., "auto_load_crowd_layout_toggle")
     */
    protected abstract String getAutoLoadPreferenceKey();

    /**
     * @return Whether to use grid layout (true) or linear layout (false)
     */
    protected abstract boolean useGridLayout();

    /**
     * @return Whether to pass special flag to saveData method
     */
    protected abstract boolean shouldSaveWithSpecialFlag();

    /**
     * Refresh the action bar with current state
     * Each fragment has its own action bar display logic
     */
    protected abstract void refreshActionBar();
}
