package com.marketplace.observer.subjects;

import com.marketplace.observer.api.Event;
import com.marketplace.observer.api.EventType;
import com.marketplace.observer.api.Observer;
import com.marketplace.observer.api.Subject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractSubject<E extends Event> implements Subject<E> {
    private final Map<EventType, List<Observer<? super E>>> listeners = new ConcurrentHashMap<>();

    @Override
    public void subscribe(EventType eventType, Observer<? super E> observer) {
        if (eventType == null || observer == null) {
            throw new IllegalArgumentException("EventType and Observer must not be null");
        }
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(observer);
    }

    @Override
    public void unsubscribe(EventType eventType, Observer<? super E> observer) {
        if (eventType == null || observer == null) {
            return;
        }
        List<Observer<? super E>> list = listeners.get(eventType);
        if (list != null) {
            list.remove(observer);
        }
    }

    @Override
    public void notifyObservers(E event) {
        if (event == null) return;
        List<Observer<? super E>> list = listeners.get(event.eventType());
        if (list != null) {
            for (Observer<? super E> observer : list) {
                observer.onEvent(event);
            }
        }
    }

    public int getListenerCount(EventType eventType) {
        List<Observer<? super E>> list = listeners.get(eventType);
        return list == null ? 0 : list.size();
    }
}