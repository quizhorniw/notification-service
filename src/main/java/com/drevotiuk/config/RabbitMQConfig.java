package com.drevotiuk.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration class that defines the exchange, queues, and bindings
 * for the messaging system.
 * 
 * <p>
 * This class sets up RabbitMQ-related beans, including the exchange, queues,
 * routing keys, and the bindings to connect queues to the exchange.
 */
@Configuration
public class RabbitMQConfig {
  @Value("${rabbitmq.exchange.notification-service}")
  private String notificationServiceExchange;

  @Value("${rabbitmq.queue.email-verification}")
  private String emailVerificationQueue;
  @Value("${rabbitmq.queue.order-created}")
  private String orderQueue;

  @Value("${rabbitmq.routingkey.email-verification}")
  private String emailVerificationRoutingKey;
  @Value("${rabbitmq.routingkey.order-created}")
  private String orderRoutingKey;

  /**
   * Defines the {@link DirectExchange} bean for the notification service.
   * 
   * @return a {@link DirectExchange} configured with the notification service
   *         exchange name
   */
  @Bean
  public DirectExchange notificationServiceExchange() {
    return new DirectExchange(notificationServiceExchange);
  }

  /**
   * Defines the {@link Queue} bean for email verification messages.
   * 
   * @return a {@link Queue} configured with the email verification queue name
   */
  @Bean
  public Queue emailVerificationQueue() {
    return new Queue(emailVerificationQueue);
  }

  /**
   * Defines the {@link Queue} bean for order creation messages.
   * 
   * @return a {@link Queue} configured with the order-created queue name
   */
  @Bean
  public Queue orderQueue() {
    return new Queue(orderQueue);
  }

  /**
   * Defines the {@link Binding} between the email verification queue and the
   * notification service exchange.
   * 
   * <p>
   * This binding uses the email verification routing key to route messages to the
   * appropriate queue.
   * 
   * @return a {@link Binding} for the email verification queue
   */
  @Bean
  public Binding emailVerificationBinding() {
    return BindingBuilder.bind(emailVerificationQueue()).to(notificationServiceExchange())
        .with(emailVerificationRoutingKey);
  }

  /**
   * Defines the {@link Binding} between the order queue and the notification
   * service exchange.
   * 
   * <p>
   * This binding uses the order-created routing key to route messages to the
   * appropriate queue.
   * 
   * @return a {@link Binding} for the order-created queue
   */
  @Bean
  public Binding orderBinding() {
    return BindingBuilder.bind(orderQueue()).to(notificationServiceExchange())
        .with(orderRoutingKey);
  }
}
