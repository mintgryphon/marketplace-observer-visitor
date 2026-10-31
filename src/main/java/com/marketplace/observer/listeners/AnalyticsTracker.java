package com.marketplace.observer.listeners;

import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.PriceChangedEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Слушатель 3: Подсчитывает общую аналитику по событиям цен.
 */
public class AnalyticsTracker implements Observer<PriceChangedEvent> {
    private final AtomicInteger totalEventsRecorded = new AtomicInteger(0);

    @Override
    public void onEvent(PriceChangedEvent event) {
        totalEventsRecorded.incrementAndGet();
    }

    public int getTotalEventsRecorded() {
        return totalEventsRecorded.get();
    }
}