package com.databits.androidscouting.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.databits.androidscouting.data.repository.PreferenceRepository;

/**
 * Factory for creating ConfigViewModel with PreferenceRepository dependency injection.
 * Required because ConfigViewModel takes constructor parameters.
 */
public class ConfigViewModelFactory implements ViewModelProvider.Factory {
    private final PreferenceRepository repository;

    public ConfigViewModelFactory(PreferenceRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ConfigViewModel.class)) {
            return (T) new ConfigViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
