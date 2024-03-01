package com.databits.androidscouting.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.databits.androidscouting.R;
import com.databits.androidscouting.databinding.FragmentMainBinding;
import com.databits.androidscouting.util.FileUtils;
import com.databits.androidscouting.util.GoogleAuthActivity;
import com.databits.androidscouting.util.ScoutUtils;
import com.databits.androidscouting.util.TeamInfo;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.io.File;
import java.util.Objects;

public class Main extends Fragment {

    private FragmentMainBinding binding;

    Preference configPreference = PowerPreference.getFileByName("Config");
    Preference debugPreference = PowerPreference.getFileByName("Debug");

    TeamInfo teamInfo;
    FileUtils fileUtils;
    ScoutUtils scoutUtils;

    String role;
    boolean lock;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Set Sane Defaults to enable debugging and new install demo
        configPreference.setDefaults(R.xml.defaults_config);
        debugPreference.setDefaults(R.xml.defaults_debug);
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }
    @SuppressLint("SourceLockedOrientationActivity")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        role = configPreference.getString("device_role");

        lock = configPreference.getBoolean("role_locked_toggle", false);

        refreshActionBar();

        int pos = configPreference.getInt("crowd_position", 0);

        debugPreference.setBoolean("isRedteam", pos != 1 && pos != 2 && pos != 3);

        teamInfo = new TeamInfo(requireContext());
        fileUtils = new FileUtils(requireContext());
        scoutUtils = new ScoutUtils(requireContext());

        NavController controller = NavHostFragment.findNavController(Main.this);

        binding.buttonScanner.setOnClickListener(view1 -> controller
                .navigate(R.id.action_StartFragment_to_ScannerFragment));
        binding.buttonCrowd.setOnClickListener(view1 -> controller
                .navigate(R.id.action_StartFragment_to_crowdScoutFragment));
        binding.buttonPit.setOnClickListener(view1 -> controller
                .navigate(R.id.action_StartFragment_to_pitScoutFragment));
        binding.buttonDrawingMap.setOnClickListener(view1 -> controller
                .navigate(R.id.action_StartFragment_to_DrawingMapFragment));
        binding.buttonRole.setOnClickListener(view1 -> controller
            .navigate(R.id.action_StartFragment_to_provisionFragment));
        binding.buttonSettings.setOnClickListener(view1 -> controller
            .navigate(R.id.action_StartFragment_to_SettingsFragment));
        binding.buttonSpecial.setOnClickListener(view1 -> controller
            .navigate(R.id.action_StartFragment_to_SpecialFragment));

        binding.teamSavedButton.setOnClickListener(view1 -> savedTeams());

        binding.buttonPermissions.setOnClickListener(view1 -> {
            if (scoutUtils.allPermissionsGranted()) {
                Toast.makeText(requireContext(), "Permissions Already Granted!",
                    Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(), ScoutUtils.REQUIRED_PERMISSIONS,
                    ScoutUtils.REQUEST_CODE_PERMISSIONS
                );
            }
        });

        binding.buttonGroupSelector.setOnPositionChangedListener(position -> {
            switch (position) {
                case 0:
                    configPreference.remove("altMode");
                    break;
                case 1:
                    configPreference.setBoolean("altMode",false);
                    break;
                case 2:
                    configPreference.setBoolean("altMode",true);
                    break;
            }
        });

        boolean alt = configPreference.getBoolean("altMode");
        if (alt) {
            binding.buttonGroupSelector.setPosition(1,false);
        } else {
            binding.buttonGroupSelector.setPosition(2,false);
        }


        if (lock) {
            role_selector(controller);
        }

        buttonState();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        binding.buttonGoogle.setOnClickListener(view1 -> {
            Intent intent = new Intent(requireContext(), GoogleAuthActivity.class);
            startActivity(intent);
        });

        ActivityResultLauncher<Intent> teamsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        importTeams(data.getData());
                        teamInfo.read_teams();
                    }
                }
            }
        );

        binding.buttonTeams.setOnClickListener(view1 -> {
            Intent data  = fileUtils.intentFileDialog();
            Intent.createChooser(data, "Select a layout.json file to import");
            teamsLauncher.launch(data);
        });

        // Launches the intent to allow the app to change the screen brightness
        binding.buttonBright.setOnClickListener(view1 -> {
            if (Settings.System.canWrite(requireContext())) {
                Toast.makeText(requireContext(), "Brightness Permission Already Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));
                startActivity(intent);
            }
        });
    }

    public void buttonState() {
        scoutUtils.setButtonStatus(binding.buttonTeams,
            fileUtils.fileExists(String.valueOf(
                new File(requireContext().getFilesDir() + "/" + "teams.csv"))),
            "Match/Team Info Loaded", "Import Match/Team Info");

        scoutUtils.setButtonStatus(binding.buttonGoogle,
            configPreference.getString("google_account_name").isEmpty(),
            "Logged into Google", "Sign into Google");

        scoutUtils.setButtonStatus(binding.buttonBright,
            Settings.System.canWrite(requireActivity()),
            "Screen Brightness Control Enabled",
            "Enable Screen Brightness Control");

        scoutUtils.setButtonStatus(binding.buttonPermissions,
            scoutUtils.allPermissionsGranted(),
            "All Required Permissions Granted",
            "Enable Required Permissions");
    }

    // Switch statement to determine which role the device is in
    public void role_selector(NavController controller) {
        switch (role) {
            case "master":
                controller.navigate(R.id.action_StartFragment_to_ScannerFragment);
                break;
            case "crowd":
                controller.navigate(R.id.action_StartFragment_to_crowdScoutFragment);
                break;
            case "pit":
                controller.navigate(R.id.action_StartFragment_to_pitScoutFragment);
                break;
            case "special":
                controller.navigate(R.id.action_StartFragment_to_SpecialFragment);
        }
    }

    public void importTeams(Uri uri) {
        Objects.requireNonNull(FileUtils.copyFileToInternal(requireContext(), uri,
            "teams.csv"));
        binding.buttonTeams.setText(R.string.main_team_match);
        binding.buttonTeams.setEnabled(false);
    }

    public void savedTeams(){
        Uri uri=Uri.parse("android.resource://"+requireContext().getPackageName()+"/raw/teams");
        FileUtils.copyFileToInternal(requireContext(),uri,"teams.csv");
        teamInfo.read_teams();
        buttonState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void refreshActionBar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        assert activity != null;
        ActionBar actionBar = activity.getSupportActionBar();
        Objects.requireNonNull(actionBar).setTitle("Welcome to the Scouting app!");
        actionBar.setSubtitle("");
    }

    @Override public void onResume() {
        super.onResume();
        buttonState();
    }
}