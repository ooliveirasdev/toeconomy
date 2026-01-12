package com.toplugins.toeconomy;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DatabaseWorker {
    private final ExecutorService executor;

    public DatabaseWorker() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ToEconomy-Database");
            t.setDaemon(true);
            return t;
        });
    }

    public <T> T call(Callable<T> task) throws Exception {
        return task.call();
    }

    public void run(Runnable task) {
        executor.execute(task);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
