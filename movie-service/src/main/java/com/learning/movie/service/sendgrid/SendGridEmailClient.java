package com.learning.movie.service.sendgrid;

import com.fasterxml.jackson.databind.JsonNode;
import com.learning.movie.dto.sendgrid.SendGridEmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class SendGridEmailClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendGridEmailClient.class);

    private final WebClient webClient;

    @Autowired
    public SendGridEmailClient(@Qualifier("twilioSendGridEmailApiClient") final WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<JsonNode> sendEmail(final SendGridEmailRequest emailRequest) {
        LOGGER.info("Entering SendGridEmailClient.sendEmail...");

        return this.webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/mail/send").build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(emailRequest)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorMap(err -> new RuntimeException("An error occurred: %s".formatted(err.getMessage()), err));
    }
}
