package com.databits.androidscouting.data.repository;

import androidx.lifecycle.LiveData;
import java.util.List;

public interface SettingsRepository {
    // Defaults / housekeeping
    void setConfigDefaults(int xmlResourceId);
    void setDebugDefaults(int xmlResourceId);
    void clearAllSettingsData();
    void removeManualTeamOverrideValue();

    // Config
    int getCurrentMatch();
    void setCurrentMatch(int match);
    String getCurrentScouter();
    void setCurrentScouter(String name);
    String getDeviceRole();
    void setDeviceRole(String role);
    String getWorkbookId();
    void setWorkbookId(String id);
    
    // Ranges
    String getCrowdRange();
    void setCrowdRange(String range);
    String getPitRange();
    void setPitRange(String range);
    String getSpecialtyRange();
    void setSpecialtyRange(String range);
    
    // Toggles & State
    boolean isRoleLocked();
    void setRoleLocked(boolean locked);
    int getCrowdPosition();
    void setCrowdPosition(int position);
    String getUploadMode();
    void setUploadMode(String mode);
    boolean isGridToggleEnabled();
    void setGridToggle(boolean enabled);
    boolean isForceUploadEnabled();
    void setForceUpload(boolean enabled);
    boolean isAutoLoadCrowdLayoutEnabled();
    void setAutoLoadCrowdLayout(boolean enabled);
    String getGoogleAccountName();
    void setGoogleAccountName(String accountName);
    boolean isSpecialSwitchEnabled();
    void setSpecialSwitch(boolean enabled);
    String getThemeMode();
    void setThemeMode(String mode);
    
    // Camera Settings
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
    
    // Debug Settings
    boolean isManualTeamOverrideEnabled();
    void setManualTeamOverride(boolean enabled);
    int getManualTeamOverrideValue();
    void setManualTeamOverrideValue(int teamNumber);
    boolean isManualMatchOverrideEnabled();
    void setManualMatchOverride(boolean enabled);
    int getManualMatchOverrideValue();
    void setManualMatchOverrideValue(int matchNumber);
    boolean isMaster();
    void setMaster(boolean isMaster);
    boolean isRedTeam();
    void setRedTeam(boolean isRedTeam);
    int getDebugMatch();
    void setDebugMatch(int match);
    int getMapBrushSize();
    void setMapBrushSize(int size);

    // LiveData Access
    LiveData<List<String>> getScouterListLive();
    void setScouterList(List<String> scouters);
    List<String> getScouterList();
}
