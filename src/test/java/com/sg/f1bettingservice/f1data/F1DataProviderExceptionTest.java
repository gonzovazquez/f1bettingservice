package com.sg.f1bettingservice.f1data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class F1DataProviderExceptionTest {

  @Test
  void shouldCreateExceptionWithMessageOnly() {
    String msg = "Something went wrong";
    F1DataProviderException ex = new F1DataProviderException(msg);
    assertThat(ex).isInstanceOf(RuntimeException.class).hasMessage(msg).hasNoCause();
    assertThat(ex.toString()).contains(msg);
  }

  @Test
  void shouldCreateExceptionWithMessageAndCause() {
    String msg = "Wrapped error";
    Throwable cause = new IllegalStateException("root cause");
    F1DataProviderException ex = new F1DataProviderException(msg, cause);
    assertThat(ex).isInstanceOf(RuntimeException.class).hasMessage(msg).hasCause(cause);
    assertThat(ex.getCause()).isInstanceOf(IllegalStateException.class).hasMessage("root cause");
    assertThat(ex.toString()).contains(msg);
  }
}
