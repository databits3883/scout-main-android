package com.databits.androidscouting.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.databits.androidscouting.data.entity.MatchData;
import java.util.List;

@Dao
public interface MatchDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MatchData data);

    @Update
    void update(MatchData data);

    @Query("SELECT * FROM match_data WHERE match_number = :matchNumber")
    MatchData getMatchData(int matchNumber);

    @Query("SELECT * FROM match_data ORDER BY match_number ASC")
    LiveData<List<MatchData>> getAllMatchData();

    @Query("SELECT crowd_data_csv FROM match_data WHERE match_number = :matchNumber")
    String getCrowdData(int matchNumber);

    @Query("SELECT pit_data_csv FROM match_data WHERE match_number = :matchNumber")
    String getPitData(int matchNumber);

    @Query("UPDATE match_data SET crowd_data_csv = :csv, updated_at = :timestamp WHERE match_number = :matchNumber")
    void updateCrowdData(int matchNumber, String csv, long timestamp);

    @Query("UPDATE match_data SET pit_data_csv = :csv, updated_at = :timestamp WHERE match_number = :matchNumber")
    void updatePitData(int matchNumber, String csv, long timestamp);

    @Query("DELETE FROM match_data")
    void deleteAll();
}
