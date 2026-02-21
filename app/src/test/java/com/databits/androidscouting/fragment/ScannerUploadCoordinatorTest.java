package com.databits.androidscouting.fragment;

import org.junit.Assert;
import org.junit.Test;

public class ScannerUploadCoordinatorTest {

    @Test
    public void decideUploadActionRequestsAuthWhenAccountMissing() {
        ScannerUploadCoordinator coordinator = new ScannerUploadCoordinator();

        ScannerUploadCoordinator.UploadDecision decision = coordinator.decideUploadAction(null, "sheet-id");

        Assert.assertEquals(ScannerUploadCoordinator.UploadActionType.REQUEST_GOOGLE_AUTH, decision.actionType);
        Assert.assertNull(decision.spreadsheetId);
    }

    @Test
    public void decideUploadActionUsesDefaultWorkbookWhenMissing() {
        ScannerUploadCoordinator coordinator = new ScannerUploadCoordinator();

        ScannerUploadCoordinator.UploadDecision decision = coordinator.decideUploadAction("user@x.com", null);

        Assert.assertEquals(ScannerUploadCoordinator.UploadActionType.EXECUTE_UPLOAD, decision.actionType);
        Assert.assertEquals("1ksCFboY3RF0d6eCQHtH2bdrPWFHXZXnicNJGz6pXndM", decision.spreadsheetId);
    }

    @Test
    public void decideUploadActionUsesProvidedWorkbook() {
        ScannerUploadCoordinator coordinator = new ScannerUploadCoordinator();

        ScannerUploadCoordinator.UploadDecision decision = coordinator.decideUploadAction("user@x.com", "abc123");

        Assert.assertEquals(ScannerUploadCoordinator.UploadActionType.EXECUTE_UPLOAD, decision.actionType);
        Assert.assertEquals("abc123", decision.spreadsheetId);
    }
}
