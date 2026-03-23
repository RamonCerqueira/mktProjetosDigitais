package com.mktplace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktplace.dto.IntegrationDtos.CepResponse;
import com.mktplace.dto.IntegrationDtos.CnpjResponse;
import com.mktplace.dto.IntegrationDtos.DocumentValidationResponse;
import com.mktplace.enums.DocumentType;
import com.mktplace.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.mktplace.validation.DocumentValidator.*;

@Service
public class IntegrationService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    public IntegrationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CepResponse fetchAddressByCep(String cep) {
        String normalized = digits(cep);
        if (normalized.length() != 8) throw new BusinessException("CEP inválido", HttpStatus.BAD_REQUEST);
        JsonNode node = getJson("https://viacep.com.br/ws/" + normalized + "/json/");
        if (node.has("erro") && node.get("erro").asBoolean()) throw new BusinessException("CEP não encontrado", HttpStatus.NOT_FOUND);
        return new CepResponse(node.path("cep").asText(), node.path("logradouro").asText(), node.path("complemento").asText(), node.path("bairro").asText(), node.path("localidade").asText(), node.path("uf").asText());
    }

    public CnpjResponse fetchCompanyByCnpj(String cnpj) {
        String normalized = digits(cnpj);
        if (!isValidCnpj(normalized)) throw new BusinessException("CNPJ inválido", HttpStatus.BAD_REQUEST);
        JsonNode node = getJson("https://www.receitaws.com.br/v1/cnpj/" + normalized);
        if (node.has("status") && "ERROR".equalsIgnoreCase(node.path("status").asText())) throw new BusinessException(node.path("message").asText("CNPJ não encontrado"), HttpStatus.NOT_FOUND);
        return new CnpjResponse(normalized, node.path("nome").asText(), node.path("fantasia").asText(), node.path("logradouro").asText() + " " + node.path("numero").asText(), node.path("bairro").asText(), node.path("municipio").asText(), node.path("uf").asText(), digits(node.path("cep").asText()), node.path("email").asText());
    }

    public DocumentValidationResponse validateDocument(DocumentType type, String document) {
        String normalized = digits(document);
        boolean valid = (type == DocumentType.CNPJ) ? isValidCnpj(normalized) : isValidCpf(normalized);
        return new DocumentValidationResponse(normalized, valid, type.name());
    }

    private JsonNode getJson(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).header("Accept", "application/json").GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) throw new BusinessException("Falha na integração externa", HttpStatus.BAD_GATEWAY);
            return objectMapper.readTree(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Integração externa indisponível", HttpStatus.BAD_GATEWAY);
        } catch (IOException e) {
            throw new BusinessException("Integração externa indisponível", HttpStatus.BAD_GATEWAY);
        }
    }
}
