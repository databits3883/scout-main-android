package com.databits.androidscouting.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentSettingsGoogleconfigBinding;
import com.databits.androidscouting.util.FileUtils;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.ScoutUtils;
import com.databits.androidscouting.util.TeamInfo;
import com.google.android.material.textfield.TextInputEditText;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.util.Objects;

public class GoogleConfig extends Fragment {
  private FragmentSettingsGoogleconfigBinding binding;
  Preference configPreference = PowerPreference.getFileByName("Config");
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

    // Go Full screen
    View decorView = requireActivity().getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    decorView.setSystemUiVisibility(uiOptions);

    NavController controller = NavHostFragment.findNavController(GoogleConfig.this);

    // Helper Classes
    scoutUtils = new ScoutUtils(requireContext());
    fileUtils = new FileUtils(requireContext());
    matchInfo = new MatchInfo();
    teamInfo = new TeamInfo(requireContext());

    binding.buttonBack.setOnClickListener(v1 -> controller.navigateUp());

    binding.buttonGoogleId.setOnClickListener(view -> {
      updateID();
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
    updateID();
  }

  private void updateID() {
    TextInputEditText editText = requireView().findViewById(R.id.google_id_text_edit);
    String sheet = configPreference.getString("linked_spreadsheet_id","");
    if (sheet.isEmpty()) {
      configPreference.setString("linked_spreadsheet_id", Objects.requireNonNull(
          editText.getText()).toString());
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

    String saved = configPreference.getString(id_prefix + "_range","");

    if (saved.isEmpty() || save) {
      // Range is name!lower:upper ex= StatsRaw!A2:Z700
      String range = Objects.requireNonNull(name.getText()) + "!" + Objects.requireNonNull(
          lower.getText()) + ":" +
          Objects.requireNonNull(upper.getText());

      // Set configPreference string to the range based on id_prefix
      configPreference.setString(id_prefix + "_range", range);
    } else {
      // Get the range from configPreference and set the name, lower, and upper text fields
      String range = configPreference.getString(id_prefix + "_range","");
      String[] split = range.split("!");
      name.setText(split[0]);
      split = split[1].split(":");
      lower.setText(split[0]);
      upper.setText(split[1]);
    }
  }

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