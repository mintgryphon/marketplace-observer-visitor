package com.marketplace.integration;

import com.marketplace.common.Money;
import com.marketplace.observer.api.EventType;
import com.marketplace.observer.events.InspectionReportEvent;
import com.marketplace.observer.listeners.PriceDropAlertService;
import com.marketplace.observer.listeners.PromotionTicketDesk;
import com.marketplace.observer.subjects.PriceService;
import com.marketplace.visitor.elements.*;
import com.marketplace.visitor.visitors.InventoryReport;
import com.marketplace.visitor.visitors.InventoryValueVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** alert (PriceChangedEvent) -> inspection (visitor on subtree) -> ticket (InspectionReportEvent). */
class InspectionReportFlowTest {
    private PriceService priceService;
    private PromotionTicketDesk ticketDesk;

    @BeforeEach
    void setUp() {
        Catalog catalog = new Catalog("ROOT", "Store");
        Category gaming = new Category("CAT-1", "Gaming");
        gaming.addChild(new PhysicalProduct("P-GAME", "Console", Money.of(500.0), 3.0, 10));
        Category consoles = new Category("CAT-1-1", "Retro Consoles");       // nested category
        consoles.addChild(new PhysicalProduct("P-RETRO", "Retro Console", Money.of(80.0), 1.0, 4));
        gaming.addChild(consoles);
        Category books = new Category("CAT-2", "Books");
        books.addChild(new DigitalProduct("P-BOOK", "E-Book", Money.of(9.0), 2.0));
        catalog.addCategory(gaming);
        catalog.addCategory(books);

        priceService = new PriceService();
        PriceDropAlertService alertService = new PriceDropAlertService(catalog, new InventoryValueVisitor(), 20.0);
        ticketDesk = new PromotionTicketDesk();
        priceService.subscribe(EventType.PRICE_CHANGED, alertService);
        alertService.subscribe(EventType.INSPECTION_REPORT, ticketDesk);
    }

    @Test
    void significantDropPublishesReportForAffectedSubtreeOnly() {
        priceService.updatePrice("P-BOOK", "CAT-2", Money.of(10.0));
        priceService.updatePrice("P-BOOK", "CAT-2", Money.of(5.0));   // 50% drop

        List<InspectionReportEvent> tickets = ticketDesk.getOpenTickets();
        assertEquals(1, tickets.size());
        InspectionReportEvent ticket = tickets.get(0);
        assertEquals("CAT-2", ticket.categoryId());
        assertEquals(50.0, ticket.discountPercent(), 1e-9);
        // only the Books subtree: 1 e-book worth 9.00 (Gaming is not inspected)
        assertEquals(new InventoryReport(1, Money.of(9.0)), ticket.report());
    }

    @Test
    void nestedCategoryIsFoundAndInspected() {
        priceService.updatePrice("P-RETRO", "CAT-1-1", Money.of(100.0));
        priceService.updatePrice("P-RETRO", "CAT-1-1", Money.of(70.0));  // 30% drop

        List<InspectionReportEvent> tickets = ticketDesk.getOpenTickets();
        assertEquals(1, tickets.size());
        // hand-calculated: 4 units * 80.00 = 320.00
        assertEquals(new InventoryReport(4, Money.of(320.0)), tickets.get(0).report());
    }

    @Test
    void smallDropOrUnknownCategoryCreatesNoTicket() {
        priceService.updatePrice("P-GAME", "CAT-1", Money.of(500.0));
        priceService.updatePrice("P-GAME", "CAT-1", Money.of(450.0));   // only 10%
        priceService.updatePrice("P-X", "CAT-404", Money.of(100.0));
        priceService.updatePrice("P-X", "CAT-404", Money.of(10.0));     // 90%, but no such category

        assertTrue(ticketDesk.getOpenTickets().isEmpty());
    }
}
