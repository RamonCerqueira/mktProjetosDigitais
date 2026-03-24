package com.mktplace.service;

import com.mktplace.enums.NotificationType;
import com.mktplace.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String fromAddress;

    public EmailNotificationService(JavaMailSender mailSender,
                                    @Value("${app.notifications.email-enabled:false}") boolean enabled,
                                    @Value("${app.notifications.email-from:no-reply@marketplace.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
    }

    public void sendIfImportant(User user, NotificationType type, String subject, String body) {
        if (!enabled || user.getEmail() == null || user.getEmail().isBlank()) return;
        if (!isImportant(type)) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("email_notification_failed user_id={} type={} error={}", user.getId(), type, e.getMessage());
        }
    }

    private boolean isImportant(NotificationType type) {
        return type == NotificationType.OFFER_ACCEPTED
                || type == NotificationType.OFFER_REJECTED
                || type == NotificationType.PAYMENT_COMPLETED
                || type == NotificationType.SUBSCRIPTION_EXPIRING;
    }
}
