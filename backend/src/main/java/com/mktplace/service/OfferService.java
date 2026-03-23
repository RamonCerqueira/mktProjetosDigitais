package com.mktplace.service;

import com.mktplace.dto.AuthDtos.*;
import com.mktplace.enums.OfferStatus;
import com.mktplace.exception.BusinessException;
import com.mktplace.model.Message;
import com.mktplace.model.Offer;
import com.mktplace.model.User;
import com.mktplace.repository.MessageRepository;
import com.mktplace.repository.OfferRepository;
import com.mktplace.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static com.mktplace.validation.InputSanitizer.clean;

@Service
public class OfferService {
    private final OfferRepository offerRepository;
    private final MessageRepository messageRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FraudPreventionService fraudPreventionService;
    private final AuditService auditService;

    public OfferService(OfferRepository offerRepository, MessageRepository messageRepository, ProjectService projectService, UserRepository userRepository, SimpMessagingTemplate messagingTemplate, FraudPreventionService fraudPreventionService, AuditService auditService) {
        this.offerRepository = offerRepository;
        this.messageRepository = messageRepository;
        this.projectService = projectService;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.fraudPreventionService = fraudPreventionService;
        this.auditService = auditService;
    }

    public OfferResponse createOffer(User buyer, OfferRequest request) {
        var project = projectService.getEntity(request.projectId());
        fraudPreventionService.validateOffer(buyer, project, request.amount());
        Offer offer = offerRepository.save(Offer.builder().project(project).buyer(buyer).seller(project.getSeller()).amount(request.amount()).status(OfferStatus.OPEN).createdAt(Instant.now()).build());
        auditService.logAction("OFFER_CREATED", "OFFER", String.valueOf(offer.getId()), "project=" + project.getId());
        return toResponse(offer);
    }

    public List<OfferResponse> myOffers(User user) {
        return offerRepository.findByBuyerIdOrSellerId(user.getId(), user.getId()).stream().map(this::toResponse).toList();
    }

    public MessageResponse sendMessage(User sender, MessageRequest request) {
        Offer offer = offerRepository.findById(request.offerId()).orElseThrow(() -> new BusinessException("Oferta não encontrada", HttpStatus.NOT_FOUND));
        User receiver = userRepository.findById(request.receiverId()).orElseThrow(() -> new BusinessException("Destinatário não encontrado", HttpStatus.NOT_FOUND));
        if (!offer.getBuyer().getId().equals(sender.getId()) && !offer.getSeller().getId().equals(sender.getId())) throw new BusinessException("Sem permissão na conversa", HttpStatus.FORBIDDEN);
        Message message = messageRepository.save(Message.builder().offer(offer).sender(sender).receiver(receiver).content(clean(request.content())).createdAt(Instant.now()).build());
        MessageResponse response = new MessageResponse(message.getId(), offer.getId(), sender.getId(), receiver.getId(), message.getContent(), message.getCreatedAt());
        messagingTemplate.convertAndSend("/topic/offers/" + offer.getId(), response);
        auditService.logAction("MESSAGE_SENT", "OFFER", String.valueOf(offer.getId()), "receiver=" + receiver.getId());
        return response;
    }

    public List<MessageResponse> listMessages(Long offerId) {
        return messageRepository.findByOfferIdOrderByCreatedAtAsc(offerId).stream().map(message -> new MessageResponse(message.getId(), offerId, message.getSender().getId(), message.getReceiver().getId(), message.getContent(), message.getCreatedAt())).toList();
    }

    private OfferResponse toResponse(Offer offer) {
        return new OfferResponse(offer.getId(), offer.getProject().getId(), offer.getAmount(), offer.getStatus().name(), offer.getBuyer().getId(), offer.getSeller().getId());
    }
}
