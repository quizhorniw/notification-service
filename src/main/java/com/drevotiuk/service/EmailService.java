package com.drevotiuk.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.MessageFormat;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.drevotiuk.model.exception.EmailBuildingException;
import com.drevotiuk.model.exception.EmailSendingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A service class responsible for building and sending emails.
 * 
 * <p>
 * This class uses {@link JavaMailSender} to send emails asynchronously.
 * It also builds email content using templates stored in resources.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
  @Value("${services.notification.email.from}")
  private String from;
  @Value("${resources.email.verification}")
  private String verificationEmailResourcePath;
  @Value("${resources.email.order-created}")
  private String orderCreatedResourcePath;
  @Value("${security.confirmation-token.expiration}")
  private String tokenExpiration;

  private static final String CHARSET = "utf-8";

  private final JavaMailSender mailSender;
  private final ResourceLoader resourceLoader;

  /**
   * Sends an email asynchronously.
   * 
   * @param to      the recipient's email address
   * @param subject the subject of the email
   * @param email   the email content
   * @throws EmailSendingException if an error occurs while sending the email
   */
  @Async
  public void send(String to, String subject, String email) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, CHARSET);
      helper.setText(email, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(from);
      log.info("Sending email to {} with subject \"{}\"", to, subject);
      mailSender.send(mimeMessage);
    } catch (MessagingException e) {
      log.warn("Failed to send email");
      throw new EmailSendingException("Failed to send email", e);
    }
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
