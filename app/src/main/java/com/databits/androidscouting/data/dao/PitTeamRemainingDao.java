package com.databits.androidscouting.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.databits.androidscouting.data.entity.PitTeamRemaining;
import java.util.List;

@Dao
public interface PitTeamRemainingDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PitTeamRemaining team);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<PitTeamRemaining> teams);

    @Query("SELECT team_number FROM pit_teams_remaining ORDER BY team_number ASC")
    List<String> getAllTeamNumbers();

    @Query("SELECT team_number FROM pit_teams_remaining ORDER BY team_number ASC")
    LiveData<List<String>> getAllTeamNumbersLive();

    @Query("DELETE FROM pit_teams_remaining WHERE team_number = :teamNumber")
    void removeTeam(String teamNumber);

    @Query("DELETE FROM pit_teams_remaining")
    void deleteAll();
}
