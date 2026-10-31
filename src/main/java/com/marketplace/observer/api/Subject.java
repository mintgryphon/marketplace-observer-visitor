package com.marketplace.observer.api;

public interface Subject<E extends Event> {
    void subscribe(EventType eventType, Observer<? super E> observer);
    void unsubscribe(EventType eventType, Observer<? super E> observer);
    void notifyObservers(E event);
}