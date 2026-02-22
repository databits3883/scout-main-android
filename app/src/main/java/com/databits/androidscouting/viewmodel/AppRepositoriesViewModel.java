package com.databits.androidscouting.viewmodel;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.databits.androidscouting.data.repository.AppRepositories;
import com.databits.androidscouting.data.repository.PreferenceRepositoryProvider;

public class AppRepositoriesViewModel extends ViewModel {
    private final AppRepositories repositories;

    private AppRepositoriesViewModel(AppRepositories repositories) {
        this.repositories = repositories;
    }

    public AppRepositories getRepositories() {
        return repositories;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Context appContext;

        public Factory(Context context) {
            this.appContext = context.getApplicationContext();
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AppRepositoriesViewModel.class)) {
                AppRepositories repos = PreferenceRepositoryProvider.graph(appContext);
                return modelClass.cast(new AppRepositoriesViewModel(repos));
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
