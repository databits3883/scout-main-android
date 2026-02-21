package com.databits.androidscouting.fragment;

import android.content.Context;
import com.databits.androidscouting.core.domain.provision.ApplyRoleProvisionUseCase;
import com.databits.androidscouting.core.domain.scanner.FindMatchedTeamSlotUseCase;
import com.databits.androidscouting.core.domain.scanner.ProcessScanPayloadUseCase;
import com.databits.androidscouting.core.domain.schedule.ImportMatchDataChunkUseCase;
import com.databits.androidscouting.core.domain.upload.QueueScanDataUseCase;
import com.databits.androidscouting.data.repository.PowerPreferenceRepository;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.databits.androidscouting.data.repository.adapter.PreferenceMatchDataImportGateway;
import com.databits.androidscouting.data.repository.adapter.PreferenceRoleProvisionGateway;
import com.databits.androidscouting.data.repository.adapter.PreferenceUploadQueueGateway;
import com.databits.androidscouting.feature.scanner.ScannerFeatureBootstrap;

final class ScannerDependencies {
    // Composition root for Scanner collaborators.
    // All dependency wiring for Scanner should happen here, not in Scanner lifecycle methods.
    final PreferenceRepository repository;
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
        PreferenceRepository repository = PowerPreferenceRepository.getInstance(context);
        return new ScannerDependencies(
            repository,
            new ScannerCameraController(),
            new ScannerUiFeedbackController(),
            new ScannerTeamScheduleController(),
            new ScannerUploadCoordinator(),
            new UploadAuditLogger(context.getFilesDir()),
            new FindMatchedTeamSlotUseCase(),
            ScannerFeatureBootstrap.provideProcessScanPayloadUseCase(),
            new ApplyRoleProvisionUseCase(new PreferenceRoleProvisionGateway(repository)),
            new ImportMatchDataChunkUseCase(new PreferenceMatchDataImportGateway(repository)),
            new QueueScanDataUseCase(new PreferenceUploadQueueGateway(repository))
        );
    }

    ScannerPayloadCoordinator createPayloadCoordinator(ScannerPayloadCoordinator.Actions actions) {
        return new ScannerPayloadCoordinator(actions);
    }

    private ScannerDependencies(
        PreferenceRepository repository,
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
        this.repository = repository;
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
