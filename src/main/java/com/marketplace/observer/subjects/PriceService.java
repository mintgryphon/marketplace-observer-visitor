package com.marketplace.observer.subjects;

import com.marketplace.common.Money;
import com.marketplace.observer.events.PriceChangedEvent;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PriceService extends AbstractSubject<PriceChangedEvent> {
    private final Map<String, Money> priceStore = new ConcurrentHashMap<>();

    public void updatePrice(String productId, String categoryId, Money newPrice) {
        Money oldPrice = priceStore.put(productId, newPrice);
        if (oldPrice == null) {
            oldPrice = newPrice;
        }
        PriceChangedEvent event = new PriceChangedEvent(
                productId,
                categoryId,
                oldPrice,
                newPrice,
                Instant.now()
        );
        notifyObservers(event);
    }

    public Money getCurrentPrice(String productId) {
        return priceStore.getOrDefault(productId, Money.ZERO);
    }
}