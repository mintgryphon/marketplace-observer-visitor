package com.marketplace.visitor;

import com.marketplace.common.Money;
import com.marketplace.visitor.elements.*;
import com.marketplace.visitor.visitors.InventoryReport;
import com.marketplace.visitor.visitors.InventoryValueVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryValueVisitorTest {
    private Catalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new Catalog("ROOT", "Catalog");
        Category category = new Category("CAT-1", "Devices and Services");

        category.addChild(new PhysicalProduct("P1", "Laptop", Money.of(100.0), 2.0, 5));
        category.addChild(new DigitalProduct("P2", "E-Book", Money.of(50.0), 500.0));
        category.addChild(new ServiceProduct("P3", "Installation", Money.of(30.0), 3));
        category.addChild(new SubscriptionProduct("P4", "Premium Plan", Money.of(10.0), 12));

        catalog.addCategory(category);
    }

    @Test
    void shouldCalculateExactInventoryValuationHandCalculated() {
        InventoryValueVisitor visitor = new InventoryValueVisitor();
        InventoryReport report = catalog.accept(visitor);

        assertEquals(Money.of(760.0), report.totalValue());
        assertEquals(8, report.totalItemsCount());
    }
}