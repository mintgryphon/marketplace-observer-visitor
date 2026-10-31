package com.marketplace.visitor.elements;

import com.marketplace.common.Money;
import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;

import java.util.Objects;

public class GiftCardProduct implements CatalogElement {
    private final String id;
    private final String name;
    private final Money denomination;

    public GiftCardProduct(String id, String name, Money denomination) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.denomination = Objects.requireNonNull(denomination);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Money getDenomination() { return denomination; }

    @Override
    public <R> R accept(CatalogVisitor<R> visitor) {
        return visitor.visit(this);
    }
}