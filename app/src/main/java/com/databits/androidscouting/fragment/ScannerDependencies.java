package com.databits.androidscouting.fragment;

import android.content.Context;
import com.databits.androidscouting.core.domain.provision.ApplyRoleProvisionUseCase;
import com.databits.androidscouting.core.domain.scanner.FindMatchedTeamSlotUseCase;
import com.databits.androidscouting.core.domain.scanner.ProcessScanPayloadUseCase;
import com.databits.androidscouting.core.domain.schedule.ImportMatchDataChunkUseCase;
import com.databits.androidscouting.core.domain.upload.QueueScanDataUseCase;
import com.databits.androidscouting.data.repository.AppRepositories;
import com.databits.androidscouting.data.repository.PreferenceRepositoryProvider;
import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import com.databits.androidscouting.data.repository.ScheduleStore;
import com.databits.androidscouting.data.repository.adapter.StoreMatchDataImportGateway;
import com.databits.androidscouting.data.repository.adapter.StoreRoleProvisionGateway;
import com.databits.androidscouting.data.repository.adapter.StoreUploadQueueGateway;
import com.databits.androidscouting.feature.scanner.ScannerFeatureBootstrap;

final class ScannerDependencies {
    // Composition root for Scanner collaborators.
    // All dependency wiring for Scanner should happen here, not in Scanner lifecycle methods.
    final ProvisionSettingsStore provisionStore;
    final ScheduleStore scheduleStore;
    final ScannerCameraController scannerCameraController;
    final ScannerUiFeedbackController scannerUiFeedbackController;
    final ScannerTeamScheduleController scannerTeamScheduleController;
    final ScannerUploadCoordinator scannerUploadCoordinator;
    final UploadAuditLogger uploadAuditLogger;
    final FindMatchedTeamSlotUseCase findMatchedTeamSlotUseCase;
    final ProcessScanPayloadUseCase processScanPayloadUseCase;
    final ApplyRoleProvisionUseCase applyRoleProvisionUseCase;
    final ImportMatchDataChunkUseCase importMatchDataChunkUseCase;
    final QueueScanDataUseCase queueScanDataUseCase;

    static ScannerDependencies create(Context context) {
        AppRepositories repositories = PreferenceRepositoryProvider.graph(context);
        return new ScannerDependencies(
            repositories.provisionSettingsStore,
            repositories.scheduleStore,
            new ScannerCameraController(),
            new ScannerUiFeedbackController(),
            new ScannerTeamScheduleController(),
            new ScannerUploadCoordinator(),
            new UploadAuditLogger(context.getFilesDir()),
            new FindMatchedTeamSlotUseCase(),
            ScannerFeatureBootstrap.provideProcessScanPayloadUseCase(),
            new ApplyRoleProvisionUseCase(new StoreRoleProvisionGateway(repositories.provisionSettingsStore)),
            new ImportMatchDataChunkUseCase(new StoreMatchDataImportGateway(repositories.scheduleStore)),
            new QueueScanDataUseCase(new StoreUploadQueueGateway(repositories.scheduleStore))
        );
    }

    ScannerPayloadCoordinator createPayloadCoordinator(ScannerPayloadCoordinator.Actions actions) {
        return new ScannerPayloadCoordinator(actions);
    }

    private ScannerDependencies(
        ProvisionSettingsStore provisionStore,
        ScheduleStore scheduleStore,
        ScannerCameraController scannerCameraController,
        ScannerUiFeedbackController scannerUiFeedbackController,
        ScannerTeamScheduleController scannerTeamScheduleController,
        ScannerUploadCoordinator scannerUploadCoordinator,
        UploadAuditLogger uploadAuditLogger,
        FindMatchedTeamSlotUseCase findMatchedTeamSlotUseCase,
        ProcessScanPayloadUseCase processScanPayloadUseCase,
        ApplyRoleProvisionUseCase applyRoleProvisionUseCase,
        ImportMatchDataChunkUseCase importMatchDataChunkUseCase,
        QueueScanDataUseCase queueScanDataUseCase
    ) {
        this.provisionStore = provisionStore;
        this.scheduleStore = scheduleStore;
        this.scannerCameraController = scannerCameraController;
        this.scannerUiFeedbackController = scannerUiFeedbackController;
        this.scannerTeamScheduleController = scannerTeamScheduleController;
        this.scannerUploadCoordinator = scannerUploadCoordinator;
        this.uploadAuditLogger = uploadAuditLogger;
        this.findMatchedTeamSlotUseCase = findMatchedTeamSlotUseCase;
        this.processScanPayloadUseCase = processScanPayloadUseCase;
        this.applyRoleProvisionUseCase = applyRoleProvisionUseCase;
        this.importMatchDataChunkUseCase = importMatchDataChunkUseCase;
        this.queueScanDataUseCase = queueScanDataUseCase;
    }

}
