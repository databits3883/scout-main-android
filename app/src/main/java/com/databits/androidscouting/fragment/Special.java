package com.databits.androidscouting.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.preference.PowerPreference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Special extends BaseScoutFragment {

  private FragmentSpecialBinding binding;
  ArrayList<String> editedList;

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
                // Extract cell data on UI thread first
                String cellData = scoutUtils.exportCell(requireView().findViewById(R.id.recycler_view));
                android.util.Log.d("Special", "Cell data extracted: " + cellData);

                // Get team data on background thread since it accesses Room database
                runInBackground(() -> {
                  int match = matchInfo.getMatch();
                  int team = 9999;
                  if (provisionStore.isManualTeamOverrideEnabled()) {
                    team = provisionStore.getManualTeamOverrideValue();
                  } else if (teamInfo.teamsLoaded() || scheduleStore.isPitRemoveEnabled()) {
                    team = teamInfo.getTeam(match);
                  }

                  // Combine data (remove leading comma from cellData) - special doesn't include team/match
                  String qrData = cellData.substring(1) + "," + teamInfo.getScouterName();
                  android.util.Log.d("Special", "Final QR data: " + qrData);

                  int finalTeam = team;
                  runOnUiIfActive(() -> {
                    Bundle bundle = controller.saveState();
                    if (bundle != null) {
                      bundle.putString("qrData", qrData);
                      bundle.putBoolean("mode", false);
                      try {
                        teamInfo.setTeam(finalTeam);
                        teamSpinner(String.valueOf(finalTeam),true, requireContext(),requireView());
                      } catch (NumberFormatException e) {
                        teamInfo.setTeam(0);
                      }
                      controller.navigate(R.id.action_SpecialFragment_to_QRFragment,
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
            Special.this,
            controller,
            R.id.action_SpecialFragment_to_ScannerFragment,
            R.id.action_SpecialFragment_to_StartFragment,
            null,
            null,
            false
        );
        return menuController.handle(menuItem);
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
    scheduleStore.setSpecialRemoveEnabled(true);
    ArrayList<String> remainingList = scheduleStore.getSpecialScoutData();
    if (remainingList != null) {
      editedList = remainingList;
    } else {
      editedList = new ArrayList<>(Arrays.asList(origList));
    }

    if (remove) {
      editedList.remove(team);
      scheduleStore.setSpecialScoutData(editedList);
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
