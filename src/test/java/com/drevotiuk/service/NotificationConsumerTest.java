package com.drevotiuk.service;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.drevotiuk.model.EmailVerificationDetails;
import com.drevotiuk.model.OrderEmailDetails;
import com.drevotiuk.model.UserView;
import com.drevotiuk.model.exception.EmailBuildingException;
import com.drevotiuk.model.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
public class NotificationConsumerTest {
  @Mock
  private EmailService emailService;
  @Mock
  private RabbitTemplate rabbitTemplate;
  @Mock
  private ResourceLoader resourceLoader;
  @Mock
  private Resource resource;
  private NotificationConsumer underTest;

  @BeforeEach
  void setUp() {
    underTest = new NotificationConsumer(emailService, rabbitTemplate, resourceLoader);
  }

  @Test
  void shouldConsumeEmailVerification() throws IOException, IllegalAccessException, NoSuchFieldException {
    // given
    setDeclaredField(underTest, "from", "mycompany@mail.com");
    setDeclaredField(underTest, "verificationEmailTopic", "TEST TOPIC");
    EmailVerificationDetails details = new EmailVerificationDetails("johndoe@mail.com", "John",
        "http://test-link.org");
    given(resourceLoader.getResource(anyString())).willReturn(resource);
    File mockFile = Files.createTempFile("test-email", ".html").toFile();
    given(resource.getFile()).willReturn(mockFile);

    // when
    underTest.consumeEmailVerification(details);

    // then
    verify(emailService).send(
        eq("mycompany@mail.com"),
        eq("johndoe@mail.com"),
        eq("TEST TOPIC"),
        anyString());
  }

  @Test
  void shouldThrowWhenResourceIsInvalidInVerificationEmailConsumer()
      throws IOException, IllegalAccessException, NoSuchFieldException {
    // given
    setDeclaredField(underTest, "from", "mycompany@mail.com");
    setDeclaredField(underTest, "verificationEmailTopic", "TEST TOPIC");
    EmailVerificationDetails details = new EmailVerificationDetails("johndoe@mail.com", "John",
        "http://test-link.org");
    given(resourceLoader.getResource(anyString())).willReturn(resource);
    given(resource.getFile()).willReturn(new File("/unexisting/path"));

    // when
    // then
    assertThatThrownBy(() -> underTest.consumeEmailVerification(details))
        .isInstanceOf(EmailBuildingException.class)
        .hasMessageContaining("Error while building email");

    verify(emailService, never()).send(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void shouldConsumeOrderCreatedEmail() throws IOException, IllegalAccessException, NoSuchFieldException {
    // given
    setDeclaredField(underTest, "from", "mycompany@mail.com");
    setDeclaredField(underTest, "orderCreatedTopic", "TEST TOPIC");
    setDeclaredField(underTest, "tokenExpiration", "999");
    OrderEmailDetails details = new OrderEmailDetails("id", LocalDate.now().toString(), BigDecimal.TEN);
    given(rabbitTemplate.convertSendAndReceive(any(), any(), eq("id")))
        .willReturn(new UserView("John", "Doe", "johndoe@mail.com", LocalDate.now().toString()));
    given(resourceLoader.getResource(anyString())).willReturn(resource);
    File mockFile = Files.createTempFile("test-email", ".html").toFile();
    given(resource.getFile()).willReturn(mockFile);

    // when
    underTest.consumeOrderCreated(details);

    // then
    verify(emailService).send(
        eq("mycompany@mail.com"),
        eq("johndoe@mail.com"),
        eq("TEST TOPIC"),
        anyString());
  }

  @Test
  void shouldThrowWhenUserIsNullInOrderCreatedConsumer() {
    // given
    OrderEmailDetails details = new OrderEmailDetails("id", LocalDate.now().toString(), BigDecimal.TEN);
    given(rabbitTemplate.convertSendAndReceive(any(), any(), eq("id"))).willReturn(null);

    // when
    // then
    assertThatThrownBy(() -> underTest.consumeOrderCreated(details))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");

    verify(emailService, never()).send(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void shouldThrowWhenUserIsOfInvalidTypeInOrderCreatedConsumer() {
    // given
    OrderEmailDetails details = new OrderEmailDetails("id", LocalDate.now().toString(), BigDecimal.TEN);
    // Returning wrong type to trigger exception throwing
    given(rabbitTemplate.convertSendAndReceive(any(), any(), eq("id"))).willReturn(BigDecimal.TEN);

    // when
    // then
    assertThatThrownBy(() -> underTest.consumeOrderCreated(details))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");

    verify(emailService, never()).send(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void shouldThrowWhenResourceIsInvalidInOrderCreatedConsumer()
      throws IOException, IllegalAccessException, NoSuchFieldException {
    // given
    setDeclaredField(underTest, "from", "mycompany@mail.com");
    setDeclaredField(underTest, "orderCreatedTopic", "TEST TOPIC");
    setDeclaredField(underTest, "tokenExpiration", "999");
    OrderEmailDetails details = new OrderEmailDetails("id", LocalDate.now().toString(), BigDecimal.TEN);
    given(rabbitTemplate.convertSendAndReceive(any(), any(), eq("id")))
        .willReturn(new UserView("John", "Doe", "johndoe@mail.com", LocalDate.now().toString()));
    given(resourceLoader.getResource(anyString())).willReturn(resource);
    given(resource.getFile()).willReturn(new File("/unexisting/path"));

    // when
    // then
    assertThatThrownBy(() -> underTest.consumeOrderCreated(details))
        .isInstanceOf(EmailBuildingException.class)
        .hasMessageContaining("Error while building email");

    verify(emailService, never()).send(anyString(), anyString(), anyString(), anyString());
  }

  private void setDeclaredField(Object target, String fieldName, Object value)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
    field.setAccessible(false);
  }
}
