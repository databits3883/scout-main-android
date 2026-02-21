package com.databits.androidscouting.data.repository;

import androidx.lifecycle.LiveData;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import java.util.List;

public interface SyncStore {
    LiveData<List<String>> getScouterListLive();
    List<String> getScouterList();
    void setScouterList(List<String> scouters);
    List<UploadQueueItem> getPendingUploads();
    LiveData<List<UploadQueueItem>> getPendingUploadsLive();
    LiveData<Integer> getPendingUploadCount();
    void markUploadSuccess(long id);
    void clearSuccessfulUploads();
    LiveData<List<String>> getPitTeamsRemainingLive();
    String getGoogleAccountName();
}
