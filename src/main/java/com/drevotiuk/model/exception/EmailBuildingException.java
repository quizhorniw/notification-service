package com.drevotiuk.model.exception;

/**
 * Custom exception class that indicates error happened while building email.
 * This exception is typically thrown in services that handle email creation.
 */
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
