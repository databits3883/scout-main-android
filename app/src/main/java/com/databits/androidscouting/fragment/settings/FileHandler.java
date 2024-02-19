package com.databits.androidscouting.fragment.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.anggrayudi.storage.SimpleStorageHelper;
import com.databits.androidscouting.databinding.FragmentSettingsFilehandlerBinding;
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

public class FileHandler extends Fragment {
  private FragmentSettingsFilehandlerBinding binding;
  Preference debugPreference = PowerPreference.getFileByName("Debug");
  ScoutUtils scoutUtils;
  FileUtils fileUtils;
  MatchInfo matchInfo;
  TeamInfo teamInfo;

  private static final String TAG = "FileHandler";

  SimpleStorageHelper storageHelper = new SimpleStorageHelper(this);

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState
  ) {

    binding = FragmentSettingsFilehandlerBinding.inflate(inflater, container, false);
    return binding.getRoot();

  }

  @Override
  public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
    super.onViewCreated(v, savedInstanceState);

    // Go Full screen
    View decorView = requireActivity().getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    decorView.setSystemUiVisibility(uiOptions);

    NavController controller = NavHostFragment.findNavController(FileHandler.this);

    // Helper Classes
    scoutUtils = new ScoutUtils(requireContext());
    fileUtils = new FileUtils(requireContext());
    matchInfo = new MatchInfo();
    teamInfo = new TeamInfo(requireContext());

    binding.buttonBack.setOnClickListener(v1 -> controller.navigateUp());

    binding.buttonStudentList.setOnClickListener(view -> {
      Intent data = fileUtils.intentFileDialog();
      Intent.createChooser(data, "Select a student.txt file to import");
      importStudentLauncher.launch(data);
    });

    binding.buttonTeamList.setOnClickListener(view -> {
      Intent data = fileUtils.intentFileDialog();
      Intent.createChooser(data, "Select a pit_team.txt file to import");
      importPitTeamLauncher.launch(data);
    });

    binding.buttonExportZip.setOnClickListener(view -> {
      String zipName = "scouting_config.zip";
      ZipFile zip = fileUtils.CreateZipFile(zipName);
      try {
        // Adds file to zip, only creates zip if file exists
        zip.addFile(new File(requireContext().getFilesDir().getPath() + "/Test.txt"));
      } catch (ZipException e) {
        throw new RuntimeException(e);
      }
    });

    binding.buttonImportZip.setOnClickListener(view -> {
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

      binding.buttonSaveLocation.setOnClickListener(v1 -> {
        storageHelper.requestStorageAccess();
      });
  }

  ActivityResultLauncher<Intent> importStudentLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          if (data != null) {
            String path = FileUtils.copyFileToInternal(requireContext(),data.getData(),
                "student.txt");
            assert path != null;
            File file1 = new File(path);
            ArrayList<String> studentList = fileUtils.readList(file1);
            debugPreference.setObject("student_list", studentList);
            Toast.makeText(requireContext(), studentList.get(0), Toast.LENGTH_SHORT).show();
          }
        }
      }
  );

  ActivityResultLauncher<Intent> importPitTeamLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          if (data != null) {
            String path = FileUtils.copyFileToInternal(requireContext(),data.getData(),"pit_team.txt");
            assert path != null;
            File file1 = new File(path);
            ArrayList<String> pitTeamList = fileUtils.readList(file1);
            debugPreference.setObject("pit_team_list", pitTeamList);
            Toast.makeText(requireContext(), pitTeamList.get(0), Toast.LENGTH_SHORT).show();
          }
        }
      }
  );

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}