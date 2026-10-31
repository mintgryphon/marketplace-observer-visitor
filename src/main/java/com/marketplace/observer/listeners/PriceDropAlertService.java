package com.marketplace.observer.listeners;

import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.PriceChangedEvent;
import com.marketplace.visitor.elements.Catalog;
import com.marketplace.visitor.elements.Category;
import com.marketplace.visitor.visitors.InventoryReport;
import com.marketplace.visitor.visitors.InventoryValueVisitor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class PriceDropAlertService implements Observer<PriceChangedEvent> {
    private final Catalog catalog;
    private final double discountThresholdPercent;
    private final AtomicInteger inspectionsTriggered = new AtomicInteger(0);

    public PriceDropAlertService(Catalog catalog, double discountThresholdPercent) {
        this.catalog = Objects.requireNonNull(catalog, "catalog cannot be null");
        this.discountThresholdPercent = discountThresholdPercent;
    }

    @Override
    public void onEvent(PriceChangedEvent event) {
        if (event.getDiscountPercentage() >= discountThresholdPercent) {
            // Находим целевую подкатегорию в дереве каталога
            Category targetCategory = findCategory(catalog, event.categoryId());
            if (targetCategory != null) {
                // Запускаем Visitor только на связанном поддереве
                InventoryValueVisitor visitor = new InventoryValueVisitor();
                InventoryReport report = targetCategory.accept(visitor);
                inspectionsTriggered.incrementAndGet();

                // В реальной системе отчет уходит в лог, очередь сообщений или создается промо-тикет
                System.out.printf("[ALERT & INSPECTION] Significant price drop (%.1f%%) on product '%s'! " +
                                "Category '%s' inspected: %d items, total value %s%n",
                        event.getDiscountPercentage(),
                        event.productId(),
                        targetCategory.getName(),
                        report.totalItemsCount(),
                        report.totalValue());
            }
        }
    }

    private Category findCategory(Catalog cat, String categoryId) {
        for (Category category : cat.getCategories()) {
            if (category.getId().equals(categoryId)) {
                return category;
            }
        }
        return null;
    }

    public int getInspectionsTriggered() {
        return inspectionsTriggered.get();
    }
}