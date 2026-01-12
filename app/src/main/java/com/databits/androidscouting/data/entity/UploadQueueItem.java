package com.databits.androidscouting.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "upload_queue")
public class UploadQueueItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "upload_type")
    public String uploadType;  // "CROWD", "PIT", "SPECIALTY"

    @ColumnInfo(name = "match_number")
    public Integer matchNumber;  // Nullable for pit/specialty

    @ColumnInfo(name = "team_number")
    public String teamNumber;

    @ColumnInfo(name = "data_csv")
    public String dataCsv;  // Store CSV row as string

    @ColumnInfo(name = "upload_status")
    public String uploadStatus;  // "PENDING", "UPLOADING", "SUCCESS", "FAILED"

    @ColumnInfo(name = "retry_count")
    public int retryCount;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "uploaded_at")
    public Long uploadedAt;  // Nullable

    @ColumnInfo(name = "error_message")
    public String errorMessage;

    public UploadQueueItem() {
        this.uploadStatus = "PENDING";
        this.retryCount = 0;
        this.createdAt = System.currentTimeMillis();
    }
}
