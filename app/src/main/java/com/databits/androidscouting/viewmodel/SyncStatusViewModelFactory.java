package com.databits.androidscouting.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.databits.androidscouting.data.repository.SyncStore;
import java.util.concurrent.Executors;

public class SyncStatusViewModelFactory implements ViewModelProvider.Factory {
    private final SyncStore repository;

    public SyncStatusViewModelFactory(SyncStore repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SyncStatusViewModel.class)) {
            return (T) new SyncStatusViewModel(repository, Executors.newSingleThreadExecutor());
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
