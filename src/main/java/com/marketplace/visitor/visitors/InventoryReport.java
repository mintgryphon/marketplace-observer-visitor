package com.marketplace.visitor.visitors;

import com.marketplace.common.Money;

import java.util.Objects;

/** Immutable result of {@link InventoryValueVisitor}: number of units and their total value. */
public record InventoryReport(
        int totalItemsCount,
        Money totalValue
) {
    public static final InventoryReport EMPTY = new InventoryReport(0, Money.ZERO);

    public InventoryReport {
        Objects.requireNonNull(totalValue, "totalValue cannot be null");
    }

    public InventoryReport plus(InventoryReport other) {
        return new InventoryReport(totalItemsCount + other.totalItemsCount, totalValue.add(other.totalValue));
    }
}
