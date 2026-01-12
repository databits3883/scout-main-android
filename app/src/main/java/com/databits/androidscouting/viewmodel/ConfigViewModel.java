package com.databits.androidscouting.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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

    // Debug preferences
    private final MutableLiveData<Boolean> manualTeamOverride;
    private final MutableLiveData<Integer> manualTeamOverrideValue;
    private final MutableLiveData<Boolean> manualMatchOverride;
    private final MutableLiveData<Integer> manualMatchOverrideValue;
    private final MutableLiveData<Boolean> isMaster;
    private final MutableLiveData<Boolean> isRedTeam;
    private final MutableLiveData<Integer> mapBrushSize;
    private final MutableLiveData<List<String>> scouterList;
    private final MutableLiveData<String[][]> teamMatchData;
    private final MutableLiveData<Integer> teamMatchListSize;
    private final MutableLiveData<Set<Integer>> processedChunks;

    // Upload data
    private final MutableLiveData<ArrayList<HashMap<String, Object>>> uploadData;
    private final MutableLiveData<ArrayList<HashMap<String, Object>>> pitUploadData;
    private final MutableLiveData<ArrayList<HashMap<String, Object>>> specialUploadData;

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

        this.manualTeamOverride = new MutableLiveData<>();
        this.manualTeamOverrideValue = new MutableLiveData<>();
        this.manualMatchOverride = new MutableLiveData<>();
        this.manualMatchOverrideValue = new MutableLiveData<>();
        this.isMaster = new MutableLiveData<>();
        this.isRedTeam = new MutableLiveData<>();
        this.mapBrushSize = new MutableLiveData<>();
        this.scouterList = new MutableLiveData<>();
        this.teamMatchData = new MutableLiveData<>();
        this.teamMatchListSize = new MutableLiveData<>();
        this.processedChunks = new MutableLiveData<>();

        this.uploadData = new MutableLiveData<>();
        this.pitUploadData = new MutableLiveData<>();
        this.specialUploadData = new MutableLiveData<>();

        // Load initial values
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

            // Debug preferences
            manualTeamOverride.postValue(repository.isManualTeamOverrideEnabled());
            manualTeamOverrideValue.postValue(repository.getManualTeamOverrideValue());
            manualMatchOverride.postValue(repository.isManualMatchOverrideEnabled());
            manualMatchOverrideValue.postValue(repository.getManualMatchOverrideValue());
            isMaster.postValue(repository.isMaster());
            isRedTeam.postValue(repository.isRedTeam());
            mapBrushSize.postValue(repository.getMapBrushSize());
            scouterList.postValue(repository.getScouterList());
            teamMatchData.postValue(repository.getTeamMatchData());
            teamMatchListSize.postValue(repository.getTeamMatchListSize());
            processedChunks.postValue(repository.getProcessedChunks());

            // Upload data
            uploadData.postValue(repository.getUploadData());
            pitUploadData.postValue(repository.getPitUploadData());
            specialUploadData.postValue(repository.getSpecialUploadData());
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

    // ==================== Upload Data Getters ====================

    public LiveData<ArrayList<HashMap<String, Object>>> getUploadData() {
        return uploadData;
    }

    public LiveData<ArrayList<HashMap<String, Object>>> getPitUploadData() {
        return pitUploadData;
    }

    public LiveData<ArrayList<HashMap<String, Object>>> getSpecialUploadData() {
        return specialUploadData;
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

    public void updateScouterList(List<String> scouters) {
        executor.execute(() -> {
            repository.setScouterList(scouters);
            scouterList.postValue(scouters);
        });
    }

    public LiveData<String[][]> getTeamMatchData() {
        return teamMatchData;
    }

    public void updateTeamMatchData(String[][] data) {
        executor.execute(() -> {
            repository.setTeamMatchData(data);
            teamMatchData.postValue(data);
        });
    }

    public LiveData<Integer> getTeamMatchListSize() {
        return teamMatchListSize;
    }

    public void updateTeamMatchListSize(int size) {
        executor.execute(() -> {
            repository.setTeamMatchListSize(size);
            teamMatchListSize.postValue(size);
        });
    }

    public LiveData<Set<Integer>> getProcessedChunks() {
        return processedChunks;
    }

    public void updateProcessedChunks(Set<Integer> chunks) {
        executor.execute(() -> {
            repository.setProcessedChunks(chunks);
            processedChunks.postValue(chunks);
        });
    }

    // ==================== Upload Data Setters ====================

    public void updateUploadData(ArrayList<HashMap<String, Object>> data) {
        executor.execute(() -> {
            repository.setUploadData(data);
            uploadData.postValue(data);
        });
    }

    public void updatePitUploadData(ArrayList<HashMap<String, Object>> data) {
        executor.execute(() -> {
            repository.setPitUploadData(data);
            pitUploadData.postValue(data);
        });
    }

    public void updateSpecialUploadData(ArrayList<HashMap<String, Object>> data) {
        executor.execute(() -> {
            repository.setSpecialUploadData(data);
            specialUploadData.postValue(data);
        });
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
