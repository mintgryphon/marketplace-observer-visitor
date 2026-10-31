package com.marketplace.visitor.elements;

import com.marketplace.common.Money;
import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;

import java.util.Objects;

/**
 * Лист 1: Физический товар с весом и объемом.
 */
public class PhysicalProduct implements CatalogElement {
    private final String id;
    private final String name;
    private final Money price;
    private final double weightKg;
    private final int stockQuantity;

    public PhysicalProduct(String id, String name, Money price, double weightKg, int stockQuantity) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.price = Objects.requireNonNull(price);
        this.weightKg = weightKg;
        this.stockQuantity = stockQuantity;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Money getPrice() { return price; }
    public double getWeightKg() { return weightKg; }
    public int getStockQuantity() { return stockQuantity; }

    @Override
    public <R> R accept(CatalogVisitor<R> visitor) {
        return visitor.visit(this);
    }
}