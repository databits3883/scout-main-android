package com.databits.androidscouting.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.databits.androidscouting.R;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepositoryProvider;
import com.databits.androidscouting.viewmodel.ConfigViewModel;
import com.databits.androidscouting.viewmodel.ConfigViewModelFactory;

import java.util.Locale;

/**
 * Dialog fragment for configuring camera settings.
 * Provides UI controls for all camera-related preferences.
 */
public class CameraSettingsDialogFragment extends DialogFragment {

    /**
     * Callback interface for notifying when settings are applied
     */
    public interface OnSettingsAppliedListener {
        void onSettingsApplied();
    }

    private ConfigViewModel viewModel;
    private PreferenceRepository repository;
    private OnSettingsAppliedListener settingsAppliedListener;

    // UI Components
    private CheckBox checkHapticFeedback;
    private CheckBox checkAudioFeedback;
    private CheckBox checkSuccessAnimation;
    private CheckBox checkShowReticle;
    private CheckBox checkShowStats;
    private CheckBox checkAutoBrightness;
    private CheckBox checkKeepScreenOn;
    private SeekBar seekExposure;
    private TextView textExposureValue;
    private SeekBar seekMinBarcodeSize;
    private TextView textMinBarcodeSize;
    private CheckBox checkCenterWeighted;
    private View layoutCenterThreshold;
    private SeekBar seekCenterThreshold;
    private TextView textCenterThreshold;
    private CheckBox checkShowFps;
    private CheckBox checkShowResolution;

    /**
     * Set the listener to be notified when settings are applied
     */
    public void setOnSettingsAppliedListener(OnSettingsAppliedListener listener) {
        this.settingsAppliedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Initialize ViewModel and Repository
        repository = PreferenceRepositoryProvider.get(requireContext());
        ConfigViewModelFactory factory = new ConfigViewModelFactory(repository);
        viewModel = new ViewModelProvider(requireActivity(), factory).get(ConfigViewModel.class);

        // Inflate the dialog layout
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.dialog_camera_settings, null);

        // Initialize UI components
        initializeViews(view);

        // Load current settings
        loadCurrentSettings();

        // Setup listeners
        setupListeners();

        // Build and return the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        return builder.create();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews(View view) {
        // Feedback settings
        checkHapticFeedback = view.findViewById(R.id.checkHapticFeedback);
        checkAudioFeedback = view.findViewById(R.id.checkAudioFeedback);
        checkSuccessAnimation = view.findViewById(R.id.checkSuccessAnimation);

        // Display settings
        checkShowReticle = view.findViewById(R.id.checkShowReticle);
        checkShowStats = view.findViewById(R.id.checkShowStats);
        checkAutoBrightness = view.findViewById(R.id.checkAutoBrightness);
        checkKeepScreenOn = view.findViewById(R.id.checkKeepScreenOn);

        // Advanced settings
        seekExposure = view.findViewById(R.id.seekExposure);
        textExposureValue = view.findViewById(R.id.textExposureValue);
        seekMinBarcodeSize = view.findViewById(R.id.seekMinBarcodeSize);
        textMinBarcodeSize = view.findViewById(R.id.textMinBarcodeSize);
        checkCenterWeighted = view.findViewById(R.id.checkCenterWeighted);
        layoutCenterThreshold = view.findViewById(R.id.layoutCenterThreshold);
        seekCenterThreshold = view.findViewById(R.id.seekCenterThreshold);
        textCenterThreshold = view.findViewById(R.id.textCenterThreshold);

        // Debug settings
        checkShowFps = view.findViewById(R.id.checkShowFps);
        checkShowResolution = view.findViewById(R.id.checkShowResolution);

        // Action buttons
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonSave = view.findViewById(R.id.buttonSave);

        buttonCancel.setOnClickListener(v -> dismiss());
        buttonSave.setOnClickListener(v -> {
            saveSettings();
            // Notify listener that settings were applied
            if (settingsAppliedListener != null) {
                settingsAppliedListener.onSettingsApplied();
            }
            dismiss();
        });
    }

    /**
     * Load current settings from repository
     */
    private void loadCurrentSettings() {
        // Feedback settings
        checkHapticFeedback.setChecked(repository.isCameraHapticFeedbackEnabled());
        checkAudioFeedback.setChecked(repository.isCameraAudioFeedbackEnabled());
        checkSuccessAnimation.setChecked(repository.isCameraShowSuccessAnimationEnabled());

        // Display settings
        checkShowReticle.setChecked(repository.isCameraShowReticleEnabled());
        checkShowStats.setChecked(repository.isCameraShowStatsEnabled());
        checkAutoBrightness.setChecked(repository.isCameraAutoBrightnessEnabled());
        checkKeepScreenOn.setChecked(repository.isCameraKeepScreenOnEnabled());

        // Exposure compensation (-2 to +2, seekbar is 0 to 4)
        int exposure = repository.getCameraExposureCompensation();
        seekExposure.setProgress(exposure + 2);
        textExposureValue.setText(String.valueOf(exposure));

        // Minimum barcode size
        int minSize = repository.getCameraMinBarcodeSize();
        seekMinBarcodeSize.setProgress(minSize);
        textMinBarcodeSize.setText(String.format(Locale.US, "%dpx", minSize));

        // Center-weighted scanning
        boolean centerWeighted = repository.isCameraCenterWeightedEnabled();
        checkCenterWeighted.setChecked(centerWeighted);
        layoutCenterThreshold.setVisibility(centerWeighted ? View.VISIBLE : View.GONE);

        int centerThreshold = repository.getCameraCenterThreshold();
        seekCenterThreshold.setProgress(centerThreshold);
        textCenterThreshold.setText(String.format(Locale.US, "%dpx", centerThreshold));

        // Debug settings
        checkShowFps.setChecked(repository.isCameraShowFpsEnabled());
        checkShowResolution.setChecked(repository.isCameraShowResolutionEnabled());
    }

    /**
     * Setup listeners for interactive controls
     */
    private void setupListeners() {
        // Exposure compensation listener
        seekExposure.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int exposure = progress - 2; // Convert 0-4 to -2 to +2
                textExposureValue.setText(String.valueOf(exposure));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Minimum barcode size listener
        seekMinBarcodeSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textMinBarcodeSize.setText(String.format(Locale.US, "%dpx", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Center-weighted checkbox listener
        checkCenterWeighted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutCenterThreshold.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Center threshold listener
        seekCenterThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textCenterThreshold.setText(String.format(Locale.US, "%dpx", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /**
     * Save all settings to repository via ViewModel
     */
    private void saveSettings() {
        // Feedback settings
        viewModel.updateCameraHapticFeedback(checkHapticFeedback.isChecked());
        viewModel.updateCameraAudioFeedback(checkAudioFeedback.isChecked());
        viewModel.updateCameraShowSuccessAnimation(checkSuccessAnimation.isChecked());

        // Display settings
        viewModel.updateCameraShowReticle(checkShowReticle.isChecked());
        viewModel.updateCameraShowStats(checkShowStats.isChecked());
        viewModel.updateCameraAutoBrightness(checkAutoBrightness.isChecked());
        viewModel.updateCameraKeepScreenOn(checkKeepScreenOn.isChecked());

        // Exposure compensation
        int exposure = seekExposure.getProgress() - 2; // Convert 0-4 to -2 to +2
        viewModel.updateCameraExposureCompensation(exposure);

        // Minimum barcode size
        viewModel.updateCameraMinBarcodeSize(seekMinBarcodeSize.getProgress());

        // Center-weighted scanning
        viewModel.updateCameraCenterWeighted(checkCenterWeighted.isChecked());
        viewModel.updateCameraCenterThreshold(seekCenterThreshold.getProgress());

        // Debug settings
        viewModel.updateCameraShowFps(checkShowFps.isChecked());
        viewModel.updateCameraShowResolution(checkShowResolution.isChecked());
    }
}
