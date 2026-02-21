package com.databits.androidscouting.fragment.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.databits.androidscouting.R;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepositoryProvider;
import com.databits.androidscouting.databinding.FragmentSettingsGoogleconfigBinding;
import com.databits.androidscouting.util.FileUtils;
import com.databits.androidscouting.util.GoogleAuthActivity;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.ScoutUtils;
import com.databits.androidscouting.util.TeamInfo;
import com.databits.androidscouting.viewmodel.ProvisionViewModel;
import com.databits.androidscouting.viewmodel.ProvisionViewModelFactory;
import com.google.android.material.textfield.TextInputEditText;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoogleConfig extends Fragment {
  private FragmentSettingsGoogleconfigBinding binding;
  private PreferenceRepository repository;
  private ProvisionViewModel viewModel;
  private ExecutorService executor;
  private Handler mainHandler;
  ScoutUtils scoutUtils;
  FileUtils fileUtils;
  MatchInfo matchInfo;
  TeamInfo teamInfo;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState
  ) {

    binding = FragmentSettingsGoogleconfigBinding.inflate(inflater, container, false);
    return binding.getRoot();

  }

  @Override
  public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
    super.onViewCreated(v, savedInstanceState);

    // Initialize ViewModel
    repository = PreferenceRepositoryProvider.get(requireContext());
    ProvisionViewModelFactory factory = new ProvisionViewModelFactory(repository);
    viewModel = new ViewModelProvider(this, factory).get(ProvisionViewModel.class);

    // Initialize executor and handler for background operations
    executor = Executors.newSingleThreadExecutor();
    mainHandler = new Handler(Looper.getMainLooper());

    // Go Full screen
    View decorView = requireActivity().getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    decorView.setSystemUiVisibility(uiOptions);

    NavController controller = NavHostFragment.findNavController(GoogleConfig.this);

    // Helper Classes
    scoutUtils = new ScoutUtils(requireContext());
    fileUtils = new FileUtils(requireContext());
    matchInfo = new MatchInfo(repository);
    teamInfo = new TeamInfo(requireContext());

    binding.buttonBack.setOnClickListener(v1 -> controller.navigateUp());

    binding.buttonGoogleId.setOnClickListener(view -> {
      updateID(true);
    });

    binding.crowdSheetLocation.buttonGoogleRange.setOnClickListener(view -> {
      updateRange(binding.crowdSheetLocation.getRoot(), "Crowd", true);
    });

    binding.pitSheetLocation.buttonGoogleRange.setOnClickListener(view -> {
      updateRange(binding.pitSheetLocation.getRoot(), "Pit", true);
    });

    binding.specialtySheetLocation.buttonGoogleRange.setOnClickListener(view -> {
      updateRange(binding.specialtySheetLocation.getRoot(), "Specialty", true);
    });

    // Run on page startup
    updateRange(binding.crowdSheetLocation.getRoot(), "Crowd", false);
    updateRange(binding.pitSheetLocation.getRoot(), "Pit", false);
    updateRange(binding.specialtySheetLocation.getRoot(), "Specialty", false);
    updateID(false);
  }

  private void updateID(boolean save) {
    TextInputEditText editText = requireView().findViewById(R.id.google_id_text_edit);
    String sheet = viewModel.getWorkbookId().getValue();
    if (sheet == null) sheet = "";
    if (sheet.isEmpty() || save) {
      viewModel.updateWorkbookId(Objects.requireNonNull(editText.getText()).toString());
    } else {
      editText.setText(sheet);
    }
  }

  private void updateRange(View view, String id_prefix, boolean save) {
    TextInputEditText name = view.findViewById(R.id.sheet_name_text_edit);
    TextInputEditText lower = view.findViewById(R.id.lower_range_text_edit);
    TextInputEditText upper = view.findViewById(R.id.upper_range_text_edit);

    String title = String.format("%s Data Location", id_prefix);

    // Set view title based on id_prefix
    switch (id_prefix) {
      case "Crowd":
        binding.crowdSheetLocation.dataTitle.setText(title);
        break;
      case "Pit":
        binding.pitSheetLocation.dataTitle.setText(title);
        break;
      case "Specialty":
        binding.specialtySheetLocation.dataTitle.setText(title);
    }

    if (save) {
      // Range is name!lower:upper ex= StatsRaw!A2:Z700
      String range = Objects.requireNonNull(name.getText()) + "!" + Objects.requireNonNull(
          lower.getText()) + ":" +
          Objects.requireNonNull(upper.getText());

      // Set range based on id_prefix
      switch (id_prefix) {
        case "Crowd":
          viewModel.updateCrowdRange(range);
          break;
        case "Pit":
          viewModel.updatePitRange(range);
          break;
        case "Specialty":
          viewModel.updateSpecialtyRange(range);
          break;
      }
    } else {
      // Get the range and set the name, lower, and upper text fields
      String range = "";
      switch (id_prefix) {
        case "Crowd":
          range = viewModel.getCrowdRange().getValue();
          break;
        case "Pit":
          range = viewModel.getPitRange().getValue();
          break;
        case "Specialty":
          range = viewModel.getSpecialtyRange().getValue();
          break;
      }
      if (range != null && !range.isEmpty()) {
        String[] split = range.split("!");
        name.setText(split[0]);
        split = split[1].split(":");
        lower.setText(split[0]);
        upper.setText(split[1]);
      }
    }

    binding.importButton.setOnClickListener(view1 -> {
      AlertDialog googleConfigDialog = new AlertDialog.Builder(requireContext())
          .setTitle("Do you want to import a new Google Config?")
          .setMessage("The format is a csv file with the following format: "
              + "workbook_id,crowd_range,pit_range,specialty_range\n\n"
              + "Range Example: CrowdRaw!A2:Z700\n\n"
              + "Select Yes and then pick your csv file to import from your device.")
          .setPositiveButton("Yes", (dialog1, which1) -> {
            Intent data = fileUtils.intentFileDialog();
            Intent.createChooser(data, "Select a google_config.csv file to import");
            importGoogleLauncher.launch(data);
          })
          .setNegativeButton("Cancel", (dialog1, which1) -> {
            // Do nothing
          })
          .create();
      googleConfigDialog.show();
    });

    // Observe google account name changes
    viewModel.getGoogleAccountName().observe(getViewLifecycleOwner(), accountName -> {
      scoutUtils.setButtonStatus(binding.googleButton,
          accountName != null && !accountName.isEmpty(),
          "Logged into Google", "Sign into Google");
    });

    binding.googleButton.setOnClickListener(view1 -> {
      Intent intent = new Intent(requireContext(), GoogleAuthActivity.class);
      startActivity(intent);
    });
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (executor != null) {
      executor.shutdown();
    }
    binding = null;
  }

  public ActivityResultLauncher<Intent> importGoogleLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          if (data != null) {
            // Move file I/O and preference writes to background thread
            executor.execute(() -> {
              try {
                // File I/O on background thread
                FileUtils.copyFileToInternal(requireContext(), data.getData(),
                    "google_config.csv");
                File teams = new File(requireContext().getFilesDir() + "/" + "google_config.csv");
                CSVReader csvReader = new CSVReader(new FileReader(teams));
                List<String[]> list = csvReader.readAll();
                int size = list.size();
                String[][] dataArr = new String[size][];
                dataArr = list.toArray(dataArr);

                // Preference writes on background thread (ViewModel uses ExecutorService internally)
                for (String[] row : dataArr) {
                  viewModel.updateWorkbookId(row[0]);
                  viewModel.updateCrowdRange(row[1]);
                  viewModel.updatePitRange(row[2]);
                  viewModel.updateSpecialtyRange(row[3]);
                }
                repository.setGoogleConfig(dataArr);

                // Update UI on main thread
                mainHandler.post(() -> {
                  if (binding != null) {
                    updateRange(binding.crowdSheetLocation.getRoot(), "Crowd", false);
                    updateRange(binding.pitSheetLocation.getRoot(), "Pit", false);
                    updateRange(binding.specialtySheetLocation.getRoot(), "Specialty", false);
                    updateID(false);
                    Toast.makeText(requireContext(), "Google configuration imported successfully",
                        Toast.LENGTH_SHORT).show();
                  }
                });
            } catch (IOException | CsvException e) {
                e.printStackTrace();
                // Show error on main thread
                mainHandler.post(() -> {
                  if (binding != null) {
                    Toast.makeText(requireContext(), "Error importing configuration: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                  }
                });
              }
            });
          }
        }
      }
  );
}
