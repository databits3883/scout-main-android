package com.databits.androidscouting.fragment;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import androidx.fragment.app.Fragment;
import com.databits.androidscouting.data.repository.CameraSettingsStore;
import com.databits.androidscouting.databinding.FragmentScannerBinding;
import java.util.Locale;

final class ScannerUiFeedbackController {
    private Vibrator vibrator;
    private ToneGenerator toneGenerator;
    private int totalScans = 0;
    private int successfulScans = 0;
    private long frameCount = 0;
    private long lastFpsTime = System.currentTimeMillis();

    void onSuccessfulScan(Fragment fragment, CameraSettingsStore repository, FragmentScannerBinding binding) {
        showScanFeedback(fragment, repository);
        animateScanSuccess(fragment, repository, binding);

        totalScans++;
        successfulScans++;
        if (repository.isCameraShowStatsEnabled()) {
            updateScanStatistics(binding);
        }
    }

    void refreshStatistics(FragmentScannerBinding binding) {
        updateScanStatistics(binding);
    }

    void onFrame(Fragment fragment, CameraSettingsStore repository, FragmentScannerBinding binding) {
        if (!repository.isCameraShowFpsEnabled() || !fragment.isAdded()) {
            return;
        }

        frameCount++;
        long now = System.currentTimeMillis();
        if (now - lastFpsTime >= 1000) {
            double fps = frameCount / ((now - lastFpsTime) / 1000.0);
            fragment.requireActivity().runOnUiThread(() -> {
                if (fragment.isAdded()) {
                    binding.fpsText.setText(String.format(Locale.US, "FPS: %.1f", fps));
                }
            });
            frameCount = 0;
            lastFpsTime = now;
        }
    }

    void updateResolutionDisplay(Fragment fragment, CameraSettingsStore repository, FragmentScannerBinding binding, View preview) {
        if (!repository.isCameraShowResolutionEnabled() || !fragment.isAdded()) {
            return;
        }

        fragment.requireActivity().runOnUiThread(() -> {
            if (fragment.isAdded()) {
                binding.resolutionText.setText(
                    String.format(Locale.US, "Resolution: %dx%d", preview.getWidth(), preview.getHeight())
                );
            }
        });
    }

    void updateDebugInfoPanelVisibility(CameraSettingsStore repository, FragmentScannerBinding binding) {
        boolean showAny = repository.isCameraShowFpsEnabled() || repository.isCameraShowResolutionEnabled();
        binding.debugInfoPanel.setVisibility(showAny ? View.VISIBLE : View.GONE);
    }

    void release() {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }

    private void showScanFeedback(Fragment fragment, CameraSettingsStore repository) {
        if (!fragment.isAdded()) {
            return;
        }

        if (repository.isCameraHapticFeedbackEnabled()) {
            if (vibrator == null) {
                vibrator = (Vibrator) fragment.requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }

        if (repository.isCameraAudioFeedbackEnabled()) {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
            }
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
        }
    }

    private void animateScanSuccess(Fragment fragment, CameraSettingsStore repository, FragmentScannerBinding binding) {
        if (!repository.isCameraShowSuccessAnimationEnabled() || !fragment.isAdded()) {
            return;
        }

        fragment.requireActivity().runOnUiThread(() -> {
            if (!fragment.isAdded()) {
                return;
            }

            binding.scanSuccessOverlay.setVisibility(View.VISIBLE);
            binding.scanSuccessOverlay.setAlpha(0.4f);
            binding.scanSuccessOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    if (fragment.isAdded()) {
                        binding.scanSuccessOverlay.setVisibility(View.GONE);
                    }
                })
                .start();
        });
    }

    private void updateScanStatistics(FragmentScannerBinding binding) {
        if (totalScans == 0) {
            binding.statsText.setText("Scanned: 0/0 (0%)");
            return;
        }

        double successRate = (successfulScans * 100.0) / totalScans;
        binding.statsText.setText(String.format(Locale.US,
            "Scanned: %d/%d (%.1f%%)", successfulScans, totalScans, successRate));
    }
}
