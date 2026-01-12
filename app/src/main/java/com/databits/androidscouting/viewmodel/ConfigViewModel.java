package com.databits.androidscouting.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for managing configuration preferences with lifecycle-aware LiveData.
 * Survives configuration changes and handles background operations safely.
 */
public class ConfigViewModel extends ViewModel {
    private final PreferenceRepository repository;
    private final ExecutorService executor;

    // Config preferences
    private final MutableLiveData<Integer> currentMatch;
    private final MutableLiveData<String> currentScouter;
    private final MutableLiveData<String> deviceRole;
    private final MutableLiveData<String> workbookId;
    private final MutableLiveData<String> crowdRange;
    private final MutableLiveData<String> pitRange;
    private final MutableLiveData<String> specialtyRange;
    private final MutableLiveData<Boolean> roleLocked;
    private final MutableLiveData<Integer> crowdPosition;
    private final MutableLiveData<String> uploadMode;
    private final MutableLiveData<Boolean> gridToggle;
    private final MutableLiveData<Boolean> forceUpload;
    private final MutableLiveData<Boolean> autoLoadCrowdLayout;
    private final MutableLiveData<String> googleAccountName;
    private final MutableLiveData<Boolean> specialSwitch;

    // Camera preferences
    private final MutableLiveData<Boolean> cameraTorch;
    private final MutableLiveData<Boolean> cameraHapticFeedback;
    private final MutableLiveData<Boolean> cameraAudioFeedback;
    private final MutableLiveData<Boolean> cameraShowReticle;
    private final MutableLiveData<Boolean> cameraAutoBrightness;
    private final MutableLiveData<Boolean> cameraKeepScreenOn;
    private final MutableLiveData<Boolean> cameraShowStats;
    private final MutableLiveData<Boolean> cameraShowSuccessAnimation;
    private final MutableLiveData<Integer> cameraExposureCompensation;
    private final MutableLiveData<Float> cameraZoomLevel;
    private final MutableLiveData<Integer> cameraMinBarcodeSize;
    private final MutableLiveData<Boolean> cameraCenterWeighted;
    private final MutableLiveData<Integer> cameraCenterThreshold;
    private final MutableLiveData<Boolean> cameraShowFps;
    private final MutableLiveData<Boolean> cameraShowResolution;

    // Debug preferences
    private final MutableLiveData<Boolean> manualTeamOverride;
    private final MutableLiveData<Integer> manualTeamOverrideValue;
    private final MutableLiveData<Boolean> manualMatchOverride;
    private final MutableLiveData<Integer> manualMatchOverrideValue;
    private final MutableLiveData<Boolean> isMaster;
    private final MutableLiveData<Boolean> isRedTeam;
    private final MutableLiveData<Integer> mapBrushSize;

    // Room-based LiveData (directly from repository, not MutableLiveData)
    private final LiveData<List<String>> scouterList;
    private final LiveData<List<UploadQueueItem>> pendingUploads;
    private final LiveData<Integer> pendingUploadCount;
    private final LiveData<List<String>> pitTeamsRemaining;

    public ConfigViewModel(PreferenceRepository repository) {
        this.repository = repository;
        this.executor = Executors.newSingleThreadExecutor();

        // Initialize LiveData
        this.currentMatch = new MutableLiveData<>();
        this.currentScouter = new MutableLiveData<>();
        this.deviceRole = new MutableLiveData<>();
        this.workbookId = new MutableLiveData<>();
        this.crowdRange = new MutableLiveData<>();
        this.pitRange = new MutableLiveData<>();
        this.specialtyRange = new MutableLiveData<>();
        this.roleLocked = new MutableLiveData<>();
        this.crowdPosition = new MutableLiveData<>();
        this.uploadMode = new MutableLiveData<>();
        this.gridToggle = new MutableLiveData<>();
        this.forceUpload = new MutableLiveData<>();
        this.autoLoadCrowdLayout = new MutableLiveData<>();
        this.googleAccountName = new MutableLiveData<>();
        this.specialSwitch = new MutableLiveData<>();

        this.cameraTorch = new MutableLiveData<>();
        this.cameraHapticFeedback = new MutableLiveData<>();
        this.cameraAudioFeedback = new MutableLiveData<>();
        this.cameraShowReticle = new MutableLiveData<>();
        this.cameraAutoBrightness = new MutableLiveData<>();
        this.cameraKeepScreenOn = new MutableLiveData<>();
        this.cameraShowStats = new MutableLiveData<>();
        this.cameraShowSuccessAnimation = new MutableLiveData<>();
        this.cameraExposureCompensation = new MutableLiveData<>();
        this.cameraZoomLevel = new MutableLiveData<>();
        this.cameraMinBarcodeSize = new MutableLiveData<>();
        this.cameraCenterWeighted = new MutableLiveData<>();
        this.cameraCenterThreshold = new MutableLiveData<>();
        this.cameraShowFps = new MutableLiveData<>();
        this.cameraShowResolution = new MutableLiveData<>();

        this.manualTeamOverride = new MutableLiveData<>();
        this.manualTeamOverrideValue = new MutableLiveData<>();
        this.manualMatchOverride = new MutableLiveData<>();
        this.manualMatchOverrideValue = new MutableLiveData<>();
        this.isMaster = new MutableLiveData<>();
        this.isRedTeam = new MutableLiveData<>();
        this.mapBrushSize = new MutableLiveData<>();

        // Initialize Room-based LiveData directly from repository
        this.scouterList = repository.getScouterListLive();
        this.pendingUploads = repository.getPendingUploadsLive();
        this.pendingUploadCount = repository.getPendingUploadCount();
        this.pitTeamsRemaining = repository.getPitTeamsRemainingLive();

        // Load initial values for simple preferences
        loadPreferences();
    }

    /**
     * Load all preferences from repository on background thread
     */
    private void loadPreferences() {
        executor.execute(() -> {
            // Config preferences
            currentMatch.postValue(repository.getCurrentMatch());
            currentScouter.postValue(repository.getCurrentScouter());
            deviceRole.postValue(repository.getDeviceRole());
            workbookId.postValue(repository.getWorkbookId());
            crowdRange.postValue(repository.getCrowdRange());
            pitRange.postValue(repository.getPitRange());
            specialtyRange.postValue(repository.getSpecialtyRange());
            roleLocked.postValue(repository.isRoleLocked());
            crowdPosition.postValue(repository.getCrowdPosition());
            uploadMode.postValue(repository.getUploadMode());
            gridToggle.postValue(repository.isGridToggleEnabled());
            forceUpload.postValue(repository.isForceUploadEnabled());
            autoLoadCrowdLayout.postValue(repository.isAutoLoadCrowdLayoutEnabled());
            googleAccountName.postValue(repository.getGoogleAccountName());
            specialSwitch.postValue(repository.isSpecialSwitchEnabled());

            // Camera preferences
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

            // Debug preferences
            manualTeamOverride.postValue(repository.isManualTeamOverrideEnabled());
            manualTeamOverrideValue.postValue(repository.getManualTeamOverrideValue());
            manualMatchOverride.postValue(repository.isManualMatchOverrideEnabled());
            manualMatchOverrideValue.postValue(repository.getManualMatchOverrideValue());
            isMaster.postValue(repository.isMaster());
            isRedTeam.postValue(repository.isRedTeam());
            mapBrushSize.postValue(repository.getMapBrushSize());

            // Note: Room-based LiveData (scouterList, pendingUploads, etc.) are
            // automatically updated by Room and don't need to be loaded here
        });
    }

    // ==================== Config Preference Getters ====================

    public LiveData<Integer> getCurrentMatch() {
        return currentMatch;
    }

    public LiveData<String> getCurrentScouter() {
        return currentScouter;
    }

    public LiveData<String> getDeviceRole() {
        return deviceRole;
    }

    public LiveData<String> getWorkbookId() {
        return workbookId;
    }

    public LiveData<String> getCrowdRange() {
        return crowdRange;
    }

    public LiveData<String> getPitRange() {
        return pitRange;
    }

    public LiveData<String> getSpecialtyRange() {
        return specialtyRange;
    }

    public LiveData<Boolean> getRoleLocked() {
        return roleLocked;
    }

    public LiveData<Integer> getCrowdPosition() {
        return crowdPosition;
    }

    public LiveData<String> getUploadMode() {
        return uploadMode;
    }

    public LiveData<Boolean> getGridToggle() {
        return gridToggle;
    }

    public LiveData<Boolean> getForceUpload() {
        return forceUpload;
    }

    public LiveData<Boolean> getAutoLoadCrowdLayout() {
        return autoLoadCrowdLayout;
    }

    public LiveData<String> getGoogleAccountName() {
        return googleAccountName;
    }

    public LiveData<Boolean> getSpecialSwitch() {
        return specialSwitch;
    }

    // ==================== Camera Preference Getters ====================

    public LiveData<Boolean> getCameraTorch() {
        return cameraTorch;
    }

    public LiveData<Boolean> getCameraHapticFeedback() {
        return cameraHapticFeedback;
    }

    public LiveData<Boolean> getCameraAudioFeedback() {
        return cameraAudioFeedback;
    }

    public LiveData<Boolean> getCameraShowReticle() {
        return cameraShowReticle;
    }

    public LiveData<Boolean> getCameraAutoBrightness() {
        return cameraAutoBrightness;
    }

    public LiveData<Boolean> getCameraKeepScreenOn() {
        return cameraKeepScreenOn;
    }

    public LiveData<Boolean> getCameraShowStats() {
        return cameraShowStats;
    }

    public LiveData<Boolean> getCameraShowSuccessAnimation() {
        return cameraShowSuccessAnimation;
    }

    public LiveData<Integer> getCameraExposureCompensation() {
        return cameraExposureCompensation;
    }

    public LiveData<Float> getCameraZoomLevel() {
        return cameraZoomLevel;
    }

    public LiveData<Integer> getCameraMinBarcodeSize() {
        return cameraMinBarcodeSize;
    }

    public LiveData<Boolean> getCameraCenterWeighted() {
        return cameraCenterWeighted;
    }

    public LiveData<Integer> getCameraCenterThreshold() {
        return cameraCenterThreshold;
    }

    public LiveData<Boolean> getCameraShowFps() {
        return cameraShowFps;
    }

    public LiveData<Boolean> getCameraShowResolution() {
        return cameraShowResolution;
    }

    // ==================== Debug Preference Getters ====================

    public LiveData<Boolean> getManualTeamOverride() {
        return manualTeamOverride;
    }

    public LiveData<Integer> getManualTeamOverrideValue() {
        return manualTeamOverrideValue;
    }

    public LiveData<Boolean> getManualMatchOverride() {
        return manualMatchOverride;
    }

    public LiveData<Integer> getManualMatchOverrideValue() {
        return manualMatchOverrideValue;
    }

    public LiveData<Boolean> getIsMaster() {
        return isMaster;
    }

    public LiveData<Boolean> getIsRedTeam() {
        return isRedTeam;
    }

    public LiveData<Integer> getMapBrushSize() {
        return mapBrushSize;
    }

    public LiveData<List<String>> getScouterList() {
        return scouterList;
    }

    // ==================== Room-based Data Getters ====================

    /**
     * Get pending upload items (Room LiveData)
     */
    public LiveData<List<UploadQueueItem>> getPendingUploads() {
        return pendingUploads;
    }

    /**
     * Get count of pending uploads (Room LiveData)
     */
    public LiveData<Integer> getPendingUploadCount() {
        return pendingUploadCount;
    }

    /**
     * Get pit teams remaining (Room LiveData)
     */
    public LiveData<List<String>> getPitTeamsRemaining() {
        return pitTeamsRemaining;
    }

    // ==================== Config Preference Setters ====================

    public void updateCurrentMatch(int match) {
        executor.execute(() -> {
            repository.setCurrentMatch(match);
            currentMatch.postValue(match);
        });
    }

    public void updateCurrentScouter(String name) {
        executor.execute(() -> {
            repository.setCurrentScouter(name);
            currentScouter.postValue(name);
        });
    }

    public void updateDeviceRole(String role) {
        executor.execute(() -> {
            repository.setDeviceRole(role);
            deviceRole.postValue(role);
        });
    }

    public void updateWorkbookId(String id) {
        executor.execute(() -> {
            repository.setWorkbookId(id);
            workbookId.postValue(id);
        });
    }

    public void updateCrowdRange(String range) {
        executor.execute(() -> {
            repository.setCrowdRange(range);
            crowdRange.postValue(range);
        });
    }

    public void updatePitRange(String range) {
        executor.execute(() -> {
            repository.setPitRange(range);
            pitRange.postValue(range);
        });
    }

    public void updateSpecialtyRange(String range) {
        executor.execute(() -> {
            repository.setSpecialtyRange(range);
            specialtyRange.postValue(range);
        });
    }

    public void updateRoleLocked(boolean locked) {
        executor.execute(() -> {
            repository.setRoleLocked(locked);
            roleLocked.postValue(locked);
        });
    }

    public void updateCrowdPosition(int position) {
        executor.execute(() -> {
            repository.setCrowdPosition(position);
            crowdPosition.postValue(position);
        });
    }

    public void updateUploadMode(String mode) {
        executor.execute(() -> {
            repository.setUploadMode(mode);
            uploadMode.postValue(mode);
        });
    }

    public void updateGridToggle(boolean enabled) {
        executor.execute(() -> {
            repository.setGridToggle(enabled);
            gridToggle.postValue(enabled);
        });
    }

    public void updateForceUpload(boolean enabled) {
        executor.execute(() -> {
            repository.setForceUpload(enabled);
            forceUpload.postValue(enabled);
        });
    }

    public void updateAutoLoadCrowdLayout(boolean enabled) {
        executor.execute(() -> {
            repository.setAutoLoadCrowdLayout(enabled);
            autoLoadCrowdLayout.postValue(enabled);
        });
    }

    public void updateGoogleAccountName(String accountName) {
        executor.execute(() -> {
            repository.setGoogleAccountName(accountName);
            googleAccountName.postValue(accountName);
        });
    }

    public void updateSpecialSwitch(boolean enabled) {
        executor.execute(() -> {
            repository.setSpecialSwitch(enabled);
            specialSwitch.postValue(enabled);
        });
    }

    // ==================== Camera Preference Setters ====================

    public void updateCameraTorch(boolean enabled) {
        executor.execute(() -> {
            repository.setCameraTorch(enabled);
            cameraTorch.postValue(enabled);
        });
    }

    public void updateCameraHapticFeedback(boolean enabled) {
        executor.execute(() -> {
            repository.setCameraHapticFeedback(enabled);
            cameraHapticFeedback.postValue(enabled);
        });
    }

    public void updateCameraAudioFeedback(boolean enabled) {
        executor.execute(() -> {
            repository.setCameraAudioFeedback(enabled);
            cameraAudioFeedback.postValue(enabled);
        });
    }

    public void updateCameraShowReticle(boolean enabled) {
        executor.execute(() -> {
            repository.setCameraShowReticle(enabled);
            cameraShowReticle.postValue(enabled);
        });
    }

    public void updateCameraAutoBrightness(boolean enabled) {
        executor.execute(() -> {
            repository.setCameraAutoBrightness(enabled);
            cameraAutoBrightness.postValue(enabled);
        });
    }

    public void updateCameraKeepScreenOn(boolean enabled) {
        executor.execute(() -> {
            repository.setCameraKeepScreenOn(enabled);
            cameraKeepScreenOn.postValue(enabled);
        });
    }

    public void updateCameraShowStats(boolean enabled) {
        executor.execute(() -> {
            repository.setCameraShowStats(enabled);
            cameraShowStats.postValue(enabled);
        });
    }

    public void updateCameraShowSuccessAnimation(boolean enabled) {
        executor.execute(() -> {
            repository.setCameraShowSuccessAnimation(enabled);
            cameraShowSuccessAnimation.postValue(enabled);
        });
    }

    public void updateCameraExposureCompensation(int exposure) {
        executor.execute(() -> {
            repository.setCameraExposureCompensation(exposure);
            cameraExposureCompensation.postValue(exposure);
        });
    }

    public void updateCameraZoomLevel(float zoom) {
        executor.execute(() -> {
            repository.setCameraZoomLevel(zoom);
            cameraZoomLevel.postValue(zoom);
        });
    }

    public void updateCameraMinBarcodeSize(int size) {
        executor.execute(() -> {
            repository.setCameraMinBarcodeSize(size);
            cameraMinBarcodeSize.postValue(size);
        });
    }

    public void updateCameraCenterWeighted(boolean enabled) {
        executor.execute(() -> {
            repository.setCameraCenterWeighted(enabled);
            cameraCenterWeighted.postValue(enabled);
        });
    }

    public void updateCameraCenterThreshold(int threshold) {
        executor.execute(() -> {
            repository.setCameraCenterThreshold(threshold);
            cameraCenterThreshold.postValue(threshold);
        });
    }

    public void updateCameraShowFps(boolean enabled) {
        executor.execute(() -> {
            repository.setCameraShowFps(enabled);
            cameraShowFps.postValue(enabled);
        });
    }

    public void updateCameraShowResolution(boolean enabled) {
        executor.execute(() -> {
            repository.setCameraShowResolution(enabled);
            cameraShowResolution.postValue(enabled);
        });
    }

    // ==================== Debug Preference Setters ====================

    public void updateManualTeamOverride(boolean enabled) {
        executor.execute(() -> {
            repository.setManualTeamOverride(enabled);
            manualTeamOverride.postValue(enabled);
        });
    }

    public void updateManualTeamOverrideValue(int teamNumber) {
        executor.execute(() -> {
            repository.setManualTeamOverrideValue(teamNumber);
            manualTeamOverrideValue.postValue(teamNumber);
        });
    }

    public void updateManualMatchOverride(boolean enabled) {
        executor.execute(() -> {
            repository.setManualMatchOverride(enabled);
            manualMatchOverride.postValue(enabled);
        });
    }

    public void updateManualMatchOverrideValue(int matchNumber) {
        executor.execute(() -> {
            repository.setManualMatchOverrideValue(matchNumber);
            manualMatchOverrideValue.postValue(matchNumber);
        });
    }

    public void updateIsMaster(boolean master) {
        executor.execute(() -> {
            repository.setMaster(master);
            isMaster.postValue(master);
        });
    }

    public void updateIsRedTeam(boolean redTeam) {
        executor.execute(() -> {
            repository.setRedTeam(redTeam);
            isRedTeam.postValue(redTeam);
        });
    }

    public void updateMapBrushSize(int size) {
        executor.execute(() -> {
            repository.setMapBrushSize(size);
            mapBrushSize.postValue(size);
        });
    }

    // ==================== Room-based Data Operations ====================

    /**
     * Update scouter list (Room-based)
     */
    public void updateScouterList(List<String> scouters) {
        repository.setScouterList(scouters);
        // No need to post value - Room LiveData updates automatically
    }

    /**
     * Update special scout data
     */
    public void updateSpecialScoutData(ArrayList<String> data) {
        repository.setSpecialScoutData(data);
    }

    /**
     * Import team schedule from CSV data (Room-based)
     */
    public void importTeamSchedule(String[][] csvData) {
        repository.importTeamSchedule(csvData);
    }

    /**
     * Get team number for a specific match and position (synchronous, for background threads)
     */
    public String getTeamNumberSync(int matchNumber, int position) {
        return repository.getTeamNumber(matchNumber, position);
    }

    /**
     * Check if a chunk has been processed (synchronous, Room query)
     */
    public boolean hasProcessedChunkSync(int chunkId) {
        return repository.hasProcessedChunk(chunkId);
    }

    /**
     * Mark a chunk as processed (Room-based)
     */
    public void markChunkProcessed(int chunkId) {
        repository.markChunkProcessed(chunkId);
    }

    /**
     * Add an item to the upload queue (Room-based)
     */
    public void addUploadItem(UploadQueueItem item) {
        repository.addUploadItem(item);
    }

    /**
     * Mark an upload as successful (Room-based)
     */
    public void markUploadSuccess(long id) {
        repository.markUploadSuccess(id);
    }

    /**
     * Clear successful uploads from queue (Room-based)
     */
    public void clearSuccessfulUploads() {
        repository.clearSuccessfulUploads();
    }

    /**
     * Remove a team from pit teams remaining (Room-based)
     */
    public void removePitTeam(String teamNumber) {
        repository.removePitTeam(teamNumber);
    }

    /**
     * Refresh all preferences from repository.
     * Useful after external changes or screen resume.
     */
    public void refresh() {
        loadPreferences();
    }

    /**
     * Get synchronous value for current match (for cases where LiveData observation isn't suitable)
     */
    public int getCurrentMatchSync() {
        return repository.getCurrentMatch();
    }

    /**
     * Get synchronous value for device role
     */
    public String getDeviceRoleSync() {
        return repository.getDeviceRole();
    }

    /**
     * Get synchronous value for current scouter
     */
    public String getCurrentScouterSync() {
        return repository.getCurrentScouter();
    }

    /**
     * Get synchronous value for isMaster flag
     */
    public boolean getIsMasterSync() {
        return repository.isMaster();
    }

    /**
     * Get synchronous value for isRoleLocked
     */
    public boolean getRoleLockedSync() {
        return repository.isRoleLocked();
    }

    /**
     * Get synchronous value for crowd position
     */
    public int getCrowdPositionSync() {
        return repository.getCrowdPosition();
    }

    /**
     * Get synchronous value for auto load crowd layout
     */
    public boolean getAutoLoadCrowdLayoutSync() {
        return repository.isAutoLoadCrowdLayoutEnabled();
    }

    /**
     * Get synchronous value for match backup
     */
    public int getMatchBackupSync() {
        return repository.getMatchBackup();
    }

    /**
     * Update match backup value
     */
    public void updateMatchBackup(int match) {
        executor.execute(() -> {
            repository.setMatchBackup(match);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
