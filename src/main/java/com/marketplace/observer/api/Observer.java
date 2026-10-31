package com.marketplace.observer.api;

@FunctionalInterface
public interface Observer<E extends Event> {
    void onEvent(E event);
}