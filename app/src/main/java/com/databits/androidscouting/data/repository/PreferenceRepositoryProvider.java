package com.databits.androidscouting.data.repository;

import android.content.Context;

public final class PreferenceRepositoryProvider {
    private static PreferenceRepository repository;

    private PreferenceRepositoryProvider() {
    }

    public static synchronized void init(Context context) {
        if (repository == null) {
            repository = new PowerPreferenceRepository(context.getApplicationContext());
        }
    }

    public static synchronized PreferenceRepository get(Context context) {
        init(context);
        return repository;
    }

    public static synchronized PreferenceRepository get() {
        if (repository == null) {
            throw new IllegalStateException("PreferenceRepositoryProvider must be initialized first");
        }
        return repository;
    }
}
