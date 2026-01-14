package com.databits.androidscouting.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentCrowdScoutBinding;
import com.databits.androidscouting.util.FileUtils;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.ScoutUtils;
import com.databits.androidscouting.util.TeamInfo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.travijuu.numberpicker.library.NumberPicker;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Crowd extends Fragment {

    private FragmentCrowdScoutBinding binding;

    Preference configPreference = PowerPreference.getFileByName("Config");
    Preference debugPreference = PowerPreference.getFileByName("Debug");
    ScoutUtils scoutUtils;
    FileUtils fileUtils;
    MatchInfo matchInfo;
    TeamInfo teamInfo;
    List<String> scouterList;
    private RecyclerView mRecyclerViewTop;
    private RecyclerView mRecyclerViewBot;
    String fileName = "crowd_layout.json";

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
                            Bundle bundle = controller.saveState();
                            if (bundle != null) {
                                String qrData = scoutUtils.saveData(requireView(),false);
                                bundle.putString("qrData", qrData);
                                bundle.putBoolean("mode", false);
                            }
                            controller.navigate(R.id.action_crowdScoutFragment_to_QRFragment,
                                bundle);
                        })
                        .setNegativeButton(R.string.cancel, (dialog, Identify) -> {
                            // CANCEL
                        })
                        .show();
                }

                if (id == R.id.actions_change_scouter) {
                    View dialogView = View.inflate(requireContext(), R.layout.popup_scouter_select,
                        null);
                    AlertDialog scouterDialog = new AlertDialog.Builder(requireContext())
                        .setTitle("Select Scouter")
                        .setView(dialogView)
                        .setPositiveButton("Set", (dialog1, which1) -> {
                            AutoCompleteTextView dropdown = ((AlertDialog) dialog1)
                                .findViewById(R.id.scouter_select);
                            // Save the scouter name to the common preference
                            configPreference.setString("current_scouter",
                                Objects.requireNonNull(dropdown).getText().toString());
                            refreshActionBar();
                        })
                        .setNegativeButton("Cancel", (dialog1, which1) -> {
                            // Do nothing
                        })
                        .create();
                    scouterList = debugPreference.getObject("scouter_list", List.class);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        R.layout.ui_list_item,
                        scouterList);
                    AutoCompleteTextView dropdown = dialogView.findViewById(R.id.scouter_select);
                    dropdown.setAdapter(adapter);
                    dropdown.setThreshold(0);
                    scouterDialog.show();
                }

                // Support manually setting the team number
                if (id == R.id.action_team_entry) {
                    AlertDialog teamNumberDialog = new AlertDialog.Builder(requireContext())
                        .setTitle("Enter Team Number")
                        .setView(R.layout.popup_team_num_submit)
                        .setPositiveButton("Set", (dialog1, which1) -> {
                            TextInputLayout textInputLayout =
                                ((AlertDialog) dialog1).findViewById(R.id.textbox_text_layout);
                            assert textInputLayout != null;
                            TextInputEditText editText =
                                textInputLayout.findViewById(R.id.enteredText);
                            int teamNumber = Integer.parseInt(
                                Objects.requireNonNull(editText.getText()).toString());
                            // Save the team number to the preference
                            debugPreference.setBoolean("manual_team_override_toggle", true);
                            debugPreference.putInt("manual_team_override_value", teamNumber);
                            mRecyclerViewTop.post(() -> scoutUtils.setupTitle(mRecyclerViewTop));
                            mRecyclerViewBot.post(() -> scoutUtils.setupTitle(mRecyclerViewBot));
                            refreshActionBar();
                        })
                        .setNegativeButton("Cancel", (dialog1, which1) -> {
                            // Do nothing
                        })
                        .create();
                    teamNumberDialog.show();
                }

                // Ask the user if they want to re-provision the device
                if (id == R.id.action_reconfigure) {
                    if (!configPreference.getBoolean("role_locked_toggle")) {
                        debugPreference.setBoolean("isMaster", false);
                        controller.navigate(R.id.action_crowdScoutFragment_to_ScannerFragment);
                        return true;
                    }
                    AlertDialog alertDialogFragment = new AlertDialog.Builder(requireContext())
                        .setTitle("Reconfigure your device?")
                        .setMessage("This will clear your data and require the device to be "
                            + "reconfigured via the Master scouter.")
                        .setPositiveButton("Yes", (dialog, which) ->
                            controller.navigate(R.id.action_crowdScoutFragment_to_ScannerFragment))
                        .setNegativeButton("No", (dialog, which) -> {
                            // Do nothing
                        })
                        .create();
                    alertDialogFragment.show();
                    return true;
                }

                // Launch the Power Preference debug screen
                if (id == R.id.action_debug) {
                    PowerPreference.showDebugScreen(true);
                }

                if (id == R.id.action_change_match) {
                    View dialogView = View.inflate(requireContext(), R.layout.popup_match_select,
                        null);

                    AlertDialog scouterDialog = new AlertDialog.Builder(requireContext())
                        .setTitle("Select Match")
                        .setView(dialogView)
                        .setPositiveButton("Set", (dialog1, which1) -> {
                            NumberPicker matchPicker =
                                ((AlertDialog) dialog1).findViewById(R.id.number_counter_inside);
                            int match = Objects.requireNonNull(matchPicker).getValue();
                            debugPreference.setBoolean("manual_match_override_toggle", true);
                            debugPreference.putInt("manual_match_override_value", match);
                            refreshActionBar();
                        })
                        .setNegativeButton("Cancel", (dialog1, which1) -> {
                            // Do nothing
                        })
                        .create();
                    NumberPicker matchPicker = dialogView.findViewById(R.id.number_counter_inside);
                    matchPicker.setValue(matchInfo.getMatch());
                    matchPicker.setMax(100);
                    matchPicker.setMin(1);
                    matchPicker.setDisplayFocusable(true);
                    scouterDialog.show();
                    return true;
                }

                if (id == R.id.action_reset) {
                    new AlertDialog.Builder(requireContext())
                        .setTitle("Unlock the View")
                        .setMessage(
                            "Are you sure you want to unlock?\n\nThis will bring you back to"
                                + " the main menu and require that your device be re-provisioned")
                        .setPositiveButton("Yes", (dialog1, which1) -> {
                            configPreference.setBoolean("role_locked_toggle", false);
                            configPreference.setBoolean("auto_load_crowd_layout_toggle", false);
                            controller.navigate(R.id.action_crowdScoutFragment_to_StartFragment);
                        })
                        .setNegativeButton("Cancel", (dialog1, which1) -> {
                            // Do nothing
                        })
                        .show();
                }

                return false;
            }
        }, this.getViewLifecycleOwner(), Lifecycle.State.CREATED);
        binding = FragmentCrowdScoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        NavController controller = NavHostFragment.findNavController(
            Crowd.this);

        if (savedInstanceState != null) {
            controller.restoreState(savedInstanceState);
        }

        // Go Full screen
        //View decorView = requireActivity().getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        //decorView.setSystemUiVisibility(uiOptions);

        // Helper Classes
        scoutUtils = new ScoutUtils(requireContext());
        fileUtils = new FileUtils(requireContext());
        matchInfo = new MatchInfo();
        teamInfo = new TeamInfo(requireContext());

        configPreference.setBoolean("grid_toggle", true);

        mRecyclerViewTop = scoutUtils.makeRecyclerView(requireContext(), v, R.id.recycler_view_top);
        mRecyclerViewBot = scoutUtils.makeRecyclerView(requireContext(), v, R.id.recycler_view_bot);

        //Sorts the tables based on saved Table Status
        int table_status = configPreference.getInt("table_mode", ScoutUtils.NONE);
        scoutUtils.tableSorter(table_status, requireView(), mRecyclerViewTop, mRecyclerViewBot);
        scoutUtils.setupTables(requireView());

        File layoutLoc = new File(requireContext().getFilesDir(), fileName);
        if (fileUtils.fileExists(layoutLoc.toString())) {
            scoutUtils.layoutMaker(table_status, fileUtils.readFile(layoutLoc),
                requireView(), mRecyclerViewTop, mRecyclerViewBot);
        }

        binding.importButton.setOnClickListener(v1 -> {
            Intent data = fileUtils.intentFileDialog();
            Intent.createChooser(data, "Select a layout.json file to import");
            importLauncher.launch(data);
        });

        binding.loadButton.setOnClickListener(v1 -> {
            String storedLayout = fileUtils.readTextFile(getResources().
                openRawResource(R.raw.crowd_layout));
            scoutUtils.layoutMaker(table_status, storedLayout, requireView(),
                mRecyclerViewTop, mRecyclerViewBot);
            binding.loadButton.setVisibility(View.INVISIBLE);
            binding.importButton.setVisibility(View.INVISIBLE);
            binding.autoLoadCheckBox.setVisibility(View.INVISIBLE);
        });

        binding.autoLoadCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
            configPreference.putBoolean("auto_load_crowd_layout_toggle", isChecked));

        if (configPreference.getBoolean("role_locked_toggle") ||
            configPreference.getBoolean("auto_load_crowd_layout_toggle")) {
            binding.loadButton.performClick();
            binding.loadButton.setVisibility(View.INVISIBLE);
            binding.importButton.setVisibility(View.INVISIBLE);
            binding.autoLoadCheckBox.setVisibility(View.INVISIBLE);
        }

        refreshActionBar();
    }

    ActivityResultLauncher<Intent> importLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    createLayout(data.getData());
                }
            }
        }
    );

    public void refreshActionBar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        assert activity != null;
        ActionBar actionBar = activity.getSupportActionBar();
        int match = matchInfo.getMatch();
        int position = configPreference.getInt("crowd_position", 0);
        String[] positionArray = getResources().getStringArray(R.array.positions);
        Objects.requireNonNull(actionBar).setTitle("Team: " + teamInfo.getTeam(match) + " Match " +
            match);
        actionBar.setSubtitle(teamInfo.getScouterName() + " - " + positionArray[position]);
    }

    public void createLayout(Uri uri) {
        int table_status = configPreference.getInt("table_mode", ScoutUtils.NONE);
        File layoutFile = new File(
            Objects.requireNonNull(FileUtils.copyFileToInternal(requireContext(), uri, fileName)));
        scoutUtils.layoutMaker(table_status,fileUtils.readFile(layoutFile), requireView(),
            mRecyclerViewTop, mRecyclerViewBot);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerViewTop.post(() -> scoutUtils.setupTitle(mRecyclerViewTop));
        mRecyclerViewBot.post(() -> scoutUtils.setupTitle(mRecyclerViewBot));
        refreshActionBar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}