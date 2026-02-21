package com.databits.androidscouting.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import java.util.concurrent.Executors;

public class ProvisionViewModelFactory implements ViewModelProvider.Factory {
    private final ProvisionSettingsStore repository;

    public ProvisionViewModelFactory(ProvisionSettingsStore repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProvisionViewModel.class)) {
            return (T) new ProvisionViewModel(repository, Executors.newSingleThreadExecutor());
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
