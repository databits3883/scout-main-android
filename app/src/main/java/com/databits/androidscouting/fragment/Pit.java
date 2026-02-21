package com.databits.androidscouting.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentPitScoutBinding;
import com.preference.PowerPreference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Pit extends BaseScoutFragment {

    private FragmentPitScoutBinding binding;
    ArrayList<String> editedList;

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.pit_menu, menu);
                // Add/Modify menu items Here
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController controller = NavHostFragment.findNavController(
                    Pit.this);
                int id = menuItem.getItemId();

                // Launch the QR code fragment
                if (id == R.id.action_qr) {
                    new AlertDialog.Builder(requireContext())
                        .setMessage("Are you sure you want to continue?\n\n"
                            + "This will clear your data and load the QR code.")
                        .setTitle("Load QR Code?")
                        .setPositiveButton("Yes", (dialog, Identify) -> {
                            // Extract cell data on UI thread first
                            String cellData = scoutUtils.exportCell(requireView().findViewById(R.id.recycler_view));
                            android.util.Log.d("Pit", "Cell data extracted: " + cellData);

                            // Get team data on background thread since it accesses Room database
                            runInBackground(() -> {
                                int match = matchInfo.getMatch();
                                int team = 9999;
                                if (provisionStore.isManualTeamOverrideEnabled()) {
                                    team = provisionStore.getManualTeamOverrideValue();
                                } else if (teamInfo.teamsLoaded() || scheduleStore.isPitRemoveEnabled()) {
                                    team = teamInfo.getTeam(match);
                                }

                                // Combine data (remove leading comma from cellData) - pit doesn't include team/match
                                String qrData = cellData.substring(1) + "," + teamInfo.getScouterName();
                                android.util.Log.d("Pit", "Final QR data: " + qrData);

                                int finalTeam = team;
                                runOnUiIfActive(() -> {
                                    Bundle bundle = controller.saveState();
                                    if (bundle != null) {
                                        bundle.putString("qrData", qrData);
                                        bundle.putBoolean("mode", true);
                                        teamInfo.setTeam(finalTeam);
                                        teamSpinner(String.valueOf(finalTeam),true, requireContext(),requireView());
                                        controller.navigate(R.id.action_pitScoutFragment_to_QRFragment,
                                            bundle);
                                    }
                                });
                            });
                        })
                        .setNegativeButton(R.string.cancel, (dialog, Identify) -> {
                            // CANCEL
                        })
                        .show();
                }
                ScoutMenuController menuController = new ScoutMenuController(
                    Pit.this,
                    controller,
                    R.id.action_pitScoutFragment_to_ScannerFragment,
                    R.id.action_pitScoutFragment_to_StartFragment,
                    null,
                    null,
                    false
                );
                return menuController.handle(menuItem);
            }

        }, this.getViewLifecycleOwner(), Lifecycle.State.CREATED);

        binding = FragmentPitScoutBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Go Full screen
        View decorView = requireActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        NavController controller = NavHostFragment.findNavController(Pit.this);

        // Initialize base class dependencies
        initializeDependencies();

        // Setup RecyclerView using base class
        setupRecyclerView(v);

        // Setup buttons using base class
        setupButtons();

        refreshActionBar();
    }

    public void teamSpinner(String team, boolean remove, Context context, View v) {
        String[] origList = context.getResources().getStringArray(R.array.team_list);
        scheduleStore.setPitRemoveEnabled(true);

        // Load remaining list on background thread
        runInBackground(() -> {
            List<String> remainingList = scheduleStore.getPitTeamsRemainingList();
            ArrayList<String> newEditedList;
            if (remainingList != null && !remainingList.isEmpty()) {
                newEditedList = new ArrayList<>(remainingList);
            } else {
                newEditedList = new ArrayList<>(Arrays.asList(origList));
            }

            if (remove) {
                newEditedList.remove(team);
                scheduleStore.setPitTeamsRemainingList(newEditedList);
            }

            editedList = newEditedList;
            runOnUiIfActive(() -> {
                // Update adapter cache with new list
                if (mRecyclerView.getAdapter() instanceof com.databits.androidscouting.adapter.MultiviewTypeAdapter) {
                    ((com.databits.androidscouting.adapter.MultiviewTypeAdapter) mRecyclerView.getAdapter())
                        .updatePitTeamsRemainingCache(newEditedList);
                }
                Objects.requireNonNull(mRecyclerView.getAdapter()).notifyItemChanged(1);
            });
        });
    }

    @Override
    public void refreshActionBar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        assert activity != null;
        ActionBar actionBar = activity.getSupportActionBar();
        Objects.requireNonNull(actionBar).setTitle(teamInfo.getScouterName() + " - " +
            "Pit Scout");
        actionBar.setSubtitle("");
    }

    @Override
    protected String getLayoutFileName() {
        return "pit_layout.json";
    }

    @Override
    protected int getDefaultLayoutResourceId() {
        return R.raw.pit_layout;
    }

    @Override
    protected String getAutoLoadPreferenceKey() {
        return "auto_load_pit_layout_toggle";
    }

    @Override
    protected boolean useGridLayout() {
        return true;
    }

    @Override
    protected boolean shouldSaveWithSpecialFlag() {
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
