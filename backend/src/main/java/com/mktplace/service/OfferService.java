package com.mktplace.service;

import com.mktplace.dto.AuthDtos.*;
import com.mktplace.enums.OfferActionType;
import com.mktplace.enums.OfferStatus;
import com.mktplace.exception.BusinessException;
import com.mktplace.model.Message;
import com.mktplace.model.Offer;
import com.mktplace.model.OfferHistory;
import com.mktplace.model.User;
import com.mktplace.repository.MessageRepository;
import com.mktplace.repository.OfferHistoryRepository;
import com.mktplace.repository.OfferRepository;
import com.mktplace.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.mktplace.validation.InputSanitizer.clean;

@Service
public class OfferService {
    private final OfferRepository offerRepository;
    private final OfferHistoryRepository offerHistoryRepository;
    private final MessageRepository messageRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FraudPreventionService fraudPreventionService;
    private final AuditService auditService;

    public OfferService(OfferRepository offerRepository, OfferHistoryRepository offerHistoryRepository, MessageRepository messageRepository, ProjectService projectService, UserRepository userRepository, SimpMessagingTemplate messagingTemplate, FraudPreventionService fraudPreventionService, AuditService auditService) {
        this.offerRepository = offerRepository;
        this.offerHistoryRepository = offerHistoryRepository;
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
        Offer offer = offerRepository.save(Offer.builder().project(project).buyer(buyer).seller(project.getSeller()).proposer(buyer).negotiationKey(UUID.randomUUID().toString()).amount(request.amount()).status(OfferStatus.OPEN).createdAt(Instant.now()).updatedAt(Instant.now()).build());
        appendHistory(offer, buyer, OfferActionType.CREATED, request.amount(), "Proposta inicial");
        auditService.logAction("OFFER_CREATED", "OFFER", String.valueOf(offer.getId()), "project=" + project.getId());
        return toResponse(offer);
    }

    public OfferResponse counterOffer(User actor, CounterOfferRequest request) {
        Offer current = getNegotiationOffer(request.offerId());
        ensureParticipant(actor, current);
        ensureOpen(current);
        if (current.getProposer().getId().equals(actor.getId())) throw new BusinessException("Não é permitido contra-propor sua própria oferta", HttpStatus.BAD_REQUEST);
        fraudPreventionService.validateOffer(current.getBuyer(), current.getProject(), request.amount());
        Offer counter = offerRepository.save(Offer.builder().project(current.getProject()).buyer(current.getBuyer()).seller(current.getSeller()).proposer(actor).parentOffer(current).negotiationKey(current.getNegotiationKey()).amount(request.amount()).status(OfferStatus.OPEN).createdAt(Instant.now()).updatedAt(Instant.now()).build());
        current.setStatus(OfferStatus.CANCELED);
        current.setUpdatedAt(Instant.now());
        offerRepository.save(current);
        appendHistory(counter, actor, OfferActionType.COUNTERED, request.amount(), "Contra-proposta criada");
        auditService.logAction("OFFER_COUNTERED", "OFFER", String.valueOf(counter.getId()), current.getNegotiationKey());
        return toResponse(counter);
    }

    public OfferResponse acceptOffer(User actor, Long offerId) {
        Offer offer = getNegotiationOffer(offerId);
        ensureParticipant(actor, offer);
        ensureOpen(offer);
        if (offer.getProposer().getId().equals(actor.getId())) throw new BusinessException("O proponente não pode aceitar a própria proposta", HttpStatus.BAD_REQUEST);
        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setUpdatedAt(Instant.now());
        offerRepository.save(offer);
        appendHistory(offer, actor, OfferActionType.ACCEPTED, offer.getAmount(), "Oferta aceita");
        auditService.logAction("OFFER_ACCEPTED", "OFFER", String.valueOf(offer.getId()), offer.getNegotiationKey());
        return toResponse(offer);
    }

    public OfferResponse rejectOffer(User actor, Long offerId) {
        Offer offer = getNegotiationOffer(offerId);
        ensureParticipant(actor, offer);
        ensureOpen(offer);
        if (offer.getProposer().getId().equals(actor.getId())) throw new BusinessException("O proponente não pode rejeitar a própria proposta", HttpStatus.BAD_REQUEST);
        offer.setStatus(OfferStatus.REJECTED);
        offer.setUpdatedAt(Instant.now());
        offerRepository.save(offer);
        appendHistory(offer, actor, OfferActionType.REJECTED, offer.getAmount(), "Oferta rejeitada");
        auditService.logAction("OFFER_REJECTED", "OFFER", String.valueOf(offer.getId()), offer.getNegotiationKey());
        return toResponse(offer);
    }

    public List<OfferResponse> myOffers(User user) {
        return offerRepository.findByBuyerIdOrSellerId(user.getId(), user.getId()).stream().map(this::toResponse).toList();
    }

    public List<OfferHistoryResponse> history(User user, Long offerId) {
        Offer offer = getNegotiationOffer(offerId);
        ensureParticipant(user, offer);
        return offerHistoryRepository.findByOfferNegotiationKeyOrderByCreatedAtAsc(offer.getNegotiationKey()).stream()
                .map(item -> new OfferHistoryResponse(item.getId(), item.getOffer().getId(), item.getActor().getId(), item.getActionType().name(), item.getAmount(), item.getDetails(), item.getCreatedAt()))
                .toList();
    }

    public MessageResponse sendMessage(User sender, MessageRequest request) {
        Offer offer = getNegotiationOffer(request.offerId());
        User receiver = userRepository.findById(request.receiverId()).orElseThrow(() -> new BusinessException("Destinatário não encontrado", HttpStatus.NOT_FOUND));
        ensureParticipant(sender, offer);
        if (!offer.getBuyer().getId().equals(receiver.getId()) && !offer.getSeller().getId().equals(receiver.getId())) throw new BusinessException("Destinatário inválido para esta negociação", HttpStatus.BAD_REQUEST);
        Message message = messageRepository.save(Message.builder().offer(offer).sender(sender).receiver(receiver).content(clean(request.content())).createdAt(Instant.now()).build());
        appendHistory(offer, sender, OfferActionType.MESSAGE_SENT, offer.getAmount(), "Mensagem enviada");
        MessageResponse response = new MessageResponse(message.getId(), offer.getId(), offer.getNegotiationKey(), sender.getId(), sender.getName(), receiver.getId(), receiver.getName(), message.getContent(), message.getCreatedAt());
        messagingTemplate.convertAndSend("/topic/offers/" + offer.getNegotiationKey(), response);
        auditService.logAction("MESSAGE_SENT", "OFFER", String.valueOf(offer.getId()), "receiver=" + receiver.getId());
        return response;
    }

    public List<MessageResponse> listMessages(User user, Long offerId) {
        Offer offer = getNegotiationOffer(offerId);
        ensureParticipant(user, offer);
        return messageRepository.findByNegotiationKeyOrderByCreatedAtAsc(offer.getNegotiationKey()).stream().map(message -> new MessageResponse(message.getId(), offerId, offer.getNegotiationKey(), message.getSender().getId(), message.getSender().getName(), message.getReceiver().getId(), message.getReceiver().getName(), message.getContent(), message.getCreatedAt())).toList();
    }

    private Offer getNegotiationOffer(Long offerId) {
        return offerRepository.findById(offerId).orElseThrow(() -> new BusinessException("Oferta não encontrada", HttpStatus.NOT_FOUND));
    }

    private void ensureParticipant(User actor, Offer offer) {
        if (!offer.getBuyer().getId().equals(actor.getId()) && !offer.getSeller().getId().equals(actor.getId())) throw new BusinessException("Sem permissão na negociação", HttpStatus.FORBIDDEN);
    }

    private void ensureOpen(Offer offer) {
        if (offer.getStatus() != OfferStatus.OPEN) throw new BusinessException("Somente ofertas em aberto podem ser manipuladas", HttpStatus.BAD_REQUEST);
    }

    private void appendHistory(Offer offer, User actor, OfferActionType action, java.math.BigDecimal amount, String details) {
        offerHistoryRepository.save(OfferHistory.builder().offer(offer).actor(actor).actionType(action).amount(amount).details(details).createdAt(Instant.now()).build());
    }

    private OfferResponse toResponse(Offer offer) {
        return new OfferResponse(offer.getId(), offer.getProject().getId(), offer.getAmount(), offer.getStatus().name(), offer.getBuyer().getId(), offer.getBuyer().getName(), offer.getSeller().getId(), offer.getSeller().getName(), offer.getProposer().getId(), offer.getParentOffer() == null ? null : offer.getParentOffer().getId(), offer.getNegotiationKey());
    }
}
