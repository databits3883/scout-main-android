package com.databits.androidscouting.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class SyncStatusViewModel extends ViewModel {
    private final PreferenceRepository repository;
    private final ExecutorService executor;

    private final LiveData<List<String>> scouterList;
    private final LiveData<List<UploadQueueItem>> pendingUploads;
    private final LiveData<Integer> pendingUploadCount;
    private final LiveData<List<String>> pitTeamsRemaining;
    private final MutableLiveData<String> googleAccountName = new MutableLiveData<>();

    public SyncStatusViewModel(PreferenceRepository repository, ExecutorService executor) {
        this.repository = repository;
        this.executor = executor;
        this.scouterList = repository.getScouterListLive();
        this.pendingUploads = repository.getPendingUploadsLive();
        this.pendingUploadCount = repository.getPendingUploadCount();
        this.pitTeamsRemaining = repository.getPitTeamsRemainingLive();
        refresh();
    }

    public void refresh() {
        executor.execute(() -> googleAccountName.postValue(repository.getGoogleAccountName()));
    }

    public LiveData<List<String>> getScouterList() { return scouterList; }
    public LiveData<List<UploadQueueItem>> getPendingUploads() { return pendingUploads; }
    public LiveData<Integer> getPendingUploadCount() { return pendingUploadCount; }
    public LiveData<List<String>> getPitTeamsRemaining() { return pitTeamsRemaining; }
    public LiveData<String> getGoogleAccountName() { return googleAccountName; }

    public void updateScouterList(List<String> scouters) {
        repository.setScouterList(scouters);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
