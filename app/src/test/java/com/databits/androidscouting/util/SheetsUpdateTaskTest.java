package com.databits.androidscouting.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.os.Handler;
import android.os.Looper;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import com.databits.androidscouting.data.repository.SyncStore;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SheetsUpdateTaskTest {

  private SheetsUpdateTask task;

  @After
  public void tearDown() {
    if (task != null) {
      task.shutdown();
    }
  }

  @Test
  public void execute_postsNoDataCallbackOnMainThread() throws Exception {
    ProvisionSettingsStore provisionStore = mock(ProvisionSettingsStore.class);
    SyncStore syncStore = mock(SyncStore.class);
    Sheets sheets = mock(Sheets.class);
    RecordingCallback callback = new RecordingCallback();

    when(provisionStore.getUploadMode()).thenReturn("Crowd");
    when(syncStore.getPendingUploads()).thenReturn(Collections.emptyList());

    task = buildTask(callback, provisionStore, syncStore, sheets);
    task.execute("sheet-id");

    assertTrue(waitForCallback(callback.latch, 2000));
    assertEquals("no_data", callback.event);
    assertEquals(Looper.getMainLooper().getThread(), callback.callbackThread);
  }

  @Test
  public void execute_retriesThenPostsSuccessOnMainThread() throws Exception {
    ProvisionSettingsStore provisionStore = mock(ProvisionSettingsStore.class);
    SyncStore syncStore = mock(SyncStore.class);
    RecordingCallback callback = new RecordingCallback();

    Sheets sheets = mock(Sheets.class);
    Sheets.Spreadsheets spreadsheets = mock(Sheets.Spreadsheets.class);
    Sheets.Spreadsheets.Values values = mock(Sheets.Spreadsheets.Values.class);
    Sheets.Spreadsheets.Values.Get get = mock(Sheets.Spreadsheets.Values.Get.class);
    Sheets.Spreadsheets.Values.Update update = mock(Sheets.Spreadsheets.Values.Update.class);
    Sheets.Spreadsheets.Values.Append append = mock(Sheets.Spreadsheets.Values.Append.class);

    when(sheets.spreadsheets()).thenReturn(spreadsheets);
    when(spreadsheets.values()).thenReturn(values);

    when(provisionStore.getUploadMode()).thenReturn("Crowd");
    when(provisionStore.getCrowdRange()).thenReturn("Crowd!A2:Z");

    UploadQueueItem item = new UploadQueueItem();
    item.id = 7L;
    item.uploadType = "CROWD";
    item.dataCsv = "a,b,c";
    when(syncStore.getPendingUploads()).thenReturn(Collections.singletonList(item));

    when(values.get("sheet-id", "Crowd!A2:Z")).thenReturn(get);
    when(get.execute()).thenReturn(new ValueRange().setValues(Collections.emptyList()));

    when(values.update(eq("sheet-id"), eq("Crowd!A2:Z"), any(ValueRange.class))).thenReturn(update);
    when(update.setValueInputOption("USER_ENTERED")).thenReturn(update);
    when(update.execute())
        .thenThrow(new IOException("first update fails"))
        .thenReturn(new UpdateValuesResponse().setUpdatedCells(3).setUpdatedRange("Crowd!A2:C2"));

    when(values.append(eq("sheet-id"), eq("Crowd!A2:Z"), any(ValueRange.class))).thenReturn(append);
    when(append.setValueInputOption("USER_ENTERED")).thenReturn(append);
    when(append.setInsertDataOption("OVERWRITE")).thenReturn(append);
    when(append.execute()).thenThrow(new IOException("append fails"));

    task = buildTask(callback, provisionStore, syncStore, sheets);
    task.execute("sheet-id");

    assertTrue(waitForCallback(callback.latch, 5000));
    assertEquals("success", callback.event);
    assertEquals(Looper.getMainLooper().getThread(), callback.callbackThread);

    verify(update, times(2)).execute();
    verify(syncStore).markUploadSuccess(7L);
    verify(syncStore).clearSuccessfulUploads();
  }

  private SheetsUpdateTask buildTask(
      RecordingCallback callback,
      ProvisionSettingsStore provisionStore,
      SyncStore syncStore,
      Sheets sheets
  ) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    return new SheetsUpdateTask(
        callback,
        provisionStore,
        syncStore,
        sheets,
        executor,
        new Handler(Looper.getMainLooper())
    );
  }

  private boolean waitForCallback(CountDownLatch latch, long timeoutMs) throws InterruptedException {
    long end = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < end && latch.getCount() > 0) {
      shadowOf(Looper.getMainLooper()).idle();
      Thread.sleep(20);
    }
    shadowOf(Looper.getMainLooper()).idle();
    return latch.await(10, TimeUnit.MILLISECONDS);
  }

  private static final class RecordingCallback implements SheetsUpdateTask.UiCallback {
    final CountDownLatch latch = new CountDownLatch(1);
    volatile String event;
    volatile Thread callbackThread;

    @Override
    public void onAuthorizationRequired(UserRecoverableAuthIOException e) {
      event = "authorization";
      callbackThread = Thread.currentThread();
      latch.countDown();
    }

    @Override
    public void onUploadSuccess(String updatedRange) {
      event = "success";
      callbackThread = Thread.currentThread();
      latch.countDown();
    }

    @Override
    public void onUploadFailed() {
      event = "failed";
      callbackThread = Thread.currentThread();
      latch.countDown();
    }

    @Override
    public void onNoDataToUpload() {
      event = "no_data";
      callbackThread = Thread.currentThread();
      latch.countDown();
    }

    @Override
    public void onDuplicateData() {
      event = "duplicate";
      callbackThread = Thread.currentThread();
      latch.countDown();
    }
  }
}
