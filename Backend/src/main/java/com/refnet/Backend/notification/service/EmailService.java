package com.refnet.Backend.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.notification.email-mode:MOCK}")
    private String emailMode;

    @Value("${spring.mail.username:noreply@refnet.com}")
    private String fromEmail;

    @Async("notificationExecutor")
    public void sendEmail(String to, String subject, String body) {
        try {
            switch (emailMode.toUpperCase()) {
                case "REAL":
                case "TEST":
                    sendRealEmail(to, subject, body);
                    break;
                case "MOCK":
                default:
                    logMockEmail(to, subject, body);
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // Fail silently to not break the main API flow as per Task 7
        }
    }

    private void sendRealEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Email sent successfully to {} (Mode: {})", to, emailMode);
    }

    private void logMockEmail(String to, String subject, String body) {
        log.info("\n********** MOCK EMAIL START **********\n" +
                "To: {}\n" +
                "Subject: {}\n" +
                "Body: {}\n" +
                "********** MOCK EMAIL END ************", to, subject, body);
    }
}
