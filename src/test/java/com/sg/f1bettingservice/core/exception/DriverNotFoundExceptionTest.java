package com.sg.f1bettingservice.core.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DriverNotFoundExceptionTest {

  @Test
  void shouldCreateDriverNotFoundExceptionWithMessage() {
    DriverNotFoundException exception = new DriverNotFoundException(1, 2);
    assertEquals("Driver with id 1 not found in event with id 2", exception.getMessage());
  }

  @Test
  void shouldCreateDriverNotFoundExceptionWithCustomMessage() {
    String customMessage = "Custom error message";
    DriverNotFoundException exception = new DriverNotFoundException(customMessage);
    assertEquals(customMessage, exception.getMessage());
  }
}
