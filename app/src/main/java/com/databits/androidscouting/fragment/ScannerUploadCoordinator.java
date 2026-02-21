package com.databits.androidscouting.fragment;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import com.databits.androidscouting.util.GoogleAuthActivity;
import com.databits.androidscouting.util.SheetsUpdateTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

final class ScannerUploadCoordinator {
    private static final String DEFAULT_SPREADSHEET_ID = "1ksCFboY3RF0d6eCQHtH2bdrPWFHXZXnicNJGz6pXndM";

    enum UploadActionType {
        REQUEST_GOOGLE_AUTH,
        EXECUTE_UPLOAD,
    }

    static final class UploadDecision {
        final UploadActionType actionType;
        final String spreadsheetId;

        private UploadDecision(UploadActionType actionType, String spreadsheetId) {
            this.actionType = actionType;
            this.spreadsheetId = spreadsheetId;
        }

        static UploadDecision requestGoogleAuth() {
            return new UploadDecision(UploadActionType.REQUEST_GOOGLE_AUTH, null);
        }

        static UploadDecision executeUpload(String spreadsheetId) {
            return new UploadDecision(UploadActionType.EXECUTE_UPLOAD, spreadsheetId);
        }
    }

    UploadDecision decideUploadAction(String googleAccountName, String workbookId) {
        if (googleAccountName == null) {
            return UploadDecision.requestGoogleAuth();
        }

        return UploadDecision.executeUpload(workbookId != null ? workbookId : DEFAULT_SPREADSHEET_ID);
    }

    void callSheets(
        Fragment fragment,
        PreferenceRepository repository,
        ActivityResultLauncher<Intent> googleAuthLauncher,
        SheetsUpdateTask sheetsUpdateTask
    ) {
        if (!fragment.isAdded()) {
            return;
        }

        UploadDecision decision = decideUploadAction(repository.getGoogleAccountName(), repository.getWorkbookId());
        if (decision.actionType == UploadActionType.REQUEST_GOOGLE_AUTH) {
            googleAuthLauncher.launch(GoogleAuthActivity.newIntent(fragment.requireContext()));
            return;
        }

        sheetsUpdateTask.execute(decision.spreadsheetId);
    }

    void onAuthorizationRequired(ActivityResultLauncher<Intent> authorizationLauncher, UserRecoverableAuthIOException e) {
        authorizationLauncher.launch(e.getIntent());
    }

    void showUploadSuccess(Fragment fragment, String updatedRange) {
        showToast(fragment, "Upload successful: " + updatedRange, Toast.LENGTH_LONG);
    }

    void showUploadFailed(Fragment fragment) {
        showToast(fragment, "Upload failed. Please try again.", Toast.LENGTH_SHORT);
    }

    void showNoDataToUpload(Fragment fragment) {
        showToast(fragment, "No data to upload.", Toast.LENGTH_SHORT);
    }

    void showDuplicateData(Fragment fragment) {
        showToast(fragment, "Data is a duplicate and was not uploaded.", Toast.LENGTH_SHORT);
    }

    private void showToast(Fragment fragment, String message, int duration) {
        if (!fragment.isAdded()) {
            return;
        }

        fragment.requireActivity().runOnUiThread(() -> {
            if (!fragment.isAdded()) {
                return;
            }
            Context context = fragment.getContext();
            if (context != null) {
                Toast.makeText(context, message, duration).show();
            }
        });
    }
}
