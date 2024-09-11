package com.drevotiuk.service;

import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.drevotiuk.model.UserView;
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
  @Value("${services.notification.email.topic.verification}")
  private String verificationEmailTopic;
  @Value("${services.notification.email.topic.order-created}")
  private String orderCreatedTopic;
  @Value("${rabbitmq.exchange.user-service}")
  private String userServiceExchange;
  @Value("${rabbitmq.routingkey.user}")
  private String userRoutingKey;

  private final EmailService emailService;
  private final RabbitTemplate rabbitTemplate;

  /**
   * Consumes messages from the email verification queue and processes them
   * to send a verification email to the user.
   * 
   * @param details the details for the email verification
   */
  @RabbitListener(queues = "${rabbitmq.queue.email-verification}")
  public void consumeEmailVerification(EmailVerificationDetails details) {
    log.info("Received email verification message; email: {}", details.getEmail());
    String email = emailService.buildVerificationEmail(details.getFirstName(), details.getLink());
    emailService.send(details.getEmail(), verificationEmailTopic, email);
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
    UserView user = fetchUser(userId);
    String emailContent = emailService.buildOrderCreatedEmail(
        user.getFirstName(), details.getOrderTime(), details.getTotalPrice());
    emailService.send(user.getEmail(), orderCreatedTopic, emailContent);
  }

  /**
   * Fetches user details by their ID from the user service via RabbitMQ.
   * 
   * @param userId the ID of the user to fetch
   * @return the {@link UserView} containing user details
   * @throws UserNotFoundException if the user cannot be found
   */
  private UserView fetchUser(String userId) {
    Optional<UserView> optionalUser = getUserById(userId);

    if (!optionalUser.isPresent()) {
      log.warn("User not found with ID {}", userId);
      throw new UserNotFoundException("User not found");
    }

    return optionalUser.get();
  }

  /**
   * Retrieves user details by sending a request to the user service via RabbitMQ.
   * 
   * @param userId the ID of the user to retrieve
   * @return an {@link Optional} containing the {@link UserView} if found, or an
   *         empty {@link Optional} if not
   * @throws MessagingException if unexpected error occurs
   */
  @SuppressWarnings("unchecked")
  private Optional<UserView> getUserById(String userId) {
    Object message = rabbitTemplate.convertSendAndReceive(userServiceExchange, userRoutingKey, userId);

    if (message == null || !(message instanceof Optional)) {
      log.warn("Unexpected error while receiving user with ID {}", userId);
      throw new MessagingException("Unexpected error while receiving user");
    }

    return (Optional<UserView>) message;
  }
}
