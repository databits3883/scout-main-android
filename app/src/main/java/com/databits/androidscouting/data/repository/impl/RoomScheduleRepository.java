package com.databits.androidscouting.data.repository.impl;

import androidx.lifecycle.LiveData;
import com.databits.androidscouting.data.dao.MatchDataDao;
import com.databits.androidscouting.data.dao.PitTeamRemainingDao;
import com.databits.androidscouting.data.dao.ProcessedChunkDao;
import com.databits.androidscouting.data.dao.SeenLineDao;
import com.databits.androidscouting.data.dao.TeamMatchScheduleDao;
import com.databits.androidscouting.data.entity.PitTeamRemaining;
import com.databits.androidscouting.data.entity.ProcessedChunk;
import com.databits.androidscouting.data.entity.SeenLine;
import com.databits.androidscouting.data.repository.ScheduleRepository;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomScheduleRepository implements ScheduleRepository {
    private final TeamMatchScheduleDao teamMatchScheduleDao;
    private final MatchDataDao matchDataDao;
    private final PitTeamRemainingDao pitTeamRemainingDao;
    private final ProcessedChunkDao processedChunkDao;
    private final SeenLineDao seenLineDao;
    
    // Preferences
    private final Preference listPreference;
    private final Preference matchPreference;
    private final Preference pitDataPreference;
    private final Preference debugPreference;

    public RoomScheduleRepository(TeamMatchScheduleDao teamMatchScheduleDao,
                                  MatchDataDao matchDataDao,
                                  PitTeamRemainingDao pitTeamRemainingDao,
                                  ProcessedChunkDao processedChunkDao,
                                  SeenLineDao seenLineDao) {
        this.teamMatchScheduleDao = teamMatchScheduleDao;
        this.matchDataDao = matchDataDao;
        this.pitTeamRemainingDao = pitTeamRemainingDao;
        this.processedChunkDao = processedChunkDao;
        this.seenLineDao = seenLineDao;
        
        this.listPreference = PowerPreference.getFileByName("List");
        this.matchPreference = PowerPreference.getFileByName("Match");
        this.pitDataPreference = PowerPreference.getFileByName("PitData");
        this.debugPreference = PowerPreference.getFileByName("Debug");
    }

    // Team Schedule
    @Override
    public String getTeamNumber(int matchNumber, int position) {
        return teamMatchScheduleDao.getTeamNumber(matchNumber, position);
    }

    @Override
    public String[][] exportTeamSchedule() {
        List<com.databits.androidscouting.data.entity.TeamMatchSchedule> scheduleList =
            teamMatchScheduleDao.getAllScheduleSync();

        if (scheduleList == null || scheduleList.isEmpty()) {
            return null;
        }

        java.util.Map<Integer, String[]> matchMap = new java.util.HashMap<>();
        for (com.databits.androidscouting.data.entity.TeamMatchSchedule item : scheduleList) {
            if (!matchMap.containsKey(item.matchNumber)) {
                matchMap.put(item.matchNumber, new String[7]);
                matchMap.get(item.matchNumber)[0] = String.valueOf(item.matchNumber);
            }
            matchMap.get(item.matchNumber)[item.crowdPosition] = item.teamNumber;
        }

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
        teamMatchScheduleDao.deleteAll();
        List<com.databits.androidscouting.data.entity.TeamMatchSchedule> schedules = new ArrayList<>();

        for (String[] row : csvData) {
            if (row.length < 7) {
                continue;
            }

            try {
                int matchNumber = Integer.parseInt(row[0]);
                String[] alliances = {"BLUE", "BLUE", "BLUE", "RED", "RED", "RED"};

                for (int i = 0; i < 6; i++) {
                    com.databits.androidscouting.data.entity.TeamMatchSchedule schedule =
                        new com.databits.androidscouting.data.entity.TeamMatchSchedule();
                    schedule.matchNumber = matchNumber;
                    schedule.crowdPosition = i + 1;
                    schedule.teamNumber = row[i + 1];
                    schedule.alliance = alliances[i];
                    schedules.add(schedule);
                }
            } catch (NumberFormatException ignored) {
                // Skip headers or malformed rows.
            }
        }

        if (!schedules.isEmpty()) {
            teamMatchScheduleDao.insertAll(schedules);
        }
    }

    // Pit Teams
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
        pitTeamRemainingDao.deleteAll();
        for (String team : teams) {
            PitTeamRemaining entity = new PitTeamRemaining();
            entity.teamNumber = team;
            pitTeamRemainingDao.insert(entity);
        }
    }

    @Override
    public void removePitTeam(String teamNumber) {
        pitTeamRemainingDao.removeTeam(teamNumber);
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
    public boolean isPitRemoveEnabled() {
        return listPreference.getBoolean("pit_remove_enabled", false);
    }

    @Override
    public void setPitRemoveEnabled(boolean enabled) {
        listPreference.setBoolean("pit_remove_enabled", enabled);
    }

    // Match Data
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

    @Override
    public Map<String, ?> getAllMatchData() {
        return matchPreference.getData();
    }

    @Override
    public Map<String, ?> getAllPitData() {
        return pitDataPreference.getData();
    }

    // Special Scout Data
    @Override
    public ArrayList<String> getSpecialScoutData() {
        return listPreference.getObject("special_scout", ArrayList.class);
    }

    @Override
    public void setSpecialScoutData(ArrayList<String> data) {
        listPreference.setObject("special_scout", data);
    }

    @Override
    public boolean isSpecialRemoveEnabled() {
        return listPreference.getBoolean("special_remove_enabled", false);
    }

    @Override
    public void setSpecialRemoveEnabled(boolean enabled) {
        listPreference.setBoolean("special_remove_enabled", enabled);
    }

    // Misc List Sizes
    @Override
    public int getTeamMatchListSize() {
        return teamMatchScheduleDao.getMatchCount();
    }

    @Override
    public void setTeamMatchListSize(int size) {
        debugPreference.setInt("team_match_list_size", size);
    }

    // Google Config
    @Override
    public String[][] getGoogleConfig() {
        return listPreference.getObject("google_config", String[][].class);
    }

    @Override
    public void setGoogleConfig(String[][] config) {
        listPreference.setObject("google_config", config);
    }

    // Processed Chunks
    @Override
    public boolean hasProcessedChunk(int chunkId) {
        return processedChunkDao.hasProcessedChunk(chunkId);
    }

    @Override
    public void markChunkProcessed(int chunkId) {
        ProcessedChunk chunk = new ProcessedChunk();
        chunk.chunkId = chunkId;
        processedChunkDao.insert(chunk);
    }

    @Override
    public List<Integer> getAllProcessedChunks() {
        return processedChunkDao.getAllChunkIds();
    }

    @Override
    public void clearProcessedChunks() {
        processedChunkDao.deleteAll();
    }

    // Seen Lines
    @Override
    public boolean hasSeenLine(String lineHash, String dataType) {
        return seenLineDao.hasSeenLine(lineHash, dataType);
    }

    @Override
    public void markLineSeen(String lineHash, String dataType) {
        SeenLine line = new SeenLine();
        line.lineHash = lineHash;
        line.dataType = dataType;
        seenLineDao.insert(line);
    }

    @Override
    public void clearSeenLines(String dataType) {
        seenLineDao.clearType(dataType);
    }

    @Override
    public void clearAllSeenLines() {
        seenLineDao.deleteAll();
    }

    // Debug Backup
    @Override
    public int getMatchBackup() {
        return debugPreference.getInt("match_backup", 1);
    }

    @Override
    public void setMatchBackup(int match) {
        debugPreference.setInt("match_backup", match);
    }
}
