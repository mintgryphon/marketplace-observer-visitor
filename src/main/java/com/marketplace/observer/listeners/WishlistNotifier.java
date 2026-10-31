package com.marketplace.observer.listeners;

import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.PriceChangedEvent;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WishlistNotifier implements Observer<PriceChangedEvent> {
    private final Set<String> watchedProductIds = ConcurrentHashMap.newKeySet();
    private final AtomicInteger notificationsSent = new AtomicInteger(0);

    public void watch(String productId) {
        watchedProductIds.add(productId);
    }

    @Override
    public void onEvent(PriceChangedEvent event) {
        if (watchedProductIds.contains(event.productId()) && event.newPrice().isLessThan(event.oldPrice())) {
            notificationsSent.incrementAndGet();
        }
    }

    public int getNotificationsSent() {
        return notificationsSent.get();
    }

    public Set<String> getWatchedProductIds() {
        return Collections.unmodifiableSet(watchedProductIds);
    }
}