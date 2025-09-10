package com.sg.f1bettingservice.core.exception;

public class EventNotFoundException extends RuntimeException {
  public EventNotFoundException(Integer eventId) {
    super("Event with id %s not found".formatted(eventId));
  }
}
