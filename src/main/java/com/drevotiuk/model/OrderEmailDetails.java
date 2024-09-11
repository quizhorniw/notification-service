package com.drevotiuk.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A DTO that holds the details of an order to be used in email notifications.
 * 
 * <p>
 * This class contains information about the user, order time, and total
 * price of the order.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderEmailDetails {
  /** The ID of the user who placed the order. */
  private String userId;

  /** The time when the order was placed. */
  private String orderTime;

  /** The total price of the order. */
  private BigDecimal totalPrice;
}
