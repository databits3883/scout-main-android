package com.databits.androidscouting.fragment;

import android.view.View;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import com.databits.androidscouting.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.preference.PowerPreference;
import com.travijuu.numberpicker.library.NumberPicker;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class ScoutMenuController {
    private final BaseScoutFragment fragment;
    private final NavController controller;
    private final int reconfigureActionId;
    private final int resetActionId;
    private final Runnable onUnlockedReconfigure;
    private final Runnable onResetBeforeNavigate;
    private final boolean refreshAfterTeamEntry;

    ScoutMenuController(
        BaseScoutFragment fragment,
        NavController controller,
        int reconfigureActionId,
        int resetActionId,
        Runnable onUnlockedReconfigure,
        Runnable onResetBeforeNavigate,
        boolean refreshAfterTeamEntry
    ) {
        this.fragment = fragment;
        this.controller = controller;
        this.reconfigureActionId = reconfigureActionId;
        this.resetActionId = resetActionId;
        this.onUnlockedReconfigure = onUnlockedReconfigure != null ? onUnlockedReconfigure : () -> {};
        this.onResetBeforeNavigate = onResetBeforeNavigate != null ? onResetBeforeNavigate : () -> {};
        this.refreshAfterTeamEntry = refreshAfterTeamEntry;
    }

    boolean handle(MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.actions_change_scouter) {
            handleChangeScouter();
            return true;
        }
        if (id == R.id.action_team_entry) {
            handleTeamEntry();
            return true;
        }
        if (id == R.id.action_reconfigure) {
            handleReconfigure();
            return true;
        }
        if (id == R.id.action_debug) {
            PowerPreference.showDebugScreen(true);
            return true;
        }
        if (id == R.id.action_change_match) {
            handleMatchChange();
            return true;
        }
        if (id == R.id.action_reset) {
            handleReset();
            return true;
        }
        return false;
    }

    private void handleChangeScouter() {
        fragment.runInBackground(() -> {
            List<String> loadedScouters = fragment.syncStore.getScouterList();
            final List<String> scouterList = loadedScouters != null ? loadedScouters : new ArrayList<>();
            fragment.runOnUiIfActive(() -> {
                View dialogView = View.inflate(fragment.requireContext(), R.layout.popup_scouter_select, null);
                AlertDialog scouterDialog = new AlertDialog.Builder(fragment.requireContext())
                    .setTitle("Select Scouter")
                    .setView(dialogView)
                    .setPositiveButton("Set", (dialog1, which1) -> {
                        AutoCompleteTextView dropdown = ((AlertDialog) dialog1)
                            .findViewById(R.id.scouter_select);
                        fragment.provisionStore.setCurrentScouter(
                            Objects.requireNonNull(dropdown).getText().toString());
                        fragment.refreshActionBar();
                    })
                    .setNegativeButton("Cancel", (dialog1, which1) -> {
                    })
                    .create();

                ArrayAdapter<String> adapter = new ArrayAdapter<>(fragment.requireContext(),
                    R.layout.ui_list_item, scouterList);
                AutoCompleteTextView dropdown = dialogView.findViewById(R.id.scouter_select);
                dropdown.setAdapter(adapter);
                dropdown.setThreshold(0);
                scouterDialog.show();
            });
        });
    }

    private void handleTeamEntry() {
        AlertDialog teamNumberDialog = new AlertDialog.Builder(fragment.requireContext())
            .setTitle("Enter Team Number")
            .setView(R.layout.popup_team_num_submit)
            .setPositiveButton("Set", (dialog1, which1) -> {
                TextInputLayout textInputLayout =
                    ((AlertDialog) dialog1).findViewById(R.id.textbox_text_layout);
                assert textInputLayout != null;
                TextInputEditText editText = textInputLayout.findViewById(R.id.enteredText);
                int teamNumber = Integer.parseInt(
                    Objects.requireNonNull(editText.getText()).toString());
                fragment.provisionStore.setManualTeamOverride(true);
                fragment.provisionStore.setManualTeamOverrideValue(teamNumber);
                fragment.mRecyclerView.post(() -> {
                    com.databits.androidscouting.layout.LayoutPresenter presenter =
                        new com.databits.androidscouting.layout.LayoutPresenter(
                            fragment.requireContext(),
                            fragment.matchInfo,
                            fragment.teamInfo,
                            fragment.scheduleStore,
                            fragment.provisionStore
                        );
                    presenter.updateTitleCells(fragment.mRecyclerView);
                });
                if (refreshAfterTeamEntry) {
                    fragment.refreshActionBar();
                }
            })
            .setNegativeButton("Cancel", (dialog1, which1) -> {
            })
            .create();
        teamNumberDialog.show();
    }

    private void handleReconfigure() {
        if (!fragment.provisionStore.isRoleLocked()) {
            onUnlockedReconfigure.run();
            controller.navigate(reconfigureActionId);
            return;
        }

        AlertDialog alertDialogFragment = new AlertDialog.Builder(fragment.requireContext())
            .setTitle("Reconfigure your device?")
            .setMessage("This will clear your data and require the device to be"
                + "reconfigured via the Master scouter.")
            .setPositiveButton("Yes", (dialog, which) -> controller.navigate(reconfigureActionId))
            .setNegativeButton("No", (dialog, which) -> {
            })
            .create();
        alertDialogFragment.show();
    }

    private void handleMatchChange() {
        View dialogView = View.inflate(fragment.requireContext(), R.layout.popup_match_select, null);

        AlertDialog scouterDialog = new AlertDialog.Builder(fragment.requireContext())
            .setTitle("Select Match")
            .setView(dialogView)
            .setPositiveButton("Set", (dialog1, which1) -> {
                NumberPicker matchPicker =
                    ((AlertDialog) dialog1).findViewById(R.id.number_counter_inside);
                int match = Objects.requireNonNull(matchPicker).getValue();
                fragment.provisionStore.setManualMatchOverride(true);
                fragment.provisionStore.setManualMatchOverrideValue(match);
                fragment.refreshActionBar();
            })
            .setNegativeButton("Cancel", (dialog1, which1) -> {
            })
            .create();

        NumberPicker matchPicker = dialogView.findViewById(R.id.number_counter_inside);
        matchPicker.setValue(fragment.matchInfo.getMatch());
        matchPicker.setMax(100);
        matchPicker.setMin(1);
        matchPicker.setDisplayFocusable(true);
        scouterDialog.show();
    }

    private void handleReset() {
        new AlertDialog.Builder(fragment.requireContext())
            .setTitle("Unlock the View")
            .setMessage(
                "Are you sure you want to unlock?\n\nThis will bring you back to"
                    + " the main menu and require that your device be re-provisioned")
            .setPositiveButton("Yes", (dialog1, which1) -> {
                fragment.provisionStore.setRoleLocked(false);
                onResetBeforeNavigate.run();
                controller.navigate(resetActionId);
            })
            .setNegativeButton("Cancel", (dialog1, which1) -> {
            })
            .show();
    }
}
