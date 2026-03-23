package com.mktplace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktplace.dto.AuthDtos.TransactionResponse;
import com.mktplace.dto.AuthDtos.TransactionWebhookRequest;
import com.mktplace.exception.BusinessException;
import com.mktplace.service.TransactionService;
import com.mktplace.service.UserContextService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final UserContextService userContextService;
    private final ObjectMapper objectMapper;
    public TransactionController(TransactionService transactionService, UserContextService userContextService, ObjectMapper objectMapper) { this.transactionService = transactionService; this.userContextService = userContextService; this.objectMapper = objectMapper; }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/purchase/{projectId}") public TransactionResponse purchase(@PathVariable Long projectId) { return transactionService.purchase(userContextService.getCurrentUser(), projectId); }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{transactionId}/confirm") public TransactionResponse confirm(@PathVariable Long transactionId) { return transactionService.confirmReceipt(userContextService.getCurrentUser(), transactionId); }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{transactionId}/refund") public TransactionResponse refund(@PathVariable Long transactionId) { return transactionService.refund(userContextService.getCurrentUser(), transactionId); }
    @PostMapping("/webhook/stripe") public void stripeWebhook(@RequestHeader(value = "Stripe-Signature", required = false) String signature, @RequestBody String payload) throws Exception { if (!transactionService.validateStripeSignature(payload, signature)) throw new BusinessException("Assinatura do webhook Stripe inválida", HttpStatus.UNAUTHORIZED); transactionService.handleStripeWebhook(objectMapper.readValue(payload, TransactionWebhookRequest.class)); }
}
