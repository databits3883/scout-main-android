package com.databits.androidscouting.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import java.util.concurrent.Executors;

public class CameraSettingsViewModelFactory implements ViewModelProvider.Factory {
    private final PreferenceRepository repository;

    public CameraSettingsViewModelFactory(PreferenceRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CameraSettingsViewModel.class)) {
            return (T) new CameraSettingsViewModel(repository, Executors.newSingleThreadExecutor());
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
