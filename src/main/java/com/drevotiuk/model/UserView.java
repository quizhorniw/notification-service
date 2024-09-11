package com.drevotiuk.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a view model for user details used in the application's user
 * interfaces.
 * This class is used to encapsulate user information for display purposes.
 * 
 * It includes fields for the user's first name, last name, email, and date of
 * birth.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserView {
  /** The user's first name. */
  private String firstName;

  /** The user's last name. */
  private String lastName;

  /** The user's email address. */
  private String email;

  /** The user's date of birth. */
  private String dateOfBirth;
}
