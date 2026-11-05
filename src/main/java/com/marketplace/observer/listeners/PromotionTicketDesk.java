package com.marketplace.observer.listeners;

import com.marketplace.observer.api.Observer;
import com.marketplace.observer.events.InspectionReportEvent;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * End of the integration chain: every inspection report becomes a promotion ticket
 * for the merchandising team (here: stored in a thread-safe queue).
 */
public class PromotionTicketDesk implements Observer<InspectionReportEvent> {
    private final Queue<InspectionReportEvent> openTickets = new ConcurrentLinkedQueue<>();

    @Override
    public void onEvent(InspectionReportEvent report) {
        openTickets.add(report);
    }

    public List<InspectionReportEvent> getOpenTickets() {
        return List.copyOf(openTickets);
    }
}
