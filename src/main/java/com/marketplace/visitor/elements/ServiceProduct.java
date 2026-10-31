package com.marketplace.visitor.elements;

import com.marketplace.common.Money;
import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;

import java.util.Objects;

public class ServiceProduct implements CatalogElement {
    private final String id;
    private final String name;
    private final Money hourlyRate;
    private final int estimatedHours;

    public ServiceProduct(String id, String name, Money hourlyRate, int estimatedHours) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.hourlyRate = Objects.requireNonNull(hourlyRate);
        this.estimatedHours = estimatedHours;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Money getHourlyRate() { return hourlyRate; }
    public int getEstimatedHours() { return estimatedHours; }

    public Money getTotalPrice() {
        return hourlyRate.multiply(estimatedHours);
    }

    @Override
    public <R> R accept(CatalogVisitor<R> visitor) {
        return visitor.visit(this);
    }
}