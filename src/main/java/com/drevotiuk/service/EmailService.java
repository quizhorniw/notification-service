package com.drevotiuk.service;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
  private static final String CHARSET = "utf-8";

  private final JavaMailSender mailSender;

  /**
   * Sends an email asynchronously.
   * 
   * @param to      the recipient's email address
   * @param subject the subject of the email
   * @param email   the email content
   * @throws EmailSendingException if an error occurs while sending the email
   */
  @Async
  public void send(String from, String to, String subject, String email) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, CHARSET);
      helper.setText(email, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(from);
      log.info("Sending email to {} with subject \"{}\"", to, subject);
      mailSender.send(mimeMessage);
    } catch (Exception e) {
      log.warn("Failed to send email");
      throw new EmailSendingException("Failed to send email", e);
    }
  }
}
