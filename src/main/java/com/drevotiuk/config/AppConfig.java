package com.drevotiuk.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * Configuration class for defining beans in the Spring application context.
 * This class provides bean definitions related to messaging and resource
 * loading.
 */
@Configuration
public class AppConfig {
  /**
   * Creates a {@link ResourceLoader} bean to load resources in the application.
   *
   * @return a {@link DefaultResourceLoader} instance
   */
  @Bean
  public ResourceLoader resourceLoader() {
    return new DefaultResourceLoader();
  }

  /**
   * Creates a {@link MessageConverter} bean to handle JSON message conversion.
   * 
   * <p>
   * This bean uses the {@link Jackson2JsonMessageConverter} to convert messages
   * to and from JSON format.
   *
   * @return a {@link Jackson2JsonMessageConverter} instance for JSON message
   *         conversion
   */
  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  /**
   * Creates an {@link AmqpTemplate} bean for sending and receiving messages via
   * RabbitMQ.
   * 
   * <p>
   * This method configures a {@link RabbitTemplate} with a connection factory and
   * assigns
   * the {@link MessageConverter} for JSON message conversion.
   *
   * @param connectionFactory the RabbitMQ connection factory
   * @return a configured {@link RabbitTemplate} with JSON message conversion
   *         capabilities
   */
  @Bean
  public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(messageConverter());
    return rabbitTemplate;
  }
}
