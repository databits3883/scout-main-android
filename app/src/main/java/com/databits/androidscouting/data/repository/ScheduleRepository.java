package com.databits.androidscouting.data.repository;

import androidx.lifecycle.LiveData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ScheduleRepository {
    // Team Schedule
    String getTeamNumber(int matchNumber, int position);
    String[][] exportTeamSchedule();
    void importTeamSchedule(String[][] csvData);
    
    // Pit Teams
    List<String> getPitTeamsRemainingList();
    LiveData<List<String>> getPitTeamsRemainingLive();
    void setPitTeamsRemainingList(List<String> teams);
    void removePitTeam(String teamNumber);
    int getPitTeamListSize();
    void setPitTeamListSize(int size);
    boolean isPitRemoveEnabled();
    void setPitRemoveEnabled(boolean enabled);
    
    // Match Data
    String getMatchData(int matchNumber);
    void setMatchData(int matchNumber, String data);
    String getPitMatchData(int matchNumber);
    void setPitMatchData(int matchNumber, String data);
    Map<String, ?> getAllMatchData();
    Map<String, ?> getAllPitData();
    
    // Special Scout Data
    ArrayList<String> getSpecialScoutData();
    void setSpecialScoutData(ArrayList<String> data);
    boolean isSpecialRemoveEnabled();
    void setSpecialRemoveEnabled(boolean enabled);
    
    // Misc List Sizes
    int getTeamMatchListSize();
    void setTeamMatchListSize(int size);

    // Google Config
    String[][] getGoogleConfig();
    void setGoogleConfig(String[][] config);

    // Processed Chunks
    boolean hasProcessedChunk(int chunkId);
    void markChunkProcessed(int chunkId);
    List<Integer> getAllProcessedChunks();
    void clearProcessedChunks();
    
    // Seen Lines (Deduplication)
    boolean hasSeenLine(String lineHash, String dataType);
    void markLineSeen(String lineHash, String dataType);
    void clearSeenLines(String dataType);
    void clearAllSeenLines();

    // Debug Backup
    int getMatchBackup();
    void setMatchBackup(int match);
}
