package com.learning.user.authentication.service.sendgrid;

import com.fasterxml.jackson.databind.JsonNode;
import com.learning.user.authentication.dto.sendgrid.SendGridEmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SendGridEmailClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendGridEmailClient.class);

    private final String sendGridEmailUri;
    private final RestClient restClient;

    @Autowired
    public SendGridEmailClient(@Value("${sendgrid.email.api.base.url}") String sendGridUrl,
                               @Value("${sendgrid.email.api.key}") String sendGridApiKey,
                               @Value("${sendgrid.email.api.uri}") String sendGridEmailUri,
                               RestClient.Builder restClientBuilder) {
        this.sendGridEmailUri = sendGridEmailUri;

        this.restClient = restClientBuilder
                .baseUrl(sendGridUrl)
                .defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(sendGridApiKey))
                .build();
    }

    public JsonNode sendEmail(SendGridEmailRequest sendGridEmailRequest) {
        LOGGER.info("Entering SendGridEmailClient.sendEmail...");

        try {
            return this.restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(this.sendGridEmailUri)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(sendGridEmailRequest)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isError()) {
                            final String body = response.bodyTo(String.class);
                            throw new RuntimeException("SendGrid API returned error: %s".formatted(body));
                        }

                        return response.bodyTo(JsonNode.class);
                    });
        } catch (Exception e) {
            throw new RuntimeException("Exception thrown sending email with SendGrid", e);
        }
    }
}
