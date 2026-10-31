package com.marketplace.visitor.visitors;

import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;
import com.marketplace.visitor.elements.*;

import java.util.ArrayList;
import java.util.List;

public class PriceTagFormattingVisitor implements CatalogVisitor<List<String>> {
    private final List<String> priceTags = new ArrayList<>();

    @Override
    public List<String> visit(Catalog catalog) {
        for (Category category : catalog.getCategories()) {
            category.accept(this);
        }
        return priceTags;
    }

    @Override
    public List<String> visit(Category category) {
        for (CatalogElement child : category.getChildren()) {
            child.accept(this);
        }
        return priceTags;
    }

    @Override
    public List<String> visit(PhysicalProduct product) {
        priceTags.add(String.format("[PHYSICAL] %s - %s (Weight: %.1f kg)",
                product.getName(), product.getPrice(), product.getWeightKg()));
        return priceTags;
    }

    @Override
    public List<String> visit(DigitalProduct product) {
        priceTags.add(String.format("[DIGITAL] %s - %s (Instant Download)",
                product.getName(), product.getPrice()));
        return priceTags;
    }

    @Override
    public List<String> visit(ServiceProduct product) {
        priceTags.add(String.format("[SERVICE] %s - %s/hr (Estimated: %d hrs)",
                product.getName(), product.getHourlyRate(), product.getEstimatedHours()));
        return priceTags;
    }

    @Override
    public List<String> visit(SubscriptionProduct product) {
        priceTags.add(String.format("[SUBSCRIPTION] %s - %s/mo (Duration: %d mos)",
                product.getName(), product.getMonthlyFee(), product.getPeriodInMonths()));
        return priceTags;
    }

    @Override
    public List<String> visit(GiftCardProduct product) {
        priceTags.add(String.format("[GIFT CARD] %s - Denomination: %s",
                product.getName(), product.getDenomination()));
        return priceTags;
    }
}