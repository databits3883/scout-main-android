package com.databits.androidscouting.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import java.util.List;

@Dao
public interface UploadQueueDao {
    @Insert
    long insert(UploadQueueItem item);

    @Insert
    void insertAll(List<UploadQueueItem> items);

    @Update
    void update(UploadQueueItem item);

    @Delete
    void delete(UploadQueueItem item);

    @Query("SELECT * FROM upload_queue WHERE upload_status = 'PENDING' OR upload_status = 'FAILED' ORDER BY created_at ASC")
    List<UploadQueueItem> getPendingUploadsSync();

    @Query("SELECT * FROM upload_queue WHERE upload_status = 'PENDING' OR upload_status = 'FAILED' ORDER BY created_at ASC")
    LiveData<List<UploadQueueItem>> getPendingUploads();

    @Query("SELECT * FROM upload_queue WHERE upload_type = :type AND upload_status = 'PENDING'")
    List<UploadQueueItem> getPendingUploadsByType(String type);

    @Query("UPDATE upload_queue SET upload_status = :status WHERE id = :id")
    void updateStatus(long id, String status);

    @Query("UPDATE upload_queue SET upload_status = :status, uploaded_at = :timestamp WHERE id = :id")
    void markUploaded(long id, String status, long timestamp);

    @Query("UPDATE upload_queue SET retry_count = retry_count + 1, error_message = :error WHERE id = :id")
    void incrementRetryCount(long id, String error);

    @Query("DELETE FROM upload_queue WHERE upload_status = 'SUCCESS'")
    void deleteSuccessfulUploads();

    @Query("DELETE FROM upload_queue")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM upload_queue WHERE upload_status = 'PENDING'")
    LiveData<Integer> getPendingCount();
}
