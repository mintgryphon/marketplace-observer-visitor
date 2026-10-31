package com.marketplace.observer.producer;

import com.marketplace.common.Money;
import com.marketplace.observer.subjects.InventoryService;
import com.marketplace.observer.subjects.PriceService;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class CatalogEventProducer implements Runnable {
    private final String producerId;
    private final PriceService priceService;
    private final InventoryService inventoryService;
    private final int eventsToGenerate;
    private final CountDownLatch completionLatch;

    public CatalogEventProducer(String producerId,
                                PriceService priceService,
                                InventoryService inventoryService,
                                int eventsToGenerate,
                                CountDownLatch completionLatch) {
        this.producerId = Objects.requireNonNull(producerId);
        this.priceService = Objects.requireNonNull(priceService);
        this.inventoryService = Objects.requireNonNull(inventoryService);
        this.eventsToGenerate = eventsToGenerate;
        this.completionLatch = completionLatch;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= eventsToGenerate; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                String productId = "PROD-" + (i % 5);
                String categoryId = "CAT-1";


                if (i % 2 == 0) {
                    priceService.updatePrice(productId, categoryId, Money.of(50.0 + (i % 10) * 5));
                } else {
                    inventoryService.updateStock(productId, categoryId, 10 + (i % 3));
                }
            }
        } finally {
            if (completionLatch != null) {
                completionLatch.countDown();
            }
        }
    }
}