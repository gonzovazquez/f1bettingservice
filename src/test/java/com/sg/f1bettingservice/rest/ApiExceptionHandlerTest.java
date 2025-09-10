package com.sg.f1bettingservice.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.sg.f1bettingservice.core.exception.DriverNotFoundException;
import com.sg.f1bettingservice.core.exception.EventNotFoundException;
import com.sg.f1bettingservice.core.exception.InsufficientBalanceException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class ApiExceptionHandlerTest {

  private ApiExceptionHandler handler = new ApiExceptionHandler();

  @Test
  void shouldReturn404NotFoundForDriverNotFoundException() {
    var ex = new DriverNotFoundException("Driver not found");

    ResponseEntity<?> response = handler.handleDriverNotFound(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(response.getBody()).isInstanceOf(Map.class);
    Map<?, ?> body = (Map<?, ?>) response.getBody();
    assertThat(body.get("status")).isEqualTo(404);
    assertThat(body.get("error")).isEqualTo("Not Found");
    assertThat(body.get("message")).toString().contains("55");
  }

  @Test
  void shouldReturn404NotFoundForEventNotFoundException() {
    var ex = new EventNotFoundException(123);

    ResponseEntity<?> response = handler.handleEventNotFound(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(404);
    Map<?, ?> body = (Map<?, ?>) response.getBody();
    assertThat(body.get("error")).isEqualTo("Not Found");
    assertThat(body.get("message")).toString().contains("123");
  }

  @Test
  void shouldReturn409ConflictForInsufficientBalanceException() {
    var ex = new InsufficientBalanceException(1);

    ResponseEntity<?> response = handler.handleMethodArgumentNotValidException(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(409);
    Map<?, ?> body = (Map<?, ?>) response.getBody();
    assertThat(body.get("error")).isEqualTo("Conflict");
    assertThat(body.get("message")).isEqualTo("User with id 1 has insufficient balance");
  }

  @Test
  void shouldReturn500ServerErrorWhenAnyOtherUnexpectedExceptionIsThrown() {
    var ex = new RuntimeException("boom!");

    ResponseEntity<?> response = handler.handleGeneric(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(500);
    Map<?, ?> body = (Map<?, ?>) response.getBody();
    assertThat(body.get("error")).isEqualTo("Internal Server Error");
    assertThat(body.get("message")).toString().contains("boom");
  }
}
