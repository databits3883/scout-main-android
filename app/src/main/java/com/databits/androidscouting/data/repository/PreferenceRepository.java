package com.databits.androidscouting.data.repository;

import androidx.lifecycle.LiveData;
import com.databits.androidscouting.data.entity.UploadQueueItem;
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

    // ==================== Camera Preferences ====================

    /**
     * Check if camera torch (flashlight) is enabled
     * @return true if torch is enabled
     */
    boolean isCameraTorchEnabled();

    /**
     * Set camera torch (flashlight) status
     * @param enabled true to enable torch
     */
    void setCameraTorch(boolean enabled);

    /**
     * Check if camera haptic feedback is enabled
     * @return true if haptic feedback is enabled
     */
    boolean isCameraHapticFeedbackEnabled();

    /**
     * Set camera haptic feedback status
     * @param enabled true to enable haptic feedback
     */
    void setCameraHapticFeedback(boolean enabled);

    /**
     * Check if camera audio feedback is enabled
     * @return true if audio feedback is enabled
     */
    boolean isCameraAudioFeedbackEnabled();

    /**
     * Set camera audio feedback status
     * @param enabled true to enable audio feedback
     */
    void setCameraAudioFeedback(boolean enabled);

    /**
     * Check if scan reticle overlay is enabled
     * @return true if reticle is shown
     */
    boolean isCameraShowReticleEnabled();

    /**
     * Set scan reticle overlay status
     * @param enabled true to show reticle
     */
    void setCameraShowReticle(boolean enabled);

    /**
     * Check if auto brightness is enabled for camera
     * @return true if auto brightness is enabled
     */
    boolean isCameraAutoBrightnessEnabled();

    /**
     * Set auto brightness status for camera
     * @param enabled true to enable auto brightness
     */
    void setCameraAutoBrightness(boolean enabled);

    /**
     * Check if keep screen on is enabled for camera
     * @return true if keep screen on is enabled
     */
    boolean isCameraKeepScreenOnEnabled();

    /**
     * Set keep screen on status for camera
     * @param enabled true to enable keep screen on
     */
    void setCameraKeepScreenOn(boolean enabled);

    /**
     * Check if scan statistics display is enabled
     * @return true if statistics are shown
     */
    boolean isCameraShowStatsEnabled();

    /**
     * Set scan statistics display status
     * @param enabled true to show statistics
     */
    void setCameraShowStats(boolean enabled);

    /**
     * Check if scan success animation is enabled
     * @return true if success animation is shown
     */
    boolean isCameraShowSuccessAnimationEnabled();

    /**
     * Set scan success animation status
     * @param enabled true to show success animation
     */
    void setCameraShowSuccessAnimation(boolean enabled);

    /**
     * Get camera exposure compensation value
     * @return Exposure compensation (-2 to +2), defaults to 0
     */
    int getCameraExposureCompensation();

    /**
     * Set camera exposure compensation value
     * @param exposure Exposure compensation value (-2 to +2)
     */
    void setCameraExposureCompensation(int exposure);

    /**
     * Get camera zoom level
     * @return Zoom level (1.0 to 4.0), defaults to 1.0
     */
    float getCameraZoomLevel();

    /**
     * Set camera zoom level
     * @param zoom Zoom level to set
     */
    void setCameraZoomLevel(float zoom);

    /**
     * Get minimum barcode size for detection
     * @return Minimum size in pixels, defaults to 100
     */
    int getCameraMinBarcodeSize();

    /**
     * Set minimum barcode size for detection
     * @param size Minimum size in pixels
     */
    void setCameraMinBarcodeSize(int size);

    /**
     * Check if center-weighted scanning is enabled
     * @return true if center-weighted scanning is enabled
     */
    boolean isCameraCenterWeightedEnabled();

    /**
     * Set center-weighted scanning status
     * @param enabled true to enable center-weighted scanning
     */
    void setCameraCenterWeighted(boolean enabled);

    /**
     * Get center threshold distance for center-weighted scanning
     * @return Threshold distance in pixels, defaults to 300
     */
    int getCameraCenterThreshold();

    /**
     * Set center threshold distance for center-weighted scanning
     * @param threshold Threshold distance in pixels
     */
    void setCameraCenterThreshold(int threshold);

    /**
     * Check if FPS counter is shown (debug)
     * @return true if FPS counter is shown
     */
    boolean isCameraShowFpsEnabled();

    /**
     * Set FPS counter display status (debug)
     * @param enabled true to show FPS counter
     */
    void setCameraShowFps(boolean enabled);

    /**
     * Check if camera resolution display is shown (debug)
     * @return true if resolution is shown
     */
    boolean isCameraShowResolutionEnabled();

    /**
     * Set camera resolution display status (debug)
     * @param enabled true to show resolution
     */
    void setCameraShowResolution(boolean enabled);


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
     * Get the scouter list (Room-based)
     * @return List of scouter names
     */
    List<String> getScouterList();

    /**
     * Get scouter list as LiveData
     * @return LiveData list of scouter names
     */
    LiveData<List<String>> getScouterListLive();

    /**
     * Set the scouter list (Room-based)
     * @param scouters List of scouter names
     */
    void setScouterList(List<String> scouters);

    // ==================== List Preferences (Room-based Team Scheduling) ====================

    /**
     * Get team number for a specific match and position (from Room database)
     * @param matchNumber Match number
     * @param position Crowd position (1-6)
     * @return Team number or null if not found
     */
    String getTeamNumber(int matchNumber, int position);

    /**
     * Export all team schedule data as String[][] for QR code generation
     * Format: [matchNumber, team1, team2, team3, team4, team5, team6]
     * @return 2D array of team schedule data, or null if no data
     */
    String[][] exportTeamSchedule();

    /**
     * Import team schedule from CSV data
     * Format: [matchNumber, crowdPosition, teamNumber, alliance]
     * @param csvData 2D array of team schedule data
     */
    void importTeamSchedule(String[][] csvData);

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
     * Get the pit teams remaining list (Room-based)
     * @return List of remaining pit team numbers
     */
    List<String> getPitTeamsRemainingList();

    /**
     * Get pit teams remaining as LiveData
     * @return LiveData list of pit team numbers
     */
    LiveData<List<String>> getPitTeamsRemainingLive();

    /**
     * Set the pit teams remaining list (Room-based)
     * @param teams List of remaining pit team numbers
     */
    void setPitTeamsRemainingList(List<String> teams);

    /**
     * Remove a team from pit teams remaining
     * @param teamNumber Team number to remove
     */
    void removePitTeam(String teamNumber);

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
     * Check if a chunk has been processed (Room-based)
     * @param chunkId Chunk ID to check
     * @return true if chunk has been processed
     */
    boolean hasProcessedChunk(int chunkId);

    /**
     * Mark a chunk as processed (Room-based)
     * @param chunkId Chunk ID to mark
     */
    void markChunkProcessed(int chunkId);

    /**
     * Get all processed chunk IDs
     * @return List of processed chunk IDs
     */
    List<Integer> getAllProcessedChunks();

    /**
     * Clear all processed chunks
     */
    void clearProcessedChunks();

    /**
     * Check if a line has been seen (Room-based deduplication)
     * @param lineHash Hash of the line content
     * @param dataType Data type: "CROWD", "PIT", or "SPECIALTY"
     * @return true if line has been seen before
     */
    boolean hasSeenLine(String lineHash, String dataType);

    /**
     * Mark a line as seen (Room-based deduplication)
     * @param lineHash Hash of the line content
     * @param dataType Data type: "CROWD", "PIT", or "SPECIALTY"
     */
    void markLineSeen(String lineHash, String dataType);

    /**
     * Clear seen lines for a specific data type
     * @param dataType Data type: "CROWD", "PIT", or "SPECIALTY"
     */
    void clearSeenLines(String dataType);

    /**
     * Clear all seen lines
     */
    void clearAllSeenLines();

    // ==================== Match Preferences (Room-based) ====================

    /**
     * Get pending upload items from the queue (synchronous for background threads)
     * @return List of pending upload items
     */
    List<UploadQueueItem> getPendingUploads();

    /**
     * Get pending upload items as LiveData for UI observation
     * @return LiveData list of pending upload items
     */
    LiveData<List<UploadQueueItem>> getPendingUploadsLive();

    /**
     * Add an item to the upload queue
     * @param item Upload queue item to add
     */
    void addUploadItem(UploadQueueItem item);

    /**
     * Mark an upload as successful
     * @param id Upload item ID
     */
    void markUploadSuccess(long id);

    /**
     * Mark an upload as failed with error message
     * @param id Upload item ID
     * @param error Error message
     */
    void markUploadFailed(long id, String error);

    /**
     * Clear all successful uploads from the queue
     */
    void clearSuccessfulUploads();

    /**
     * Get count of pending uploads as LiveData
     * @return LiveData integer count
     */
    LiveData<Integer> getPendingUploadCount();

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
