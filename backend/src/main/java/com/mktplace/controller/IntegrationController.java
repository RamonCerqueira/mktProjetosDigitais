package com.mktplace.controller;

import com.mktplace.dto.IntegrationDtos.CepResponse;
import com.mktplace.dto.IntegrationDtos.CnpjResponse;
import com.mktplace.dto.IntegrationDtos.DocumentValidationResponse;
import com.mktplace.enums.DocumentType;
import com.mktplace.service.IntegrationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/integrations")
public class IntegrationController {
    private final IntegrationService integrationService;

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping("/cep/{cep}")
    public CepResponse lookupCep(@PathVariable String cep) {
        return integrationService.fetchAddressByCep(cep);
    }

    @GetMapping("/cnpj/{cnpj}")
    public CnpjResponse lookupCnpj(@PathVariable String cnpj) {
        return integrationService.fetchCompanyByCnpj(cnpj);
    }

    @GetMapping("/validate/{type}/{document}")
    public DocumentValidationResponse validate(@PathVariable DocumentType type, @PathVariable String document) {
        return integrationService.validateDocument(type, document);
    }
}
