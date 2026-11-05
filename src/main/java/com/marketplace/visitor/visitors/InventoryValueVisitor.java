package com.marketplace.visitor.visitors;

import com.marketplace.visitor.elements.*;

/**
 * Accumulating visitor: walks the whole subtree and accumulates units and capital value
 * into one {@link InventoryReport}. The accumulation happens through return values,
 * not through fields, so the visitor is reusable and thread-safe.
 */
public class InventoryValueVisitor extends AggregatingCatalogVisitor<InventoryReport> {

    @Override
    protected InventoryReport empty() {
        return InventoryReport.EMPTY;
    }

    @Override
    protected InventoryReport combine(InventoryReport left, InventoryReport right) {
        return left.plus(right);
    }

    @Override
    public InventoryReport visit(PhysicalProduct product) {
        int units = product.getStockQuantity();
        return new InventoryReport(units, product.getPrice().multiply(units));
    }

    @Override
    public InventoryReport visit(DigitalProduct product) {
        return new InventoryReport(1, product.getPrice());
    }

    @Override
    public InventoryReport visit(ServiceProduct product) {
        return new InventoryReport(1, product.getTotalPrice());
    }

    @Override
    public InventoryReport visit(SubscriptionProduct product) {
        return new InventoryReport(1, product.getTotalContractValue());
    }

    @Override
    public InventoryReport visit(GiftCardProduct product) {
        return new InventoryReport(1, product.getDenomination());
    }
}
