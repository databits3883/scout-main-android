package com.databits.androidscouting.data.repository;

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

    private final Preference configPreference;
    private final Preference debugPreference;
    private final Preference listPreference;
    private final Preference matchPreference;
    private final Preference pitDataPreference;

    /**
     * Private constructor to enforce Singleton pattern.
     * Initializes all preference file references.
     */
    private PowerPreferenceRepository() {
        this.configPreference = PowerPreference.getFileByName("Config");
        this.debugPreference = PowerPreference.getFileByName("Debug");
        this.listPreference = PowerPreference.getFileByName("List");
        this.matchPreference = PowerPreference.getFileByName("Match");
        this.pitDataPreference = PowerPreference.getFileByName("PitData");
    }

    /**
     * Get the singleton instance of PowerPreferenceRepository.
     * Thread-safe lazy initialization.
     *
     * @return Singleton instance
     */
    public static synchronized PowerPreferenceRepository getInstance() {
        if (instance == null) {
            instance = new PowerPreferenceRepository();
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
    public int getTeamMatchListSize() {
        return debugPreference.getInt("team_match_list_size", 0);
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

    @Override
    public List<String> getScouterList() {
        return debugPreference.getObject("scouter_list", List.class);
    }

    @Override
    public void setScouterList(List<String> scouters) {
        debugPreference.setObject("scouter_list", scouters);
    }

    // ==================== List Preferences ====================

    @Override
    public String[][] getTeamMatchData() {
        return listPreference.getObject("team_match", String[][].class);
    }

    @Override
    public void setTeamMatchData(String[][] data) {
        listPreference.setObject("team_match", data);
    }

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
    public ArrayList<String> getPitTeamsRemainingList() {
        return listPreference.getObject("pit_teams_remaining_list", ArrayList.class);
    }

    @Override
    public void setPitTeamsRemainingList(ArrayList<String> teams) {
        listPreference.setObject("pit_teams_remaining_list", teams);
    }

    @Override
    public ArrayList<String> getSpecialScoutData() {
        return listPreference.getObject("special_scout", ArrayList.class);
    }

    @Override
    public void setSpecialScoutData(ArrayList<String> data) {
        listPreference.setObject("special_scout", data);
    }

    @Override
    public Set<Integer> getProcessedChunks() {
        return listPreference.getObject("processedChunks", Set.class, new HashSet<>());
    }

    @Override
    public void setProcessedChunks(Set<Integer> chunks) {
        listPreference.setObject("processedChunks", chunks);
    }

    @Override
    public Set<String> getSeenLines() {
        return listPreference.getObject("seen_lines", Set.class, new HashSet<>());
    }

    @Override
    public void setSeenLines(Set<String> lines) {
        listPreference.setObject("seen_lines", lines);
    }

    @Override
    public Set<String> getSpecialSeenLines() {
        return listPreference.getObject("special_seen_lines", Set.class, new HashSet<>());
    }

    @Override
    public void setSpecialSeenLines(Set<String> lines) {
        listPreference.setObject("special_seen_lines", lines);
    }

    @Override
    public Set<String> getPitSeenLines() {
        return listPreference.getObject("pit_seen_lines", Set.class, new HashSet<>());
    }

    @Override
    public void setPitSeenLines(Set<String> lines) {
        listPreference.setObject("pit_seen_lines", lines);
    }

    // ==================== Match Preferences ====================

    @Override
    public ArrayList<HashMap<String, Object>> getUploadData() {
        return new ArrayList<>(matchPreference.getObject("upload_data", ArrayList.class, new ArrayList<>()));
    }

    @Override
    public void setUploadData(ArrayList<HashMap<String, Object>> data) {
        matchPreference.setObject("upload_data", data);
    }

    @Override
    public ArrayList<HashMap<String, Object>> getPitUploadData() {
        return new ArrayList<>(matchPreference.getObject("pit_upload_data", ArrayList.class, new ArrayList<>()));
    }

    @Override
    public void setPitUploadData(ArrayList<HashMap<String, Object>> data) {
        matchPreference.setObject("pit_upload_data", data);
    }

    @Override
    public ArrayList<HashMap<String, Object>> getSpecialUploadData() {
        return new ArrayList<>(matchPreference.getObject("special_upload_data", ArrayList.class, new ArrayList<>()));
    }

    @Override
    public void setSpecialUploadData(ArrayList<HashMap<String, Object>> data) {
        matchPreference.setObject("special_upload_data", data);
    }

    @Override
    public void clearUploadData() {
        matchPreference.clear();
    }

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
}
