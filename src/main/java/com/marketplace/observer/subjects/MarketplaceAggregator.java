package com.marketplace.observer.subjects;

import com.marketplace.observer.api.EventType;
import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.MarketTrendEvent;
import com.marketplace.observer.events.PriceChangedEvent;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Chain of observers: listens to raw {@link PriceChangedEvent}s and republishes an aggregated
 * {@link MarketTrendEvent} every {@code publishEvery} events.
 * <p>
 * All counters live in one immutable {@link Stats} snapshot swapped atomically with
 * {@code updateAndGet}, so a published trend is always consistent even when several
 * producer threads deliver events at the same time (no lock needed).
 */
public class MarketplaceAggregator extends AbstractSubject<MarketTrendEvent> implements Observer<PriceChangedEvent> {
    private final AtomicReference<Stats> stats = new AtomicReference<>(Stats.EMPTY);
    private final int publishEvery;

    public MarketplaceAggregator(int publishEvery) {
        super(EventType.MARKET_TREND);
        if (publishEvery <= 0) {
            throw new IllegalArgumentException("publishEvery must be positive");
        }
        this.publishEvery = publishEvery;
    }

    @Override
    public void onEvent(PriceChangedEvent event) {
        double discount = event.getDiscountPercentage();
        Stats snapshot = stats.updateAndGet(current -> current.record(discount));

        if (snapshot.events() % publishEvery == 0) {
            notifyObservers(new MarketTrendEvent(
                    snapshot.events(),
                    snapshot.drops(),
                    snapshot.averageDiscount(),
                    Instant.now()));
        }
    }

    public int getTotalEventsProcessed() {
        return stats.get().events();
    }

    private record Stats(int events, int drops, double discountSum) {
        static final Stats EMPTY = new Stats(0, 0, 0.0);

        Stats record(double discount) {
            return discount > 0.0
                    ? new Stats(events + 1, drops + 1, discountSum + discount)
                    : new Stats(events + 1, drops, discountSum);
        }

        double averageDiscount() {
            return drops == 0 ? 0.0 : discountSum / drops;
        }
    }
}
