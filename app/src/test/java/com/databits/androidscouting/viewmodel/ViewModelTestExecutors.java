package com.databits.androidscouting.viewmodel;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

final class ViewModelTestExecutors {
    private ViewModelTestExecutors() {
    }

    static ExecutorService immediate() {
        return new ExecutorService() {
            @Override public void shutdown() {}
            @Override public List<Runnable> shutdownNow() { return null; }
            @Override public boolean isShutdown() { return false; }
            @Override public boolean isTerminated() { return false; }
            @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return true; }
            @Override public <T> Future<T> submit(Callable<T> task) { try { task.call(); } catch (Exception e) { throw new RuntimeException(e); } return null; }
            @Override public <T> Future<T> submit(Runnable task, T result) { task.run(); return null; }
            @Override public Future<?> submit(Runnable task) { task.run(); return null; }
            @Override public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) { return null; }
            @Override public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) { return null; }
            @Override public <T> T invokeAny(Collection<? extends Callable<T>> tasks) { return null; }
            @Override public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) { return null; }
            @Override public void execute(Runnable command) { command.run(); }
        };
    }
}
