package io.github.maseev.alpaca.api.streaming;

import io.github.maseev.alpaca.api.streaming.entity.Event;
import io.github.maseev.alpaca.api.streaming.listener.EventListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.emptyList;

@SuppressWarnings("rawtypes, unchecked")
public class SubscriptionManager {

  private final Map<Class<? extends Event>, List<EventListener>> subscribers;

  public SubscriptionManager() {
    subscribers = new ConcurrentHashMap<>();
  }

  void subscribe(EventListener listener, Class<? extends Event> eventType) {
    subscribers.compute(eventType, (key, value) -> {
      if (value == null) {
        List<EventListener> listeners = new CopyOnWriteArrayList<>();
        listeners.add(listener);

        return listeners;
      }

      value.add(listener);

      return value;
    });
  }

  <T extends Event> void invoke(T event) {
    List<EventListener> listeners =
      subscribers.getOrDefault(event.getClass(), emptyList());

    for (EventListener listener : listeners) {
      listener.onEvent(event);
    }
  }
}
