package com.marketplace.observer;

import com.marketplace.common.Money;
import com.marketplace.observer.api.EventType;
import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.PriceChangedEvent;
import com.marketplace.observer.subjects.PriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicSubscriptionTest {
    private PriceService priceService;

    @BeforeEach
    void setUp() {
        priceService = new PriceService();
    }

    @Test
    void shouldUnsubscribeSelfFromInsideEventHandler() {
        AtomicInteger callCount = new AtomicInteger(0);

        Observer<PriceChangedEvent> selfUnsubscribingObserver = new Observer<>() {
            @Override
            public void onEvent(PriceChangedEvent event) {
                callCount.incrementAndGet();
                priceService.unsubscribe(EventType.PRICE_CHANGED, this);
            }
        };

        priceService.subscribe(EventType.PRICE_CHANGED, selfUnsubscribingObserver);

        priceService.updatePrice("PROD-1", "CAT-1", Money.of(100.0));
        priceService.updatePrice("PROD-1", "CAT-1", Money.of(90.0));
        priceService.updatePrice("PROD-1", "CAT-1", Money.of(80.0));

        assertEquals(1, callCount.get());
        assertEquals(0, priceService.getListenerCount(EventType.PRICE_CHANGED));
    }

    @Test
    void shouldReceiveEventsOnlyAfterRuntimeSubscription() {
        AtomicInteger callCount = new AtomicInteger(0);
        Observer<PriceChangedEvent> lateListener = event -> callCount.incrementAndGet();

        priceService.updatePrice("PROD-1", "CAT-1", Money.of(100.0));
        assertEquals(0, callCount.get());

        priceService.subscribe(EventType.PRICE_CHANGED, lateListener);
        priceService.updatePrice("PROD-1", "CAT-1", Money.of(90.0));
        assertEquals(1, callCount.get());
    }

    @Test
    void shouldStopReceivingEventsAfterStandardUnsubscribe() {
        AtomicInteger callCount = new AtomicInteger(0);
        Observer<PriceChangedEvent> listener = event -> callCount.incrementAndGet();

        priceService.subscribe(EventType.PRICE_CHANGED, listener);
        priceService.updatePrice("PROD-1", "CAT-1", Money.of(100.0));
        priceService.unsubscribe(EventType.PRICE_CHANGED, listener);
        priceService.updatePrice("PROD-1", "CAT-1", Money.of(90.0));

        assertEquals(1, callCount.get());
    }

    @Test
    void shouldGracefullyHandleNullEvents() {
        assertEquals(0, priceService.getListenerCount(EventType.PRICE_CHANGED));
        priceService.notifyObservers(null);
        assertEquals(0, priceService.getListenerCount(EventType.PRICE_CHANGED));
    }
}