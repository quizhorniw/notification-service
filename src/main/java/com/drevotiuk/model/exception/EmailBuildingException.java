package com.drevotiuk.model.exception;

public class EmailBuildingException extends RuntimeException {
  private static final long serialVersionUID = 7433226077773525379L;

  public EmailBuildingException(String message) {
    super(message);
  }

  public EmailBuildingException(String message, Throwable cause) {
    super(message, cause);
  }

  public EmailBuildingException(Throwable cause) {
    super(cause);
  }
}
