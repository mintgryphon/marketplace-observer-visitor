package com.marketplace.observer.subjects;

import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.MarketTrendEvent;
import com.marketplace.observer.events.PriceChangedEvent;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public class MarketplaceAggregator extends AbstractSubject<MarketTrendEvent> implements Observer<PriceChangedEvent> {
    private final AtomicInteger totalEvents = new AtomicInteger(0);
    private final AtomicInteger totalDrops = new AtomicInteger(0);
    private final DoubleAdder accumulatedDiscount = new DoubleAdder();
    private final int publishThreshold;

    public MarketplaceAggregator(int publishThreshold) {
        this.publishThreshold = publishThreshold;
    }

    @Override
    public void onEvent(PriceChangedEvent event) {
        int count = totalEvents.incrementAndGet();
        double discount = event.getDiscountPercentage();

        if (discount > 0.0) {
            totalDrops.incrementAndGet();
            accumulatedDiscount.add(discount);
        }

        if (count % publishThreshold == 0) {
            int drops = totalDrops.get();
            double avg = drops == 0 ? 0.0 : accumulatedDiscount.sum() / drops;
            MarketTrendEvent trendEvent = new MarketTrendEvent(
                    count,
                    drops,
                    avg,
                    Instant.now()
            );
            notifyObservers(trendEvent);
        }
    }

    public int getTotalEventsProcessed() {
        return totalEvents.get();
    }
}