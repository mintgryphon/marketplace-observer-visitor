package com.marketplace.observer.listeners;

import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.StockChangedEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Слушатель 4: Фиксирует инциденты обнуления остатков на складе.
 */
public class InventoryAuditor implements Observer<StockChangedEvent> {
    private final AtomicInteger outOfStockAlerts = new AtomicInteger(0);

    @Override
    public void onEvent(StockChangedEvent event) {
        if (event.isOutOfStock()) {
            outOfStockAlerts.incrementAndGet();
        }
    }

    public int getOutOfStockAlerts() {
        return outOfStockAlerts.get();
    }
}