package com.marketplace.observer.listeners;

import com.marketplace.observer.api.EventType;
import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.InspectionReportEvent;
import com.marketplace.observer.events.PriceChangedEvent;
import com.marketplace.observer.subjects.AbstractSubject;
import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.elements.Catalog;
import com.marketplace.visitor.elements.Category;
import com.marketplace.visitor.visitors.InventoryReport;
import com.marketplace.visitor.visitors.InventoryValueVisitor;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Integration link (alert -> inspection -> report event).
 * <ol>
 *   <li>Observer side: receives {@link PriceChangedEvent}s from {@code PriceService}.</li>
 *   <li>If the discount reaches the threshold, runs {@link InventoryValueVisitor} on the
 *       affected category subtree only (not the whole catalog).</li>
 *   <li>Publishes the result back into the system as an {@link InspectionReportEvent}.</li>
 * </ol>
 * The injected visitor is stateless, so one instance is safely shared by all producer threads.
 */
public class PriceDropAlertService extends AbstractSubject<InspectionReportEvent>
        implements Observer<PriceChangedEvent> {
    private final Catalog catalog;
    private final InventoryValueVisitor inspector;
    private final double discountThresholdPercent;
    private final AtomicInteger inspectionsTriggered = new AtomicInteger(0);

    public PriceDropAlertService(Catalog catalog, InventoryValueVisitor inspector, double discountThresholdPercent) {
        super(EventType.INSPECTION_REPORT);
        this.catalog = Objects.requireNonNull(catalog, "catalog cannot be null");
        this.inspector = Objects.requireNonNull(inspector, "inspector cannot be null");
        this.discountThresholdPercent = discountThresholdPercent;
    }

    @Override
    public void onEvent(PriceChangedEvent event) {
        double discount = event.getDiscountPercentage();
        if (discount < discountThresholdPercent) {
            return;
        }
        findCategory(catalog.getCategories(), event.categoryId()).ifPresent(category -> {
            InventoryReport report = category.accept(inspector);
            inspectionsTriggered.incrementAndGet();
            notifyObservers(new InspectionReportEvent(
                    event.productId(),
                    category.getId(),
                    category.getName(),
                    discount,
                    report,
                    Instant.now()));
        });
    }

    /** Depth-first search; categories may be nested inside other categories. */
    private Optional<Category> findCategory(List<? extends CatalogElement> elements, String categoryId) {
        for (CatalogElement element : elements) {
            if (element instanceof Category category) {
                if (category.getId().equals(categoryId)) {
                    return Optional.of(category);
                }
                Optional<Category> nested = findCategory(category.getChildren(), categoryId);
                if (nested.isPresent()) {
                    return nested;
                }
            }
        }
        return Optional.empty();
    }

    public int getInspectionsTriggered() {
        return inspectionsTriggered.get();
    }
}
