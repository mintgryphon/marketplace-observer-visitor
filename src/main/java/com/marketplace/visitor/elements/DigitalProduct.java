package com.marketplace.visitor.elements;

import com.marketplace.common.Money;
import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;

import java.util.Objects;

public class DigitalProduct implements CatalogElement {
    private final String id;
    private final String name;
    private final Money price;
    private final double downloadSizeMb;

    public DigitalProduct(String id, String name, Money price, double downloadSizeMb) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.price = Objects.requireNonNull(price);
        this.downloadSizeMb = downloadSizeMb;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Money getPrice() { return price; }
    public double getDownloadSizeMb() { return downloadSizeMb; }

    @Override
    public <R> R accept(CatalogVisitor<R> visitor) {
        return visitor.visit(this);
    }
}