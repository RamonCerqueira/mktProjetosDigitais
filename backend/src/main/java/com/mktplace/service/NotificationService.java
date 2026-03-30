package com.mktplace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktplace.dto.NotificationDtos.NotificationListResponse;
import com.mktplace.dto.NotificationDtos.NotificationResponse;
import com.mktplace.enums.NotificationType;
import com.mktplace.events.OfferLifecycleEvent;
import com.mktplace.events.SubscriptionLifecycleEvent;
import com.mktplace.events.TransactionLifecycleEvent;
import com.mktplace.exception.BusinessException;
import com.mktplace.model.Notification;
import com.mktplace.model.User;
import com.mktplace.observability.EventEnvelope;
import com.mktplace.repository.NotificationRepository;
import com.mktplace.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailNotificationService emailNotificationService;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               SimpMessagingTemplate messagingTemplate,
                               EmailNotificationService emailNotificationService,
                               ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.emailNotificationService = emailNotificationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processEnvelope(EventEnvelope envelope) {
        switch (envelope.eventType()) {
            case "OfferLifecycleEvent" -> processOfferEvent(envelope);
            case "TransactionLifecycleEvent" -> processTransactionEvent(envelope);
            case "SubscriptionLifecycleEvent" -> processSubscriptionEvent(envelope);
            default -> {
            }
        }
    }

    public NotificationListResponse list(User user, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<NotificationResponse> items = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, safeLimit))
                .stream().map(this::toResponse).toList();
        return new NotificationListResponse(items, unreadCount(user));
    }

    public long unreadCount(User user) {
        return notificationRepository.countByUserIdAndReadAtIsNull(user.getId());
    }

    @Transactional
    public NotificationResponse markAsRead(User user, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new BusinessException("Notificação não encontrada", HttpStatus.NOT_FOUND));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadAtIsNull(user.getId());
        Instant now = Instant.now();
        unread.forEach(item -> item.setReadAt(now));
        notificationRepository.saveAll(unread);
    }

    private void processOfferEvent(EventEnvelope envelope) {
        OfferLifecycleEvent payload = objectMapper.convertValue(envelope.payload(), OfferLifecycleEvent.class);
        if (payload.recipientId() == null) return;

        NotificationType type;
        String title;
        String body;
        switch (payload.action()) {
            case "MESSAGE_SENT" -> {
                type = NotificationType.NEW_MESSAGE;
                title = "Nova mensagem na negociação";
                body = "Você recebeu uma nova mensagem na oferta #" + payload.offerId() + ".";
            }
            case "CREATED", "COUNTERED" -> {
                type = NotificationType.NEW_OFFER;
                title = "Nova proposta recebida";
                body = "Uma proposta de " + formatMoney(payload.amount()) + " foi registrada na oferta #" + payload.offerId() + ".";
            }
            case "ACCEPTED" -> {
                type = NotificationType.OFFER_ACCEPTED;
                title = "Proposta aceita";
                body = "Sua proposta na oferta #" + payload.offerId() + " foi aceita.";
            }
            case "REJECTED" -> {
                type = NotificationType.OFFER_REJECTED;
                title = "Proposta rejeitada";
                body = "Sua proposta na oferta #" + payload.offerId() + " foi rejeitada.";
            }
            default -> {
                return;
            }
        }
        createAndDispatch(payload.recipientId(), type, title, body, eventKey(envelope, payload.offerId(), payload.recipientId(), payload.action()));
    }

    private void processTransactionEvent(EventEnvelope envelope) {
        TransactionLifecycleEvent payload = objectMapper.convertValue(envelope.payload(), TransactionLifecycleEvent.class);
        if (!"HELD".equals(payload.status()) && !"payment_intent.succeeded".equals(payload.action())) return;

        List<Long> recipients = new ArrayList<>();
        recipients.add(payload.buyerId());
        if (payload.sellerId() != null && !payload.sellerId().equals(payload.buyerId())) recipients.add(payload.sellerId());

        for (Long recipientId : recipients) {
            createAndDispatch(
                    recipientId,
                    NotificationType.PAYMENT_COMPLETED,
                    "Pagamento confirmado",
                    "Pagamento de " + formatMoney(payload.amount()) + " confirmado para a transação #" + payload.transactionId() + ".",
                    eventKey(envelope, payload.transactionId(), recipientId, payload.action())
            );
        }
    }

    private void processSubscriptionEvent(EventEnvelope envelope) {
        SubscriptionLifecycleEvent payload = objectMapper.convertValue(envelope.payload(), SubscriptionLifecycleEvent.class);
        if (!"PAST_DUE".equals(payload.action()) && !"subscription.payment_failed".equals(payload.action())) return;
        createAndDispatch(
                payload.userId(),
                NotificationType.SUBSCRIPTION_EXPIRING,
                "Assinatura expirada ou pendente",
                "Sua assinatura entrou em estado pendente. Regularize o pagamento para manter anúncios ativos.",
                eventKey(envelope, payload.subscriptionId(), payload.userId(), payload.action())
        );
    }

    private void createAndDispatch(Long userId, NotificationType type, String title, String body, String eventKey) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        if (notificationRepository.findByEventKeyAndUserId(eventKey, userId).isPresent()) return;

        Notification saved = notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .eventKey(eventKey)
                .createdAt(Instant.now())
                .build());

        NotificationResponse response = toResponse(saved);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, response);
        emailNotificationService.sendIfImportant(user, type, title, body);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    private String eventKey(EventEnvelope envelope, Long sourceId, Long userId, String action) {
        return envelope.eventType() + ":" + sourceId + ":" + userId + ":" + action;
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "R$ 0,00";
        return "R$ " + String.format(Locale.forLanguageTag("pt-BR"), "%,.2f", value).replace(',', 'X').replace('.', ',').replace('X', '.');
    }
}
