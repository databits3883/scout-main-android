package com.databits.androidscouting.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.databits.androidscouting.data.database.ScoutDatabase;
import com.databits.androidscouting.data.dao.*;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import com.preference.PowerPreference;
import com.preference.Preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of PreferenceRepository using PowerPreference library.
 *
 * This class provides a centralized, type-safe way to access all app preferences.
 * It uses the Singleton pattern to ensure a single instance across the app.
 *
 * Usage:
 * <pre>
 * PreferenceRepository repo = PowerPreferenceRepository.getInstance();
 * int match = repo.getCurrentMatch();
 * repo.setCurrentMatch(5);
 * </pre>
 */
public class PowerPreferenceRepository implements PreferenceRepository {

    private static PowerPreferenceRepository instance;

    // PowerPreference files for simple config/settings
    private final Preference configPreference;
    private final Preference debugPreference;
    private final Preference listPreference;
    private final Preference matchPreference;
    private final Preference pitDataPreference;

    // Room DAOs for structured data
    private final UploadQueueDao uploadQueueDao;
    private final TeamMatchScheduleDao teamMatchScheduleDao;
    private final MatchDataDao matchDataDao;
    private final SeenLineDao seenLineDao;
    private final ProcessedChunkDao processedChunkDao;
    private final ScouterDao scouterDao;
    private final PitTeamRemainingDao pitTeamRemainingDao;

    /**
     * Private constructor to enforce Singleton pattern.
     * Initializes all preference file references and Room DAOs.
     *
     * @param context Application context for Room database initialization
     */
    private PowerPreferenceRepository(Context context) {
        // Initialize PowerPreference files
        this.configPreference = PowerPreference.getFileByName("Config");
        this.debugPreference = PowerPreference.getFileByName("Debug");
        this.listPreference = PowerPreference.getFileByName("List");
        this.matchPreference = PowerPreference.getFileByName("Match");
        this.pitDataPreference = PowerPreference.getFileByName("PitData");

        // Initialize Room database and DAOs
        ScoutDatabase db = ScoutDatabase.getInstance(context.getApplicationContext());
        this.uploadQueueDao = db.uploadQueueDao();
        this.teamMatchScheduleDao = db.teamMatchScheduleDao();
        this.matchDataDao = db.matchDataDao();
        this.seenLineDao = db.seenLineDao();
        this.processedChunkDao = db.processedChunkDao();
        this.scouterDao = db.scouterDao();
        this.pitTeamRemainingDao = db.pitTeamRemainingDao();
    }

    /**
     * Get the singleton instance of PowerPreferenceRepository.
     * Thread-safe lazy initialization.
     *
     * @param context Application context for Room database initialization
     * @return Singleton instance
     */
    public static synchronized PowerPreferenceRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PowerPreferenceRepository(context);
        }
        return instance;
    }

    /**
     * Get existing instance without Context (use only after getInstance(Context) has been called)
     *
     * @return Singleton instance
     * @throws IllegalStateException if called before getInstance(Context)
     */
    public static synchronized PowerPreferenceRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PowerPreferenceRepository must be initialized with Context first");
        }
        return instance;
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
        // Try new key first, fall back to legacy key
        String range = configPreference.getString("Crowd_range", null);
        if (range == null) {
            range = configPreference.getString("crowd_range", null);
        }
        return range;
    }

    @Override
    public void setCrowdRange(String range) {
        configPreference.setString("Crowd_range", range);
        // Also set legacy key for compatibility
        configPreference.setString("crowd_range", range);
    }

    @Override
    public String getPitRange() {
        // Try new key first, fall back to legacy key
        String range = configPreference.getString("Pit_range", null);
        if (range == null) {
            range = configPreference.getString("pit_range", null);
        }
        return range;
    }

    @Override
    public void setPitRange(String range) {
        configPreference.setString("Pit_range", range);
        // Also set legacy key for compatibility
        configPreference.setString("pit_range", range);
    }

    @Override
    public String getSpecialtyRange() {
        // Try new key first, fall back to legacy key
        String range = configPreference.getString("Specialty_range", null);
        if (range == null) {
            range = configPreference.getString("specialty_range", null);
        }
        return range;
    }

    @Override
    public void setSpecialtyRange(String range) {
        configPreference.setString("Specialty_range", range);
        // Also set legacy key for compatibility
        configPreference.setString("specialty_range", range);
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
        return uploadQueueDao.getPendingUploadsSync();
    }

    @Override
    public LiveData<List<UploadQueueItem>> getPendingUploadsLive() {
        return uploadQueueDao.getPendingUploads();
    }

    @Override
    public void addUploadItem(UploadQueueItem item) {
        new Thread(() -> uploadQueueDao.insert(item)).start();
    }

    @Override
    public void markUploadSuccess(long id) {
        new Thread(() -> uploadQueueDao.markUploaded(id, "SUCCESS", System.currentTimeMillis())).start();
    }

    @Override
    public void markUploadFailed(long id, String error) {
        new Thread(() -> {
            uploadQueueDao.incrementRetryCount(id, error);
            uploadQueueDao.updateStatus(id, "FAILED");
        }).start();
    }

    @Override
    public void clearSuccessfulUploads() {
        new Thread(() -> uploadQueueDao.deleteSuccessfulUploads()).start();
    }

    @Override
    public LiveData<Integer> getPendingUploadCount() {
        return uploadQueueDao.getPendingCount();
    }

    // ==================== Room-based Team Schedule Operations ====================

    @Override
    public String getTeamNumber(int matchNumber, int position) {
        String teamNumber = teamMatchScheduleDao.getTeamNumber(matchNumber, position);
        android.util.Log.d("PowerPreferenceRepository",
            "getTeamNumber(match=" + matchNumber + ", pos=" + position + ") returned: " + teamNumber);
        return teamNumber;
    }

    @Override
    public String[][] exportTeamSchedule() {
        // Get all team schedule data from Room
        List<com.databits.androidscouting.data.entity.TeamMatchSchedule> scheduleList =
            teamMatchScheduleDao.getAllScheduleSync();

        if (scheduleList == null || scheduleList.isEmpty()) {
            return null;
        }

        // Group by match number and sort
        java.util.Map<Integer, String[]> matchMap = new java.util.HashMap<>();

        for (com.databits.androidscouting.data.entity.TeamMatchSchedule item : scheduleList) {
            if (!matchMap.containsKey(item.matchNumber)) {
                // Initialize array: [matchNumber, team1, team2, team3, team4, team5, team6]
                matchMap.put(item.matchNumber, new String[7]);
                matchMap.get(item.matchNumber)[0] = String.valueOf(item.matchNumber);
            }
            // Position is 1-6, array index is 1-6
            matchMap.get(item.matchNumber)[item.crowdPosition] = item.teamNumber;
        }

        // Convert map to sorted array
        java.util.List<Integer> matchNumbers = new java.util.ArrayList<>(matchMap.keySet());
        java.util.Collections.sort(matchNumbers);

        String[][] result = new String[matchNumbers.size()][];
        for (int i = 0; i < matchNumbers.size(); i++) {
            result[i] = matchMap.get(matchNumbers.get(i));
        }

        return result;
    }

    @Override
    public void importTeamSchedule(String[][] csvData) {
        new Thread(() -> {
            List<com.databits.androidscouting.data.entity.TeamMatchSchedule> schedules = new ArrayList<>();
            android.util.Log.d("PowerPreferenceRepository", "Importing " + csvData.length + " rows of match data");

            for (String[] row : csvData) {
                // CSV format: Match, Blue1, Blue2, Blue3, Red1, Red2, Red3
                if (row.length >= 7) {
                    try {
                        int matchNumber = Integer.parseInt(row[0]);

                        // Create 6 schedule entries (one for each position)
                        String[] alliances = {"BLUE", "BLUE", "BLUE", "RED", "RED", "RED"};

                        for (int i = 0; i < 6; i++) {
                            com.databits.androidscouting.data.entity.TeamMatchSchedule schedule =
                                new com.databits.androidscouting.data.entity.TeamMatchSchedule();
                            schedule.matchNumber = matchNumber;
                            schedule.crowdPosition = i + 1; // Positions 1-6
                            schedule.teamNumber = row[i + 1]; // Columns 1-6 contain team numbers
                            schedule.alliance = alliances[i];
                            schedules.add(schedule);

                            android.util.Log.d("PowerPreferenceRepository",
                                "Importing: Match=" + schedule.matchNumber +
                                ", Position=" + schedule.crowdPosition +
                                ", Team=" + schedule.teamNumber +
                                ", Alliance=" + schedule.alliance);
                        }
                    } catch (NumberFormatException e) {
                        // Skip header row or invalid data
                        android.util.Log.w("PowerPreferenceRepository", "Skipping invalid row: " + java.util.Arrays.toString(row));
                        continue;
                    }
                } else {
                    android.util.Log.w("PowerPreferenceRepository", "Skipping row with insufficient columns (need 7, got " + row.length + "): " + java.util.Arrays.toString(row));
                }
            }

            if (!schedules.isEmpty()) {
                teamMatchScheduleDao.insertAll(schedules);
                android.util.Log.d("PowerPreferenceRepository", "Successfully inserted " + schedules.size() + " team schedule entries");
            } else {
                android.util.Log.w("PowerPreferenceRepository", "No valid schedule entries to insert");
            }
        }).start();
    }

    @Override
    public int getTeamMatchListSize() {
        return teamMatchScheduleDao.getMatchCount();
    }

    // ==================== Room-based Processed Chunks Operations ====================

    @Override
    public boolean hasProcessedChunk(int chunkId) {
        return processedChunkDao.hasProcessedChunk(chunkId);
    }

    @Override
    public void markChunkProcessed(int chunkId) {
        new Thread(() -> {
            com.databits.androidscouting.data.entity.ProcessedChunk chunk =
                new com.databits.androidscouting.data.entity.ProcessedChunk();
            chunk.chunkId = chunkId;
            processedChunkDao.insert(chunk);
        }).start();
    }

    @Override
    public List<Integer> getAllProcessedChunks() {
        return processedChunkDao.getAllChunkIds();
    }

    @Override
    public void clearProcessedChunks() {
        new Thread(() -> processedChunkDao.deleteAll()).start();
    }

    // ==================== Room-based Seen Lines Operations ====================

    @Override
    public boolean hasSeenLine(String lineHash, String dataType) {
        return seenLineDao.hasSeenLine(lineHash, dataType);
    }

    @Override
    public void markLineSeen(String lineHash, String dataType) {
        new Thread(() -> {
            com.databits.androidscouting.data.entity.SeenLine line =
                new com.databits.androidscouting.data.entity.SeenLine();
            line.lineHash = lineHash;
            line.dataType = dataType;
            seenLineDao.insert(line);
        }).start();
    }

    @Override
    public void clearSeenLines(String dataType) {
        new Thread(() -> seenLineDao.clearType(dataType)).start();
    }

    @Override
    public void clearAllSeenLines() {
        new Thread(() -> seenLineDao.deleteAll()).start();
    }

    // ==================== Room-based Scouter Operations ====================

    @Override
    public LiveData<List<String>> getScouterListLive() {
        LiveData<List<com.databits.androidscouting.data.entity.Scouter>> scoutersLive = scouterDao.getActiveScouters();
        // Transform LiveData<List<Scouter>> to LiveData<List<String>>
        return new androidx.lifecycle.MediatorLiveData<List<String>>() {{
            addSource(scoutersLive, scouters -> {
                if (scouters != null) {
                    List<String> names = new ArrayList<>();
                    for (com.databits.androidscouting.data.entity.Scouter s : scouters) {
                        names.add(s.name);
                    }
                    setValue(names);
                }
            });
        }};
    }

    @Override
    public List<String> getScouterList() {
        return scouterDao.getScouterNames();
    }

    @Override
    public void setScouterList(List<String> scouters) {
        new Thread(() -> {
            List<com.databits.androidscouting.data.entity.Scouter> entities = new ArrayList<>();
            for (String name : scouters) {
                com.databits.androidscouting.data.entity.Scouter s =
                    new com.databits.androidscouting.data.entity.Scouter();
                s.name = name;
                entities.add(s);
            }
            scouterDao.deleteAll();
            scouterDao.insertAll(entities);
        }).start();
    }

    // ==================== Room-based Pit Teams Remaining Operations ====================

    @Override
    public List<String> getPitTeamsRemainingList() {
        return pitTeamRemainingDao.getAllTeamNumbers();
    }

    @Override
    public LiveData<List<String>> getPitTeamsRemainingLive() {
        return pitTeamRemainingDao.getAllTeamNumbersLive();
    }

    @Override
    public void setPitTeamsRemainingList(List<String> teams) {
        new Thread(() -> {
            List<com.databits.androidscouting.data.entity.PitTeamRemaining> entities = new ArrayList<>();
            for (String teamNumber : teams) {
                com.databits.androidscouting.data.entity.PitTeamRemaining team =
                    new com.databits.androidscouting.data.entity.PitTeamRemaining();
                team.teamNumber = teamNumber;
                entities.add(team);
            }
            pitTeamRemainingDao.deleteAll();
            pitTeamRemainingDao.insertAll(entities);
        }).start();
    }

    @Override
    public void removePitTeam(String teamNumber) {
        new Thread(() -> pitTeamRemainingDao.removeTeam(teamNumber)).start();
    }
}
