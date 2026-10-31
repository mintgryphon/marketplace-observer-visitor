package com.marketplace.visitor;

import com.marketplace.common.Money;
import com.marketplace.visitor.elements.*;
import com.marketplace.visitor.visitors.ShippingCostVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShippingCostVisitorTest {
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
    void shouldCalculateExactShippingCostHandCalculated() {
        ShippingCostVisitor visitor = new ShippingCostVisitor();
        Money totalShipping = catalog.accept(visitor);

        assertEquals(Money.of(20.0), totalShipping);
    }

    @Test
    void shouldApplyMinimumShippingFeeForLightProducts() {
        Category lightCat = new Category("CAT-LIGHT", "Light Items");
        lightCat.addChild(new PhysicalProduct("P-LIGHT", "MicroSD Card", Money.of(20.0), 0.05, 10));

        ShippingCostVisitor visitor = new ShippingCostVisitor();
        Money shipping = lightCat.accept(visitor);

        assertEquals(Money.of(5.0), shipping);
    }
}