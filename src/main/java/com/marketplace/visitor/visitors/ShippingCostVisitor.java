package com.marketplace.visitor.visitors;

import com.marketplace.common.Money;
import com.marketplace.visitor.elements.*;

/** Numeric visitor: total shipping cost of a subtree. */
public class ShippingCostVisitor extends AggregatingCatalogVisitor<Money> {
    private static final double RATE_PER_KG = 2.50;
    private static final double MINIMUM_PARCEL_FEE = 5.00;
    private static final double SERVICE_TRAVEL_FEE = 15.00;

    @Override
    protected Money empty() {
        return Money.ZERO;
    }

    @Override
    protected Money combine(Money left, Money right) {
        return left.add(right);
    }

    @Override
    public Money visit(PhysicalProduct product) {
        return Money.of(Math.max(MINIMUM_PARCEL_FEE, product.getWeightKg() * RATE_PER_KG));
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

    @Override
    public Money visit(GiftCardProduct product) {
        return Money.ZERO;
    }
}
