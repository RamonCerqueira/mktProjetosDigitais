package com.mktplace.controller;

import com.mktplace.dto.AuthDtos.TransactionResponse;
import com.mktplace.service.TransactionService;
import com.mktplace.service.UserContextService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@PreAuthorize("isAuthenticated()")
public class TransactionController {
    private final TransactionService transactionService;
    private final UserContextService userContextService;
    public TransactionController(TransactionService transactionService, UserContextService userContextService) { this.transactionService = transactionService; this.userContextService = userContextService; }
    @PostMapping("/purchase/{projectId}") public TransactionResponse purchase(@PathVariable Long projectId) { return transactionService.purchase(userContextService.getCurrentUser(), projectId); }
}
