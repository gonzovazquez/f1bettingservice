package com.sg.f1bettingservice.core.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InsufficientBalanceExceptionTest {

  @Test
  void shouldThrowExceptionForInsufficientBalanceWithRightMessage() {
    var exception = new InsufficientBalanceException(1);
    assertThat(exception.getMessage()).isEqualTo("User with id 1 has insufficient balance");
  }
}
