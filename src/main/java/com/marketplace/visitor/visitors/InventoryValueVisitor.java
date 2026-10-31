package com.marketplace.visitor.visitors;

import com.marketplace.common.Money;
import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;
import com.marketplace.visitor.elements.*;

public class InventoryValueVisitor implements CatalogVisitor<InventoryReport> {
    private int itemCount = 0;
    private Money totalValue = Money.ZERO;

    @Override
    public InventoryReport visit(Catalog catalog) {
        for (Category category : catalog.getCategories()) {
            category.accept(this);
        }
        return new InventoryReport(itemCount, totalValue);
    }

    @Override
    public InventoryReport visit(Category category) {
        for (CatalogElement child : category.getChildren()) {
            child.accept(this);
        }
        return new InventoryReport(itemCount, totalValue);
    }

    @Override
    public InventoryReport visit(PhysicalProduct product) {
        itemCount += product.getStockQuantity();
        totalValue = totalValue.add(product.getPrice().multiply(product.getStockQuantity()));
        return new InventoryReport(itemCount, totalValue);
    }

    @Override
    public InventoryReport visit(DigitalProduct product) {
        itemCount += 1;
        totalValue = totalValue.add(product.getPrice());
        return new InventoryReport(itemCount, totalValue);
    }

    @Override
    public InventoryReport visit(ServiceProduct product) {
        itemCount += 1;
        totalValue = totalValue.add(product.getTotalPrice());
        return new InventoryReport(itemCount, totalValue);
    }

    @Override
    public InventoryReport visit(SubscriptionProduct product) {
        itemCount += 1;
        totalValue = totalValue.add(product.getTotalContractValue());
        return new InventoryReport(itemCount, totalValue);
    }

    @Override
    public InventoryReport visit(GiftCardProduct product) {
        itemCount += 1;
        totalValue = totalValue.add(product.getDenomination());
        return new InventoryReport(itemCount, totalValue);
    }
}