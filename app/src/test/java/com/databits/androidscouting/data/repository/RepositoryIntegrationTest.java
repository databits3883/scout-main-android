package com.databits.androidscouting.data.repository;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import com.databits.androidscouting.data.database.ScoutDatabase;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import com.preference.PowerPreference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28}) // Robolectric requires a specific SDK version sometimes
public class RepositoryIntegrationTest {

    private ScoutDatabase db;
    private DefaultPreferenceRepository repository;
    private ExecutorService synchronousExecutor;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        
        // Initialize PowerPreference (needed because Repository uses it)
        PowerPreference.init(context);
        
        // Create in-memory database
        db = Room.inMemoryDatabaseBuilder(context, ScoutDatabase.class)
                .allowMainThreadQueries() // Allow main thread for testing simplicity
                .build();
        
        // Create synchronous executor
        synchronousExecutor = new ExecutorService() {
            @Override public void shutdown() {}
            @Override public java.util.List<Runnable> shutdownNow() { return null; }
            @Override public boolean isShutdown() { return false; }
            @Override public boolean isTerminated() { return false; }
            @Override public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) { return true; }
            @Override public <T> java.util.concurrent.Future<T> submit(java.util.concurrent.Callable<T> task) { 
                try { task.call(); } catch (Exception e) { throw new RuntimeException(e); }
                return null;
            }
            @Override public <T> java.util.concurrent.Future<T> submit(Runnable task, T result) { task.run(); return null; }
            @Override public java.util.concurrent.Future<?> submit(Runnable task) { task.run(); return null; }
            @Override public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks) { return null; }
            @Override public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks, long timeout, java.util.concurrent.TimeUnit unit) { return null; }
            @Override public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks) { return null; }
            @Override public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks, long timeout, java.util.concurrent.TimeUnit unit) { return null; }
            @Override public void execute(Runnable command) { command.run(); }
        };

        // Create repository with injected DB and Executor
        repository = new DefaultPreferenceRepository(context, db, synchronousExecutor);
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void testUploadQueueOperations() {
        // Arrange
        UploadQueueItem item = new UploadQueueItem();
        item.dataCsv = "Test,Data,CSV";
        item.uploadType = "CROWD";
        item.uploadStatus = "PENDING";
        item.createdAt = System.currentTimeMillis();

        // Act - Add item (runs synchronously now)
        repository.addUploadItem(item);
        
        // Assert - Verify item exists via DAO
        List<UploadQueueItem> items = db.uploadQueueDao().getPendingUploadsSync();
        assertEquals(1, items.size());
        assertEquals("Test,Data,CSV", items.get(0).dataCsv);
        
        // Act - Mark success
        long id = items.get(0).id;
        repository.markUploadSuccess(id);
        
        // Assert - Verify status updated (should no longer appear in pending list)
        items = db.uploadQueueDao().getPendingUploadsSync();
        assertEquals(0, items.size());
    }

    @Test
    public void testUploadQueueFailureThenClearSuccessful() {
        UploadQueueItem first = new UploadQueueItem();
        first.dataCsv = "1,111,data";
        first.uploadType = "CROWD";
        first.uploadStatus = "PENDING";
        first.createdAt = System.currentTimeMillis();

        UploadQueueItem second = new UploadQueueItem();
        second.dataCsv = "2,222,data";
        second.uploadType = "PIT";
        second.uploadStatus = "PENDING";
        second.createdAt = System.currentTimeMillis();

        repository.addUploadItem(first);
        repository.addUploadItem(second);

        List<UploadQueueItem> pending = db.uploadQueueDao().getPendingUploadsSync();
        assertEquals(2, pending.size());

        long firstId = pending.get(0).id;
        long secondId = pending.get(1).id;

        repository.markUploadSuccess(firstId);
        repository.markUploadFailed(secondId, "network timeout");

        List<UploadQueueItem> afterMarking = db.uploadQueueDao().getPendingUploadsSync();
        assertEquals(1, afterMarking.size());
        UploadQueueItem failed = afterMarking.get(0);
        assertEquals("FAILED", failed.uploadStatus);
        assertEquals(1, failed.retryCount);

        repository.clearSuccessfulUploads();

        List<UploadQueueItem> remainingPending = db.uploadQueueDao().getPendingUploadsSync();
        assertEquals(1, remainingPending.size());
        assertEquals(secondId, remainingPending.get(0).id);
    }

    @Test
    public void testImportExportTeamScheduleSkipsMalformedRowsAndSortsMatches() {
        String[][] csvData = new String[][]{
            {"Header", "B1", "B2", "B3", "R1", "R2", "R3"},
            {"2", "201", "202", "203", "204", "205", "206"},
            {"bad", "x", "x", "x", "x", "x", "x"},
            {"1", "101", "102", "103", "104", "105", "106"},
            {"3", "too", "short"}
        };

        repository.importTeamSchedule(csvData);

        assertEquals(2, repository.getTeamMatchListSize());
        assertEquals("101", repository.getTeamNumber(1, 1));
        assertEquals("206", repository.getTeamNumber(2, 6));

        String[][] exported = repository.exportTeamSchedule();
        assertNotNull(exported);
        assertEquals(2, exported.length);
        assertEquals("1", exported[0][0]);
        assertEquals("2", exported[1][0]);
        assertEquals(
            Arrays.asList("1", "101", "102", "103", "104", "105", "106"),
            Arrays.asList(exported[0])
        );
    }
}
