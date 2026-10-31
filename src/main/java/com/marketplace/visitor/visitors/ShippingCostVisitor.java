package com.marketplace.visitor.visitors;

import com.marketplace.common.Money;
import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;
import com.marketplace.visitor.elements.*;

public class ShippingCostVisitor implements CatalogVisitor<Money> {
    private static final double RATE_PER_KG = 2.50;
    private static final double SERVICE_TRAVEL_FEE = 15.00;

    @Override
    public Money visit(Catalog catalog) {
        Money total = Money.ZERO;
        for (Category category : catalog.getCategories()) {
            total = total.add(category.accept(this));
        }
        return total;
    }

    @Override
    public Money visit(Category category) {
        Money total = Money.ZERO;
        for (CatalogElement child : category.getChildren()) {
            total = total.add(child.accept(this));
        }
        return total;
    }

    @Override
    public Money visit(PhysicalProduct product) {
        double cost = Math.max(5.0, product.getWeightKg() * RATE_PER_KG);
        return Money.of(cost);
    }

    @Override
    public Money visit(DigitalProduct product) {
        return Money.ZERO;
    }

    @Override
    public Money visit(ServiceProduct product) {
        return Money.of(SERVICE_TRAVEL_FEE);
    }

    @Override
    public Money visit(SubscriptionProduct product) {
        return Money.ZERO;
    }
}