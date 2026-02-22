package com.databits.androidscouting.fragment;

import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import com.databits.androidscouting.util.SheetsUpdateTask;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScannerUploadCoordinatorTest {

    @Mock
    private Scanner fragment;
    @Mock
    private ProvisionSettingsStore provisionStore;
    @Mock
    private ActivityResultLauncher<Intent> googleAuthLauncher;
    @Mock
    private ActivityResultLauncher<Intent> authorizationLauncher;
    @Mock
    private SheetsUpdateTask sheetsUpdateTask;

    private ScannerUploadCoordinator coordinator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        coordinator = new ScannerUploadCoordinator();
    }

    @Test
    public void decideUploadActionRequestsAuthWhenAccountMissing() {
        ScannerUploadCoordinator.UploadDecision decision = coordinator.decideUploadAction(null, "sheet-id");

        Assert.assertEquals(ScannerUploadCoordinator.UploadActionType.REQUEST_GOOGLE_AUTH, decision.actionType);
        Assert.assertNull(decision.spreadsheetId);
    }

    @Test
    public void decideUploadActionUsesDefaultWorkbookWhenMissing() {
        ScannerUploadCoordinator.UploadDecision decision = coordinator.decideUploadAction("user@x.com", null);

        Assert.assertEquals(ScannerUploadCoordinator.UploadActionType.EXECUTE_UPLOAD, decision.actionType);
        Assert.assertEquals("1ksCFboY3RF0d6eCQHtH2bdrPWFHXZXnicNJGz6pXndM", decision.spreadsheetId);
    }

    @Test
    public void decideUploadActionUsesProvidedWorkbook() {
        ScannerUploadCoordinator.UploadDecision decision = coordinator.decideUploadAction("user@x.com", "abc123");

        Assert.assertEquals(ScannerUploadCoordinator.UploadActionType.EXECUTE_UPLOAD, decision.actionType);
        Assert.assertEquals("abc123", decision.spreadsheetId);
    }

    @Test
    public void callSheets_skipsWorkWhenFragmentDetached() {
        when(fragment.isAdded()).thenReturn(false);

        coordinator.callSheets(fragment, provisionStore, googleAuthLauncher, sheetsUpdateTask);

        verify(googleAuthLauncher, never()).launch(org.mockito.ArgumentMatchers.any());
        verify(sheetsUpdateTask, never()).execute(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void callSheets_executesUploadWhenAttachedAndConfigured() {
        when(fragment.isAdded()).thenReturn(true);
        when(provisionStore.getGoogleAccountName()).thenReturn("user@x.com");
        when(provisionStore.getWorkbookId()).thenReturn("sheet-42");

        coordinator.callSheets(fragment, provisionStore, googleAuthLauncher, sheetsUpdateTask);

        verify(sheetsUpdateTask).execute("sheet-42");
        verify(googleAuthLauncher, never()).launch(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void showUploadCallbacks_areNoOpWhenFragmentDetached() {
        when(fragment.isAdded()).thenReturn(false);

        coordinator.showUploadSuccess(fragment, "A1:B2");
        coordinator.showUploadFailed(fragment);
        coordinator.showNoDataToUpload(fragment);
        coordinator.showDuplicateData(fragment);

        verify(fragment, never()).requireActivity();
    }

    @Test
    public void onAuthorizationRequired_launchesRecoverableIntent() {
        Intent intent = new Intent("test.action.AUTH");
        com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException exception =
            mock(com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException.class);
        when(exception.getIntent()).thenReturn(intent);

        coordinator.onAuthorizationRequired(authorizationLauncher, exception);

        verify(authorizationLauncher).launch(intent);
    }
}
