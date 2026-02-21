package com.databits.androidscouting.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ProvisionViewModel extends ViewModel {
    private final ProvisionSettingsStore repository;
    private final ExecutorService executor;

    private final MutableLiveData<String> currentScouter = new MutableLiveData<>();
    private final MutableLiveData<String> deviceRole = new MutableLiveData<>();
    private final MutableLiveData<String> workbookId = new MutableLiveData<>();
    private final MutableLiveData<String> crowdRange = new MutableLiveData<>();
    private final MutableLiveData<String> pitRange = new MutableLiveData<>();
    private final MutableLiveData<String> specialtyRange = new MutableLiveData<>();
    private final MutableLiveData<Boolean> roleLocked = new MutableLiveData<>();
    private final MutableLiveData<Integer> crowdPosition = new MutableLiveData<>();
    private final MutableLiveData<String> uploadMode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> gridToggle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> autoLoadCrowdLayout = new MutableLiveData<>();
    private final MutableLiveData<String> googleAccountName = new MutableLiveData<>();
    private final MutableLiveData<Boolean> specialSwitch = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isMaster = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRedTeam = new MutableLiveData<>();
    private final MutableLiveData<Integer> mapBrushSize = new MutableLiveData<>();

    private final LiveData<List<String>> scouterList;

    public ProvisionViewModel(ProvisionSettingsStore repository, ExecutorService executor) {
        this.repository = repository;
        this.executor = executor;
        this.scouterList = repository.getScouterListLive();
        refresh();
    }

    public void refresh() {
        executor.execute(() -> {
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
            autoLoadCrowdLayout.postValue(repository.isAutoLoadCrowdLayoutEnabled());
            googleAccountName.postValue(repository.getGoogleAccountName());
            specialSwitch.postValue(repository.isSpecialSwitchEnabled());
            isMaster.postValue(repository.isMaster());
            isRedTeam.postValue(repository.isRedTeam());
            mapBrushSize.postValue(repository.getMapBrushSize());
        });
    }

    public LiveData<String> getCurrentScouter() { return currentScouter; }
    public LiveData<String> getDeviceRole() { return deviceRole; }
    public LiveData<String> getWorkbookId() { return workbookId; }
    public LiveData<String> getCrowdRange() { return crowdRange; }
    public LiveData<String> getPitRange() { return pitRange; }
    public LiveData<String> getSpecialtyRange() { return specialtyRange; }
    public LiveData<Boolean> getRoleLocked() { return roleLocked; }
    public LiveData<Integer> getCrowdPosition() { return crowdPosition; }
    public LiveData<String> getUploadMode() { return uploadMode; }
    public LiveData<Boolean> getGridToggle() { return gridToggle; }
    public LiveData<Boolean> getAutoLoadCrowdLayout() { return autoLoadCrowdLayout; }
    public LiveData<String> getGoogleAccountName() { return googleAccountName; }
    public LiveData<Boolean> getSpecialSwitch() { return specialSwitch; }
    public LiveData<Boolean> getIsMaster() { return isMaster; }
    public LiveData<Boolean> getIsRedTeam() { return isRedTeam; }
    public LiveData<Integer> getMapBrushSize() { return mapBrushSize; }
    public LiveData<List<String>> getScouterList() { return scouterList; }

    public void updateCurrentScouter(String name) { executor.execute(() -> { repository.setCurrentScouter(name); currentScouter.postValue(name); }); }
    public void updateDeviceRole(String role) { executor.execute(() -> { repository.setDeviceRole(role); deviceRole.postValue(role); }); }
    public void updateWorkbookId(String id) { executor.execute(() -> { repository.setWorkbookId(id); workbookId.postValue(id); }); }
    public void updateCrowdRange(String range) { executor.execute(() -> { repository.setCrowdRange(range); crowdRange.postValue(range); }); }
    public void updatePitRange(String range) { executor.execute(() -> { repository.setPitRange(range); pitRange.postValue(range); }); }
    public void updateSpecialtyRange(String range) { executor.execute(() -> { repository.setSpecialtyRange(range); specialtyRange.postValue(range); }); }
    public void updateRoleLocked(boolean locked) { executor.execute(() -> { repository.setRoleLocked(locked); roleLocked.postValue(locked); }); }
    public void updateCrowdPosition(int position) { executor.execute(() -> { repository.setCrowdPosition(position); crowdPosition.postValue(position); }); }
    public void updateUploadMode(String mode) { executor.execute(() -> { repository.setUploadMode(mode); uploadMode.postValue(mode); }); }
    public void updateGridToggle(boolean enabled) { executor.execute(() -> { repository.setGridToggle(enabled); gridToggle.postValue(enabled); }); }
    public void updateAutoLoadCrowdLayout(boolean enabled) { executor.execute(() -> { repository.setAutoLoadCrowdLayout(enabled); autoLoadCrowdLayout.postValue(enabled); }); }
    public void updateGoogleAccountName(String accountName) { executor.execute(() -> { repository.setGoogleAccountName(accountName); googleAccountName.postValue(accountName); }); }
    public void updateSpecialSwitch(boolean enabled) { executor.execute(() -> { repository.setSpecialSwitch(enabled); specialSwitch.postValue(enabled); }); }
    public void updateIsMaster(boolean master) { executor.execute(() -> { repository.setMaster(master); isMaster.postValue(master); }); }
    public void updateIsRedTeam(boolean redTeam) { executor.execute(() -> { repository.setRedTeam(redTeam); isRedTeam.postValue(redTeam); }); }
    public void updateMapBrushSize(int size) { executor.execute(() -> { repository.setMapBrushSize(size); mapBrushSize.postValue(size); }); }

    public void updateScouterList(List<String> scouters) { repository.setScouterList(scouters); }
    public void updateSpecialScoutData(ArrayList<String> data) { repository.setSpecialScoutData(data); }
    public void updateManualTeamOverride(boolean enabled) { repository.setManualTeamOverride(enabled); }
    public void updateManualMatchOverride(boolean enabled) { repository.setManualMatchOverride(enabled); }
    public void updateMatchBackup(int match) { executor.execute(() -> repository.setMatchBackup(match)); }

    public int getMatchBackupSync() { return repository.getMatchBackup(); }
    public String getDeviceRoleSync() { return repository.getDeviceRole(); }
    public boolean getRoleLockedSync() { return repository.isRoleLocked(); }
    public int getCrowdPositionSync() { return repository.getCrowdPosition(); }
    public boolean getIsMasterSync() { return repository.isMaster(); }
    public boolean getAutoLoadCrowdLayoutSync() { return repository.isAutoLoadCrowdLayoutEnabled(); }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
