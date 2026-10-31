package com.marketplace.observer.events;

import com.marketplace.observer.api.Event;
import com.marketplace.observer.api.EventType;

import java.time.Instant;
import java.util.Objects;

public record StockChangedEvent(
        String productId,
        String categoryId,
        int oldStock,
        int newStock,
        Instant timestamp
) implements Event {
    public StockChangedEvent {
        Objects.requireNonNull(productId, "productId cannot be null");
        Objects.requireNonNull(categoryId, "categoryId cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
    }

    @Override
    public EventType eventType() {
        return EventType.STOCK_CHANGED;
    }

    public boolean isOutOfStock() {
        return newStock <= 0;
    }
}