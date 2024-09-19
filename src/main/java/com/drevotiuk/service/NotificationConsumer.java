package com.drevotiuk.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.MessageFormat;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.drevotiuk.model.UserView;
import com.drevotiuk.model.exception.EmailBuildingException;
import com.drevotiuk.model.exception.UserNotFoundException;
import com.drevotiuk.model.EmailVerificationDetails;
import com.drevotiuk.model.OrderEmailDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A service class responsible for consuming messages from RabbitMQ queues
 * and processing them to send notifications via email.
 * It then builds the appropriate email content and sends it.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
  @Value("${notifications.email.from}")
  private String from;

  @Value("${notifications.email.topic.verification}")
  private String verificationEmailTopic;
  @Value("${resources.email.verification}")
  private String verificationEmailResourcePath;
  @Value("${resources.email.order-created}")

  private String orderCreatedTopic;
  @Value("${rabbitmq.exchange.user-service}")
  private String orderCreatedResourcePath;
  @Value("${security.confirmation-token.expiration}")

  private String tokenExpiration;
  @Value("${notifications.email.topic.order-created}")

  private String userServiceExchange;
  @Value("${rabbitmq.routingkey.user}")
  private String userRoutingKey;

  private final EmailService emailService;
  private final RabbitTemplate rabbitTemplate;
  private final ResourceLoader resourceLoader;

  /**
   * Consumes messages from the email verification queue and processes them
   * to send a verification email to the user.
   * 
   * @param details the details for the email verification
   */
  @RabbitListener(queues = "${rabbitmq.queue.email-verification}")
  public void consumeEmailVerification(EmailVerificationDetails details) {
    log.info("Received email verification message; email: {}", details.getEmail());
    String email = buildVerificationEmail(details.getFirstName(), details.getLink());
    emailService.send(from, details.getEmail(), verificationEmailTopic, email);
  }

  /**
   * Consumes messages from the order created queue and processes them
   * to send an order confirmation email to the user.
   * 
   * @param details the details for the order creation
   */
  @RabbitListener(queues = "${rabbitmq.queue.order-created}")
  public void consumeOrderCreated(OrderEmailDetails details) {
    String userId = details.getUserId();
    log.info("Received order created message; userID: {}", userId);
    UserView user = getUserById(userId);
    String emailContent = buildOrderCreatedEmail(
        user.getFirstName(), details.getOrderTime(), details.getTotalPrice());
    emailService.send(from, user.getEmail(), orderCreatedTopic, emailContent);
  }

  /**
   * Retrieves user details by sending a request to the user service via RabbitMQ.
   * 
   * @param userId the ID of the user to retrieve
   * @return a {@link UserView}
   */
  private UserView getUserById(String userId) {
    Object message = rabbitTemplate.convertSendAndReceive(userServiceExchange, userRoutingKey, userId);

    return validateAndCastUser(message, userId);
  }

  /**
   * Validates and casts an object to {@link UserView}.
   *
   * @param obj    the object to validate and cast
   * @param userId the userID associated with the validation
   * @return the cast object as {@link UserView}
   * @throws UserNotFoundException if the object is null or not of the
   *                               {@link UserView} type.
   */
  private UserView validateAndCastUser(Object obj, String userId) {
    if (obj == null || !(obj instanceof UserView)) {
      log.warn("User not found with ID {}", userId);
      throw new UserNotFoundException("User not found with ID: " + userId);
    }

    return (UserView) obj;
  }

  /**
   * Builds the verification email content using a template.
   * 
   * @param name the recipient's name
   * @param link the verification link
   * @return the formatted verification email content
   */
  public String buildVerificationEmail(String name, String link) {
    return MessageFormat.format(getEmail(verificationEmailResourcePath), name, link, tokenExpiration);
  }

  /**
   * Builds the order-created email content using a template.
   * 
   * @param name       the recipient's name
   * @param date       the order date
   * @param totalPrice the total price of the order
   * @return the formatted order-created email content
   */
  public String buildOrderCreatedEmail(String name, String date, BigDecimal totalPrice) {
    return MessageFormat.format(getEmail(orderCreatedResourcePath), name, date, totalPrice);
  }

  /**
   * Retrieves the email template content from the specified resource path.
   * 
   * @param emailPath the path to the email template resource
   * @return the email template content
   * @throws EmailBuildingException if an error occurs while reading the email
   *                                template
   */
  private String getEmail(String emailPath) {
    try {
      Resource emailResource = resourceLoader
          .getResource(String.format("classpath:%s", emailPath));
      return new String(Files.readAllBytes(emailResource.getFile().toPath()));
    } catch (IOException exception) {
      log.warn("Error while building email of path {}", emailPath);
      throw new EmailBuildingException("Error while building email");
    }
  }
}
