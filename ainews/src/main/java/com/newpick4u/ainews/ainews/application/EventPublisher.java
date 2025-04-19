package com.newpick4u.ainews.ainews.application;

public interface EventPublisher {

  boolean isSupport(EventType eventType);

  void sendMessage(String message, EventType eventType);
}
