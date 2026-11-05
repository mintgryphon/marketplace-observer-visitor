package com.marketplace.observer.subjects;

import com.marketplace.observer.api.Event;
import com.marketplace.observer.api.EventType;
import com.marketplace.observer.api.Observer;
import com.marketplace.observer.api.Subject;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe listener registry shared by all subjects.
 * <ul>
 *   <li>One {@link CopyOnWriteArrayList} per event type, created eagerly in the constructor.
 *       The map itself is never modified afterwards, so it needs no locking.</li>
 *   <li>Publishing iterates over a snapshot of the list: subscribe/unsubscribe from another thread,
 *       or from inside a handler, never throws {@code ConcurrentModificationException} and never
 *       blocks the publisher. No lock is taken on the whole subject.</li>
 *   <li>A failing listener does not stop delivery to the remaining listeners.</li>
 * </ul>
 */
public abstract class AbstractSubject<E extends Event> implements Subject<E> {
    private final Map<EventType, List<Observer<? super E>>> listeners;
    private final AtomicInteger failedDeliveries = new AtomicInteger(0);

    protected AbstractSubject(EventType publishedType, EventType... morePublishedTypes) {
        Set<EventType> types = EnumSet.of(publishedType, morePublishedTypes);
        Map<EventType, List<Observer<? super E>>> registry = new EnumMap<>(EventType.class);
        for (EventType type : types) {
            registry.put(type, new CopyOnWriteArrayList<>());
        }
        this.listeners = registry;
    }

    @Override
    public void subscribe(EventType eventType, Observer<? super E> observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer must not be null");
        }
        listenersOf(eventType).add(observer);
    }

    @Override
    public void unsubscribe(EventType eventType, Observer<? super E> observer) {
        listenersOf(eventType).remove(observer);
    }

    @Override
    public void notifyObservers(E event) {
        if (event == null) return;
        for (Observer<? super E> observer : listenersOf(event.eventType())) {
            try {
                observer.onEvent(event);
            } catch (RuntimeException failure) {
                failedDeliveries.incrementAndGet();
            }
        }
    }

    public int getListenerCount(EventType eventType) {
        return listenersOf(eventType).size();
    }

    public int getFailedDeliveries() {
        return failedDeliveries.get();
    }

    private List<Observer<? super E>> listenersOf(EventType eventType) {
        List<Observer<? super E>> list = eventType == null ? null : listeners.get(eventType);
        if (list == null) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " does not publish " + eventType);
        }
        return list;
    }
}
