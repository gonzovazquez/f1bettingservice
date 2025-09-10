package com.sg.f1bettingservice.rest;

import static java.time.LocalTime.now;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.sg.f1bettingservice.core.exception.DriverNotFoundException;
import com.sg.f1bettingservice.core.exception.EventNotFoundException;
import com.sg.f1bettingservice.core.exception.InsufficientBalanceException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

  @ExceptionHandler(DriverNotFoundException.class)
  public ResponseEntity<?> handleDriverNotFound(DriverNotFoundException ex) {
    log.error("Driver not found exception", ex);
    return buildResponse(NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(EventNotFoundException.class)
  public ResponseEntity<?> handleEventNotFound(EventNotFoundException ex) {
    log.error("Event not found exception", ex);
    return buildResponse(NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(InsufficientBalanceException.class)
  public ResponseEntity<?> handleMethodArgumentNotValidException(InsufficientBalanceException ex) {
    log.error("Insufficient balance exception", ex);
    return buildResponse(CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    log.error("Method argument not valid exception", ex);
    return buildResponse(BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneric(Exception ex) {
    log.error("Unexpected exception", ex);
    return buildResponse(INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage());
  }

  private ResponseEntity<?> buildResponse(HttpStatus status, String message) {
    var body =
        Map.of(
            "timestamp", now(),
            "status", status.value(),
            "error", status.getReasonPhrase(),
            "message", message);
    return ResponseEntity.status(status).body(body);
  }
}
