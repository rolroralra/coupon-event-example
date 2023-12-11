package com.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class ExecutorTest {

    public static void testWithMultipleThreads(Consumer<Integer> consumer, int threadCount)
        throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int threadNumber = i;

            executorService.execute(() -> {
                try {
                    consumer.accept(threadNumber);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
    }

}
