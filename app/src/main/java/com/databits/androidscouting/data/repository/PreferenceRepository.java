package com.databits.androidscouting.data.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Repository interface for accessing application preferences.
 *
 * This interface abstracts preference access to provide:
 * - Single source of truth for preference keys
 * - Type-safe preference access
 * - Easy testing with mock implementations
 * - Ability to switch preference implementations without changing UI code
 */
public interface PreferenceRepository {

    // ==================== Initialization Methods ====================

    /**
     * Set default values for Config preferences from XML resource
     * @param xmlResourceId XML resource ID containing default values
     */
    void setConfigDefaults(int xmlResourceId);

    /**
     * Set default values for Debug preferences from XML resource
     * @param xmlResourceId XML resource ID containing default values
     */
    void setDebugDefaults(int xmlResourceId);

    // ==================== Config Preferences ====================

    /**
     * Get the current match number
     * @return Current match number, defaults to 1
     */
    int getCurrentMatch();

    /**
     * Set the current match number
     * @param match Match number to set
     */
    void setCurrentMatch(int match);

    /**
     * Get the current scouter name
     * @return Current scouter name, defaults to "Scouter"
     */
    String getCurrentScouter();

    /**
     * Set the current scouter name
     * @param name Scouter name to set
     */
    void setCurrentScouter(String name);

    /**
     * Get the device role (e.g., "crowd1", "pit", etc.)
     * @return Device role or null if not set
     */
    String getDeviceRole();

    /**
     * Set the device role
     * @param role Device role to set
     */
    void setDeviceRole(String role);

    /**
     * Get the Google Sheets workbook ID
     * @return Workbook ID or null if not set
     */
    String getWorkbookId();

    /**
     * Set the Google Sheets workbook ID
     * @param id Workbook ID to set
     */
    void setWorkbookId(String id);

    /**
     * Get the crowd data range (e.g., "Sheet1!A1:Z100")
     * @return Crowd range or null if not set
     */
    String getCrowdRange();

    /**
     * Set the crowd data range
     * @param range Crowd range to set
     */
    void setCrowdRange(String range);

    /**
     * Get the pit data range
     * @return Pit range or null if not set
     */
    String getPitRange();

    /**
     * Set the pit data range
     * @param range Pit range to set
     */
    void setPitRange(String range);

    /**
     * Get the specialty data range
     * @return Specialty range or null if not set
     */
    String getSpecialtyRange();

    /**
     * Set the specialty data range
     * @param range Specialty range to set
     */
    void setSpecialtyRange(String range);

    /**
     * Check if device role is locked
     * @return true if role is locked
     */
    boolean isRoleLocked();

    /**
     * Set device role lock status
     * @param locked true to lock role
     */
    void setRoleLocked(boolean locked);

    /**
     * Get the crowd position (0-based index)
     * @return Crowd position, defaults to 0
     */
    int getCrowdPosition();

    /**
     * Set the crowd position
     * @param position Position to set
     */
    void setCrowdPosition(int position);

    /**
     * Get the upload mode ("Crowd", "Pit", or "Specialty")
     * @return Upload mode or null if not set
     */
    String getUploadMode();

    /**
     * Set the upload mode
     * @param mode Upload mode to set
     */
    void setUploadMode(String mode);

    /**
     * Check if grid toggle is enabled
     * @return true if grid is enabled
     */
    boolean isGridToggleEnabled();

    /**
     * Set grid toggle status
     * @param enabled true to enable grid
     */
    void setGridToggle(boolean enabled);

    /**
     * Check if force upload is enabled
     * @return true if force upload is enabled
     */
    boolean isForceUploadEnabled();

    /**
     * Set force upload status
     * @param enabled true to enable force upload
     */
    void setForceUpload(boolean enabled);

    /**
     * Check if auto-load crowd layout is enabled
     * @return true if auto-load is enabled
     */
    boolean isAutoLoadCrowdLayoutEnabled();

    /**
     * Set auto-load crowd layout status
     * @param enabled true to enable auto-load
     */
    void setAutoLoadCrowdLayout(boolean enabled);

    /**
     * Get the Google account name for Sheets access
     * @return Account name or null if not set
     */
    String getGoogleAccountName();

    /**
     * Set the Google account name for Sheets access
     * @param accountName Account name to set
     */
    void setGoogleAccountName(String accountName);

    /**
     * Check if special switch is enabled
     * @return true if special switch is enabled
     */
    boolean isSpecialSwitchEnabled();

    /**
     * Set special switch status
     * @param enabled true to enable special switch
     */
    void setSpecialSwitch(boolean enabled);

    // ==================== Debug Preferences ====================

    /**
     * Check if manual team override is enabled
     * @return true if manual override is enabled
     */
    boolean isManualTeamOverrideEnabled();

    /**
     * Set manual team override status
     * @param enabled true to enable manual override
     */
    void setManualTeamOverride(boolean enabled);

    /**
     * Get the manual team override value
     * @return Override team number
     */
    int getManualTeamOverrideValue();

    /**
     * Set the manual team override value
     * @param teamNumber Team number to override with
     */
    void setManualTeamOverrideValue(int teamNumber);

    /**
     * Check if manual match override is enabled
     * @return true if manual match override is enabled
     */
    boolean isManualMatchOverrideEnabled();

    /**
     * Set manual match override status
     * @param enabled true to enable manual override
     */
    void setManualMatchOverride(boolean enabled);

    /**
     * Get the manual match override value
     * @return Override match number
     */
    int getManualMatchOverrideValue();

    /**
     * Set the manual match override value
     * @param matchNumber Match number to override with
     */
    void setManualMatchOverrideValue(int matchNumber);

    /**
     * Check if device is master
     * @return true if device is master
     */
    boolean isMaster();

    /**
     * Set master device status
     * @param isMaster true to set as master
     */
    void setMaster(boolean isMaster);

    /**
     * Check if device is on red team
     * @return true if on red team
     */
    boolean isRedTeam();

    /**
     * Set red team status
     * @param isRedTeam true if on red team
     */
    void setRedTeam(boolean isRedTeam);

    /**
     * Get the debug match number
     * @return Debug match number
     */
    int getDebugMatch();

    /**
     * Set the debug match number
     * @param match Debug match number to set
     */
    void setDebugMatch(int match);

    /**
     * Get the team match list size
     * @return Number of team matches, defaults to 0
     */
    int getTeamMatchListSize();

    /**
     * Set the team match list size
     * @param size Number of team matches
     */
    void setTeamMatchListSize(int size);

    /**
     * Get the pit team list size
     * @return Number of pit teams
     */
    int getPitTeamListSize();

    /**
     * Set the pit team list size
     * @param size Number of pit teams
     */
    void setPitTeamListSize(int size);

    /**
     * Get the map brush size
     * @return Brush size, defaults to 15
     */
    int getMapBrushSize();

    /**
     * Set the map brush size
     * @param size Brush size to set
     */
    void setMapBrushSize(int size);

    /**
     * Get the scouter list
     * @return List of scouter names
     */
    List<String> getScouterList();

    /**
     * Set the scouter list
     * @param scouters List of scouter names
     */
    void setScouterList(List<String> scouters);

    // ==================== List Preferences ====================

    /**
     * Get the team match data as 2D array
     * @return Team match data or null if not set
     */
    String[][] getTeamMatchData();

    /**
     * Set the team match data
     * @param data Team match data as 2D array
     */
    void setTeamMatchData(String[][] data);

    /**
     * Check if pit remove is enabled
     * @return true if pit remove is enabled
     */
    boolean isPitRemoveEnabled();

    /**
     * Set pit remove enabled status
     * @param enabled true to enable pit remove
     */
    void setPitRemoveEnabled(boolean enabled);

    /**
     * Check if special remove is enabled
     * @return true if special remove is enabled
     */
    boolean isSpecialRemoveEnabled();

    /**
     * Set special remove enabled status
     * @param enabled true to enable special remove
     */
    void setSpecialRemoveEnabled(boolean enabled);

    /**
     * Get the Google config data
     * @return Google config as 2D array or null if not set
     */
    String[][] getGoogleConfig();

    /**
     * Set the Google config data
     * @param config Google config as 2D array
     */
    void setGoogleConfig(String[][] config);

    /**
     * Get the pit teams remaining list
     * @return ArrayList of remaining pit teams
     */
    ArrayList<String> getPitTeamsRemainingList();

    /**
     * Set the pit teams remaining list
     * @param teams ArrayList of remaining pit teams
     */
    void setPitTeamsRemainingList(ArrayList<String> teams);

    /**
     * Get the special scout data list
     * @return ArrayList of special scout data
     */
    ArrayList<String> getSpecialScoutData();

    /**
     * Set the special scout data list
     * @param data ArrayList of special scout data
     */
    void setSpecialScoutData(ArrayList<String> data);

    /**
     * Get the set of processed chunks
     * @return Set of processed chunk indices
     */
    Set<Integer> getProcessedChunks();

    /**
     * Set the set of processed chunks
     * @param chunks Set of processed chunk indices
     */
    void setProcessedChunks(Set<Integer> chunks);

    /**
     * Get the set of seen lines for crowd upload
     * @return Set of seen line strings
     */
    Set<String> getSeenLines();

    /**
     * Set the set of seen lines for crowd upload
     * @param lines Set of seen line strings
     */
    void setSeenLines(Set<String> lines);

    /**
     * Get the set of seen lines for special upload
     * @return Set of special seen line strings
     */
    Set<String> getSpecialSeenLines();

    /**
     * Set the set of seen lines for special upload
     * @param lines Set of special seen line strings
     */
    void setSpecialSeenLines(Set<String> lines);

    /**
     * Get the set of seen lines for pit upload
     * @return Set of pit seen line strings
     */
    Set<String> getPitSeenLines();

    /**
     * Set the set of seen lines for pit upload
     * @param lines Set of pit seen line strings
     */
    void setPitSeenLines(Set<String> lines);

    // ==================== Match Preferences ====================

    /**
     * Get the upload data queue
     * @return List of data to upload
     */
    ArrayList<HashMap<String, Object>> getUploadData();

    /**
     * Set the upload data queue
     * @param data List of data to upload
     */
    void setUploadData(ArrayList<HashMap<String, Object>> data);

    /**
     * Get the pit upload data queue
     * @return List of pit data to upload
     */
    ArrayList<HashMap<String, Object>> getPitUploadData();

    /**
     * Set the pit upload data queue
     * @param data List of pit data to upload
     */
    void setPitUploadData(ArrayList<HashMap<String, Object>> data);

    /**
     * Get the special upload data queue
     * @return List of special data to upload
     */
    ArrayList<HashMap<String, Object>> getSpecialUploadData();

    /**
     * Set the special upload data queue
     * @param data List of special data to upload
     */
    void setSpecialUploadData(ArrayList<HashMap<String, Object>> data);

    /**
     * Clear all upload data (match preferences)
     */
    void clearUploadData();

    /**
     * Get match data for a specific match number
     * @param matchNumber Match number to retrieve data for
     * @return Match data string or "No Data" if not found
     */
    String getMatchData(int matchNumber);

    /**
     * Set match data for a specific match number
     * @param matchNumber Match number to set data for
     * @param data Match data string to save
     */
    void setMatchData(int matchNumber, String data);

    /**
     * Get pit match data for a specific match number
     * @param matchNumber Match number to retrieve pit data for
     * @return Pit match data string or "No Data" if not found
     */
    String getPitMatchData(int matchNumber);

    /**
     * Set pit match data for a specific match number
     * @param matchNumber Match number to set pit data for
     * @param data Pit match data string to save
     */
    void setPitMatchData(int matchNumber, String data);

    // ==================== Debug Operations ====================

    /**
     * Get the match backup value (debug)
     * @return Backup match number
     */
    int getMatchBackup();

    /**
     * Set the match backup value (debug)
     * @param match Backup match number
     */
    void setMatchBackup(int match);

    /**
     * Remove manual team override value
     */
    void removeManualTeamOverrideValue();

    // ==================== Bulk Operations ====================

    /**
     * Get all match data
     * @return Map of all match preference data
     */
    Map<String, ?> getAllMatchData();

    /**
     * Get all pit data
     * @return Map of all pit data preference data
     */
    Map<String, ?> getAllPitData();
}
