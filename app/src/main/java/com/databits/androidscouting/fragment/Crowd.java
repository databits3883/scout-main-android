package com.databits.androidscouting.fragment;

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
import com.databits.androidscouting.databinding.FragmentCrowdScoutBinding;
import java.util.Objects;

public class Crowd extends BaseScoutFragment {

    private FragmentCrowdScoutBinding binding;

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.crowd_menu, menu);
                // Add/Modify menu items Here
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController controller = NavHostFragment.findNavController(Crowd.this);
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
                            android.util.Log.d("Crowd", "Cell data extracted: " + cellData);

                            // Get team/match data on background thread since it accesses Room database
                            runInBackground(() -> {
                                int match = matchInfo.getMatch();
                                int team = 9999;
                                if (provisionStore.isManualTeamOverrideEnabled()) {
                                    team = provisionStore.getManualTeamOverrideValue();
                                } else if (teamInfo.teamsLoaded()) {
                                    team = teamInfo.getTeam(match);
                                }

                                // Combine data (remove leading comma from cellData)
                                String qrData = team + "," + match + "," + cellData.substring(1) + "," + teamInfo.getScouterName();
                                android.util.Log.d("Crowd", "Final QR data: " + qrData);

                                runOnUiIfActive(() -> {
                                    Bundle bundle = controller.saveState();
                                    if (bundle != null) {
                                        bundle.putString("qrData", qrData);
                                        bundle.putBoolean("mode", false);
                                        controller.navigate(R.id.action_crowdScoutFragment_to_QRFragment,
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
                    Crowd.this,
                    controller,
                    R.id.action_crowdScoutFragment_to_ScannerFragment,
                    R.id.action_crowdScoutFragment_to_StartFragment,
                    () -> provisionStore.setMaster(false),
                    () -> provisionStore.setAutoLoadCrowdLayout(false),
                    true
                );
                return menuController.handle(menuItem);
            }
        }, this.getViewLifecycleOwner(), Lifecycle.State.CREATED);
        binding = FragmentCrowdScoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        NavController controller = NavHostFragment.findNavController(Crowd.this);

        if (savedInstanceState != null) {
            controller.restoreState(savedInstanceState);
        }

        // Initialize base class dependencies
        initializeDependencies();

        // Setup RecyclerView using base class
        setupRecyclerView(v);

        // Setup buttons using base class
        setupButtons();

        refreshActionBar();
    }

    @Override
    public void refreshActionBar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        assert activity != null;
        ActionBar actionBar = activity.getSupportActionBar();
        int match = matchInfo.getMatch();
        int position = provisionStore.getCrowdPosition();
        String[] positionArray = getResources().getStringArray(R.array.positions);

        // Load team data on background thread to avoid Room database access on main thread
        runInBackground(() -> {
            int team = teamInfo.getTeam(match);
            String scouterName = teamInfo.getScouterName();
            runOnUiIfActive(() -> {
                Objects.requireNonNull(actionBar).setTitle("Team: " + team + " Match " + match);
                actionBar.setSubtitle(scouterName + " - " + positionArray[position]);
            });
        });
    }

    @Override
    protected String getLayoutFileName() {
        return "crowd_layout.json";
    }

    @Override
    protected int getDefaultLayoutResourceId() {
        return R.raw.crowd_layout;
    }

    @Override
    protected String getAutoLoadPreferenceKey() {
        return "auto_load_crowd_layout_toggle";
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
