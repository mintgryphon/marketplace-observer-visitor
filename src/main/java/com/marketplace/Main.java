package com.marketplace;

import com.marketplace.common.Money;
import com.marketplace.observer.api.EventType;
import com.marketplace.observer.listeners.*;
import com.marketplace.observer.producer.CatalogEventProducer;
import com.marketplace.observer.subjects.InventoryService;
import com.marketplace.observer.subjects.MarketplaceAggregator;
import com.marketplace.observer.subjects.PriceService;
import com.marketplace.visitor.elements.*;
import com.marketplace.visitor.visitors.InventoryValueVisitor;
import com.marketplace.visitor.visitors.ShippingCostVisitor;
import com.marketplace.visitor.visitors.XmlExportVisitor;

import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== INITIALIZING ONLINE MARKETPLACE SYSTEM ===");


        Catalog catalog = new Catalog("ROOT_CATALOG", "MegaMarket");

        Category electronics = new Category("CAT-1", "Consumer Electronics");
        electronics.addChild(new PhysicalProduct("PROD-0", "Mechanical Keyboard", Money.of(120.0), 1.5, 50));
        electronics.addChild(new DigitalProduct("PROD-1", "IDE Ultimate License", Money.of(200.0), 450.0));
        electronics.addChild(new ServiceProduct("PROD-2", "PC Assembly Service", Money.of(40.0), 3));
        electronics.addChild(new SubscriptionProduct("PROD-3", "Cloud Storage 1TB", Money.of(10.0), 12));

        Category accessories = new Category("CAT-2", "Accessories");
        accessories.addChild(new PhysicalProduct("PROD-4", "USB-C Cable", Money.of(15.0), 0.1, 150));

        catalog.addCategory(electronics);
        catalog.addCategory(accessories);


        System.out.println("\n--- VISITOR OPERATIONS ---");
        XmlExportVisitor xmlVisitor = new XmlExportVisitor();
        System.out.println("Catalog XML Export preview:\n" + catalog.accept(xmlVisitor));

        ShippingCostVisitor shippingVisitor = new ShippingCostVisitor();
        System.out.println("Total Shipping Cost for Electronics: " + electronics.accept(shippingVisitor));

        InventoryValueVisitor valueVisitor = new InventoryValueVisitor();
        System.out.println("Total Catalog Valuation: " + catalog.accept(valueVisitor).totalValue());

        PriceService priceService = new PriceService();
        InventoryService inventoryService = new InventoryService();
        MarketplaceAggregator aggregator = new MarketplaceAggregator(5); // Агрегирует каждые 5 событий

        WishlistNotifier wishlistNotifier = new WishlistNotifier();
        wishlistNotifier.watch("PROD-0");
        wishlistNotifier.watch("PROD-1");

        SearchCacheInvalidator cacheInvalidator = new SearchCacheInvalidator();
        AnalyticsTracker analyticsTracker = new AnalyticsTracker();
        InventoryAuditor inventoryAuditor = new InventoryAuditor();
        PriceDropAlertService alertService = new PriceDropAlertService(catalog, 20.0); // Алерт при скидке >= 20%

        priceService.subscribe(EventType.PRICE_CHANGED, wishlistNotifier);
        priceService.subscribe(EventType.PRICE_CHANGED, cacheInvalidator);
        priceService.subscribe(EventType.PRICE_CHANGED, analyticsTracker);
        priceService.subscribe(EventType.PRICE_CHANGED, alertService);
        priceService.subscribe(EventType.PRICE_CHANGED, aggregator); // Цепочка: PriceService -> Aggregator

        inventoryService.subscribe(EventType.STOCK_CHANGED, cacheInvalidator);
        inventoryService.subscribe(EventType.STOCK_CHANGED, inventoryAuditor);

        System.out.println("\n--- RUNNING MULTITHREADED PRODUCERS ---");
        int threadPoolSize = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        int eventsPerProducer = 10;
        CountDownLatch latch = new CountDownLatch(threadPoolSize);

        priceService.updatePrice("PROD-0", "CAT-1", Money.of(150.0));
        priceService.updatePrice("PROD-1", "CAT-1", Money.of(250.0));

        for (int i = 1; i <= threadPoolSize; i++) {
            executorService.submit(new CatalogEventProducer(
                    "Producer-" + i,
                    priceService,
                    inventoryService,
                    eventsPerProducer,
                    latch
            ));
        }

        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            System.out.println("All producer tasks finished: " + completed);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Execution interrupted");
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\n--- EXECUTION SUMMARY ---");
        System.out.println("Analytics Events Recorded: " + analyticsTracker.getTotalEventsRecorded());
        System.out.println("Cache Invalidation Calls: " + cacheInvalidator.getInvalidationCount());
        System.out.println("Wishlist Price-Drop Alerts: " + wishlistNotifier.getNotificationsSent());
        System.out.println("Deep Category Inspections Triggered: " + alertService.getInspectionsTriggered());
        System.out.println("Aggregator Processed: " + aggregator.getTotalEventsProcessed() + " events");
        System.out.println("System gracefully stopped without errors.");
    }
}