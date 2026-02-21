package com.databits.androidscouting.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.databits.androidscouting.data.database.ScoutDatabase;
import com.databits.androidscouting.data.dao.*;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import com.databits.androidscouting.data.repository.impl.PowerPreferenceSettingsRepository;
import com.databits.androidscouting.data.repository.impl.RoomScheduleRepository;
import com.databits.androidscouting.data.repository.impl.RoomUploadRepository;
import com.preference.PowerPreference;
import com.preference.Preference;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default store-backed repository implementation using PowerPreference and Room.
 */
public class DefaultPreferenceRepository implements CameraSettingsStore, ProvisionSettingsStore, SyncStore, ScheduleStore {

    // PowerPreference files for simple config/settings
    private final Preference configPreference;
    private final Preference debugPreference;
    private final Preference listPreference;
    private final Preference matchPreference;
    private final Preference pitDataPreference;

    private final ExecutorService executor;
    private final SettingsRepository settingsRepository;
    private final ScheduleRepository scheduleRepository;
    private final UploadRepository uploadRepository;

    DefaultPreferenceRepository(Context context) {
        this(context, ScoutDatabase.getInstance(context.getApplicationContext()), Executors.newSingleThreadExecutor());
    }

    /**
     * Constructor for testing that allows injecting a specific database instance.
     *
     * @param context Application context
     * @param db ScoutDatabase instance (can be in-memory for testing)
     */
    @androidx.annotation.VisibleForTesting
    protected DefaultPreferenceRepository(Context context, ScoutDatabase db) {
        this(context, db, Executors.newSingleThreadExecutor());
    }

    /**
     * Constructor for testing that allows injecting a specific database instance and executor.
     *
     * @param context Application context
     * @param db ScoutDatabase instance (can be in-memory for testing)
     * @param executor ExecutorService for background tasks
     */
    @androidx.annotation.VisibleForTesting
    protected DefaultPreferenceRepository(Context context, ScoutDatabase db, ExecutorService executor) {
        this.executor = executor;

        // Initialize PowerPreference files
        this.configPreference = PowerPreference.getFileByName("Config");
        this.debugPreference = PowerPreference.getFileByName("Debug");
        this.listPreference = PowerPreference.getFileByName("List");
        this.matchPreference = PowerPreference.getFileByName("Match");
        this.pitDataPreference = PowerPreference.getFileByName("PitData");

        // Initialize Room DAOs
        UploadQueueDao uploadQueueDao = db.uploadQueueDao();
        TeamMatchScheduleDao teamMatchScheduleDao = db.teamMatchScheduleDao();
        MatchDataDao matchDataDao = db.matchDataDao();
        SeenLineDao seenLineDao = db.seenLineDao();
        ProcessedChunkDao processedChunkDao = db.processedChunkDao();
        ScouterDao scouterDao = db.scouterDao();
        PitTeamRemainingDao pitTeamRemainingDao = db.pitTeamRemainingDao();

        this.settingsRepository = new PowerPreferenceSettingsRepository(scouterDao);
        this.scheduleRepository = new RoomScheduleRepository(
            teamMatchScheduleDao,
            matchDataDao,
            pitTeamRemainingDao,
            processedChunkDao,
            seenLineDao
        );
        this.uploadRepository = new RoomUploadRepository(uploadQueueDao);
    }

    // ==================== Initialization Methods ====================

    @Override
    public void setConfigDefaults(int xmlResourceId) {
        configPreference.setDefaults(xmlResourceId);
    }

    @Override
    public void setDebugDefaults(int xmlResourceId) {
        debugPreference.setDefaults(xmlResourceId);
    }

    // ==================== Config Preferences ====================

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

    // ==================== Camera Preferences ====================

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


    // ==================== Debug Preferences ====================

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
    public void setTeamMatchListSize(int size) {
        debugPreference.setInt("team_match_list_size", size);
    }

    @Override
    public int getPitTeamListSize() {
        return debugPreference.getInt("pit_team_list_size", 0);
    }

    @Override
    public void setPitTeamListSize(int size) {
        debugPreference.setInt("pit_team_list_size", size);
    }

    @Override
    public int getMapBrushSize() {
        return debugPreference.getInt("map_brush_size", 15);
    }

    @Override
    public void setMapBrushSize(int size) {
        debugPreference.setInt("map_brush_size", size);
    }


    // ==================== List Preferences ====================

    @Override
    public boolean isPitRemoveEnabled() {
        return listPreference.getBoolean("pit_remove_enabled", false);
    }

    @Override
    public void setPitRemoveEnabled(boolean enabled) {
        listPreference.setBoolean("pit_remove_enabled", enabled);
    }

    @Override
    public boolean isSpecialRemoveEnabled() {
        return listPreference.getBoolean("special_remove_enabled", false);
    }

    @Override
    public void setSpecialRemoveEnabled(boolean enabled) {
        listPreference.setBoolean("special_remove_enabled", enabled);
    }

    @Override
    public String[][] getGoogleConfig() {
        return listPreference.getObject("google_config", String[][].class);
    }

    @Override
    public void setGoogleConfig(String[][] config) {
        listPreference.setObject("google_config", config);
    }

    @Override
    public ArrayList<String> getSpecialScoutData() {
        return listPreference.getObject("special_scout", ArrayList.class);
    }

    @Override
    public void setSpecialScoutData(ArrayList<String> data) {
        listPreference.setObject("special_scout", data);
    }

    // ==================== Match Preferences ====================

    @Override
    public String getMatchData(int matchNumber) {
        return matchPreference.getString(String.format("Match%d", matchNumber), "No Data");
    }

    @Override
    public void setMatchData(int matchNumber, String data) {
        matchPreference.setString(String.format("Match%d", matchNumber), data);
    }

    @Override
    public String getPitMatchData(int matchNumber) {
        return pitDataPreference.getString(String.format("Match%d", matchNumber), "No Data");
    }

    @Override
    public void setPitMatchData(int matchNumber, String data) {
        pitDataPreference.setString(String.format("Match%d", matchNumber), data);
    }

    // ==================== Debug Operations ====================

    @Override
    public int getMatchBackup() {
        return debugPreference.getInt("match_backup", 1);
    }

    @Override
    public void setMatchBackup(int match) {
        debugPreference.setInt("match_backup", match);
    }

    @Override
    public void removeManualTeamOverrideValue() {
        debugPreference.remove("manual_team_override_value");
    }

    // ==================== Bulk Operations ====================

    @Override
    public Map<String, ?> getAllMatchData() {
        return matchPreference.getData();
    }

    @Override
    public Map<String, ?> getAllPitData() {
        return pitDataPreference.getData();
    }

    // ==================== Room-based Upload Queue Operations ====================

    @Override
    public List<UploadQueueItem> getPendingUploads() {
        return uploadRepository.getPendingUploads();
    }

    @Override
    public LiveData<List<UploadQueueItem>> getPendingUploadsLive() {
        return uploadRepository.getPendingUploadsLive();
    }

    @Override
    public void addUploadItem(UploadQueueItem item) {
        executor.execute(() -> uploadRepository.addUploadItem(item));
    }

    @Override
    public void markUploadSuccess(long id) {
        executor.execute(() -> uploadRepository.markUploadSuccess(id));
    }

    @Override
    public void markUploadFailed(long id, String error) {
        executor.execute(() -> uploadRepository.markUploadFailed(id, error));
    }

    @Override
    public void clearSuccessfulUploads() {
        executor.execute(uploadRepository::clearSuccessfulUploads);
    }

    @Override
    public LiveData<Integer> getPendingUploadCount() {
        return uploadRepository.getPendingUploadCount();
    }

    // ==================== Room-based Team Schedule Operations ====================

    @Override
    public String getTeamNumber(int matchNumber, int position) {
        return scheduleRepository.getTeamNumber(matchNumber, position);
    }

    @Override
    public String[][] exportTeamSchedule() {
        return scheduleRepository.exportTeamSchedule();
    }

    @Override
    public void importTeamSchedule(String[][] csvData) {
        executor.execute(() -> scheduleRepository.importTeamSchedule(csvData));
    }

    @Override
    public int getTeamMatchListSize() {
        return scheduleRepository.getTeamMatchListSize();
    }

    // ==================== Room-based Processed Chunks Operations ====================

    @Override
    public boolean hasProcessedChunk(int chunkId) {
        return scheduleRepository.hasProcessedChunk(chunkId);
    }

    @Override
    public void markChunkProcessed(int chunkId) {
        executor.execute(() -> scheduleRepository.markChunkProcessed(chunkId));
    }

    @Override
    public List<Integer> getAllProcessedChunks() {
        return scheduleRepository.getAllProcessedChunks();
    }

    @Override
    public void clearProcessedChunks() {
        executor.execute(scheduleRepository::clearProcessedChunks);
    }

    // ==================== Room-based Seen Lines Operations ====================

    @Override
    public boolean hasSeenLine(String lineHash, String dataType) {
        return scheduleRepository.hasSeenLine(lineHash, dataType);
    }

    @Override
    public void markLineSeen(String lineHash, String dataType) {
        executor.execute(() -> scheduleRepository.markLineSeen(lineHash, dataType));
    }

    @Override
    public void clearSeenLines(String dataType) {
        executor.execute(() -> scheduleRepository.clearSeenLines(dataType));
    }

    @Override
    public void clearAllSeenLines() {
        executor.execute(scheduleRepository::clearAllSeenLines);
    }

    // ==================== Room-based Scouter Operations ====================

    @Override
    public LiveData<List<String>> getScouterListLive() {
        return settingsRepository.getScouterListLive();
    }

    @Override
    public List<String> getScouterList() {
        return settingsRepository.getScouterList();
    }

    @Override
    public void setScouterList(List<String> scouters) {
        executor.execute(() -> settingsRepository.setScouterList(scouters));
    }

    // ==================== Room-based Pit Teams Remaining Operations ====================

    @Override
    public List<String> getPitTeamsRemainingList() {
        return scheduleRepository.getPitTeamsRemainingList();
    }

    @Override
    public LiveData<List<String>> getPitTeamsRemainingLive() {
        return scheduleRepository.getPitTeamsRemainingLive();
    }

    @Override
    public void setPitTeamsRemainingList(List<String> teams) {
        executor.execute(() -> scheduleRepository.setPitTeamsRemainingList(teams));
    }

    @Override
    public void removePitTeam(String teamNumber) {
        executor.execute(() -> scheduleRepository.removePitTeam(teamNumber));
    }
}
