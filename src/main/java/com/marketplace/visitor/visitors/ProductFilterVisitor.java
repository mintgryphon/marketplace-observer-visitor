package com.marketplace.visitor.visitors;

import com.marketplace.common.Money;
import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;
import com.marketplace.visitor.elements.*;

import java.util.ArrayList;
import java.util.List;

public class ProductFilterVisitor implements CatalogVisitor<List<CatalogElement>> {
    private final Money maxPrice;
    private final List<CatalogElement> matched = new ArrayList<>();

    public ProductFilterVisitor(Money maxPrice) {
        this.maxPrice = maxPrice;
    }

    @Override
    public List<CatalogElement> visit(Catalog catalog) {
        for (Category category : catalog.getCategories()) {
            category.accept(this);
        }
        return matched;
    }

    @Override
    public List<CatalogElement> visit(Category category) {
        for (CatalogElement child : category.getChildren()) {
            child.accept(this);
        }
        return matched;
    }

    @Override
    public List<CatalogElement> visit(PhysicalProduct product) {
        if (product.getPrice().isLessThan(maxPrice) || product.getPrice().equals(maxPrice)) {
            matched.add(product);
        }
        return matched;
    }

    @Override
    public List<CatalogElement> visit(DigitalProduct product) {
        if (product.getPrice().isLessThan(maxPrice) || product.getPrice().equals(maxPrice)) {
            matched.add(product);
        }
        return matched;
    }

    @Override
    public List<CatalogElement> visit(ServiceProduct product) {
        if (product.getTotalPrice().isLessThan(maxPrice) || product.getTotalPrice().equals(maxPrice)) {
            matched.add(product);
        }
        return matched;
    }

    @Override
    public List<CatalogElement> visit(SubscriptionProduct product) {
        if (product.getTotalContractValue().isLessThan(maxPrice) || product.getTotalContractValue().equals(maxPrice)) {
            matched.add(product);
        }
        return matched;
    }

    @Override
    public List<CatalogElement> visit(GiftCardProduct product) {
        if (product.getDenomination().isLessThan(maxPrice) || product.getDenomination().equals(maxPrice)) {
            matched.add(product);
        }
        return matched;
    }
}