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

        if (lock) {
            role_selector(controller);
        }

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
    }
}