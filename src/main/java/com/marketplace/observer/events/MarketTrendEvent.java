package com.marketplace.observer.events;

import com.marketplace.observer.api.Event;
import com.marketplace.observer.api.EventType;

import java.time.Instant;
import java.util.Objects;

public record MarketTrendEvent(
        int totalEventsAnalyzed,
        int totalPriceDrops,
        double averageDiscountPercent,
        Instant timestamp
) implements Event {
    public MarketTrendEvent {
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
    }

    @Override
    public EventType eventType() {
        return EventType.MARKET_TREND;
    }
}