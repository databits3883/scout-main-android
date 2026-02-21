package com.databits.androidscouting.data.repository.impl;

import androidx.lifecycle.LiveData;
import com.databits.androidscouting.data.dao.UploadQueueDao;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import com.databits.androidscouting.data.repository.UploadRepository;
import java.util.List;

public class RoomUploadRepository implements UploadRepository {
    private final UploadQueueDao uploadQueueDao;

    public RoomUploadRepository(UploadQueueDao uploadQueueDao) {
        this.uploadQueueDao = uploadQueueDao;
    }

    @Override
    public void addUploadItem(UploadQueueItem item) {
        uploadQueueDao.insert(item);
    }

    @Override
    public List<UploadQueueItem> getPendingUploads() {
        return uploadQueueDao.getPendingUploadsSync();
    }

    @Override
    public LiveData<List<UploadQueueItem>> getPendingUploadsLive() {
        return uploadQueueDao.getPendingUploads();
    }

    @Override
    public LiveData<Integer> getPendingUploadCount() {
        return uploadQueueDao.getPendingCount();
    }

    @Override
    public void markUploadSuccess(long id) {
        uploadQueueDao.markUploaded(id, "SUCCESS", System.currentTimeMillis());
    }

    @Override
    public void markUploadFailed(long id, String error) {
        uploadQueueDao.incrementRetryCount(id, error);
        uploadQueueDao.updateStatus(id, "FAILED");
    }

    @Override
    public void clearSuccessfulUploads() {
        uploadQueueDao.deleteSuccessfulUploads();
    }
}
