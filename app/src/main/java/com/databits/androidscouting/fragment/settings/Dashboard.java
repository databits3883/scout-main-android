package com.databits.androidscouting.fragment.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.anggrayudi.storage.SimpleStorageHelper;
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
import java.util.ArrayList;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import static android.content.ContentValues.TAG;

public class Dashboard extends Fragment {
  private FragmentSettingsDashboardBinding binding;
  ScoutUtils scoutUtils;
  FileUtils fileUtils;
  MatchInfo matchInfo;
  TeamInfo teamInfo;

  Preference configPreference = PowerPreference.getFileByName("Config");
  Preference debugPreference = PowerPreference.getFileByName("Debug");

  SimpleStorageHelper storageHelper = new SimpleStorageHelper(this);

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

    binding.buttonExportZip.setOnClickListener(view -> {
      storageHelper.requestStorageAccess();
      String zipName = "scouting_config.zip";
      ZipFile zip = fileUtils.CreateZipFile(zipName);
      try {
        // Adds file to zip, only creates zip if file exists
        zip.addFile(new File(requireContext().getFilesDir().getPath() +
            "/scouter_list.txt"));
      } catch (ZipException e) {
        throw new RuntimeException(e);
      }
    });

    binding.buttonImportZip.setOnClickListener(view -> {
      storageHelper.requestStorageAccess();
      storageHelper.openFilePicker(69, false);
      storageHelper.setOnFileSelected((uri, document) -> {
        if (document == null) {
          Log.d(TAG, "Error or No file selected");
        } else {
          fileUtils.handleZip(document.get(0).getUri());
        }
        return null;
      });
    });

    binding.googleStatusIndicator.indicatorButton.setOnClickListener(v1 -> controller.navigate(
        R.id.action_SettingsFragment_to_SettingsManualConfigFragment));

    binding.scouterListStatusIndicator.indicatorButton.setOnClickListener(view -> {
      AlertDialog teamNumberDialog = new AlertDialog.Builder(requireContext())
          .setTitle("Do you want to import a new scouter list?")
          .setMessage("This will overwrite the current scouter list. \n\n"
              + "The format is a text file with one scouter name per line. \n\n"
              + "Select Yes and then select a text file to import from your device.")
          .setPositiveButton("Yes", (dialog1, which1) -> {
            Intent data = fileUtils.intentFileDialog();
            Intent.createChooser(data, "Select a scouter_list.txt file to import");
            importScouterLauncher.launch(data);
          })
          .setNegativeButton("Cancel", (dialog1, which1) -> {
            // Do nothing
          })
          .create();
      teamNumberDialog.show();
    });

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
            new File(requireContext().getFilesDir() + "/" + "scouter_list.txt"))) && !debugPreference.getString("scouter_list").isEmpty(),
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

  public ActivityResultLauncher<Intent> importScouterLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          if (data != null) {
            String path = FileUtils.copyFileToInternal(requireContext(),data.getData(),
                "scouter_list.txt");
            assert path != null;
            File file1 = new File(path);
            ArrayList<String> studentList = fileUtils.readList(file1);
            debugPreference.setObject("scouter_list", studentList);
          }
        }
      }
  );
}