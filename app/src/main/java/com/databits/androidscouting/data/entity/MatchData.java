package com.databits.androidscouting.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "match_data")
public class MatchData {
    @PrimaryKey
    @ColumnInfo(name = "match_number")
    public int matchNumber;

    @ColumnInfo(name = "crowd_data_csv")
    public String crowdDataCsv;  // CSV string for this match

    @ColumnInfo(name = "pit_data_csv")
    public String pitDataCsv;

    @ColumnInfo(name = "specialty_data_csv")
    public String specialtyDataCsv;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public MatchData() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}
