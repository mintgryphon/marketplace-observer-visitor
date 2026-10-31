package com.marketplace.visitor.api;

import com.marketplace.visitor.elements.Catalog;
import com.marketplace.visitor.elements.Category;
import com.marketplace.visitor.elements.DigitalProduct;
import com.marketplace.visitor.elements.PhysicalProduct;
import com.marketplace.visitor.elements.ServiceProduct;
import com.marketplace.visitor.elements.SubscriptionProduct;

public interface CatalogVisitor<R> {
    R visit(Catalog catalog);
    R visit(Category category);
    R visit(PhysicalProduct product);
    R visit(DigitalProduct product);
    R visit(ServiceProduct product);
    R visit(SubscriptionProduct product);
}