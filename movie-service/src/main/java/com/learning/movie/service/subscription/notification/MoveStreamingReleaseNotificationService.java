package com.learning.movie.service.subscription.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.learning.movie.dto.sendgrid.SendGridEmailRequest;
import com.learning.movie.service.sendgrid.SendGridEmailClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MoveStreamingReleaseNotificationService implements EmailNotificationService<SendGridEmailRequest> {
    private final SendGridEmailClient sendGridEmailClient;

    @Autowired
    public MoveStreamingReleaseNotificationService(final SendGridEmailClient sendGridEmailClient) {
        this.sendGridEmailClient = sendGridEmailClient;
    }

    @Override
    public Mono<JsonNode> notifyRecipient(SendGridEmailRequest content) {
        return this.sendGridEmailClient.sendEmail(content);
    }
}
