package org.example;

import java.io.Serializable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class InstanceOfScalability
{
    private static Object intNum = Integer.valueOf(42);
    private static Object longNum = Long.valueOf(42L);

    public static void main(String[] args) throws Exception {
        final int threads = args.length == 2 ? Integer.valueOf(args[0]) : Runtime.getRuntime().availableProcessors();
        final int durationInSecs = args.length == 2 ? Integer.valueOf(args[1]) : 10;
        ExecutorService executorService = Executors.newCachedThreadPool();
        final CyclicBarrier started = new CyclicBarrier(threads + 1);
        final AtomicBoolean stop = new AtomicBoolean();
        final Future<Long>[] results = new Future[threads];
        for (int i = 0; i < threads; i++) {
            results[i] = executorService.submit(() -> checkTask(started, stop));
        }
        executorService.shutdown();
        started.await();
        TimeUnit.SECONDS.sleep(durationInSecs);
        stop.set(true);
        long total = 0;
        for (Future<Long> count : results) {
            total += count.get();
        }
        System.out.println("Total elapsed time = " + durationInSecs + " secs. Total checks = " + total);
    }

    private static long checkTask(CyclicBarrier started, AtomicBoolean stop) {
        long checks = 0;
        try {
            started.await();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            return 0;
        }
        while (!stop.get()) {
            if (!isBothComparableAndSerializable(longNum) ||
                !isBothComparableAndSerializable(intNum)) {
                System.err.println("FINISHING EARLIER: this shouldn't HAPPEN!");
                return checks;
            }
            checks++;
        }
        return checks;
    }

    private static boolean isBothComparableAndSerializable(Object o) {
        if (o instanceof Comparable && o instanceof Serializable) {
            return true;
        }
        return false;
    }
}
