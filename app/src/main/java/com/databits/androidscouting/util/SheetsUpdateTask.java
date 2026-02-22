package com.databits.androidscouting.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.databits.androidscouting.core.model.UploadMode;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import com.databits.androidscouting.data.repository.SyncStore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SheetsUpdateTask {
  private static final String TAG = "SheetsUpdateTask";
  private static final int MAX_RETRIES = 3;
  private static final int BASE_DELAY_MS = 1000;

  private final Sheets sheetsService;
  private final ExecutorService executor;
  private final Handler mainHandler;
  private final SheetsUpdateTask.UiCallback uiCallback;
  private final ProvisionSettingsStore provisionStore;
  private final SyncStore syncStore;

  public interface UiCallback {
    void onAuthorizationRequired(UserRecoverableAuthIOException e);
    void onUploadSuccess(String updatedRange);
    void onUploadFailed();
    void onNoDataToUpload();
    void onDuplicateData();
  }

  public SheetsUpdateTask(
      Context context,
      SheetsUpdateTask.UiCallback uiCallback,
      ProvisionSettingsStore provisionStore,
      SyncStore syncStore
  ) {
    this(
        uiCallback,
        provisionStore,
        syncStore,
        createSheetsService(context, provisionStore),
        Executors.newSingleThreadExecutor(),
        new Handler(Looper.getMainLooper())
    );
  }

  SheetsUpdateTask(
      SheetsUpdateTask.UiCallback uiCallback,
      ProvisionSettingsStore provisionStore,
      SyncStore syncStore,
      Sheets sheetsService,
      ExecutorService executor,
      Handler mainHandler
  ) {
    this.uiCallback = uiCallback;
    this.provisionStore = provisionStore;
    this.syncStore = syncStore;
    this.sheetsService = sheetsService;
    this.executor = executor;
    this.mainHandler = mainHandler;
  }

  private static Sheets createSheetsService(Context context, ProvisionSettingsStore provisionStore) {
    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
            context, Arrays.asList(SheetsScopes.SPREADSHEETS))
        .setBackOff(new ExponentialBackOff());
    String accountName = provisionStore.getGoogleAccountName();
    credential.setSelectedAccountName(accountName);

    HttpTransport transport = new NetHttpTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    return new Sheets.Builder(transport, jsonFactory, credential)
        .setApplicationName("Android Scouter")
        .build();
  }

  public void execute(String spreadsheetId) {
    executor.execute(() -> {
      SheetsUpdateTask.UploadData uploadData = prepareUploadData();
      if (uploadData == null) {
        postNoDataToUpload();
        return;
      }

      try {
        if (isDataDuplicate(spreadsheetId, uploadData.range, uploadData.values)) {
          postDuplicateData();
          return;
        }

        attemptUploadWithRetries(spreadsheetId, uploadData);
      } catch (UserRecoverableAuthIOException e) {
        postAuthorizationRequired(e);
      } catch (IOException e) {
        Log.e(TAG, "Error checking for duplicate data or uploading.", e);
        postUploadFailed();
      }
    });
  }

  private SheetsUpdateTask.UploadData prepareUploadData() {
    UploadMode uploadMode = UploadMode.fromRaw(provisionStore.getUploadMode());
    String uploadType;
    switch (uploadMode) {
      case CROWD:
        uploadType = "CROWD";
        break;
      case PIT:
        uploadType = "PIT";
        break;
      case SPECIALTY:
        uploadType = "SPECIALTY";
        break;
      default:
        return null;
    }

    // Get pending uploads from Room database
    List<com.databits.androidscouting.data.entity.UploadQueueItem> pendingItems =
        syncStore.getPendingUploads();

    if (pendingItems == null || pendingItems.isEmpty()) {
      return null;
    }

    // Filter by upload type and collect data + IDs
    List<List<Object>> uploadValues = new ArrayList<>();
    List<Long> itemIds = new ArrayList<>();

    for (com.databits.androidscouting.data.entity.UploadQueueItem item : pendingItems) {
      if (uploadType.equals(item.uploadType)) {
        // Split CSV string into list of fields
        String[] fields = item.dataCsv.split(",");
        List<Object> row = new ArrayList<>();
        for (String field : fields) {
          row.add(field);
        }
        uploadValues.add(row);
        itemIds.add(item.id);
      }
    }

    if (uploadValues.isEmpty()) {
      return null;
    }

    String range = getRangeForUploadMode(uploadMode.getRawValue());
    return new SheetsUpdateTask.UploadData(range, uploadValues, itemIds);
  }

  private String getRangeForUploadMode(String uploadMode) {
    switch (uploadMode) {
      case "Crowd":
        return provisionStore.getCrowdRange();
      case "Pit":
        return provisionStore.getPitRange();
      case "Specialty":
        return provisionStore.getSpecialtyRange();
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
    for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
      try {
        String updatedRange = performUpload(spreadsheetId, uploadData);
        if (updatedRange != null) {
          for (Long itemId : uploadData.itemIds) {
            syncStore.markUploadSuccess(itemId);
          }
          syncStore.clearSuccessfulUploads();
          postUploadSuccess(updatedRange);
          return;
        }
      } catch (UserRecoverableAuthIOException e) {
        postAuthorizationRequired(e);
        return;
      } catch (IOException e) {
        Log.e(TAG, "IOException during upload attempt: " + (attempt + 1), e);
      }

      if (attempt < MAX_RETRIES - 1) {
        long delay = BASE_DELAY_MS * (1L << attempt);
        Log.d(TAG, "Upload failed. Retrying in " + delay + "ms...");
        try {
          Thread.sleep(delay);
        } catch (InterruptedException interruptedException) {
          Thread.currentThread().interrupt();
          postUploadFailed();
          return;
        }
      }
    }

    Log.e(TAG, "Upload failed after " + MAX_RETRIES + " attempts.");
    postUploadFailed();
  }


  private String performUpload(String spreadsheetId, SheetsUpdateTask.UploadData uploadData) throws IOException {
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
        return updateResponse.getUpdatedRange();
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
        return appendResponse.getUpdates().getUpdatedRange();
      }
    } catch (IOException e) {
      Log.e(TAG, "Append also failed.", e);
      throw e;
    }

    return null;
  }

  private void postAuthorizationRequired(UserRecoverableAuthIOException e) {
    mainHandler.post(() -> uiCallback.onAuthorizationRequired(e));
  }

  private void postUploadSuccess(String updatedRange) {
    mainHandler.post(() -> uiCallback.onUploadSuccess(updatedRange));
  }

  private void postUploadFailed() {
    mainHandler.post(uiCallback::onUploadFailed);
  }

  private void postNoDataToUpload() {
    mainHandler.post(uiCallback::onNoDataToUpload);
  }

  private void postDuplicateData() {
    mainHandler.post(uiCallback::onDuplicateData);
  }

  public void shutdown() {
    executor.shutdownNow();
    mainHandler.removeCallbacksAndMessages(null);
  }

  private static class UploadData {
    final String range;
    final List<List<Object>> values;
    final List<Long> itemIds;

    UploadData(String range, List<List<Object>> values, List<Long> itemIds) {
      this.range = range;
      this.values = values;
      this.itemIds = itemIds;
    }
  }
}
