package com.learning.movie.service.subscription;

import com.learning.movie.dto.subscription.StreamingPlatform;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface MovieStreamingReleaseSubscriptionService {

    Mono<Void> subscribe(Long movieId, String movieName, Set<StreamingPlatform> streamingPlatforms);
}
