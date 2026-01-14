package com.databits.androidscouting.util;

import android.app.Activity;
import android.app.AlertDialog;  // For alert dialogs
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.preference.Preference;
import com.preference.PowerPreference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.databits.androidscouting.util.GoogleAuthActivity.REQUEST_ACCOUNT_PICKER;

//FILE UPDATED
/**
 * SheetsUpdateTask.java
 *
 * This class manages uploading data to Google Sheets, handling authentication,
 * error recovery, and retry logic for a more reliable upload process.
 * The following improvements have been applied:
 *
 * 1. **Dialog-Based User Feedback:** Alert dialogs have been added to notify users about key events:
 *    - Duplicate data detected (`showDuplicateDataDialog()`).
 *    - No valid data available for upload (`showNoDataDialog()`).
 *    - Upload failure after multiple attempts (`showUploadFailedDialog()`).
 *
 * 2. **Enhanced Error Handling:** Improved exception handling for API calls,
 *    including `UserRecoverableAuthIOException` cases where re-authentication is needed.
 *
 * 3. **Dynamic Data Comparison:** The `isDataDifferent()` method ensures that only new data is uploaded,
 *    preventing redundant entries in Google Sheets.
 *
 * 4. **Retry Mechanism:** The upload process includes an exponential backoff strategy,
 *    automatically retrying failed attempts up to five times.
 *
 * 5. **Preference Integration:** Uses `PreferenceManager` to retrieve configuration settings
 *    such as account credentials and upload modes for a centralized approach.
 *
 * NOTE: File structure and dependency references remain consistent with previous implementations.
 */

public class SheetsUpdateTask {
  // Keep track of the last auth error.
  public static UserRecoverableAuthIOException mLastError;

  private final Context context;
  private final Activity activity;
  private final Sheets sheetsService;
  private String spreadsheetId;

  // Preferences for configuration and debugging.
  Preference configPreference = PreferenceManager.getInstance().getConfigPreference();
  Preference debugPreference = PreferenceManager.getInstance().getDebugPreference();
  Preference matchPreference = PreferenceManager.getInstance().getMatchPreference();

  // Executor for background work and a handler for UI updates.
  private final ExecutorService executor;
  private final Handler handler;

  // Retry configuration.
  private final int maxRetries = 5;
  private final int baseDelayMs = 1000;

  public SheetsUpdateTask(Activity activity) {
    this.activity = activity;
    this.context = activity.getBaseContext();
    this.handler = new Handler(Looper.getMainLooper());
    this.executor = Executors.newSingleThreadExecutor();

    // Set up Google Account Credential.
    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
            context, Arrays.asList(SheetsScopes.SPREADSHEETS))
        .setBackOff(new ExponentialBackOff());
    String accountName = configPreference.getString("google_account_name", null);
    credential.setSelectedAccountName(accountName);
    Log.d("SheetsUpdateTaskRunner", "Selected Account Name: " + accountName);

    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    sheetsService = new Sheets.Builder(transport, jsonFactory, credential)
        .setApplicationName("Android Scouter")
        .build();
  }

  public void execute() {
    // Set the spreadsheet ID (ensure this is valid).
    spreadsheetId = "1ksCFboY3RF0d6eCQHtH2bdrPWFHXZXnicNJGz6pXndM";
    Log.d("SheetsUpdateTaskRunner", "Spreadsheet ID: " + spreadsheetId);

    // Determine the upload mode.
    String uploadMode = configPreference.getString("uploadMode", "");
    Log.d("SheetsUpdateTaskRunner", "Upload Mode: " + uploadMode);

    // Retrieve data from local preferences.
    List<List<String>> columnData = null;
    if (uploadMode != null) {
      switch (uploadMode) {
        case "Crowd":
          columnData = new ArrayList<>(matchPreference.getObject("upload_data",
              ArrayList.class, new ArrayList<>()));
          break;
        case "Pit":
          columnData = new ArrayList<>(matchPreference.getObject("pit_upload_data",
              ArrayList.class, new ArrayList<>()));
          break;
        case "Specialty":  // Consistent spelling "Specialty"
          columnData = new ArrayList<>(matchPreference.getObject("special_upload_data",
              ArrayList.class, new ArrayList<>()));
          break;
        default:
          Log.d("SheetsUpdateTaskRunner", "No valid upload mode set.");
      }
    }
    Log.d("SheetsUpdateTaskRunner", "Column Data Size: " +
        (columnData != null ? columnData.size() : 0));

    // If no data is available (e.g., user clicked upload without scanning), show an alert.
    if (columnData == null || columnData.isEmpty()) {
      handler.post(this::showNoDataDialog);
      return;
    }

    // Determine the target sheet range.
    String range = "";
    if (uploadMode != null) {
      switch (uploadMode) {
        case "Crowd":
          range = configPreference.getString("Crowd_range", "Sheet1!A1");
          break;
        case "Pit":
          range = configPreference.getString("Pit_range", "Sheet1!A1");
          break;
        case "Specialty":
          // Wrap the sheet name in single quotes if needed.
          range = configPreference.getString("Specialty_range", "'SuperRawDatabase'!A1:AA997");
          break;
      }
    }
    Log.d("SheetsUpdateTaskRunner", "Sheet Range: " + range);

    // Prepare the payload.
    ValueRange content = new ValueRange();
    content.setMajorDimension("ROWS");
    content.setRange(range);
    List<List<Object>> uploadData = new ArrayList<>();
    for (List<String> rowData : columnData) {
      uploadData.add(new ArrayList<>(rowData));
    }
    content.setValues(uploadData);

    // Pre-upload check: compare local data with current sheet data.
    String finalRange = range;
    executor.execute(() -> {
      try {
        if (!isDataDifferent(uploadData, finalRange)) {
          // Data is duplicate; show a duplicate alert.
          handler.post(() -> showDuplicateDataDialog());
          return;
        }
      } catch (IOException e) {
        Log.e("SheetsUpdateTaskRunner", "Error comparing sheet data: ", e);
      }
      // Data is different; attempt upload with retry logic.
      attemptUpload(content, finalRange, uploadData, 0);
    });
  }

  /**
   * Compares the local upload data with the current data in the sheet.
   * Returns true if the data is different; false if identical.
   */
  private boolean isDataDifferent(List<List<Object>> localData, String range) throws IOException {
    ValueRange currentData = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute();
    List<List<Object>> sheetData = currentData.getValues();
    if (sheetData == null || sheetData.isEmpty()) {
      return true; // Sheet is empty, so data is considered different.
    }
    return !sheetData.equals(localData);
  }

  /**
   * Attempts to upload the data using update(), and falls back to append() if necessary.
   * Retries with exponential backoff up to maxRetries.
   */
  private void attemptUpload(final ValueRange content, final String range,
      final List<List<Object>> uploadData, final int attempt) {
    executor.execute(() -> {
      UpdateValuesResponse updateResponse = null;
      AppendValuesResponse appendResponse = null;
      boolean success = false;
      try {
        Log.d("SheetsUpdateTaskRunner", "Attempt " + (attempt + 1) + " using update()...");
        updateResponse = sheetsService.spreadsheets().values().update(spreadsheetId, range, content)
            .setValueInputOption("USER_ENTERED")
            .execute();
        if (updateResponse != null && updateResponse.getUpdatedRange() != null) {
          Log.d("SheetsUpdateTaskRunner", "Update success: Updated Range: " +
              updateResponse.getUpdatedRange());
          success = true;
        } else {
          Log.d("SheetsUpdateTaskRunner", "Update returned null or empty response.");
        }
      } catch (UserRecoverableAuthIOException g) {
        mLastError = g;
        Log.e("SheetsUpdateTaskRunner", "UserRecoverableAuthIOException on update: ", g);
        debugPreference.putBoolean("upload_error", true);
        debugPreference.setString("upload_error_message", g.getMessage());
        handler.post(() -> activity.startActivityForResult(mLastError.getIntent(), REQUEST_ACCOUNT_PICKER));
        return;
      } catch (IOException e) {
        Log.e("SheetsUpdateTaskRunner", "IOException on update attempt: ", e);
      }
      // If update did not succeed, try append() as fallback.
      if (!success) {
        try {
          Log.d("SheetsUpdateTaskRunner", "Attempt " + (attempt + 1) + " using append()...");
          appendResponse = sheetsService.spreadsheets().values().append(spreadsheetId, range, content)
              .setValueInputOption("USER_ENTERED")
              .setInsertDataOption("OVERWRITE")
              .execute();
          if (appendResponse != null && appendResponse.getUpdates() != null &&
              appendResponse.getUpdates().getUpdatedRange() != null) {
            Log.d("SheetsUpdateTaskRunner", "Append success: Appended Range: " +
                appendResponse.getUpdates().getUpdatedRange());
            success = true;
          } else {
            Log.d("SheetsUpdateTaskRunner", "Append returned null or empty response.");
          }
        } catch (UserRecoverableAuthIOException g2) {
          mLastError = g2;
          Log.e("SheetsUpdateTaskRunner", "UserRecoverableAuthIOException on append: ", g2);
          debugPreference.putBoolean("upload_error", true);
          debugPreference.setString("upload_error_message", g2.getMessage());
          handler.post(() -> activity.startActivityForResult(mLastError.getIntent(), REQUEST_ACCOUNT_PICKER));
          return;
        } catch (IOException e2) {
          Log.e("SheetsUpdateTaskRunner", "IOException on append attempt: ", e2);
        }
      }
      // Process final outcome.
      if (success) {
        UpdateValuesResponse finalUpdateResponse = updateResponse;
        AppendValuesResponse finalAppendResponse = appendResponse;
        handler.post(() -> {
          String finalRange;
          if (finalUpdateResponse != null && finalUpdateResponse.getUpdatedRange() != null) {
            finalRange = finalUpdateResponse.getUpdatedRange();
          } else if (finalAppendResponse != null && finalAppendResponse.getUpdates() != null &&
              finalAppendResponse.getUpdates().getUpdatedRange() != null) {
            finalRange = finalAppendResponse.getUpdates().getUpdatedRange();
          } else {
            finalRange = "Unknown";
          }
          Toast.makeText(context, "Data uploaded. Updated Range: " + finalRange, Toast.LENGTH_LONG).show();
          Log.d("SheetsUpdateTaskRunner", "Final upload success. Updated Range: " + finalRange);
          matchPreference.clear();
        });
      } else {
        if (attempt < maxRetries - 1) {
          int delay = (int) (baseDelayMs * Math.pow(2, attempt));
          Log.d("SheetsUpdateTaskRunner", "Upload failed on attempt " + (attempt + 1) +
              ". Retrying in " + delay + "ms...");
          handler.postDelayed(() -> attemptUpload(content, range, uploadData, attempt + 1), delay);
        } else {
          handler.post(() -> showUploadFailedDialog());
        }
      }
    });
  }

  /**
   * Displays an alert dialog when duplicate data is detected.
   */
  private void showDuplicateDataDialog() {
    new AlertDialog.Builder(activity)
        .setTitle("Duplicate Data")
        .setMessage("The data you are trying to upload is identical to what is already in Google Sheets. Duplicate entries are not allowed.")
        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
        .show();
  }

  /**
   * Displays an alert dialog when no valid data is available for upload.
   */
  private void showNoDataDialog() {
    new AlertDialog.Builder(activity)
        .setTitle("No Data")
        .setMessage("No valid QR code data was scanned. Please scan a valid QR code before uploading.")
        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
        .show();
  }

  /**
   * Displays an alert dialog when the upload fails after multiple attempts.
   */
  private void showUploadFailedDialog() {
    new AlertDialog.Builder(activity)
        .setTitle("Upload Failed")
        .setMessage("Data upload failed after multiple attempts. Please check your network or try again later.")
        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
        .show();
  }
}
