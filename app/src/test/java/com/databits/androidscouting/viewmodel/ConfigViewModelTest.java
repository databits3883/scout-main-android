package com.databits.androidscouting.viewmodel;

import androidx.lifecycle.Observer;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ConfigViewModelTest extends BaseViewModelTest {

    @Mock
    private PreferenceRepository repository;

    @Mock
    private Observer<Boolean> boolObserver;
    
    @Mock
    private Observer<Integer> intObserver;
    
    @Mock
    private Observer<String> stringObserver;

    private ConfigViewModel viewModel;
    private ExecutorService synchronousExecutor;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // simple synchronous executor that runs tasks immediately on the same thread
        synchronousExecutor = new ExecutorService() {
            @Override public void shutdown() {}
            @Override public java.util.List<Runnable> shutdownNow() { return null; }
            @Override public boolean isShutdown() { return false; }
            @Override public boolean isTerminated() { return false; }
            @Override public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) { return true; }
            @Override public <T> java.util.concurrent.Future<T> submit(java.util.concurrent.Callable<T> task) { 
                try {
                    task.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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
    }

    @Test
    public void testInitializationLoadsPreferences() {
        // Arrange
        when(repository.isCameraTorchEnabled()).thenReturn(true);
        when(repository.getCurrentMatch()).thenReturn(5);
        when(repository.getDeviceRole()).thenReturn("Red 1");

        // Act
        viewModel = new ConfigViewModel(repository, synchronousExecutor);
        
        // Observe
        viewModel.getCameraTorch().observeForever(boolObserver);
        viewModel.getCurrentMatch().observeForever(intObserver);
        viewModel.getDeviceRole().observeForever(stringObserver);

        // Assert
        verify(boolObserver).onChanged(true);
        verify(intObserver).onChanged(5);
        verify(stringObserver).onChanged("Red 1");
    }

    @Test
    public void testUpdateCameraTorch() {
        // Arrange
        viewModel = new ConfigViewModel(repository, synchronousExecutor);
        viewModel.getCameraTorch().observeForever(boolObserver);

        // Act
        viewModel.updateCameraTorch(false);

        // Assert
        verify(repository).setCameraTorch(false);
        verify(boolObserver, atLeastOnce()).onChanged(false);
    }

    @Test
    public void testUpdateCurrentMatch() {
        // Arrange
        viewModel = new ConfigViewModel(repository, synchronousExecutor);
        viewModel.getCurrentMatch().observeForever(intObserver);

        // Act
        viewModel.updateCurrentMatch(10);

        // Assert
        verify(repository).setCurrentMatch(10);
        verify(intObserver).onChanged(10);
    }
}
