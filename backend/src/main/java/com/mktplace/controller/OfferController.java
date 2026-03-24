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
    @PostMapping("/counter") public OfferResponse counter(@RequestBody CounterOfferRequest request) { return offerService.counterOffer(userContextService.getCurrentUser(), request); }
    @PostMapping("/{offerId}/accept") public OfferResponse accept(@PathVariable Long offerId) { return offerService.acceptOffer(userContextService.getCurrentUser(), offerId); }
    @PostMapping("/{offerId}/reject") public OfferResponse reject(@PathVariable Long offerId) { return offerService.rejectOffer(userContextService.getCurrentUser(), offerId); }
    @GetMapping public List<OfferResponse> listMine() { return offerService.myOffers(userContextService.getCurrentUser()); }
    @GetMapping("/{offerId}/history") public List<OfferHistoryResponse> history(@PathVariable Long offerId) { return offerService.history(userContextService.getCurrentUser(), offerId); }
    @GetMapping("/{offerId}/messages") public List<MessageResponse> listMessages(@PathVariable Long offerId) { return offerService.listMessages(userContextService.getCurrentUser(), offerId); }
    @PostMapping("/messages") public MessageResponse sendMessage(@RequestBody MessageRequest request) { return offerService.sendMessage(userContextService.getCurrentUser(), request); }
}
