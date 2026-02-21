package com.databits.androidscouting.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import java.util.concurrent.Executors;

public class SyncStatusViewModelFactory implements ViewModelProvider.Factory {
    private final PreferenceRepository repository;

    public SyncStatusViewModelFactory(PreferenceRepository repository) {
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
