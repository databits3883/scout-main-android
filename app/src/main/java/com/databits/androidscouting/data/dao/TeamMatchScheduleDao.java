package com.databits.androidscouting.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.databits.androidscouting.data.entity.TeamMatchSchedule;
import java.util.List;

@Dao
public interface TeamMatchScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TeamMatchSchedule schedule);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TeamMatchSchedule> schedules);

    @Update
    void update(TeamMatchSchedule schedule);

    @Query("SELECT * FROM team_match_schedule ORDER BY match_number ASC, crowd_position ASC")
    LiveData<List<TeamMatchSchedule>> getAllSchedules();

    @Query("SELECT * FROM team_match_schedule ORDER BY match_number ASC, crowd_position ASC")
    List<TeamMatchSchedule> getAllScheduleSync();

    @Query("SELECT * FROM team_match_schedule WHERE match_number = :matchNumber ORDER BY crowd_position ASC")
    List<TeamMatchSchedule> getMatchSchedule(int matchNumber);

    @Query("SELECT * FROM team_match_schedule WHERE match_number = :matchNumber AND crowd_position = :position")
    TeamMatchSchedule getTeamForPosition(int matchNumber, int position);

    @Query("SELECT team_number FROM team_match_schedule WHERE match_number = :matchNumber AND crowd_position = :position")
    String getTeamNumber(int matchNumber, int position);

    @Query("UPDATE team_match_schedule SET is_scouted = 1 WHERE match_number = :matchNumber AND crowd_position = :position")
    void markScouted(int matchNumber, int position);

    @Query("DELETE FROM team_match_schedule")
    void deleteAll();

    @Query("SELECT COUNT(DISTINCT match_number) FROM team_match_schedule")
    int getMatchCount();
}
