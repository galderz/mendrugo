package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiThread
{
    static final List<byte[]> leak = new ArrayList<>();

    public static void main(String[] args)
    {
        int numThreads = 11;

        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        final List<Future<?>> results = new ArrayList<>();
        results.add(executor.submit(new AvailableMemoryReporter()));
        for (int i = 0; i < numThreads - 1; i++)
        {
            results.add(executor.submit(new OutOfMemoryProducer()));
        }

        // Wait for all
        for (Future<?> result : results)
        {
            try
            {
                result.get();
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static final class OutOfMemoryProducer implements Runnable
    {
        @Override
        public void run()
        {
            while (true)
            {
                byte[] b = new byte[10 * 1024 * 1024]; // 10MB byte object
                leak.add(b);
            }
        }
    }

    private static final class AvailableMemoryReporter implements Runnable
    {
        @Override
        public void run()
        {
            int index = 1;
            Runtime rt = Runtime.getRuntime();
            System.out.printf("[%3s] Available heap memory: %s%n", index++, rt.freeMemory());
        }
    }
}
