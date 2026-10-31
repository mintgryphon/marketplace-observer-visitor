package com.marketplace.observer.api;

import java.time.Instant;

public interface Event {
    EventType eventType();
    Instant timestamp();
}