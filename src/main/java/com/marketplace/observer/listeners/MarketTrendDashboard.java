package com.marketplace.observer.listeners;

import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.MarketTrendEvent;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Listens to the aggregated trend published by {@code MarketplaceAggregator} (second link of the chain). */
public class MarketTrendDashboard implements Observer<MarketTrendEvent> {
    private final AtomicReference<MarketTrendEvent> latestTrend = new AtomicReference<>();
    private final AtomicInteger trendsReceived = new AtomicInteger(0);

    @Override
    public void onEvent(MarketTrendEvent trend) {
        latestTrend.set(trend);
        trendsReceived.incrementAndGet();
    }

    public Optional<MarketTrendEvent> getLatestTrend() {
        return Optional.ofNullable(latestTrend.get());
    }

    public int getTrendsReceived() {
        return trendsReceived.get();
    }
}
