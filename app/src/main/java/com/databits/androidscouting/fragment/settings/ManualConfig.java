package com.databits.androidscouting.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.anggrayudi.storage.SimpleStorageHelper;
import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentSettingsManualconfigBinding;
import com.databits.androidscouting.util.FileUtils;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.ScoutUtils;
import com.databits.androidscouting.util.TeamInfo;
import com.google.android.material.textfield.TextInputEditText;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.util.Objects;

public class ManualConfig extends Fragment {
  private FragmentSettingsManualconfigBinding binding;
  Preference configPreference = PowerPreference.getFileByName("Config");
  ScoutUtils scoutUtils;
  FileUtils fileUtils;
  MatchInfo matchInfo;
  TeamInfo teamInfo;

  SimpleStorageHelper storageHelper = new SimpleStorageHelper(this);

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState
  ) {

    binding = FragmentSettingsManualconfigBinding.inflate(inflater, container, false);
    return binding.getRoot();

  }

  @Override
  public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
    super.onViewCreated(v, savedInstanceState);

    // Go Full screen
    View decorView = requireActivity().getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    decorView.setSystemUiVisibility(uiOptions);

    NavController controller = NavHostFragment.findNavController(ManualConfig.this);

    // Helper Classes
    scoutUtils = new ScoutUtils(requireContext());
    fileUtils = new FileUtils(requireContext());
    matchInfo = new MatchInfo();
    teamInfo = new TeamInfo(requireContext());

    binding.buttonBack.setOnClickListener(v1 -> controller.navigateUp());

    binding.buttonGoogleId.setOnClickListener(view -> {
      updateID();
    });

    updateID();
  }

  private void updateID() {
    TextInputEditText editText = requireView().findViewById(R.id.google_text_edit);
    String sheet = configPreference.getString("linked_spreadsheet_id","");
    if (sheet.equals("")) {
      configPreference.setString("linked_spreadsheet_id", Objects.requireNonNull(
          editText.getText()).toString());
    } else {
      editText.setText(sheet);
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