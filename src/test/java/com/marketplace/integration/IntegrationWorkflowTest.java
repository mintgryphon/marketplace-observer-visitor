package com.marketplace.integration;

import com.marketplace.common.Money;
import com.marketplace.observer.api.EventType;
import com.marketplace.observer.listeners.InventoryAuditor;
import com.marketplace.observer.listeners.PriceDropAlertService;
import com.marketplace.observer.listeners.WishlistNotifier;
import com.marketplace.observer.subjects.InventoryService;
import com.marketplace.observer.subjects.PriceService;
import com.marketplace.visitor.elements.Catalog;
import com.marketplace.visitor.elements.Category;
import com.marketplace.visitor.elements.PhysicalProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegrationWorkflowTest {
    private Catalog catalog;
    private Category category;
    private PriceService priceService;
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        catalog = new Catalog("CATALOG", "Store");
        category = new Category("CAT-1", "Gaming");
        category.addChild(new PhysicalProduct("P-GAME", "Console", Money.of(500.0), 3.0, 10));
        catalog.addCategory(category);

        priceService = new PriceService();
        inventoryService = new InventoryService();
    }

    @Test
    void shouldTriggerVisitorInspectionOnSubtreeWhenSignificantPriceDropOccurs() {
        PriceDropAlertService alertService = new PriceDropAlertService(catalog, 20.0);
        priceService.subscribe(EventType.PRICE_CHANGED, alertService);

        priceService.updatePrice("P-GAME", "CAT-1", Money.of(500.0));
        assertEquals(0, alertService.getInspectionsTriggered());

        priceService.updatePrice("P-GAME", "CAT-1", Money.of(350.0));
        assertEquals(1, alertService.getInspectionsTriggered());

        priceService.updatePrice("P-GAME", "CAT-1", Money.of(340.0));
        assertEquals(1, alertService.getInspectionsTriggered());
    }

    @Test
    void shouldNotifyWishlistOnlyOnPriceDropsForTrackedItems() {
        WishlistNotifier wishlist = new WishlistNotifier();
        wishlist.watch("P-GAME");
        priceService.subscribe(EventType.PRICE_CHANGED, wishlist);

        priceService.updatePrice("P-GAME", "CAT-1", Money.of(500.0));
        priceService.updatePrice("P-GAME", "CAT-1", Money.of(600.0));
        assertEquals(0, wishlist.getNotificationsSent());

        priceService.updatePrice("P-GAME", "CAT-1", Money.of(450.0));
        assertEquals(1, wishlist.getNotificationsSent());
    }

    @Test
    void shouldAlertOnlyWhenStockBecomesZeroOrNegative() {
        InventoryAuditor auditor = new InventoryAuditor();
        inventoryService.subscribe(EventType.STOCK_CHANGED, auditor);

        inventoryService.updateStock("P-GAME", "CAT-1", 10);
        inventoryService.updateStock("P-GAME", "CAT-1", 2);
        assertEquals(0, auditor.getOutOfStockAlerts());

        inventoryService.updateStock("P-GAME", "CAT-1", 0);
        assertEquals(1, auditor.getOutOfStockAlerts());
    }
}