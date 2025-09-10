package com.sg.f1bettingservice.core.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EventNotFoundExceptionTest {

  @Test
  void shouldThrowsExceptionForEventNotFoundWithRightMessage() {
    var exception = new EventNotFoundException(1);
    assertEquals("Event with id 1 not found", exception.getMessage());
  }
}
