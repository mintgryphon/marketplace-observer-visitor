package com.marketplace.visitor.visitors;

import com.marketplace.common.Money;
import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.elements.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Filter visitor: returns every product whose price is at most {@code maxPrice}. */
public class ProductFilterVisitor extends AggregatingCatalogVisitor<List<CatalogElement>> {
    private final Money maxPrice;

    public ProductFilterVisitor(Money maxPrice) {
        this.maxPrice = Objects.requireNonNull(maxPrice, "maxPrice cannot be null");
    }

    @Override
    protected List<CatalogElement> empty() {
        return List.of();
    }

    @Override
    protected List<CatalogElement> combine(List<CatalogElement> left, List<CatalogElement> right) {
        if (left.isEmpty()) return right;
        if (right.isEmpty()) return left;
        List<CatalogElement> merged = new ArrayList<>(left);
        merged.addAll(right);
        return List.copyOf(merged);
    }

    @Override
    public List<CatalogElement> visit(PhysicalProduct product) {
        return keepIfAffordable(product, product.getPrice());
    }

    @Override
    public List<CatalogElement> visit(DigitalProduct product) {
        return keepIfAffordable(product, product.getPrice());
    }

    @Override
    public List<CatalogElement> visit(ServiceProduct product) {
        return keepIfAffordable(product, product.getTotalPrice());
    }

    @Override
    public List<CatalogElement> visit(SubscriptionProduct product) {
        return keepIfAffordable(product, product.getTotalContractValue());
    }

    @Override
    public List<CatalogElement> visit(GiftCardProduct product) {
        return keepIfAffordable(product, product.getDenomination());
    }

    private List<CatalogElement> keepIfAffordable(CatalogElement product, Money price) {
        return price.isAtMost(maxPrice) ? List.of(product) : List.of();
    }
}
