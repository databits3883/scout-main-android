package com.databits.androidscouting.data.repository;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import androidx.lifecycle.LiveData;

public interface ScheduleStore {
    int getTeamMatchListSize();
    void setTeamMatchListSize(int size);

    void importTeamSchedule(String[][] csvData);
    String[][] exportTeamSchedule();
    String getTeamNumber(int matchNumber, int position);

    boolean isPitRemoveEnabled();
    void setPitRemoveEnabled(boolean enabled);
    int getPitTeamListSize();
    void setPitTeamListSize(int size);
    LiveData<List<String>> getPitTeamsRemainingLive();
    List<String> getPitTeamsRemainingList();
    void setPitTeamsRemainingList(List<String> teams);
    boolean isSpecialRemoveEnabled();
    void setSpecialRemoveEnabled(boolean enabled);
    ArrayList<String> getSpecialScoutData();
    String getMatchData(int matchNumber);
    void setMatchData(int matchNumber, String data);
    String getPitMatchData(int matchNumber);
    void setPitMatchData(int matchNumber, String data);
    Map<String, ?> getAllMatchData();
    Map<String, ?> getAllPitData();

    String[][] getGoogleConfig();
    void setGoogleConfig(String[][] config);

    boolean hasProcessedChunk(int chunkId);
    void markChunkProcessed(int chunkId);
    List<Integer> getAllProcessedChunks();
    void clearProcessedChunks();
    boolean hasSeenLine(String lineHash, String dataType);
    void markLineSeen(String lineHash, String dataType);
    void clearSeenLines(String dataType);
    void clearAllSeenLines();
    void addUploadItem(com.databits.androidscouting.data.entity.UploadQueueItem item);
    void markUploadSuccess(long id);
    void markUploadFailed(long id, String error);
    void clearSuccessfulUploads();
    void removePitTeam(String teamNumber);
    void setSpecialScoutData(ArrayList<String> data);
}
