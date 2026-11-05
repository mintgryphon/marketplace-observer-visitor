package com.marketplace.visitor;

import com.marketplace.common.Money;
import com.marketplace.visitor.elements.*;
import com.marketplace.visitor.visitors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisitorReuseTest {
    private Catalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new Catalog("ROOT", "Catalog");
        Category category = new Category("CAT-1", "Mixed");
        category.addChild(new PhysicalProduct("P1", "Laptop", Money.of(100.0), 2.0, 5));
        category.addChild(new GiftCardProduct("G1", "Gift Card", Money.of(25.0)));
        catalog.addCategory(category);
    }

    @Test
    void sameVisitorInstanceGivesSameResultWhenReused() {
        InventoryValueVisitor inventory = new InventoryValueVisitor();
        ProductFilterVisitor filter = new ProductFilterVisitor(Money.of(30.0));
        XmlExportVisitor xml = new XmlExportVisitor();
        PriceTagFormattingVisitor tags = new PriceTagFormattingVisitor();

        assertEquals(catalog.accept(inventory), catalog.accept(inventory));
        assertEquals(catalog.accept(filter), catalog.accept(filter));
        assertEquals(catalog.accept(xml), catalog.accept(xml));
        assertEquals(catalog.accept(tags), catalog.accept(tags));
        assertEquals(new InventoryReport(6, Money.of(525.0)), catalog.accept(inventory));
    }

    @Test
    void visitorRunsOnNestedSubtreeOnly() {
        // Catalog -> Category -> Category -> product : nested containers are handled by recursion
        Category outer = new Category("CAT-2", "Outer");
        Category inner = new Category("CAT-2-1", "Inner");
        inner.addChild(new DigitalProduct("D1", "E-Book", Money.of(12.5), 3.0));
        outer.addChild(inner);
        outer.addChild(new ServiceProduct("S1", "Setup", Money.of(20.0), 2));
        catalog.addCategory(outer);

        // hand-calculated: 12.50 + 20 * 2 = 52.50, 2 items
        assertEquals(new InventoryReport(2, Money.of(52.5)), outer.accept(new InventoryValueVisitor()));
        // whole catalog: 525 + 52.50 = 577.50, 8 items
        assertEquals(new InventoryReport(8, Money.of(577.5)), catalog.accept(new InventoryValueVisitor()));
    }
}
