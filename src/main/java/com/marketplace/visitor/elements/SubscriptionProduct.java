package com.marketplace.visitor.elements;

import com.marketplace.common.Money;
import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;

import java.util.Objects;

/**
 * Лист 4: Подписка (периодический платеж).
 */
public class SubscriptionProduct implements CatalogElement {
    private final String id;
    private final String name;
    private final Money monthlyFee;
    private final int periodInMonths;

    public SubscriptionProduct(String id, String name, Money monthlyFee, int periodInMonths) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.monthlyFee = Objects.requireNonNull(monthlyFee);
        this.periodInMonths = periodInMonths;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Money getMonthlyFee() { return monthlyFee; }
    public int getPeriodInMonths() { return periodInMonths; }

    public Money getTotalContractValue() {
        return monthlyFee.multiply(periodInMonths);
    }

    @Override
    public <R> R accept(CatalogVisitor<R> visitor) {
        return visitor.visit(this);
    }
}