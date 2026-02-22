package com.databits.androidscouting.fragment;

import android.animation.ValueAnimator;
import android.content.pm.ActivityInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import com.databits.androidscouting.R;
import com.databits.androidscouting.data.repository.CameraSettingsStore;
import com.databits.androidscouting.databinding.FragmentScannerBinding;
import com.databits.androidscouting.viewmodel.CameraSettingsViewModel;

final class ScannerCameraUiController {
    private final Fragment fragment;
    private final FragmentScannerBinding binding;
    private final CameraSettingsViewModel viewModel;
    private final CameraSettingsStore repository;
    private final ScannerUiFeedbackController feedbackController;

    private LifecycleCameraController camController;
    private PreviewView preview;
    private boolean isTorchOn = false;
    private boolean isScanningPaused = false;
    private android.util.Range<Integer> exposureRange;

    ScannerCameraUiController(
        Fragment fragment,
        FragmentScannerBinding binding,
        CameraSettingsViewModel viewModel,
        CameraSettingsStore repository,
        ScannerUiFeedbackController feedbackController
    ) {
        this.fragment = fragment;
        this.binding = binding;
        this.viewModel = viewModel;
        this.repository = repository;
        this.feedbackController = feedbackController;
    }

    void attachCameraController(LifecycleCameraController camController, PreviewView preview) {
        this.camController = camController;
        this.preview = preview;
    }

    boolean isScanningPaused() {
        return isScanningPaused;
    }

    void setScanningPaused(boolean paused) {
        isScanningPaused = paused;
    }

    void setupControls() {
        setupTorchButton();
        setupZoomButtons();
        setupPauseScanButton();
        observeCameraSettings();
    }

    void setupAutoSettings() {
        if (repository.isCameraAutoBrightnessEnabled()) {
            Window window = fragment.requireActivity().getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = 1.0f;
            window.setAttributes(layoutParams);
        }

        if (repository.isCameraKeepScreenOnEnabled()) {
            fragment.requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        fragment.requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    void restoreWindowSettings() {
        if (repository.isCameraAutoBrightnessEnabled()) {
            Window window = fragment.requireActivity().getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            window.setAttributes(layoutParams);
        }

        if (repository.isCameraKeepScreenOnEnabled()) {
            fragment.requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    void applyCameraSettings() {
        if (camController == null) {
            return;
        }

        androidx.camera.core.CameraInfo cameraInfo = camController.getCameraInfo();
        if (cameraInfo != null) {
            androidx.camera.core.ExposureState exposureState = cameraInfo.getExposureState();
            if (exposureState != null) {
                exposureRange = exposureState.getExposureCompensationRange();
            }
        }

        int exposureCompensation = repository.getCameraExposureCompensation();
        if (exposureRange != null && exposureRange.contains(exposureCompensation)) {
            camController.getCameraControl().setExposureCompensationIndex(exposureCompensation);
        }
    }

    private void setupTorchButton() {
        binding.torchButton.setOnClickListener(v -> {
            isTorchOn = !isTorchOn;
            if (camController != null) {
                camController.enableTorch(isTorchOn);
            }
            viewModel.updateCameraTorch(isTorchOn);
            binding.torchButton.setImageResource(isTorchOn ?
                R.drawable.ic_flashlight_on : R.drawable.ic_flashlight_off);
            binding.torchButton.setContentDescription(fragment.getString(isTorchOn ?
                R.string.torch_on : R.string.torch_off));
        });

        viewModel.getCameraTorch().observe(fragment.getViewLifecycleOwner(), enabled -> {
            if (enabled != null && enabled != isTorchOn) {
                isTorchOn = enabled;
                if (camController != null) {
                    camController.enableTorch(enabled);
                }
                binding.torchButton.setImageResource(enabled ?
                    R.drawable.ic_flashlight_on : R.drawable.ic_flashlight_off);
            }
        });
    }

    private void setupZoomButtons() {
        final float zoomStep = 0.25f;
        final float zoomContinuousStep = 0.05f;
        final float minZoom = 1.0f;
        final float maxZoom = 4.0f;
        final int repeatDelay = 50;

        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        final boolean[] isLongPressing = {false};

        binding.zoomInButton.setOnClickListener(v -> {
            if (!isLongPressing[0] && camController != null) {
                float currentZoom = camController.getZoomState().getValue().getZoomRatio();
                float newZoom = Math.min(currentZoom + zoomStep, maxZoom);
                animateZoom(newZoom);
                viewModel.updateCameraZoomLevel(newZoom);
            }
        });

        binding.zoomInButton.setOnLongClickListener(v -> {
            isLongPressing[0] = true;
            final Runnable zoomRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isLongPressing[0] && camController != null) {
                        float currentZoom = camController.getZoomState().getValue().getZoomRatio();
                        float newZoom = Math.min(currentZoom + zoomContinuousStep, maxZoom);
                        if (newZoom < maxZoom) {
                            camController.setZoomRatio(newZoom);
                            handler.postDelayed(this, repeatDelay);
                        } else {
                            camController.setZoomRatio(maxZoom);
                            isLongPressing[0] = false;
                        }
                    }
                }
            };
            handler.post(zoomRunnable);
            return true;
        });

        binding.zoomInButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (isLongPressing[0]) {
                    isLongPressing[0] = false;
                    handler.removeCallbacksAndMessages(null);
                    if (camController != null) {
                        float finalZoom = camController.getZoomState().getValue().getZoomRatio();
                        viewModel.updateCameraZoomLevel(finalZoom);
                    }
                }
            }
            return false;
        });

        binding.zoomOutButton.setOnClickListener(v -> {
            if (!isLongPressing[0] && camController != null) {
                float currentZoom = camController.getZoomState().getValue().getZoomRatio();
                float newZoom = Math.max(currentZoom - zoomStep, minZoom);
                animateZoom(newZoom);
                viewModel.updateCameraZoomLevel(newZoom);
            }
        });

        binding.zoomOutButton.setOnLongClickListener(v -> {
            isLongPressing[0] = true;
            final Runnable zoomRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isLongPressing[0] && camController != null) {
                        float currentZoom = camController.getZoomState().getValue().getZoomRatio();
                        float newZoom = Math.max(currentZoom - zoomContinuousStep, minZoom);
                        if (newZoom > minZoom) {
                            camController.setZoomRatio(newZoom);
                            handler.postDelayed(this, repeatDelay);
                        } else {
                            camController.setZoomRatio(minZoom);
                            isLongPressing[0] = false;
                        }
                    }
                }
            };
            handler.post(zoomRunnable);
            return true;
        });

        binding.zoomOutButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (isLongPressing[0]) {
                    isLongPressing[0] = false;
                    handler.removeCallbacksAndMessages(null);
                    if (camController != null) {
                        float finalZoom = camController.getZoomState().getValue().getZoomRatio();
                        viewModel.updateCameraZoomLevel(finalZoom);
                    }
                }
            }
            return false;
        });

        viewModel.getCameraZoomLevel().observe(fragment.getViewLifecycleOwner(), zoom -> {
            if (zoom != null && camController != null) {
                camController.setZoomRatio(zoom);
            }
        });
    }

    private void animateZoom(float targetZoom) {
        if (camController == null || camController.getZoomState().getValue() == null) {
            return;
        }

        Float currentZoom = camController.getZoomState().getValue().getZoomRatio();
        if (currentZoom == null) {
            currentZoom = 1.0f;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(currentZoom, targetZoom);
        animator.addUpdateListener(anim -> camController.setZoomRatio((Float) anim.getAnimatedValue()));
        animator.setDuration(200);
        animator.start();
    }

    private void setupPauseScanButton() {
        binding.pauseScanButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setScanningPaused(isChecked);
            binding.pauseScanButton.setText(isChecked ? R.string.resume_scanning : R.string.pause_scanning);
        });
    }

    private void observeCameraSettings() {
        viewModel.getCameraShowReticle().observe(fragment.getViewLifecycleOwner(), enabled -> {
            if (enabled != null) {
                binding.scanReticle.setVisibility(enabled ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getCameraShowStats().observe(fragment.getViewLifecycleOwner(), enabled -> {
            if (enabled != null) {
                binding.statsText.setVisibility(enabled ? View.VISIBLE : View.GONE);
                binding.scanControlBar.setVisibility(enabled ? View.VISIBLE : View.GONE);
                if (enabled) {
                    feedbackController.refreshStatistics(binding);
                }
            }
        });

        viewModel.getCameraShowFps().observe(fragment.getViewLifecycleOwner(), enabled -> {
            if (enabled != null) {
                binding.fpsText.setVisibility(enabled ? View.VISIBLE : View.GONE);
                feedbackController.updateDebugInfoPanelVisibility(repository, binding);
            }
        });

        viewModel.getCameraShowResolution().observe(fragment.getViewLifecycleOwner(), enabled -> {
            if (enabled != null) {
                binding.resolutionText.setVisibility(enabled ? View.VISIBLE : View.GONE);
                if (enabled && preview != null) {
                    feedbackController.updateResolutionDisplay(fragment, repository, binding, preview);
                }
                feedbackController.updateDebugInfoPanelVisibility(repository, binding);
            }
        });

        viewModel.getCameraExposureCompensation().observe(fragment.getViewLifecycleOwner(), exposure -> {
            if (exposure != null && camController != null && exposureRange != null && exposureRange.contains(exposure)) {
                camController.getCameraControl().setExposureCompensationIndex(exposure);
            }
        });
    }
}
