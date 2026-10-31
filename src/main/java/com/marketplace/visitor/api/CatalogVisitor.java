package com.marketplace.visitor.api;

import com.marketplace.visitor.elements.*;

public interface CatalogVisitor<R> {
    R visit(Catalog catalog);
    R visit(Category category);
    R visit(PhysicalProduct product);
    R visit(DigitalProduct product);
    R visit(ServiceProduct product);
    R visit(SubscriptionProduct product);
    R visit(GiftCardProduct product);
}