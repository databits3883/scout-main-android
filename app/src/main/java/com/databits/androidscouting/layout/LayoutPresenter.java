package com.databits.androidscouting.layout;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import com.databits.androidscouting.R;
import com.databits.androidscouting.adapter.MultiviewTypeAdapter;
import com.databits.androidscouting.factory.AdapterFactory;
import com.databits.androidscouting.model.Cell;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.TeamInfo;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;
import java.util.Objects;

/**
 * Handles presentation of layout data to the UI.
 * Applies adapters to RecyclerViews and updates title cells with lifecycle safety.
 */
public class LayoutPresenter {
    private final Context context;
    private final MatchInfo matchInfo;
    private final TeamInfo teamInfo;
    private final AdapterFactory adapterFactory;

    public LayoutPresenter(Context context, MatchInfo matchInfo, TeamInfo teamInfo) {
        this.context = context;
        this.matchInfo = matchInfo;
        this.teamInfo = teamInfo;
        this.adapterFactory = new AdapterFactory();
    }

    /**
     * Apply layout to RecyclerView with lifecycle-safe title updates
     * @param recyclerView Target RecyclerView
     * @param cells List of cells to display
     * @param lifecycleOwner Fragment lifecycle owner for safe async operations
     */
    public void applyLayout(RecyclerView recyclerView, List<Cell> cells, LifecycleOwner lifecycleOwner) {
        // Create and set adapter
        MultiviewTypeAdapter adapter = adapterFactory.create(cells);
        recyclerView.setAdapter(adapter);

        // Load pit teams remaining cache to avoid database access in onBindViewHolder
        adapter.loadPitTeamsRemainingCache();

        // Notify changes
        adapter.notifyDataSetChanged();

        // Update title cells (lifecycle-safe)
        if (lifecycleOwner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            recyclerView.post(() -> {
                // Check lifecycle again before updating
                if (lifecycleOwner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    updateTitleCells(recyclerView);
                }
            });
        }
    }

    /**
     * Update Title cells with current match and team information.
     * This is called after the RecyclerView has laid out its children.
     */
    public void updateTitleCells(RecyclerView recyclerView) {
        MultiviewTypeAdapter adapter = (MultiviewTypeAdapter) recyclerView.getAdapter();
        if (adapter == null || adapter.mCell == null) {
            return;
        }

        // Find and update all Title cells
        for (int i = 0; i < adapter.mCell.size(); i++) {
            if (Objects.equals(adapter.mCell.get(i).getType(), "Title")) {
                View cellView = recyclerView.getChildAt(i);
                if (cellView != null) {
                    updateSingleTitleCell(cellView);
                }
            }
        }
    }

    /**
     * Update a single title cell view with match and team information
     */
    private void updateSingleTitleCell(View cellView) {
        TextView teamNumber = cellView.findViewById(R.id.team_number);
        TextView matchNumber = cellView.findViewById(R.id.match_number);

        if (teamNumber != null) {
            if (!teamInfo.teamsLoaded()) {
                teamNumber.setText("No Team");
            } else {
                teamNumber.setText(String.valueOf(teamInfo.getTeam(matchInfo.getMatch())));
            }
        }

        if (matchNumber != null) {
            matchNumber.setText(String.valueOf(matchInfo.getMatch()));
        }
    }

    /**
     * Show error message to user with optional retry action
     * @param view View to attach Snackbar to
     * @param errorMessage User-friendly error message
     */
    public void showError(View view, String errorMessage) {
        Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG).show();
    }
}
