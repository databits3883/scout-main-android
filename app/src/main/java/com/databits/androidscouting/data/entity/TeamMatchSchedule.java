package com.databits.androidscouting.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "team_match_schedule",
    indices = {
        @Index(value = {"match_number", "crowd_position"}, unique = true)
    }
)
public class TeamMatchSchedule {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "match_number")
    public int matchNumber;

    @ColumnInfo(name = "crowd_position")
    public int crowdPosition;  // 1-6

    @NonNull
    @ColumnInfo(name = "team_number")
    public String teamNumber;

    @NonNull
    @ColumnInfo(name = "alliance")
    public String alliance;  // "BLUE" or "RED"

    @ColumnInfo(name = "is_scouted")
    public boolean isScouted;

    public TeamMatchSchedule() {
        this.isScouted = false;
    }
}
