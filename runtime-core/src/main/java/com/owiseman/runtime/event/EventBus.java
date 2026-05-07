package com.owiseman.runtime.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class EventBus {

    private static final Logger LOG = Logger.getLogger(EventBus.class.getName());

    private final Map<String, List<Consumer<Event>>> subscribers = new ConcurrentHashMap<>();

    public void subscribe(String eventType, Consumer<Event> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
        LOG.fine("Subscribed to event type: " + eventType);
    }

    public void unsubscribe(String eventType, Consumer<Event> handler) {
        List<Consumer<Event>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    public void publish(String eventType, Map<String, Object> data) {
        Event event = new Event(eventType, data);
        publish(event);
    }

    public void publish(Event event) {
        List<Consumer<Event>> handlers = subscribers.get(event.type());
        if (handlers != null) {
            for (Consumer<Event> handler : handlers) {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    LOG.warning("Event handler error for " + event.type() + ": " + e.getMessage());
                }
            }
        }
        LOG.fine("Published event: " + event.type());
    }

    public void publishAsync(String eventType, Map<String, Object> data) {
        Thread.ofVirtual().name("eventbus-" + eventType).start(() -> publish(eventType, data));
    }

    public int subscriberCount(String eventType) {
        List<Consumer<Event>> handlers = subscribers.get(eventType);
        return handlers != null ? handlers.size() : 0;
    }

    public void clear() {
        subscribers.clear();
    }
}
