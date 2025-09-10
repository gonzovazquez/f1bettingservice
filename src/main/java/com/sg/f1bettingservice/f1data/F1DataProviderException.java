package com.sg.f1bettingservice.f1data;

public class F1DataProviderException extends RuntimeException {
  public F1DataProviderException(String message, Throwable e) {
    super(message, e);
  }

  public F1DataProviderException(String message) {
    super(message);
  }
}
