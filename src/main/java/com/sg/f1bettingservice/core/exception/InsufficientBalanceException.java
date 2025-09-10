package com.sg.f1bettingservice.core.exception;

public class InsufficientBalanceException extends RuntimeException {
  public InsufficientBalanceException(Integer userId) {
    super("User with id %s has insufficient balance".formatted(userId));
  }
}
