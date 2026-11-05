package com.marketplace.observer.producer;

import com.marketplace.common.Money;
import com.marketplace.observer.subjects.InventoryService;
import com.marketplace.observer.subjects.PriceService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Simulated market activity for one producer thread: alternates price and stock updates
 * over a fixed set of products. Each product is published with its real category id.
 */
public class CatalogEventProducer implements Runnable {
    private final PriceService priceService;
    private final InventoryService inventoryService;
    private final List<Map.Entry<String, String>> productsWithCategory;
    private final int eventsToGenerate;

    public CatalogEventProducer(PriceService priceService,
                                InventoryService inventoryService,
                                Map<String, String> categoryIdByProductId,
                                int eventsToGenerate) {
        this.priceService = Objects.requireNonNull(priceService);
        this.inventoryService = Objects.requireNonNull(inventoryService);
        this.productsWithCategory = List.copyOf(new TreeMap<>(categoryIdByProductId).entrySet());
        if (productsWithCategory.isEmpty()) {
            throw new IllegalArgumentException("at least one product is required");
        }
        this.eventsToGenerate = eventsToGenerate;
    }

    @Override
    public void run() {
        for (int i = 1; i <= eventsToGenerate && !Thread.currentThread().isInterrupted(); i++) {
            Map.Entry<String, String> product = productsWithCategory.get(i % productsWithCategory.size());
            if (isPriceUpdate(i)) {
                priceService.updatePrice(product.getKey(), product.getValue(), Money.of(50.0 + (i % 10) * 5));
            } else {
                inventoryService.updateStock(product.getKey(), product.getValue(), 10 + (i % 3));
            }
        }
    }

    /** Even steps publish a price change, odd steps a stock change. */
    private static boolean isPriceUpdate(int step) {
        return step % 2 == 0;
    }

    /** Number of price events one producer publishes; used to verify that no event was lost. */
    public static int priceUpdatesFor(int eventsToGenerate) {
        return eventsToGenerate / 2;
    }
}
