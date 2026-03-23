package com.mktplace.controller;

import com.mktplace.dto.AuthDtos.*;
import com.mktplace.service.OfferService;
import com.mktplace.service.UserContextService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offers")
@PreAuthorize("isAuthenticated()")
public class OfferController {
    private final OfferService offerService;
    private final UserContextService userContextService;
    public OfferController(OfferService offerService, UserContextService userContextService) { this.offerService = offerService; this.userContextService = userContextService; }
    @PostMapping public OfferResponse create(@RequestBody OfferRequest request) { return offerService.createOffer(userContextService.getCurrentUser(), request); }
    @GetMapping public List<OfferResponse> listMine() { return offerService.myOffers(userContextService.getCurrentUser()); }
    @GetMapping("/{offerId}/messages") public List<MessageResponse> listMessages(@PathVariable Long offerId) { return offerService.listMessages(offerId); }
    @PostMapping("/messages") public MessageResponse sendMessage(@RequestBody MessageRequest request) { return offerService.sendMessage(userContextService.getCurrentUser(), request); }
}
