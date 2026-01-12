package com.databits.androidscouting.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.databits.androidscouting.data.entity.ProcessedChunk;
import java.util.List;

@Dao
public interface ProcessedChunkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ProcessedChunk chunk);

    @Query("SELECT EXISTS(SELECT 1 FROM processed_chunks WHERE chunk_id = :chunkId)")
    boolean hasProcessedChunk(int chunkId);

    @Query("SELECT chunk_id FROM processed_chunks")
    List<Integer> getAllChunkIds();

    @Query("DELETE FROM processed_chunks")
    void deleteAll();
}
