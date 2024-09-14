package com.drevotiuk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import com.drevotiuk.model.exception.EmailSendingException;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
  @Mock
  private JavaMailSender mailSender;
  private EmailService underTest;

  @BeforeEach
  void setUp() {
    underTest = new EmailService(mailSender);
  }

  @Test
  void shouldSendEmail() throws MessagingException, IOException {
    // given
    String from = "testmail_from@mail.com";
    String to = "testmail_to@mail.com";
    String subject = "Test Subject";
    String email = "Test Email Content";
    MimeMessage mimeMessage = new MimeMessage((Session) null);
    given(mailSender.createMimeMessage()).willReturn(mimeMessage);

    // when
    underTest.send(from, to, subject, email);

    // then
    assertThat(mimeMessage.getHeader("From")).contains(from);
    assertThat(mimeMessage.getHeader("To")).contains(to);
    assertThat(mimeMessage.getContent()).isInstanceOf(String.class).isEqualTo(email);
    assertThat(mimeMessage.getSubject()).isEqualTo(subject);
    verify(mailSender).send(mimeMessage);
  }

  @Test
  void shouldThrowWhenEmailDetailsAreInvalid() throws MessagingException, IOException {
    // given
    String from = null; // Triggers exception throwing
    String to = "testmail_to@mail.com";
    String subject = "Test Subject";
    String email = "Test Email Content";
    MimeMessage mimeMessage = new MimeMessage((Session) null);
    given(mailSender.createMimeMessage()).willReturn(mimeMessage);

    // when
    // then
    assertThatThrownBy(() -> underTest.send(from, to, subject, email))
        .isInstanceOf(EmailSendingException.class)
        .hasMessageContaining("Failed to send email");

    verify(mailSender, never()).send(mimeMessage);
  }
}
