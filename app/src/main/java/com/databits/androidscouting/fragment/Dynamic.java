package com.databits.androidscouting.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentDynamicBinding;
import com.databits.androidscouting.util.FileUtils;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.ScoutUtils;
import com.databits.androidscouting.util.TeamInfo;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.io.File;
import java.util.Objects;

public class Dynamic extends Fragment {
  private FragmentDynamicBinding binding;
  Preference configPreference = PowerPreference.getFileByName("Config");
  ScoutUtils scoutUtils;
  FileUtils fileUtils;
  MatchInfo matchInfo;
  TeamInfo teamInfo;
  private RecyclerView mRecyclerViewTop;
  private RecyclerView mRecyclerViewBot;
  String fileName = "dynamic_layout.json";

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState
  ) {

    binding = FragmentDynamicBinding.inflate(inflater, container, false);
    return binding.getRoot();

  }

  @Override
  public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
    super.onViewCreated(v, savedInstanceState);

    NavController controller = NavHostFragment.findNavController(Dynamic.this);

    // Helper Classes
    scoutUtils = new ScoutUtils(requireContext());
    fileUtils = new FileUtils(requireContext());
    matchInfo = new MatchInfo();
    teamInfo = new TeamInfo(requireContext());

    mRecyclerViewTop = scoutUtils.makeRecyclerView(requireContext(),v, R.id.recycler_view_top);
    mRecyclerViewBot = scoutUtils.makeRecyclerView(requireContext(),v, R.id.recycler_view_bot);

    //Sorts the tables based on saved Table Status
    int table_status = configPreference.getInt("table_mode", ScoutUtils.BOTH);
    scoutUtils.tableSorter(table_status, requireView(), mRecyclerViewTop, mRecyclerViewBot);
    scoutUtils.setupTables(requireView());

    File layoutLoc = new File(requireContext().getFilesDir(), fileName);
    if (fileUtils.fileExists(layoutLoc.toString())) {
      scoutUtils.layoutMaker(ScoutUtils.BOTH,fileUtils.readFile(layoutLoc),
          requireView(), mRecyclerViewTop, mRecyclerViewBot);
    }

    binding.importButton.setOnClickListener(v1 -> {
      Intent data =  fileUtils.intentFileDialog();
      Intent.createChooser(data, "Select a File to Import");
      importLauncher.launch(data);
    });

    binding.qrButton.setOnClickListener(view -> {
      Bundle bundle = new Bundle();
      bundle.putString("qrData", scoutUtils.saveData(v, false));
      controller.navigate(R.id.action_dynamicFragment_to_QRFragment, bundle);
    });

    binding.testButton.setOnClickListener(view -> PowerPreference.showDebugScreen(true));
  }

  ActivityResultLauncher<Intent> importLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          if (data != null) {
            Uri uri = data.getData();
            File file = new File(
                Objects.requireNonNull(FileUtils.copyFileToInternal(requireContext(), uri, fileName)));
            scoutUtils.layoutMaker(ScoutUtils.NONE,fileUtils.readFile(file), requireView(), mRecyclerViewTop, mRecyclerViewBot);
          }
        }
      }
  );

  @Override
  public void onResume() {
    super.onResume();
    mRecyclerViewTop.post(() -> scoutUtils.setupTitle(mRecyclerViewTop));
    mRecyclerViewBot.post(() -> scoutUtils.setupTitle(mRecyclerViewBot));
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}