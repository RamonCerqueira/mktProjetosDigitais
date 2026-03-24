package com.mktplace.service;

import com.mktplace.enums.NotificationType;
import com.mktplace.model.User;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String fromAddress;
    private final String frontendBaseUrl;

    public EmailNotificationService(JavaMailSender mailSender,
                                    @Value("${app.notifications.email-enabled:false}") boolean enabled,
                                    @Value("${app.notifications.email-from:no-reply@marketplace.local}") String fromAddress,
                                    @Value("${app.notifications.frontend-base-url:http://localhost:3000}") String frontendBaseUrl) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public void sendRegistrationConfirmation(User user) {
        sendHtml(user.getEmail(), "Cadastro confirmado | MicroSaaS Market",
                emailLayout("Cadastro confirmado", "Sua conta foi criada com sucesso e já está pronta para uso.",
                        "Acessar painel", frontendBaseUrl + "/dashboard", "Boas-vindas ao marketplace profissional de compra e venda de projetos digitais."));
    }

    public void sendPasswordReset(User user, String token, int expiresMinutes) {
        String link = frontendBaseUrl + "/reset-password?token=" + token;
        sendHtml(user.getEmail(), "Recuperação de senha | MicroSaaS Market",
                emailLayout("Redefina sua senha", "Recebemos um pedido para redefinir sua senha.",
                        "Redefinir senha", link, "Este link expira em " + expiresMinutes + " minutos. Se você não solicitou, ignore este e-mail."));
    }

    public void sendNegotiationNotification(User user, String subject, String message, String ctaPath) {
        sendHtml(user.getEmail(), subject,
                emailLayout("Atualização de negociação", message,
                        "Abrir negociações", frontendBaseUrl + ctaPath, "Nossa recomendação: revise prazos, condições e evidências antes de prosseguir."));
    }

    public void sendPaymentNotification(User user, String subject, String message) {
        sendHtml(user.getEmail(), subject,
                emailLayout("Atualização de pagamento", message,
                        "Abrir transações", frontendBaseUrl + "/dashboard", "Se houver divergência, abra uma disputa com evidências na plataforma."));
    }

    public void sendIfImportant(User user, NotificationType type, String subject, String body) {
        if (!isImportant(type)) return;
        if (type == NotificationType.PAYMENT_COMPLETED) {
            sendPaymentNotification(user, subject + " | MicroSaaS Market", body);
            return;
        }
        if (type == NotificationType.NEW_OFFER || type == NotificationType.NEW_MESSAGE || type == NotificationType.OFFER_ACCEPTED || type == NotificationType.OFFER_REJECTED) {
            sendNegotiationNotification(user, subject + " | MicroSaaS Market", body, "/dashboard");
            return;
        }
        sendHtml(user.getEmail(), subject + " | MicroSaaS Market",
                emailLayout("Notificação importante", body,
                        "Abrir plataforma", frontendBaseUrl + "/dashboard", "Este aviso foi enviado automaticamente pela plataforma."));
    }

    private void sendHtml(String to, String subject, String html) {
        if (!enabled || to == null || to.isBlank()) return;
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.warn("email_notification_failed to={} subject={} error={}", to, subject, e.getMessage());
        }
    }

    private String emailLayout(String title, String lead, String ctaLabel, String ctaUrl, String footer) {
        return """
                <!doctype html>
                <html lang=\"pt-BR\">
                <body style=\"margin:0;background:#0b1020;font-family:Inter,Arial,sans-serif;color:#0f172a;\">
                  <table width=\"100%%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding:32px 12px;\">
                    <tr><td align=\"center\">
                      <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;border-radius:16px;overflow:hidden;\">
                        <tr><td style=\"background:#0f172a;padding:20px 28px;color:#34d399;font-weight:700;font-size:20px;\">MicroSaaS Market</td></tr>
                        <tr><td style=\"padding:28px;\">
                          <h1 style=\"margin:0 0 12px 0;color:#0f172a;font-size:24px;\">%s</h1>
                          <p style=\"margin:0 0 18px 0;color:#334155;font-size:15px;line-height:1.6;\">%s</p>
                          <a href=\"%s\" style=\"display:inline-block;background:#10b981;color:#ffffff;text-decoration:none;font-weight:600;padding:12px 20px;border-radius:10px;\">%s</a>
                          <p style=\"margin:18px 0 0 0;color:#475569;font-size:13px;line-height:1.6;\">%s</p>
                        </td></tr>
                        <tr><td style=\"padding:18px 28px;background:#f8fafc;color:#64748b;font-size:12px;\">© MicroSaaS Market · Notificação automática da plataforma</td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(title, lead, ctaUrl, ctaLabel, footer);
    }

    private boolean isImportant(NotificationType type) {
        return type == NotificationType.NEW_MESSAGE
                || type == NotificationType.NEW_OFFER
                || type == NotificationType.OFFER_ACCEPTED
                || type == NotificationType.OFFER_REJECTED
                || type == NotificationType.PAYMENT_COMPLETED
                || type == NotificationType.SUBSCRIPTION_EXPIRING;
    }
}
