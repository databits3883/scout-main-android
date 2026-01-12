package com.databits.androidscouting.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.databits.androidscouting.data.entity.Scouter;
import java.util.List;

@Dao
public interface ScouterDao {
    @Insert
    void insert(Scouter scouter);

    @Insert
    void insertAll(List<Scouter> scouters);

    @Delete
    void delete(Scouter scouter);

    @Query("SELECT * FROM scouters WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<Scouter>> getActiveScouters();

    @Query("SELECT name FROM scouters WHERE is_active = 1 ORDER BY name ASC")
    List<String> getScouterNames();

    @Query("DELETE FROM scouters")
    void deleteAll();
}
