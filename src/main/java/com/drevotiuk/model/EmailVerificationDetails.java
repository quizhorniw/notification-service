package com.drevotiuk.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A DTO that holds the details needed for email verification.
 * 
 * <p>
 * This class contains the email address, first name, and a verification link.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class EmailVerificationDetails {
  /** The email address of the user to verify. */
  private String email;

  /** The first name of the user. */
  private String firstName;

  /** The verification link to be sent in the email. */
  private String link;
}
