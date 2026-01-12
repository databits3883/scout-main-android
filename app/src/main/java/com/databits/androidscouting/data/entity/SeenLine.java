package com.databits.androidscouting.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "seen_lines",
    indices = {@Index(value = {"line_hash", "data_type"}, unique = true)}
)
public class SeenLine {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "line_hash")
    public String lineHash;  // MD5 or hash of the line content

    @NonNull
    @ColumnInfo(name = "data_type")
    public String dataType;  // "CROWD", "PIT", "SPECIALTY"

    @ColumnInfo(name = "first_seen")
    public long firstSeen;

    public SeenLine() {
        this.firstSeen = System.currentTimeMillis();
    }
}
