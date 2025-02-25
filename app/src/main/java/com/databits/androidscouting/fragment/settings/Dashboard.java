package com.databits.androidscouting.fragment.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
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
import java.util.List;
import java.util.Objects;

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
    View dialogView = View.inflate(requireContext(), R.layout.popup_scouter_list_preview,
        null);

    AlertDialog previewDialog = new AlertDialog.Builder(requireContext())
        .setTitle("Data Preview")
        .setView(dialogView)
        .setNegativeButton("Cancel", (dialog3,which3) -> {
          // Do nothing
        }).create();

    binding.scouterListStatusIndicator.indicatorButton.setOnClickListener(view -> {
      AlertDialog scouterListDialog = new AlertDialog.Builder(requireContext())
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
          .setNeutralButton("Loaded Data Preview",(dialog2, which2) -> {
            previewDialog.show();
          })
          .create();
      TextView text = dialogView.findViewById(R.id.data_view);
      text.setMovementMethod(new ScrollingMovementMethod());
      List<String> scouterList = debugPreference.getObject("scouter_list", List.class);
      if (scouterList == null) {
        scouterList = new ArrayList<>();
      }
      // Add all scouterList strings to a string with each being on a newline
      StringBuilder scouterListString = new StringBuilder();
      for (String scouter : scouterList) {
        scouterListString.append(scouter).append("\n");
      }
      text.setText(scouterListString.toString());
      scouterListDialog.show();
    });

    binding.buttonDebug.setOnClickListener(view1 -> PowerPreference.showDebugScreen(true));

    binding.permissionStatusIndicator.indicatorButton.setOnClickListener(view1 -> {

      AlertDialog permissionDialog = new AlertDialog.Builder(requireContext())
          .setTitle("Do you want to grant the following permissions?")
          .setMessage("1. Manage Settings permission which is needed for the app to control the "
              + "brightness of the screen in the QR view\n"
              + "2. Camera is required for the QR scanner to work")
          .setPositiveButton("Yes", (dialog1, which1) -> {
            // Launches the intent to allow the app to change the screen brightness
            if (Settings.System.canWrite(requireContext())) {
              Toast.makeText(requireContext(), "Brightness control permission already granted!",
                  Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(requireContext(), "Brightness control permission requested",
                  Toast.LENGTH_SHORT).show();
              Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
              intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));
              startActivity(intent);
            }

            // Checks if all other permissions are granted
            if (scoutUtils.allPermissionsGranted()) {
              Toast.makeText(requireContext(), "All other permissions already granted!",
                  Toast.LENGTH_SHORT).show();
            } else {
              ActivityCompat.requestPermissions(
                  requireActivity(), ScoutUtils.REQUIRED_PERMISSIONS,
                  ScoutUtils.REQUEST_CODE_PERMISSIONS
              );
            }
          })
          .setNegativeButton("Cancel", (dialog1, which1) -> {
            // Do nothing
          })
          .create();
      permissionDialog.show();

    });

    binding.matchListStatusIndicator.indicatorButton.setOnClickListener(view1 -> {
      AlertDialog matchListDialog = new AlertDialog.Builder(requireContext())
          .setTitle("Do you want to import a new match list?")
          .setMessage("This will overwrite the current match list. \n\n"
              + "The format is a csv file, 1st line has the header and then one match per line.\n\n"
              + "Match,Blue 1,Blue 2,Blue 3,Red 1,Red 2,Red 3\n\n"
              + "1,2169,6147,7619,2062,4786,2987\n"
              + "2,7530,3100,6758,8803,5913,4229\n\n"
              + "Select Yes and then select your file to import it from your device.")
          .setPositiveButton("Yes", (dialog1, which1) -> {
            Intent data  = fileUtils.intentFileDialog();
            Intent.createChooser(data, "Select a match.csv file to import");
            matchLauncher.launch(data);
          })
          .setNegativeButton("Cancel", (dialog1, which1) -> {
            // Do nothing
          })
          .create();
      matchListDialog.show();

    });

    updateStatusIndicators();
  }

  public void updateStatusIndicators(){
    setStatusIndicator(binding.internetStatusIndicator, "Internet", mainActivity.checkConnection(),
        "Online", "Offline");

    setStatusIndicator(binding.matchListStatusIndicator, "Match List",
        debugPreference.getInt("team_match_list_size") > 0 ||
        fileUtils.fileExists(String.valueOf(
            new File(requireContext().getFilesDir() + "/" + "match.csv"))),
        "Loaded", "Not Loaded");

    setStatusIndicator(binding.scouterListStatusIndicator, "Scouter List",
        fileUtils.fileExists(String.valueOf(
            new File(requireContext().getFilesDir() + "/" + "scouter_list.txt"))) && !debugPreference.getString("scouter_list").isEmpty(),
        "Loaded", "Not Loaded");

    setStatusIndicator(binding.googleStatusIndicator, "Google", !configPreference.getString("google_account_name").isEmpty(),
        "Logged in", "Logged out");

    setStatusIndicator(binding.permissionStatusIndicator, "Permissions",
        scoutUtils.allPermissionsGranted() && Settings.System.canWrite(requireActivity()),
        "Granted", "Denied");
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

  ActivityResultLauncher<Intent> matchLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          if (data != null) {
            Objects.requireNonNull(FileUtils.copyFileToInternal(requireContext(), data.getData(),
                "match.csv"));
            teamInfo.read_teams();
          }
        }
      }
  );
}