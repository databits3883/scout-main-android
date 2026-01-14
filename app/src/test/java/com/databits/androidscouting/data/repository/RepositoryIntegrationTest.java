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
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28}) // Robolectric requires a specific SDK version sometimes
public class RepositoryIntegrationTest {

    private ScoutDatabase db;
    private PowerPreferenceRepository repository;
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
                try { task.call(); } catch (Exception e) { e.printStackTrace(); }
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
        repository = new PowerPreferenceRepository(context, db, synchronousExecutor);
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
        item.status = "PENDING";
        item.timestamp = System.currentTimeMillis();

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
}
