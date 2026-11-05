package com.marketplace.observer;

import com.marketplace.common.Money;
import com.marketplace.observer.api.EventType;
import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.PriceChangedEvent;
import com.marketplace.observer.subjects.PriceService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObserverRobustnessTest {

    @Test
    void failingListenerDoesNotStopDeliveryToOthers() {
        PriceService priceService = new PriceService();
        AtomicInteger healthyCalls = new AtomicInteger(0);
        Observer<PriceChangedEvent> broken = event -> {
            throw new IllegalStateException("listener bug");
        };

        priceService.subscribe(EventType.PRICE_CHANGED, broken);
        priceService.subscribe(EventType.PRICE_CHANGED, event -> healthyCalls.incrementAndGet());

        priceService.updatePrice("P1", "C1", Money.of(10.0));
        priceService.updatePrice("P1", "C1", Money.of(9.0));

        assertEquals(2, healthyCalls.get());
        assertEquals(2, priceService.getFailedDeliveries());
    }

    @Test
    void subscribingToEventTypeTheSubjectNeverPublishesIsRejected() {
        PriceService priceService = new PriceService();

        assertThrows(IllegalArgumentException.class,
                () -> priceService.subscribe(EventType.STOCK_CHANGED, event -> { }));
    }
}
