package com.sg.f1bettingservice.core.exception;

public class DriverNotFoundException extends RuntimeException {
  public DriverNotFoundException(Integer driverId, Integer eventId) {
    super("Driver with id %s not found in event with id %s".formatted(driverId, eventId));
  }

  public DriverNotFoundException(String message) {
    super(message);
  }
}
