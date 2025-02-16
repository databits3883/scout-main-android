package com.databits.androidscouting.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.databits.androidscouting.MainActivity;
import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentSettingsDashboardBinding;
import com.databits.androidscouting.databinding.UiStatusIndicatorBinding;
import com.databits.androidscouting.util.FileUtils;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.ScoutUtils;
import com.databits.androidscouting.util.TeamInfo;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.io.File;

public class Dashboard extends Fragment {
  private FragmentSettingsDashboardBinding binding;
  ScoutUtils scoutUtils;
  FileUtils fileUtils;
  MatchInfo matchInfo;
  TeamInfo teamInfo;

  Preference configPreference = PowerPreference.getFileByName("Config");


  MainActivity mainActivity;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState
  ) {

    binding = FragmentSettingsDashboardBinding.inflate(inflater, container, false);
    return binding.getRoot();

  }

  @Override
  public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
    super.onViewCreated(v, savedInstanceState);

    mainActivity = (MainActivity) requireContext();

    // Go Full screen
    View decorView = requireActivity().getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    decorView.setSystemUiVisibility(uiOptions);

    NavController controller = NavHostFragment.findNavController(Dashboard.this);

    // Helper Classes
    scoutUtils = new ScoutUtils(requireContext());
    fileUtils = new FileUtils(requireContext());
    matchInfo = new MatchInfo();
    teamInfo = new TeamInfo(requireContext());


    // Set Action Bar Title
    ActionBar actionBar = mainActivity.getSupportActionBar();
    actionBar.setTitle("Settings Dashboard");

    binding.buttonBack.setOnClickListener(v1 -> controller.navigateUp());

    binding.buttonTest.setOnClickListener(v1 -> controller.navigate(
        R.id.action_SettingsFragment_to_SettingsTestingFragment));

    binding.buttonFile.setOnClickListener(v1 -> controller.navigate(
        R.id.action_SettingsFragment_to_SettingsFileHandlerFragment));

    binding.googleStatusIndicator.indicatorButton.setOnClickListener(v1 -> controller.navigate(
        R.id.action_SettingsFragment_to_SettingsManualConfigFragment));

    updateStatusIndicators();
  }

  public void updateStatusIndicators(){
    setStatusIndicator(binding.internetStatusIndicator, "Internet", mainActivity.checkConnection(),
        "Online", "Offline");

    setStatusIndicator(binding.matchListStatusIndicator, "Match List",
        fileUtils.fileExists(String.valueOf(
            new File(requireContext().getFilesDir() + "/" + "teams.csv"))),
        "Loaded", "Not Loaded");

    setStatusIndicator(binding.scouterListStatusIndicator, "Scouter List",
        fileUtils.fileExists(String.valueOf(
            new File(requireContext().getFilesDir() + "/" + "scouter.csv"))),
        "Loaded", "Not Loaded");

    setStatusIndicator(binding.googleStatusIndicator, "Google", configPreference.getString("google_account_name").isEmpty(),
        "Logged in", "Logged out");

    setStatusIndicator(binding.permissionStatusIndicator, "Permissions",
        scoutUtils.allPermissionsGranted(), "Granted", "Denied");
  }

  public void setStatusIndicator(UiStatusIndicatorBinding StatusIndicator, String title,
      boolean condition, String trueText, String falseText) {
    StatusIndicator.indicatorTitleText.setText(title);
    StatusIndicator.indicatorStatusText.setText(condition ? trueText : falseText);
    StatusIndicator.indicatorButton.setImageResource(condition ? R.drawable.checkmark :
        R.drawable.close);
    StatusIndicator.indicatorButton.setBackgroundTintList(ContextCompat.getColorStateList(
        requireContext(), condition ? com.github.dhaval2404.colorpicker.R.color.green_200 :
            com.github.dhaval2404.colorpicker.R.color.red_200));
  }

  @Override
  public void onResume() {
    super.onResume();
    updateStatusIndicators();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}