package com.learning.movie.service.subscription.notification;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

public interface EmailNotificationService<T> {

    Mono<JsonNode> notifyRecipient(T content);
}
