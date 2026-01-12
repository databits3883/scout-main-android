package com.databits.androidscouting.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "processed_chunks")
public class ProcessedChunk {
    @PrimaryKey
    @ColumnInfo(name = "chunk_id")
    public int chunkId;

    @ColumnInfo(name = "processed_at")
    public long processedAt;

    public ProcessedChunk() {
        this.processedAt = System.currentTimeMillis();
    }
}
