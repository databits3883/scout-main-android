package com.databits.androidscouting.data.repository;

import androidx.lifecycle.LiveData;
import java.util.ArrayList;
import java.util.List;

public interface ProvisionSettingsStore {
    void setConfigDefaults(int xmlResourceId);
    void setDebugDefaults(int xmlResourceId);

    int getCurrentMatch();
    void setCurrentMatch(int match);
    String getCurrentScouter();
    void setCurrentScouter(String name);
    String getDeviceRole();
    void setDeviceRole(String role);
    String getWorkbookId();
    void setWorkbookId(String id);
    String getCrowdRange();
    void setCrowdRange(String range);
    String getPitRange();
    void setPitRange(String range);
    String getSpecialtyRange();
    void setSpecialtyRange(String range);
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
    boolean isManualTeamOverrideEnabled();
    int getManualTeamOverrideValue();
    void setManualTeamOverrideValue(int teamNumber);
    boolean isManualMatchOverrideEnabled();
    int getManualMatchOverrideValue();
    void setManualMatchOverrideValue(int matchNumber);
    void removeManualTeamOverrideValue();

    String getGoogleAccountName();
    void setGoogleAccountName(String accountName);
    String getThemeMode();
    void setThemeMode(String mode);
    boolean isSpecialSwitchEnabled();
    void setSpecialSwitch(boolean enabled);
    boolean isMaster();
    void setMaster(boolean isMaster);
    boolean isRedTeam();
    void setRedTeam(boolean isRedTeam);
    int getMapBrushSize();
    void setMapBrushSize(int size);
    int getDebugMatch();
    void setDebugMatch(int match);
    int getMatchBackup();
    void setMatchBackup(int match);

    LiveData<List<String>> getScouterListLive();
    void setScouterList(List<String> scouters);
    void setSpecialScoutData(ArrayList<String> data);
    void setManualTeamOverride(boolean enabled);
    void setManualMatchOverride(boolean enabled);
}
