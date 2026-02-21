package com.databits.androidscouting.data.repository;

public interface CameraSettingsStore {
    boolean isCameraTorchEnabled();
    void setCameraTorch(boolean enabled);
    boolean isCameraHapticFeedbackEnabled();
    void setCameraHapticFeedback(boolean enabled);
    boolean isCameraAudioFeedbackEnabled();
    void setCameraAudioFeedback(boolean enabled);
    boolean isCameraShowReticleEnabled();
    void setCameraShowReticle(boolean enabled);
    boolean isCameraAutoBrightnessEnabled();
    void setCameraAutoBrightness(boolean enabled);
    boolean isCameraKeepScreenOnEnabled();
    void setCameraKeepScreenOn(boolean enabled);
    boolean isCameraShowStatsEnabled();
    void setCameraShowStats(boolean enabled);
    boolean isCameraShowSuccessAnimationEnabled();
    void setCameraShowSuccessAnimation(boolean enabled);
    int getCameraExposureCompensation();
    void setCameraExposureCompensation(int exposure);
    float getCameraZoomLevel();
    void setCameraZoomLevel(float zoom);
    int getCameraMinBarcodeSize();
    void setCameraMinBarcodeSize(int size);
    boolean isCameraCenterWeightedEnabled();
    void setCameraCenterWeighted(boolean enabled);
    int getCameraCenterThreshold();
    void setCameraCenterThreshold(int threshold);
    boolean isCameraShowFpsEnabled();
    void setCameraShowFps(boolean enabled);
    boolean isCameraShowResolutionEnabled();
    void setCameraShowResolution(boolean enabled);
}
