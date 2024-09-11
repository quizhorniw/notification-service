package com.drevotiuk.model.exception;

/**
 * Custom exception class that indicates error happened while sending email.
 * This exception is typically thrown in services that handle email sending.
 */
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
