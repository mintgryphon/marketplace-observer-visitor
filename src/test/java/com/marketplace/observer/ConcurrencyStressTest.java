package com.marketplace.observer;

import com.marketplace.common.Money;
import com.marketplace.observer.api.EventType;
import com.marketplace.observer.events.PriceChangedEvent;
import com.marketplace.observer.subjects.PriceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrencyStressTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void shouldProcessExactEventCountUnderConcurrentLoad() throws InterruptedException {
        int threads = 4;
        int eventsPerThread = 1000;
        int expectedTotalEvents = threads * eventsPerThread;

        PriceService priceService = new PriceService();
        AtomicInteger actualEventCount = new AtomicInteger(0);

        priceService.subscribe(EventType.PRICE_CHANGED, (PriceChangedEvent event) -> actualEventCount.incrementAndGet());

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch finishSignal = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            pool.submit(() -> {
                try {
                    startSignal.await();
                    for (int i = 0; i < eventsPerThread; i++) {
                        priceService.updatePrice(
                                "PROD-" + threadId + "-" + i,
                                "CAT-STRESS",
                                Money.of(10.0 + (i % 50))
                        );
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishSignal.countDown();
                }
            });
        }

        startSignal.countDown();
        boolean finishedInTime = finishSignal.await(5, TimeUnit.SECONDS);
        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);

        assertEquals(true, finishedInTime);
        assertEquals(expectedTotalEvents, actualEventCount.get());
    }
}