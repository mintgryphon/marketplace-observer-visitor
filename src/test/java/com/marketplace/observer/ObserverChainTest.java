package com.marketplace.observer;

import com.marketplace.common.Money;
import com.marketplace.observer.api.EventType;
import com.marketplace.observer.events.MarketTrendEvent;
import com.marketplace.observer.subjects.MarketplaceAggregator;
import com.marketplace.observer.subjects.PriceService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ObserverChainTest {

    @Test
    void shouldAggregateRawEventsAndPublishTrendEvent() {
        PriceService priceService = new PriceService();
        MarketplaceAggregator aggregator = new MarketplaceAggregator(3);

        AtomicInteger trendEventsReceived = new AtomicInteger(0);
        AtomicReference<MarketTrendEvent> lastTrend = new AtomicReference<>();

        aggregator.subscribe(EventType.MARKET_TREND, event -> {
            trendEventsReceived.incrementAndGet();
            lastTrend.set(event);
        });

        priceService.subscribe(EventType.PRICE_CHANGED, aggregator);

        priceService.updatePrice("P1", "C1", Money.of(100.0));
        priceService.updatePrice("P1", "C1", Money.of(80.0));
        priceService.updatePrice("P2", "C1", Money.of(200.0));
        priceService.updatePrice("P2", "C1", Money.of(100.0));
        priceService.updatePrice("P3", "C1", Money.of(50.0));
        priceService.updatePrice("P3", "C1", Money.of(40.0));

        assertEquals(2, trendEventsReceived.get());
        assertNotNull(lastTrend.get());
        assertEquals(6, lastTrend.get().totalEventsAnalyzed());
        // hand-calculated: drops 100->80 (20%), 200->100 (50%), 50->40 (20%) => 3 drops, avg 30%
        assertEquals(3, lastTrend.get().totalPriceDrops());
        assertEquals(30.0, lastTrend.get().averageDiscountPercent(), 1e-9);
    }

    @Test
    void shouldCountExactEventsInChain() {
        PriceService priceService = new PriceService();
        MarketplaceAggregator aggregator = new MarketplaceAggregator(10);
        priceService.subscribe(EventType.PRICE_CHANGED, aggregator);

        for (int i = 0; i < 7; i++) {
            priceService.updatePrice("P" + i, "C1", Money.of(10.0 + i));
        }

        assertEquals(7, aggregator.getTotalEventsProcessed());
    }
}