package com.databits.androidscouting.data.repository.impl;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.databits.androidscouting.data.dao.ScouterDao;
import com.databits.androidscouting.data.entity.Scouter;
import com.databits.androidscouting.data.repository.SettingsRepository;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.util.ArrayList;
import java.util.List;

public class PowerPreferenceSettingsRepository implements SettingsRepository {
    private final Preference configPreference;
    private final Preference debugPreference;
    private final ScouterDao scouterDao;

    public PowerPreferenceSettingsRepository(ScouterDao scouterDao) {
        this.configPreference = PowerPreference.getFileByName("Config");
        this.debugPreference = PowerPreference.getFileByName("Debug");
        this.scouterDao = scouterDao;
    }

    // Config
    @Override
    public int getCurrentMatch() {
        return configPreference.getInt("current_match", 1);
    }

    @Override
    public void setCurrentMatch(int match) {
        configPreference.setInt("current_match", match);
    }

    @Override
    public String getCurrentScouter() {
        return configPreference.getString("current_scouter", "Scouter");
    }

    @Override
    public void setCurrentScouter(String name) {
        configPreference.setString("current_scouter", name);
    }

    @Override
    public String getDeviceRole() {
        return configPreference.getString("device_role", null);
    }

    @Override
    public void setDeviceRole(String role) {
        configPreference.setString("device_role", role);
    }

    @Override
    public String getWorkbookId() {
        return configPreference.getString("workbook_id", null);
    }

    @Override
    public void setWorkbookId(String id) {
        configPreference.setString("workbook_id", id);
    }

    // Ranges
    @Override
    public String getCrowdRange() {
        return configPreference.getString("Crowd_range", null);
    }

    @Override
    public void setCrowdRange(String range) {
        configPreference.setString("Crowd_range", range);
    }

    @Override
    public String getPitRange() {
        return configPreference.getString("Pit_range", null);
    }

    @Override
    public void setPitRange(String range) {
        configPreference.setString("Pit_range", range);
    }

    @Override
    public String getSpecialtyRange() {
        return configPreference.getString("Specialty_range", null);
    }

    @Override
    public void setSpecialtyRange(String range) {
        configPreference.setString("Specialty_range", range);
    }

    // Toggles
    @Override
    public boolean isRoleLocked() {
        return configPreference.getBoolean("role_locked_toggle", false);
    }

    @Override
    public void setRoleLocked(boolean locked) {
        configPreference.setBoolean("role_locked_toggle", locked);
    }

    @Override
    public int getCrowdPosition() {
        return configPreference.getInt("crowd_position", 0);
    }

    @Override
    public void setCrowdPosition(int position) {
        configPreference.setInt("crowd_position", position);
    }

    @Override
    public String getUploadMode() {
        return configPreference.getString("uploadMode", null);
    }

    @Override
    public void setUploadMode(String mode) {
        configPreference.setString("uploadMode", mode);
    }

    @Override
    public boolean isGridToggleEnabled() {
        return configPreference.getBoolean("grid_toggle", false);
    }

    @Override
    public void setGridToggle(boolean enabled) {
        configPreference.setBoolean("grid_toggle", enabled);
    }

    @Override
    public boolean isForceUploadEnabled() {
        return configPreference.getBoolean("force_upload_toggle", false);
    }

    @Override
    public void setForceUpload(boolean enabled) {
        configPreference.setBoolean("force_upload_toggle", enabled);
    }

    @Override
    public boolean isAutoLoadCrowdLayoutEnabled() {
        return configPreference.getBoolean("auto_load_crowd_layout_toggle", false);
    }

    @Override
    public void setAutoLoadCrowdLayout(boolean enabled) {
        configPreference.setBoolean("auto_load_crowd_layout_toggle", enabled);
    }

    @Override
    public String getGoogleAccountName() {
        return configPreference.getString("google_account_name", null);
    }

    @Override
    public void setGoogleAccountName(String accountName) {
        configPreference.setString("google_account_name", accountName);
    }

    @Override
    public boolean isSpecialSwitchEnabled() {
        return configPreference.getBoolean("specialSwitch", false);
    }

    @Override
    public void setSpecialSwitch(boolean enabled) {
        configPreference.setBoolean("specialSwitch", enabled);
    }

    @Override
    public String getThemeMode() {
        return configPreference.getString("theme_mode", "system");
    }

    @Override
    public void setThemeMode(String mode) {
        configPreference.setString("theme_mode", mode);
    }

    // Camera
    @Override
    public boolean isCameraTorchEnabled() {
        return configPreference.getBoolean("camera_torch_enabled", false);
    }

    @Override
    public void setCameraTorch(boolean enabled) {
        configPreference.setBoolean("camera_torch_enabled", enabled);
    }

    @Override
    public boolean isCameraHapticFeedbackEnabled() {
        return configPreference.getBoolean("camera_haptic_feedback", true);
    }

    @Override
    public void setCameraHapticFeedback(boolean enabled) {
        configPreference.setBoolean("camera_haptic_feedback", enabled);
    }

    @Override
    public boolean isCameraAudioFeedbackEnabled() {
        return configPreference.getBoolean("camera_audio_feedback", false);
    }

    @Override
    public void setCameraAudioFeedback(boolean enabled) {
        configPreference.setBoolean("camera_audio_feedback", enabled);
    }

    @Override
    public boolean isCameraShowReticleEnabled() {
        return configPreference.getBoolean("camera_show_reticle", true);
    }

    @Override
    public void setCameraShowReticle(boolean enabled) {
        configPreference.setBoolean("camera_show_reticle", enabled);
    }

    @Override
    public boolean isCameraAutoBrightnessEnabled() {
        return configPreference.getBoolean("camera_auto_brightness", true);
    }

    @Override
    public void setCameraAutoBrightness(boolean enabled) {
        configPreference.setBoolean("camera_auto_brightness", enabled);
    }

    @Override
    public boolean isCameraKeepScreenOnEnabled() {
        return configPreference.getBoolean("camera_keep_screen_on", true);
    }

    @Override
    public void setCameraKeepScreenOn(boolean enabled) {
        configPreference.setBoolean("camera_keep_screen_on", enabled);
    }

    @Override
    public boolean isCameraShowStatsEnabled() {
        return configPreference.getBoolean("camera_show_stats", false);
    }

    @Override
    public void setCameraShowStats(boolean enabled) {
        configPreference.setBoolean("camera_show_stats", enabled);
    }

    @Override
    public boolean isCameraShowSuccessAnimationEnabled() {
        return configPreference.getBoolean("camera_show_success_animation", true);
    }

    @Override
    public void setCameraShowSuccessAnimation(boolean enabled) {
        configPreference.setBoolean("camera_show_success_animation", enabled);
    }

    @Override
    public int getCameraExposureCompensation() {
        return configPreference.getInt("camera_exposure_compensation", 0);
    }

    @Override
    public void setCameraExposureCompensation(int exposure) {
        configPreference.setInt("camera_exposure_compensation", exposure);
    }

    @Override
    public float getCameraZoomLevel() {
        return configPreference.getFloat("camera_zoom_level", 1.0f);
    }

    @Override
    public void setCameraZoomLevel(float zoom) {
        configPreference.setFloat("camera_zoom_level", zoom);
    }

    @Override
    public int getCameraMinBarcodeSize() {
        return configPreference.getInt("camera_min_barcode_size", 100);
    }

    @Override
    public void setCameraMinBarcodeSize(int size) {
        configPreference.setInt("camera_min_barcode_size", size);
    }

    @Override
    public boolean isCameraCenterWeightedEnabled() {
        return configPreference.getBoolean("camera_center_weighted", true);
    }

    @Override
    public void setCameraCenterWeighted(boolean enabled) {
        configPreference.setBoolean("camera_center_weighted", enabled);
    }

    @Override
    public int getCameraCenterThreshold() {
        return configPreference.getInt("camera_center_threshold", 300);
    }

    @Override
    public void setCameraCenterThreshold(int threshold) {
        configPreference.setInt("camera_center_threshold", threshold);
    }

    @Override
    public boolean isCameraShowFpsEnabled() {
        return debugPreference.getBoolean("camera_show_fps", false);
    }

    @Override
    public void setCameraShowFps(boolean enabled) {
        debugPreference.setBoolean("camera_show_fps", enabled);
    }

    @Override
    public boolean isCameraShowResolutionEnabled() {
        return debugPreference.getBoolean("camera_show_resolution", false);
    }

    @Override
    public void setCameraShowResolution(boolean enabled) {
        debugPreference.setBoolean("camera_show_resolution", enabled);
    }

    // Debug
    @Override
    public boolean isManualTeamOverrideEnabled() {
        return debugPreference.getBoolean("manual_team_override_toggle", false);
    }

    @Override
    public void setManualTeamOverride(boolean enabled) {
        debugPreference.setBoolean("manual_team_override_toggle", enabled);
    }

    @Override
    public int getManualTeamOverrideValue() {
        return debugPreference.getInt("manual_team_override_value", 0);
    }

    @Override
    public void setManualTeamOverrideValue(int teamNumber) {
        debugPreference.setInt("manual_team_override_value", teamNumber);
    }

    @Override
    public boolean isManualMatchOverrideEnabled() {
        return debugPreference.getBoolean("manual_match_override_toggle", false);
    }

    @Override
    public void setManualMatchOverride(boolean enabled) {
        debugPreference.setBoolean("manual_match_override_toggle", enabled);
    }

    @Override
    public int getManualMatchOverrideValue() {
        return debugPreference.getInt("manual_match_override_value", 0);
    }

    @Override
    public void setManualMatchOverrideValue(int matchNumber) {
        debugPreference.setInt("manual_match_override_value", matchNumber);
    }

    @Override
    public boolean isMaster() {
        return debugPreference.getBoolean("isMaster", false);
    }

    @Override
    public void setMaster(boolean isMaster) {
        debugPreference.setBoolean("isMaster", isMaster);
    }

    @Override
    public boolean isRedTeam() {
        return debugPreference.getBoolean("isRedteam", false);
    }

    @Override
    public void setRedTeam(boolean isRedTeam) {
        debugPreference.setBoolean("isRedteam", isRedTeam);
    }

    @Override
    public int getDebugMatch() {
        return debugPreference.getInt("debug_match", getCurrentMatch());
    }

    @Override
    public void setDebugMatch(int match) {
        debugPreference.setInt("debug_match", match);
    }

    @Override
    public int getMapBrushSize() {
        return debugPreference.getInt("map_brush_size", 15);
    }

    @Override
    public void setMapBrushSize(int size) {
        debugPreference.setInt("map_brush_size", size);
    }

    @Override
    public LiveData<List<String>> getScouterListLive() {
        LiveData<List<Scouter>> scoutersLive = scouterDao.getActiveScouters();
        return new MediatorLiveData<List<String>>() {{
            addSource(scoutersLive, scouters -> {
                if (scouters == null) {
                    setValue(new ArrayList<>());
                    return;
                }

                List<String> names = new ArrayList<>();
                for (Scouter scouter : scouters) {
                    names.add(scouter.name);
                }
                setValue(names);
            });
        }};
    }

    @Override
    public void setScouterList(List<String> scouters) {
        scouterDao.deleteAll();
        List<Scouter> entities = new ArrayList<>();
        for (String scouter : scouters) {
            Scouter entity = new Scouter();
            entity.name = scouter;
            entities.add(entity);
        }

        if (!entities.isEmpty()) {
            scouterDao.insertAll(entities);
        }
    }
    
    @Override
    public List<String> getScouterList() {
        return scouterDao.getScouterNames();
    }
}
