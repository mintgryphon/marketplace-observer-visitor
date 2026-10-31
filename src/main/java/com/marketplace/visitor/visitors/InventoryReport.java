package com.marketplace.visitor.visitors;

import com.marketplace.common.Money;

public record InventoryReport(
        int totalItemsCount,
        Money totalValue
) {}