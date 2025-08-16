package com.learning.movie.service.subscription;

import com.learning.movie.dto.subscription.StreamingPlatform;
import com.learning.movie.model.subscription.MovieReleaseSubscription;
import com.learning.movie.model.subscription.SubscriptionPlatform;
import com.learning.movie.repository.client.AppUserClient;
import com.learning.movie.repository.subscription.MovieStreamingReleaseSubscriptionRepository;
import com.learning.movie.repository.subscription.SubscriptionPlatformRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Set;

@Service
public class MovieStreamingReleaseSubscriptionServiceImpl implements MovieStreamingReleaseSubscriptionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovieStreamingReleaseSubscriptionServiceImpl.class);

    private final AppUserClient appUserClient;
    private final MovieStreamingReleaseSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlatformRepository subscriptionPlatformRepository;

    @Autowired
    public MovieStreamingReleaseSubscriptionServiceImpl(final AppUserClient appUserClient,
                                                        final MovieStreamingReleaseSubscriptionRepository subscriptionRepository,
                                                        final SubscriptionPlatformRepository subscriptionPlatformRepository) {
        this.appUserClient = appUserClient;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionPlatformRepository = subscriptionPlatformRepository;
    }

    @Override
    public Mono<Void> subscribe(Long movieId, String movieName, Set<StreamingPlatform> streamingPlatforms) {
        LOGGER.info("Entering MovieStreamingReleaseSubscriptionServiceImpl.subscribe...");

        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (Jwt) securityContext.getAuthentication().getPrincipal())
                .flatMap(jwt -> this.appUserClient.getPrincipal(jwt)
                        .flatMap(userId -> this.appUserClient.getUserDetails(jwt, userId)))
                .map(userDetails -> new MovieReleaseSubscription(userDetails.username(), userDetails.email(), movieId, movieName))
                .flatMap(this.subscriptionRepository::save)
                .map(savedSubscription -> streamingPlatforms.stream()
                        .map(streamingPlatform -> new SubscriptionPlatform(savedSubscription.getSubscriptionId(), streamingPlatform))
                        .toList())
                .flatMapMany(this.subscriptionPlatformRepository::saveAll)
                .then();
    }
}
