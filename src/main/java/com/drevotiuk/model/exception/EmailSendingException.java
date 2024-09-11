package com.drevotiuk.model.exception;

public class EmailSendingException extends RuntimeException {
  private static final long serialVersionUID = 3713707580966322469L;

  public EmailSendingException(String message) {
    super(message);
  }

  public EmailSendingException(String message, Throwable cause) {
    super(message, cause);
  }

  public EmailSendingException(Throwable cause) {
    super(cause);
  }
}
