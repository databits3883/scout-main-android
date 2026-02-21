package com.databits.androidscouting.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import java.util.concurrent.ExecutorService;

public class CameraSettingsViewModel extends ViewModel {
    private final PreferenceRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<Boolean> cameraTorch = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraHapticFeedback = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraAudioFeedback = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraShowReticle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraAutoBrightness = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraKeepScreenOn = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraShowStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraShowSuccessAnimation = new MutableLiveData<>();
    private final MutableLiveData<Integer> cameraExposureCompensation = new MutableLiveData<>();
    private final MutableLiveData<Float> cameraZoomLevel = new MutableLiveData<>();
    private final MutableLiveData<Integer> cameraMinBarcodeSize = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraCenterWeighted = new MutableLiveData<>();
    private final MutableLiveData<Integer> cameraCenterThreshold = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraShowFps = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraShowResolution = new MutableLiveData<>();

    public CameraSettingsViewModel(PreferenceRepository repository, ExecutorService executor) {
        this.repository = repository;
        this.executor = executor;
        refresh();
    }

    public void refresh() {
        executor.execute(() -> {
            cameraTorch.postValue(repository.isCameraTorchEnabled());
            cameraHapticFeedback.postValue(repository.isCameraHapticFeedbackEnabled());
            cameraAudioFeedback.postValue(repository.isCameraAudioFeedbackEnabled());
            cameraShowReticle.postValue(repository.isCameraShowReticleEnabled());
            cameraAutoBrightness.postValue(repository.isCameraAutoBrightnessEnabled());
            cameraKeepScreenOn.postValue(repository.isCameraKeepScreenOnEnabled());
            cameraShowStats.postValue(repository.isCameraShowStatsEnabled());
            cameraShowSuccessAnimation.postValue(repository.isCameraShowSuccessAnimationEnabled());
            cameraExposureCompensation.postValue(repository.getCameraExposureCompensation());
            cameraZoomLevel.postValue(repository.getCameraZoomLevel());
            cameraMinBarcodeSize.postValue(repository.getCameraMinBarcodeSize());
            cameraCenterWeighted.postValue(repository.isCameraCenterWeightedEnabled());
            cameraCenterThreshold.postValue(repository.getCameraCenterThreshold());
            cameraShowFps.postValue(repository.isCameraShowFpsEnabled());
            cameraShowResolution.postValue(repository.isCameraShowResolutionEnabled());
        });
    }

    public LiveData<Boolean> getCameraTorch() { return cameraTorch; }
    public LiveData<Boolean> getCameraHapticFeedback() { return cameraHapticFeedback; }
    public LiveData<Boolean> getCameraAudioFeedback() { return cameraAudioFeedback; }
    public LiveData<Boolean> getCameraShowReticle() { return cameraShowReticle; }
    public LiveData<Boolean> getCameraAutoBrightness() { return cameraAutoBrightness; }
    public LiveData<Boolean> getCameraKeepScreenOn() { return cameraKeepScreenOn; }
    public LiveData<Boolean> getCameraShowStats() { return cameraShowStats; }
    public LiveData<Boolean> getCameraShowSuccessAnimation() { return cameraShowSuccessAnimation; }
    public LiveData<Integer> getCameraExposureCompensation() { return cameraExposureCompensation; }
    public LiveData<Float> getCameraZoomLevel() { return cameraZoomLevel; }
    public LiveData<Integer> getCameraMinBarcodeSize() { return cameraMinBarcodeSize; }
    public LiveData<Boolean> getCameraCenterWeighted() { return cameraCenterWeighted; }
    public LiveData<Integer> getCameraCenterThreshold() { return cameraCenterThreshold; }
    public LiveData<Boolean> getCameraShowFps() { return cameraShowFps; }
    public LiveData<Boolean> getCameraShowResolution() { return cameraShowResolution; }

    public void updateCameraTorch(boolean enabled) { executor.execute(() -> { repository.setCameraTorch(enabled); cameraTorch.postValue(enabled); }); }
    public void updateCameraHapticFeedback(boolean enabled) { executor.execute(() -> { repository.setCameraHapticFeedback(enabled); cameraHapticFeedback.postValue(enabled); }); }
    public void updateCameraAudioFeedback(boolean enabled) { executor.execute(() -> { repository.setCameraAudioFeedback(enabled); cameraAudioFeedback.postValue(enabled); }); }
    public void updateCameraShowReticle(boolean enabled) { executor.execute(() -> { repository.setCameraShowReticle(enabled); cameraShowReticle.postValue(enabled); }); }
    public void updateCameraAutoBrightness(boolean enabled) { executor.execute(() -> { repository.setCameraAutoBrightness(enabled); cameraAutoBrightness.postValue(enabled); }); }
    public void updateCameraKeepScreenOn(boolean enabled) { executor.execute(() -> { repository.setCameraKeepScreenOn(enabled); cameraKeepScreenOn.postValue(enabled); }); }
    public void updateCameraShowStats(boolean enabled) { executor.execute(() -> { repository.setCameraShowStats(enabled); cameraShowStats.postValue(enabled); }); }
    public void updateCameraShowSuccessAnimation(boolean enabled) { executor.execute(() -> { repository.setCameraShowSuccessAnimation(enabled); cameraShowSuccessAnimation.postValue(enabled); }); }
    public void updateCameraExposureCompensation(int exposure) { executor.execute(() -> { repository.setCameraExposureCompensation(exposure); cameraExposureCompensation.postValue(exposure); }); }
    public void updateCameraZoomLevel(float zoom) { executor.execute(() -> { repository.setCameraZoomLevel(zoom); cameraZoomLevel.postValue(zoom); }); }
    public void updateCameraMinBarcodeSize(int size) { executor.execute(() -> { repository.setCameraMinBarcodeSize(size); cameraMinBarcodeSize.postValue(size); }); }
    public void updateCameraCenterWeighted(boolean enabled) { executor.execute(() -> { repository.setCameraCenterWeighted(enabled); cameraCenterWeighted.postValue(enabled); }); }
    public void updateCameraCenterThreshold(int threshold) { executor.execute(() -> { repository.setCameraCenterThreshold(threshold); cameraCenterThreshold.postValue(threshold); }); }
    public void updateCameraShowFps(boolean enabled) { executor.execute(() -> { repository.setCameraShowFps(enabled); cameraShowFps.postValue(enabled); }); }
    public void updateCameraShowResolution(boolean enabled) { executor.execute(() -> { repository.setCameraShowResolution(enabled); cameraShowResolution.postValue(enabled); }); }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
