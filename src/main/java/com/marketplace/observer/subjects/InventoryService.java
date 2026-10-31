package com.marketplace.observer.subjects;

import com.marketplace.observer.events.StockChangedEvent;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryService extends AbstractSubject<StockChangedEvent> {
    private final Map<String, Integer> stockStore = new ConcurrentHashMap<>();

    public void updateStock(String productId, String categoryId, int newStock) {
        Integer oldStock = stockStore.put(productId, newStock);
        if (oldStock == null) {
            oldStock = newStock;
        }
        StockChangedEvent event = new StockChangedEvent(
                productId,
                categoryId,
                oldStock,
                newStock,
                Instant.now()
        );
        notifyObservers(event);
    }

    public int getStock(String productId) {
        return stockStore.getOrDefault(productId, 0);
    }
}