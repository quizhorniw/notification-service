package com.drevotiuk.model.exception;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;

/**
 * Custom exception class that indicates a user was not found.
 * This exception is typically thrown when a requested user does not exist in
 * the system.
 */
public class UserNotFoundException extends AmqpRejectAndDontRequeueException {
  private static final long serialVersionUID = 1688007575250357292L;

  public UserNotFoundException(String message) {
    super(message);
  }

  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserNotFoundException(Throwable cause) {
    super(cause);
  }
}
