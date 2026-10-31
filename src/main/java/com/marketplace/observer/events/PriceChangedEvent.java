package com.marketplace.observer.events;

import com.marketplace.common.Money;
import com.marketplace.observer.api.Event;
import com.marketplace.observer.api.EventType;

import java.time.Instant;
import java.util.Objects;

public record PriceChangedEvent(
        String productId,
        String categoryId,
        Money oldPrice,
        Money newPrice,
        Instant timestamp
) implements Event {
    public PriceChangedEvent {
        Objects.requireNonNull(productId, "productId cannot be null");
        Objects.requireNonNull(categoryId, "categoryId cannot be null");
        Objects.requireNonNull(oldPrice, "oldPrice cannot be null");
        Objects.requireNonNull(newPrice, "newPrice cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
    }

    @Override
    public EventType eventType() {
        return EventType.PRICE_CHANGED;
    }

    public double getDiscountPercentage() {
        if (oldPrice.compareTo(Money.ZERO) <= 0) return 0.0;
        if (newPrice.compareTo(oldPrice) >= 0) return 0.0;
        double diff = oldPrice.getAmount().doubleValue() - newPrice.getAmount().doubleValue();
        return (diff / oldPrice.getAmount().doubleValue()) * 100.0;
    }
}