package com.databits.androidscouting.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SheetsUpdateTask {
  private static final String TAG = "SheetsUpdateTask";
  private static final int MAX_RETRIES = 3;
  private static final int BASE_DELAY_MS = 1000;

  private final Sheets sheetsService;
  private final ExecutorService executor;
  private final Handler mainHandler;
  private final SheetsUpdateTask.UiCallback uiCallback;
  private final Preference configPreference;
  private final Preference matchPreference;
  private final Preference debugPreference;

  public interface UiCallback {
    void onAuthorizationRequired(UserRecoverableAuthIOException e);
    void onUploadSuccess(String updatedRange);
    void onUploadFailed();
    void onNoDataToUpload();
    void onDuplicateData();
  }

  public SheetsUpdateTask(Context context, SheetsUpdateTask.UiCallback uiCallback) {
    this.uiCallback = uiCallback;
    this.executor = Executors.newSingleThreadExecutor();
    this.mainHandler = new Handler(Looper.getMainLooper());

    this.configPreference = PreferenceManager.getInstance().getConfigPreference();
    this.matchPreference = PreferenceManager.getInstance().getMatchPreference();
    this.debugPreference = PreferenceManager.getInstance().getDebugPreference();

    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
            context, Arrays.asList(SheetsScopes.SPREADSHEETS))
        .setBackOff(new ExponentialBackOff());
    String accountName = configPreference.getString("google_account_name", null);
    credential.setSelectedAccountName(accountName);

    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    sheetsService = new Sheets.Builder(transport, jsonFactory, credential)
        .setApplicationName("Android Scouter")
        .build();
  }

  public void execute(String spreadsheetId) {
    executor.execute(() -> {
      SheetsUpdateTask.UploadData uploadData = prepareUploadData();
      if (uploadData == null) {
        uiCallback.onNoDataToUpload();
        return;
      }

      try {
        if (isDataDuplicate(spreadsheetId, uploadData.range, uploadData.values)) {
          uiCallback.onDuplicateData();
          return;
        }

        attemptUploadWithRetries(spreadsheetId, uploadData);
      } catch (UserRecoverableAuthIOException e) {
        uiCallback.onAuthorizationRequired(e);
      } catch (IOException e) {
        Log.e(TAG, "Error checking for duplicate data or uploading.", e);
        uiCallback.onUploadFailed();
      }
    });
  }

  private SheetsUpdateTask.UploadData prepareUploadData() {
    String uploadMode = configPreference.getString("uploadMode", "");
    List<List<String>> columnData = getColumnData(uploadMode);

    if (columnData == null || columnData.isEmpty()) {
      return null;
    }

    String range = getRangeForUploadMode(uploadMode);
    List<List<Object>> uploadValues = new ArrayList<>();
    for (List<String> rowData : columnData) {
      uploadValues.add(new ArrayList<>(rowData));
    }

    return new SheetsUpdateTask.UploadData(range, uploadValues);
  }

  private List<List<String>> getColumnData(String uploadMode) {
    switch (uploadMode) {
      case "Crowd":
        return new ArrayList<>(matchPreference.getObject("upload_data", ArrayList.class, new ArrayList<>()));
      case "Pit":
        return new ArrayList<>(matchPreference.getObject("pit_upload_data", ArrayList.class, new ArrayList<>()));
      case "Specialty":
        return new ArrayList<>(matchPreference.getObject("special_upload_data", ArrayList.class, new ArrayList<>()));
      default:
        Log.d(TAG, "No valid upload mode set.");
        return null;
    }
  }

  private String getRangeForUploadMode(String uploadMode) {
    switch (uploadMode) {
      case "Crowd":
        return configPreference.getString("Crowd_range", "Sheet1!A1");
      case "Pit":
        return configPreference.getString("Pit_range", "Sheet1!A1");
      case "Specialty":
        return configPreference.getString("Specialty_range", "'SuperRawDatabase'!A1:AA997");
      default:
        return "Sheet1!A1";
    }
  }

  private boolean isDataDuplicate(String spreadsheetId, String range, List<List<Object>> localData) throws IOException {
    ValueRange currentData = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute();
    List<List<Object>> sheetData = currentData.getValues();
    return sheetData != null && !sheetData.isEmpty() && sheetData.equals(localData);
  }

  private void attemptUploadWithRetries(String spreadsheetId, SheetsUpdateTask.UploadData uploadData) {
    AtomicInteger attempt = new AtomicInteger(0);
    Runnable uploadRunnable = () -> {
      try {
        if (performUpload(spreadsheetId, uploadData)) {
          // Post preference clear to main thread for thread safety
          mainHandler.post(() -> matchPreference.clear());
        } else {
          handleUploadFailure(attempt.getAndIncrement(), spreadsheetId, uploadData);
        }
      } catch (UserRecoverableAuthIOException e) {
        uiCallback.onAuthorizationRequired(e);
      } catch (IOException e) {
        Log.e(TAG, "IOException during upload attempt: " + attempt.get(), e);
        handleUploadFailure(attempt.getAndIncrement(), spreadsheetId, uploadData);
      }
    };
    executor.execute(uploadRunnable);
  }

  private void handleUploadFailure(int attempt, String spreadsheetId, SheetsUpdateTask.UploadData uploadData) {
    if (attempt < MAX_RETRIES) {
      long delay = BASE_DELAY_MS * (1L << attempt); // Exponential backoff
      Log.d(TAG, "Upload failed. Retrying in " + delay + "ms...");
      try {
        Thread.sleep(delay);
        attemptUploadWithRetries(spreadsheetId, uploadData);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        uiCallback.onUploadFailed();
      }
    } else {
      Log.e(TAG, "Upload failed after " + MAX_RETRIES + " attempts.");
      uiCallback.onUploadFailed();
    }
  }


  private boolean performUpload(String spreadsheetId, SheetsUpdateTask.UploadData uploadData) throws IOException {
    ValueRange content = new ValueRange()
        .setValues(uploadData.values)
        .setMajorDimension("ROWS")
        .setRange(uploadData.range);

    // First, try to update existing cells.
    try {
      UpdateValuesResponse updateResponse = sheetsService.spreadsheets().values()
          .update(spreadsheetId, uploadData.range, content)
          .setValueInputOption("USER_ENTERED")
          .execute();
      if (updateResponse != null && updateResponse.getUpdatedCells() > 0) {
        Log.d(TAG, "Update success: " + updateResponse.getUpdatedRange());
        uiCallback.onUploadSuccess(updateResponse.getUpdatedRange());
        return true;
      }
    } catch (IOException e) {
      Log.w(TAG, "Update failed, trying append.", e);
    }

    // If update fails or affects 0 cells, try to append.
    try {
      AppendValuesResponse appendResponse = sheetsService.spreadsheets().values()
          .append(spreadsheetId, uploadData.range, content)
          .setValueInputOption("USER_ENTERED")
          .setInsertDataOption("OVERWRITE")
          .execute();
      if (appendResponse != null && appendResponse.getUpdates() != null && appendResponse.getUpdates().getUpdatedCells() > 0) {
        Log.d(TAG, "Append success: " + appendResponse.getUpdates().getUpdatedRange());
        uiCallback.onUploadSuccess(appendResponse.getUpdates().getUpdatedRange());
        return true;
      }
    } catch (IOException e) {
      Log.e(TAG, "Append also failed.", e);
      throw e;
    }

    return false;
  }

  private static class UploadData {
    final String range;
    final List<List<Object>> values;

    UploadData(String range, List<List<Object>> values) {
      this.range = range;
      this.values = values;
    }
  }
}
