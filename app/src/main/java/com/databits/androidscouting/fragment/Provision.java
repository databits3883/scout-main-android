package com.databits.androidscouting.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentProvisionBinding;
import com.databits.androidscouting.util.MatchInfo;
import com.databits.androidscouting.util.QrCodeGenerator;
import com.databits.androidscouting.util.TeamInfo;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Provision extends Fragment {

  private FragmentProvisionBinding binding;

  AtomicReference<String> lock_status = new AtomicReference<>("true");
  AtomicReference<String> scouter_name = new AtomicReference<>("");
  AtomicReference<String> role = new AtomicReference<>("master");
  AtomicReference<String> crowd_position = new AtomicReference<>("1");
  AtomicReference<String> content_string = new AtomicReference<>("Nothing");
  AtomicReference<String> data_erase = new AtomicReference<>("false");
  AtomicReference<String> special_selector = new AtomicReference<>("false");

  AtomicReference<Integer> match = new AtomicReference<>(1);

  List<String> scouterList;

  MatchInfo matchInfo;

  TeamInfo teamInfo;

  QrCodeGenerator qrCodeGenerator;

  List<String> entryLabels = new ArrayList<>();

  Preference configPreference = PowerPreference.getFileByName("Config");
  Preference debugPreference = PowerPreference.getFileByName("Debug");
  Preference listPreference = PowerPreference.getFileByName("List");

  boolean lock = configPreference.getBoolean("role_locked_toggle", false);

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState
  ) {
    requireActivity().addMenuProvider(new MenuProvider() {
      @Override
      public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.provision_menu, menu);
        // Add/Modify menu items Here
      }

      @Override
      public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        NavController controller = NavHostFragment.findNavController(Provision.this);
        teamInfo = new TeamInfo(requireContext());
        int id = menuItem.getItemId();

        if (id == R.id.action_provision_self) {
          if (data_erase.get().equals("true")) {
            PowerPreference.clearAllData();
            data_erase.set("false");
          }

          teamInfo.read_teams();

          configPreference.setString("device_role", role.get());
          configPreference.setInt("crowd_position", Integer.parseInt(crowd_position.get()));
          configPreference.setString("current_scouter", scouter_name.get());
          configPreference.setBoolean("role_locked_toggle", lock_status.get().equals("true"));
          configPreference.setBoolean("specialSwitch", Boolean.parseBoolean(special_selector.get()));
          //configPreference.setInt("current_match", matchInfo.getMatch());
          controller.navigate(R.id.action_provisionFragment_to_StartFragment);
        }

        // Launch the Power Preference debug screen
        if (id == R.id.action_debug) {
          PowerPreference.showDebugScreen(true);
        }

        return false;
      }
    }, this.getViewLifecycleOwner(), Lifecycle.State.CREATED);
    binding = FragmentProvisionBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    scouterList = debugPreference.getObject("scouter_list", List.class);

    NavController controller = NavHostFragment.findNavController(Provision.this);

    binding.buttonBackProvision.setOnClickListener(view1 -> controller.navigateUp());

    binding.buttonBackProvision.setOnLongClickListener(view1 -> {
      controller.navigate(R.id.action_provisionFragment_to_ScannerFragment);
      return true;
    });

    matchInfo = new MatchInfo();

    qrCodeGenerator = new QrCodeGenerator(requireContext());

    binding.buttonUpdate.setOnClickListener(view1 -> {
      generateQrCode();
    });

    binding.buttonConfigProvision.setOnClickListener(view1 -> {
      AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
      alertDialog.setTitle("Config");
      alertDialog.setMessage("Config");
      //alertDialog.show();

      String[][] matchData = listPreference.getObject("team_match", String[][].class);
      if (matchData == null) {
        Toast.makeText(requireContext(), "Not all data was found", Toast.LENGTH_LONG).show();
      } else {
        int numMatches = matchData.length;
        if (numMatches > 0) {
          int chunkSize = 6;
          int numChunks = (numMatches + chunkSize - 1) / chunkSize;
          String[] qr = new String[numChunks];
          // Add two more slot for the scouter list and google QR codes
          Bitmap[] qr_img = new Bitmap[numChunks + 2];

          for (int i = 0; i < numChunks; i++) {
            StringBuilder chunkData = new StringBuilder();
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, numMatches);

            for (int j = start; j < end; j++) {
              chunkData.append(Arrays.toString(matchData[j]));
            }
            qr[i] = "MatchData" + "," + i + "," + chunkData;
            // Save bitmap for each match
            qr_img[i] = qrCodeGenerator.generateQRCode(qr[i], 1000, 35, false);
          }

          ArrayList<String> scouterList = debugPreference.getObject(
              "scouter_list", List.class);
          // Add all scouterList strings to one string with a , separating each
          String scouterListString = "ScoutData"
              + ","
              + String.join(",", scouterList);
          // Store the scouter list QR code in the 2nd to last slot
          qr_img[numChunks] = qrCodeGenerator.generateQRCode(scouterListString,
              1000, 35, false);

          // Build the google config string from the configPreference
          String googleConfig = "GoogleConfig"
              + ","
              + configPreference.getString("workbook_id")
              + ","
              + configPreference.getString("Crowd_range")
              + ","
              + configPreference.getString("Pit_range")
              + ","
              + configPreference.getString("Specialty_range");

          // Store the google config QR code in the last slot
          qr_img[numChunks+1] = qrCodeGenerator.generateQRCode(googleConfig,
              1000, 35, false);

          // Start the animation loop
          final Handler handler = new Handler(Looper.getMainLooper());
          final Runnable runnable = getRunnable(qr_img, handler);
          handler.post(runnable);
        }
      }
    });

    AutoCompleteTextView custom_scout = binding.customScouter;
    TextInputLayout scout_selector = binding.menuLayout;
    SwitchMaterial custom_switch = binding.customScouterSwitch;
    SwitchMaterial role_lock_switch = binding.roleLockSwitch;
    SegmentedButtonGroup position_selector = binding.buttonGroupPosition;
    SegmentedButtonGroup role_selector = binding.buttonGroupRole;

    position_selector.setPosition(0, true);
    role_selector.setPosition(1, true);
    role_lock_switch.setChecked(true);

    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.ui_list_item,
        scouterList);
    AutoCompleteTextView dropdown = binding.scouterSelect;

    entryLabels = Arrays.asList(
        requireContext().getResources().getStringArray(R.array.team_list));
    dropdown.setAdapter(adapter);
    dropdown.setThreshold(0);

    custom_scout.setSelectAllOnFocus(true);
    custom_scout.setOnEditorActionListener((v, keyCode, event) -> {
      if ((keyCode == KeyEvent.KEYCODE_ENTER)) {
        custom_scout.clearFocus();
        return true;
      }
      scouterList.add(custom_scout.getText().toString());
      generateQrCode();
      return false;
    });

    position_selector.setOnPositionChangedListener(position -> {
      switch (position) {
        case 0:
          crowd_position.set("1");
          break;
        case 1:
          crowd_position.set("2");
          break;
        case 2:
          crowd_position.set("3");
          break;
        case 3:
          crowd_position.set("4");
          break;
        case 4:
          crowd_position.set("5");
          break;
        case 5:
          crowd_position.set("6");
          break;
      }
      generateQrCode();
    });


    role_selector.setOnPositionChangedListener(position -> {
      switch (position) {
        case 0:
          role.set("master");
          position_selector.setVisibility(View.INVISIBLE);
          break;
        case 1:
          role.set("crowd");
          position_selector.setVisibility(View.VISIBLE);
          break;
        case 2:
          role.set("pit");
          position_selector.setVisibility(View.INVISIBLE);
          break;
        case 3:
          role.set("special");
          position_selector.setVisibility(View.INVISIBLE);
          break;
      }
      generateQrCode();
    });

    role_lock_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (isChecked) {
        role_lock_switch.setText(R.string.role_locked);
        lock_status.set("true");
      } else {
        role_lock_switch.setText(R.string.role_unlocked);
        lock_status.set("false");
      }
      generateQrCode();
    });

    dropdown.setOnItemClickListener((parent, view1, position, id) -> {
      scouter_name.set(dropdown.getText().toString());
      generateQrCode();
    });

    custom_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (isChecked) {
        custom_scout.setVisibility(View.VISIBLE);
        scout_selector.setVisibility(View.INVISIBLE);
      } else {
        custom_scout.setVisibility(View.INVISIBLE);
        scout_selector.setVisibility(View.VISIBLE);
      }
      generateQrCode();
    });

    if (lock) {
      binding.buttonBackProvision.setText(R.string.reprovision);
      binding.buttonBackProvision.setOnClickListener(view1 ->
          controller.navigate(R.id.action_provisionFragment_to_ScannerFragment));
    }

    binding.dataEraseSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (isChecked) {
        buttonView.setText(R.string.delete_data);
        data_erase.set("true");
      } else {
        buttonView.setText(R.string.dont_delete_data);
        data_erase.set("false");
      }
      generateQrCode();
    });

    generateQrCode();
    refreshActionBar();
  }

  @NonNull private Runnable getRunnable(Bitmap[] qr_img, Handler handler) {
    final int[] currentIndex = { 0 };
    final int[] currentRepeat = { 0 };
    long delayMillis = 300;
    final int repeatCount = 1;
    ImageView qr_img_view = requireView().findViewById(R.id.qr_img);
    return new Runnable() {
      @Override
      public void run() {
        if (currentIndex[0] < qr_img.length) {
          qr_img_view.setImageBitmap(qr_img[currentIndex[0]]);
          currentIndex[0]++;
          handler.postDelayed(this, delayMillis);
        } else {
          // Check if we need to repeat
          if (currentRepeat[0] < repeatCount) {
            currentRepeat[0]++;
            currentIndex[0] = 0; // Reset index for next repeat
            handler.postDelayed(this, delayMillis); // Start the next repeat
          }
        }
      }
    };
  }

  private void generateQrCode() {
    SwitchMaterial custom_switch = requireView().findViewById(R.id.custom_scouter_switch);
    AutoCompleteTextView custom_scout = requireView().findViewById(R.id.custom_scouter);
    AutoCompleteTextView dropdown = requireView().findViewById(R.id.scouter_select);

    if (custom_switch.isChecked()) {
      scouter_name.set(String.valueOf(custom_scout.getText()));
    } else {
      scouter_name.set(dropdown.getText().toString());
    }

    content_string.set(
        String.format("role,%s,crowd_position,%s,name,%s,lock,%s,match,%s,format,%s,special,%s",
        role.get(), crowd_position.get(), scouter_name.get(), lock_status.get(),
        match.get(), data_erase.get(), special_selector.get()));

    ImageView qr_img = requireView().findViewById(R.id.qr_img);
    qr_img.setImageBitmap(qrCodeGenerator.generateQRCode(content_string.get(),
        1000, 35, true));
  }

  public void refreshActionBar() {
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    assert activity != null;
    ActionBar actionBar = activity.getSupportActionBar();
    Objects.requireNonNull(actionBar).setTitle("Provisioning");
    actionBar.setSubtitle("");
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

}