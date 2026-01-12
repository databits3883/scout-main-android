package com.databits.androidscouting.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.databits.androidscouting.data.entity.SeenLine;
import java.util.List;

@Dao
public interface SeenLineDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(SeenLine line);

    @Query("SELECT EXISTS(SELECT 1 FROM seen_lines WHERE line_hash = :hash AND data_type = :type)")
    boolean hasSeenLine(String hash, String type);

    @Query("SELECT * FROM seen_lines WHERE data_type = :type")
    List<SeenLine> getSeenLinesByType(String type);

    @Query("DELETE FROM seen_lines WHERE data_type = :type")
    void clearType(String type);

    @Query("DELETE FROM seen_lines")
    void deleteAll();
}
