package com.marketplace.observer.listeners;

import com.marketplace.observer.api.Event;
import com.marketplace.observer.api.Observer;

import java.util.concurrent.atomic.AtomicInteger;

public class SearchCacheInvalidator implements Observer<Event> {
    private final AtomicInteger invalidationCount = new AtomicInteger(0);

    @Override
    public void onEvent(Event event) {
        invalidationCount.incrementAndGet();
    }

    public int getInvalidationCount() {
        return invalidationCount.get();
    }
}