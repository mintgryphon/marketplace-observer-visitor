package com.marketplace.observer.events;

import com.marketplace.observer.api.Event;
import com.marketplace.observer.api.EventType;
import com.marketplace.visitor.visitors.InventoryReport;

import java.time.Instant;
import java.util.Objects;

/**
 * Result of the Observer -> Visitor integration: a significant price drop triggered an
 * inspection of the product's category subtree, and the visitor's report re-enters the system
 * as this event.
 */
public record InspectionReportEvent(
        String triggeringProductId,
        String categoryId,
        String categoryName,
        double discountPercent,
        InventoryReport report,
        Instant timestamp
) implements Event {
    public InspectionReportEvent {
        Objects.requireNonNull(triggeringProductId, "triggeringProductId cannot be null");
        Objects.requireNonNull(categoryId, "categoryId cannot be null");
        Objects.requireNonNull(categoryName, "categoryName cannot be null");
        Objects.requireNonNull(report, "report cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
    }

    @Override
    public EventType eventType() {
        return EventType.INSPECTION_REPORT;
    }
}
