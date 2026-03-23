package com.mktplace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktplace.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class StripeService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final String secretKey;
    private final String webhookSecret;
    private final String successUrl;
    private final String cancelUrl;

    public StripeService(ObjectMapper objectMapper,
                         @Value("${app.stripe.secret-key:}") String secretKey,
                         @Value("${app.stripe.webhook-secret:}") String webhookSecret,
                         @Value("${app.stripe.success-url:http://localhost:3000/dashboard}") String successUrl,
                         @Value("${app.stripe.cancel-url:http://localhost:3000/projects}") String cancelUrl) {
        this.objectMapper = objectMapper;
        this.secretKey = secretKey;
        this.webhookSecret = webhookSecret;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    public StripeCheckoutResponse createCheckoutSession(Long transactionId, BigDecimal amount, String projectTitle) {
        if (secretKey == null || secretKey.isBlank()) throw new BusinessException("Stripe não configurado", HttpStatus.SERVICE_UNAVAILABLE);
        try {
            String form = encode(Map.of(
                    "mode", "payment",
                    "success_url", successUrl,
                    "cancel_url", cancelUrl,
                    "line_items[0][price_data][currency]", "brl",
                    "line_items[0][price_data][product_data][name]", projectTitle,
                    "line_items[0][price_data][unit_amount]", amount.multiply(new BigDecimal("100")).toBigInteger().toString(),
                    "line_items[0][quantity]", "1",
                    "metadata[transactionId]", String.valueOf(transactionId)
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.stripe.com/v1/checkout/sessions"))
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) throw new BusinessException("Falha ao criar checkout no Stripe", HttpStatus.BAD_GATEWAY);
            JsonNode node = objectMapper.readTree(response.body());
            return new StripeCheckoutResponse(node.path("id").asText(), node.path("url").asText(), node.path("payment_intent").asText(null));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new BusinessException("Stripe indisponível", HttpStatus.BAD_GATEWAY);
        }
    }

    public boolean validateWebhook(String payload, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank() || signatureHeader == null || signatureHeader.isBlank()) return false;
        String[] parts = signatureHeader.split(",");
        String timestamp = null;
        String v1 = null;
        for (String part : parts) {
            if (part.startsWith("t=")) timestamp = part.substring(2);
            if (part.startsWith("v1=")) v1 = part.substring(3);
        }
        if (timestamp == null || v1 == null) return false;
        String signedPayload = timestamp + "." + payload;
        return hmacSha256(webhookSecret, signedPayload).equals(v1);
    }

    private String encode(Map<String, String> fields) {
        StringJoiner joiner = new StringJoiner("&");
        fields.forEach((key, value) -> joiner.add(URLEncoder.encode(key, StandardCharsets.UTF_8) + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8)));
        return joiner.toString();
    }

    private String hmacSha256(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BusinessException("Não foi possível validar webhook do Stripe", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public record StripeCheckoutResponse(String sessionId, String checkoutUrl, String paymentIntentId) {}
}
