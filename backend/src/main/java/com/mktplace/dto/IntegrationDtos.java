package com.mktplace.dto;

public class IntegrationDtos {
    public record CepResponse(String cep, String street, String complement, String neighborhood, String city, String state) {}
    public record CnpjResponse(String cnpj, String companyName, String tradeName, String street, String neighborhood, String city, String state, String postalCode, String email) {}
    public record DocumentValidationResponse(String document, boolean valid, String type) {}
}
