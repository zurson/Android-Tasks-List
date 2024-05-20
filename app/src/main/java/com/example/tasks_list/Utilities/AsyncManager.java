package com.example.tasks_list.Utilities;

import android.os.Looper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class AsyncManager {

    public static Thread runAsyncTask(Runnable runnable) {
        if (runnable == null)
            return null;

        Thread thread = new Thread(() -> {
            Looper.prepare();
            runnable.run();
        });

        thread.setDaemon(true);
        thread.start();

        return thread;
    }


    public static <T> CompletableFuture<T> asyncTask(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }

}
