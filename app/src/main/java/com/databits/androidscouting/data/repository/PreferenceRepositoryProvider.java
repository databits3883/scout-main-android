package com.databits.androidscouting.data.repository;

import android.content.Context;

public final class PreferenceRepositoryProvider {
    private static AppRepositories repositories;

    private PreferenceRepositoryProvider() {
    }

    public static synchronized void init(Context context) {
        if (repositories == null) {
            DefaultPreferenceRepository repository = new DefaultPreferenceRepository(context.getApplicationContext());
            repositories = new AppRepositories(repository, repository, repository, repository);
        }
    }

    public static synchronized AppRepositories graph(Context context) {
        init(context);
        return repositories;
    }

    public static synchronized AppRepositories graph() {
        if (repositories == null) {
            throw new IllegalStateException("PreferenceRepositoryProvider must be initialized first");
        }
        return repositories;
    }

}
