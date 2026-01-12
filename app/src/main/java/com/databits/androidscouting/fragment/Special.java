package com.databits.androidscouting.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentSpecialBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.preference.PowerPreference;
import com.travijuu.numberpicker.library.NumberPicker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Special extends BaseScoutFragment {

  private FragmentSpecialBinding binding;
  ArrayList<String> editedList;
  List<String> scouterList;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState
  ) {
    requireActivity().addMenuProvider(new MenuProvider() {
      @Override
      public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.special_menu, menu);
        // Add/Modify menu items Here
      }

      @Override
      public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        NavController controller = NavHostFragment.findNavController(
            Special.this);
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
                  String data = scoutUtils.saveData(requireView(), true);
                  bundle.putString("qrData", data);
                  bundle.putBoolean("mode", false);
                  try {
                    int team = Integer.parseInt(data.split(",")[0]);
                    teamInfo.setTeam(team);
                    teamSpinner(String.valueOf(team),true, requireContext(),requireView());
                  } catch (NumberFormatException e) {
                      teamInfo.setTeam(0);
                  }
                }

                controller.navigate(R.id.action_SpecialFragment_to_QRFragment,
                    bundle);
              })
              .setNegativeButton(R.string.cancel, (dialog, Identify) -> {
                // CANCEL
              })
              .show();
        }

        if (id == R.id.actions_change_scouter) {
          // Load scouter list on background thread
          new Thread(() -> {
            List<String> loadedScouterList = repository.getScouterList();
            requireActivity().runOnUiThread(() -> {
              scouterList = loadedScouterList;
              View dialogView = View.inflate(requireContext(), R.layout.popup_scouter_select, null);
              AlertDialog scouterDialog = new AlertDialog.Builder(requireContext())
                  .setTitle("Select Scouter")
                  .setView(dialogView)
                  .setPositiveButton("Set", (dialog1, which1) -> {
                    AutoCompleteTextView dropdown = ((AlertDialog) dialog1)
                        .findViewById(R.id.scouter_select);
                    // Save the scouter name to the common preference
                    repository.setCurrentScouter(
                        Objects.requireNonNull(dropdown).getText().toString());
                    refreshActionBar();
                  })
                  .setNegativeButton("Cancel", (dialog1, which1) -> {
                    // Do nothing
                  })
                  .create();
              ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                  R.layout.ui_list_item,
                  scouterList);
              AutoCompleteTextView dropdown = dialogView.findViewById(R.id.scouter_select);
              dropdown.setAdapter(adapter);
              dropdown.setThreshold(0);
              scouterDialog.show();
            });
          }).start();
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
                repository.setManualTeamOverride(true);
                repository.setManualTeamOverrideValue(teamNumber);
                mRecyclerView.post(() -> {
                  com.databits.androidscouting.layout.LayoutPresenter presenter =
                      new com.databits.androidscouting.layout.LayoutPresenter(
                          requireContext(), matchInfo, teamInfo);
                  presenter.updateTitleCells(mRecyclerView);
                });
              })
              .setNegativeButton("Cancel", (dialog1, which1) -> {
                // Do nothing
              })
              .create();
          teamNumberDialog.show();
        }

        // Ask the user if they want to re-provision the device
        if (id == R.id.action_reconfigure) {
          if (!repository.isRoleLocked()) {
            controller.navigate(R.id.action_SpecialFragment_to_ScannerFragment);
            return true;
          }
          AlertDialog alertDialogFragment = new AlertDialog.Builder(requireContext())
              .setTitle("Reconfigure your device?")
              .setMessage("This will clear your data and require the device to be"
                  + "reconfigured via the Master scouter.")
              .setPositiveButton("Yes", (dialog, which) ->
                  controller.navigate(R.id.action_SpecialFragment_to_ScannerFragment))
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
                repository.setManualMatchOverride(true);
                repository.setManualMatchOverrideValue(match);
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
                repository.setRoleLocked(false);
                controller.navigate(R.id.action_SpecialFragment_to_StartFragment);
              })
              .setNegativeButton("Cancel", (dialog1, which1) -> {
                // Do nothing
              })
              .show();
        }

        return false;
      }

    }, this.getViewLifecycleOwner(), Lifecycle.State.CREATED);

    binding = FragmentSpecialBinding.inflate(inflater, container, false);
    return binding.getRoot();

  }

  @Override
  public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
    super.onViewCreated(v, savedInstanceState);

    // Go Full screen
    View decorView = requireActivity().getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    decorView.setSystemUiVisibility(uiOptions);

    NavController controller = NavHostFragment.findNavController(Special.this);

    // Initialize base class dependencies
    initializeDependencies();

    // Setup RecyclerView using base class
    setupRecyclerView(v);

    // Setup buttons using base class
    setupButtons();

    Button testButton = v.findViewById(R.id.testButton);
    if (testButton != null) {
      testButton.setOnClickListener(view -> PowerPreference.showDebugScreen(true));
    }

    refreshActionBar();
  }

  public void teamSpinner(String team, boolean remove, Context context, View v) {
    String[] origList = context.getResources().getStringArray(R.array.team_list);
    repository.setSpecialRemoveEnabled(true);
    ArrayList<String> remainingList = repository.getSpecialScoutData();
    if (remainingList != null) {
      editedList = remainingList;
    } else {
      editedList = new ArrayList<>(Arrays.asList(origList));
    }

    if (remove) {
      editedList.remove(team);
      repository.setSpecialScoutData(editedList);
    }
    Objects.requireNonNull(mRecyclerView.getAdapter()).notifyItemChanged(1);
  }

  @Override
  public void refreshActionBar() {
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    assert activity != null;
    ActionBar actionBar = activity.getSupportActionBar();
    int match = matchInfo.getMatch();
    Objects.requireNonNull(actionBar).setTitle(" Match " +
        match);
    actionBar.setSubtitle(teamInfo.getScouterName() + " - " + "Special Scout");
  }

  @Override
  protected String getLayoutFileName() {
    return "special_layout.json";
  }

  @Override
  protected int getDefaultLayoutResourceId() {
    return R.raw.special_layout;
  }

  @Override
  protected String getAutoLoadPreferenceKey() {
    return "auto_load_special_layout_toggle";
  }

  @Override
  protected boolean useGridLayout() {
    return false;  // Special uses linear layout
  }

  @Override
  protected boolean shouldSaveWithSpecialFlag() {
    return true;  // Special fragment uses special flag
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}