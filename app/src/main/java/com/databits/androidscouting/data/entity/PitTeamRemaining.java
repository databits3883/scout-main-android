package com.databits.androidscouting.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pit_teams_remaining")
public class PitTeamRemaining {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "team_number")
    public String teamNumber;

    @ColumnInfo(name = "added_at")
    public long addedAt;

    public PitTeamRemaining() {
        this.addedAt = System.currentTimeMillis();
    }
}
