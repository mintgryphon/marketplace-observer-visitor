package com.marketplace.visitor.visitors;

import com.marketplace.visitor.elements.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Formats a human-readable price tag for every product of the subtree. */
public class PriceTagFormattingVisitor extends AggregatingCatalogVisitor<List<String>> {

    @Override
    protected List<String> empty() {
        return List.of();
    }

    @Override
    protected List<String> combine(List<String> left, List<String> right) {
        List<String> merged = new ArrayList<>(left);
        merged.addAll(right);
        return List.copyOf(merged);
    }

    @Override
    public List<String> visit(PhysicalProduct product) {
        return List.of(String.format(Locale.ROOT, "[PHYSICAL] %s - %s (Weight: %.1f kg)",
                product.getName(), product.getPrice(), product.getWeightKg()));
    }

    @Override
    public List<String> visit(DigitalProduct product) {
        return List.of(String.format("[DIGITAL] %s - %s (Instant Download)",
                product.getName(), product.getPrice()));
    }

    @Override
    public List<String> visit(ServiceProduct product) {
        return List.of(String.format("[SERVICE] %s - %s/hr (Estimated: %d hrs)",
                product.getName(), product.getHourlyRate(), product.getEstimatedHours()));
    }

    @Override
    public List<String> visit(SubscriptionProduct product) {
        return List.of(String.format("[SUBSCRIPTION] %s - %s/mo (Duration: %d mos)",
                product.getName(), product.getMonthlyFee(), product.getPeriodInMonths()));
    }

    @Override
    public List<String> visit(GiftCardProduct product) {
        return List.of(String.format("[GIFT CARD] %s - Denomination: %s",
                product.getName(), product.getDenomination()));
    }
}
