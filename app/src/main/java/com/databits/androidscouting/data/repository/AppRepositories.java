package com.databits.androidscouting.data.repository;

public final class AppRepositories {
    public final CameraSettingsStore cameraSettingsStore;
    public final ProvisionSettingsStore provisionSettingsStore;
    public final SyncStore syncStore;
    public final ScheduleStore scheduleStore;

    AppRepositories(
        CameraSettingsStore cameraSettingsStore,
        ProvisionSettingsStore provisionSettingsStore,
        SyncStore syncStore,
        ScheduleStore scheduleStore
    ) {
        this.cameraSettingsStore = cameraSettingsStore;
        this.provisionSettingsStore = provisionSettingsStore;
        this.syncStore = syncStore;
        this.scheduleStore = scheduleStore;
    }
}
