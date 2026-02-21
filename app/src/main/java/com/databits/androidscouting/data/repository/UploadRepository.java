package com.databits.androidscouting.data.repository;

import androidx.lifecycle.LiveData;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import java.util.List;

public interface UploadRepository {
    // Queue Management
    void addUploadItem(UploadQueueItem item);
    List<UploadQueueItem> getPendingUploads();
    LiveData<List<UploadQueueItem>> getPendingUploadsLive();
    LiveData<Integer> getPendingUploadCount();
    
    // Status Updates
    void markUploadSuccess(long id);
    void markUploadFailed(long id, String error);
    void clearSuccessfulUploads();
}
