package com.marketplace.observer;

import com.marketplace.common.Money;
import com.marketplace.observer.api.EventType;
import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.PriceChangedEvent;
import com.marketplace.observer.subjects.PriceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Listeners join and leave while 4 threads are publishing.
 * The stable listener must still see exactly every event, and nothing may throw.
 */
class ConcurrentSubscriptionTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void subscribeAndUnsubscribeDuringConcurrentPublishingIsSafe() throws Exception {
        int publishers = 4;
        int eventsPerPublisher = 1000;
        PriceService priceService = new PriceService();
        AtomicInteger stableCount = new AtomicInteger(0);
        priceService.subscribe(EventType.PRICE_CHANGED, event -> stableCount.incrementAndGet());

        ExecutorService pool = Executors.newFixedThreadPool(publishers + 1);
        AtomicBoolean publishing = new AtomicBoolean(true);

        Future<?> churn = pool.submit(() -> {
            while (publishing.get()) {
                Observer<PriceChangedEvent> temporary = event -> { };
                priceService.subscribe(EventType.PRICE_CHANGED, temporary);
                priceService.unsubscribe(EventType.PRICE_CHANGED, temporary);
            }
        });

        List<Future<?>> producers = new ArrayList<>();
        for (int t = 0; t < publishers; t++) {
            final int threadId = t;
            producers.add(pool.submit(() -> {
                for (int i = 0; i < eventsPerPublisher; i++) {
                    priceService.updatePrice("P-" + threadId + "-" + i, "C1", Money.of(1.0 + i));
                }
            }));
        }
        for (Future<?> producer : producers) {
            producer.get(); // rethrows any exception from the publisher thread
        }
        publishing.set(false);
        churn.get();

        pool.shutdown();
        assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS));
        assertEquals(publishers * eventsPerPublisher, stableCount.get());
        assertEquals(1, priceService.getListenerCount(EventType.PRICE_CHANGED));
        assertEquals(0, priceService.getFailedDeliveries());
    }
}
