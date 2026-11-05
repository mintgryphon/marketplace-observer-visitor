package com.marketplace;

import com.marketplace.common.Money;
import com.marketplace.observer.api.EventType;
import com.marketplace.observer.events.InspectionReportEvent;
import com.marketplace.observer.listeners.*;
import com.marketplace.observer.producer.CatalogEventProducer;
import com.marketplace.observer.subjects.InventoryService;
import com.marketplace.observer.subjects.MarketplaceAggregator;
import com.marketplace.observer.subjects.PriceService;
import com.marketplace.visitor.elements.*;
import com.marketplace.visitor.visitors.InventoryValueVisitor;
import com.marketplace.visitor.visitors.ShippingCostVisitor;
import com.marketplace.visitor.visitors.XmlExportVisitor;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Composition root: the only place where objects are created and wired together.
 * Everything else receives its dependencies through constructors.
 * <p>
 * Usage: {@code java com.marketplace.Main [producerThreads] [eventsPerProducer]}
 * (defaults: 3 threads, 10 events each).
 */
public class Main {
    private static final int DEFAULT_PRODUCER_THREADS = 3;
    private static final int DEFAULT_EVENTS_PER_PRODUCER = 10;
    private static final double PRICE_DROP_ALERT_PERCENT = 20.0;
    private static final int TREND_PUBLISH_EVERY = 5;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 10;

    public static void main(String[] args) throws InterruptedException {
        int producerThreads = positiveArg(args, 0, DEFAULT_PRODUCER_THREADS);
        int eventsPerProducer = positiveArg(args, 1, DEFAULT_EVENTS_PER_PRODUCER);

        System.out.println("=== ONLINE MARKETPLACE: " + producerThreads + " producer threads ===");

        // ---------- Visitor side: stable catalog tree ----------
        Catalog catalog = new Catalog("ROOT_CATALOG", "MegaMarket");

        Category electronics = new Category("CAT-1", "Consumer Electronics");
        electronics.addChild(new PhysicalProduct("PROD-0", "Mechanical Keyboard", Money.of(120.0), 1.5, 50));
        electronics.addChild(new DigitalProduct("PROD-1", "IDE Ultimate License", Money.of(200.0), 450.0));
        electronics.addChild(new ServiceProduct("PROD-2", "PC Assembly Service", Money.of(40.0), 3));
        electronics.addChild(new SubscriptionProduct("PROD-3", "Cloud Storage 1TB", Money.of(10.0), 12));

        Category accessories = new Category("CAT-2", "Accessories");
        accessories.addChild(new PhysicalProduct("PROD-4", "USB-C Cable", Money.of(15.0), 0.1, 150));
        accessories.addChild(new GiftCardProduct("PROD-5", "Gift Card 50", Money.of(50.0)));

        catalog.addCategory(electronics);
        catalog.addCategory(accessories);

        Map<String, String> categoryIdByProductId = Map.of(
                "PROD-0", "CAT-1", "PROD-1", "CAT-1", "PROD-2", "CAT-1",
                "PROD-3", "CAT-1", "PROD-4", "CAT-2", "PROD-5", "CAT-2");

        InventoryValueVisitor inventoryVisitor = new InventoryValueVisitor();

        System.out.println("\n--- VISITOR OPERATIONS ---");
        System.out.print("Catalog XML export:\n" + catalog.accept(new XmlExportVisitor()));
        System.out.println("Shipping cost for Electronics: " + electronics.accept(new ShippingCostVisitor()));
        System.out.println("Total catalog valuation: " + catalog.accept(inventoryVisitor).totalValue());

        // ---------- Observer side: subjects and listeners ----------
        PriceService priceService = new PriceService();
        InventoryService inventoryService = new InventoryService();
        MarketplaceAggregator aggregator = new MarketplaceAggregator(TREND_PUBLISH_EVERY);
        PriceDropAlertService alertService =
                new PriceDropAlertService(catalog, inventoryVisitor, PRICE_DROP_ALERT_PERCENT);

        WishlistNotifier wishlistNotifier = new WishlistNotifier();
        wishlistNotifier.watch("PROD-0");
        wishlistNotifier.watch("PROD-1");
        SearchCacheInvalidator cacheInvalidator = new SearchCacheInvalidator();
        AnalyticsTracker analyticsTracker = new AnalyticsTracker();
        InventoryAuditor inventoryAuditor = new InventoryAuditor();
        MarketTrendDashboard trendDashboard = new MarketTrendDashboard();
        PromotionTicketDesk ticketDesk = new PromotionTicketDesk();

        priceService.subscribe(EventType.PRICE_CHANGED, wishlistNotifier);
        priceService.subscribe(EventType.PRICE_CHANGED, cacheInvalidator);
        priceService.subscribe(EventType.PRICE_CHANGED, analyticsTracker);
        priceService.subscribe(EventType.PRICE_CHANGED, alertService);   // integration: event -> visitor
        priceService.subscribe(EventType.PRICE_CHANGED, aggregator);     // chain: price -> trend
        inventoryService.subscribe(EventType.STOCK_CHANGED, cacheInvalidator);
        inventoryService.subscribe(EventType.STOCK_CHANGED, inventoryAuditor);
        aggregator.subscribe(EventType.MARKET_TREND, trendDashboard);
        alertService.subscribe(EventType.INSPECTION_REPORT, ticketDesk); // visitor result -> new event

        // ---------- Run ----------
        priceService.updatePrice("PROD-0", "CAT-1", Money.of(150.0));
        priceService.updatePrice("PROD-1", "CAT-1", Money.of(250.0));
        int warmUpPriceEvents = 2;

        System.out.println("\n--- RUNNING MULTITHREADED PRODUCERS ---");
        ExecutorService executor = Executors.newFixedThreadPool(producerThreads);
        for (int i = 0; i < producerThreads; i++) {
            executor.submit(new CatalogEventProducer(
                    priceService, inventoryService, categoryIdByProductId, eventsPerProducer));
        }
        boolean finished = shutdownGracefully(executor);

        // ---------- Report ----------
        int expectedPriceEvents = warmUpPriceEvents
                + producerThreads * CatalogEventProducer.priceUpdatesFor(eventsPerProducer);

        System.out.println("\n--- EXECUTION SUMMARY ---");
        System.out.println("Producers finished in time: " + finished);
        System.out.println("Price events expected / delivered: "
                + expectedPriceEvents + " / " + analyticsTracker.getTotalEventsRecorded());
        System.out.println("Cache invalidations: " + cacheInvalidator.getInvalidationCount());
        System.out.println("Wishlist price-drop alerts: " + wishlistNotifier.getNotificationsSent());
        System.out.println("Out-of-stock alerts: " + inventoryAuditor.getOutOfStockAlerts());
        System.out.println("Aggregator processed: " + aggregator.getTotalEventsProcessed()
                + ", trends published: " + trendDashboard.getTrendsReceived());
        System.out.println("Category inspections: " + alertService.getInspectionsTriggered());
        for (InspectionReportEvent ticket : ticketDesk.getOpenTickets()) {
            System.out.printf("  ticket: %s dropped %.1f%% -> category '%s' has %d items worth %s%n",
                    ticket.triggeringProductId(), ticket.discountPercent(), ticket.categoryName(),
                    ticket.report().totalItemsCount(), ticket.report().totalValue());
        }
    }

    /** shutdown() lets submitted tasks finish (no lost events); shutdownNow() only if they hang. */
    private static boolean shutdownGracefully(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        if (executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            return true;
        }
        executor.shutdownNow();
        return false;
    }

    private static int positiveArg(String[] args, int index, int defaultValue) {
        if (args.length <= index) {
            return defaultValue;
        }
        int value = Integer.parseInt(args[index]);
        if (value <= 0) {
            throw new IllegalArgumentException("argument " + index + " must be positive: " + value);
        }
        return value;
    }
}
