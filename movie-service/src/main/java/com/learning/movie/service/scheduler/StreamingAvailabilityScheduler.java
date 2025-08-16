package com.learning.movie.service.scheduler;

import com.learning.movie.dto.sendgrid.SendGridEmailRequest;
import com.learning.movie.dto.subscription.StreamingPlatform;
import com.learning.movie.dto.tmdb.provider.WatchProvider;
import com.learning.movie.dto.tmdb.provider.WatchProviderRegion;
import com.learning.movie.dto.tmdb.provider.WatchProvidersResponse;
import com.learning.movie.mapper.NotificationMapper;
import com.learning.movie.model.subscription.MovieReleaseSubscription;
import com.learning.movie.model.subscription.SubscriptionPlatform;
import com.learning.movie.repository.TmdbRepository;
import com.learning.movie.repository.subscription.MovieStreamingReleaseSubscriptionRepository;
import com.learning.movie.repository.subscription.SubscriptionPlatformRepository;
import com.learning.movie.service.subscription.notification.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class StreamingAvailabilityScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingAvailabilityScheduler.class);

    private static final String CACHE_PREFIX_KEY = "watchProvidersForMovie:";

    private final MovieStreamingReleaseSubscriptionRepository movieSubscriptionRepository;
    private final SubscriptionPlatformRepository platformRepository;
    private final TmdbRepository tmdbRepository;
    private final ReactiveRedisTemplate<String, WatchProvidersResponse> reactiveRedisTemplate;
    private final EmailNotificationService<SendGridEmailRequest> emailNotificationService;

    @Autowired
    public StreamingAvailabilityScheduler(final MovieStreamingReleaseSubscriptionRepository movieSubscriptionRepository,
                                          final SubscriptionPlatformRepository platformRepository,
                                          final TmdbRepository tmdbRepository,
                                          @Qualifier("watchProvidersRedisTemplate") final ReactiveRedisTemplate<String, WatchProvidersResponse> reactiveRedisTemplate,
                                          final EmailNotificationService<SendGridEmailRequest> emailNotificationService) {
        this.movieSubscriptionRepository = movieSubscriptionRepository;
        this.platformRepository = platformRepository;
        this.tmdbRepository = tmdbRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.emailNotificationService = emailNotificationService;
    }

    @Scheduled(cron = "*/30 * * * * ?", zone = "UTC")
    public void pollForPlatformReleaseSubscriptions() {
        this.checkStreamingAvailability()
                .doOnSubscribe(subscription -> LOGGER.info("Polling for movie streaming platform release subscriptions..."))
                .subscribe();
    }

    /**
     * A CRON Job that is scheduled to run at 2:00 AM for checking to see what movies from a collection of user's requests
     * become available for streaming and notifying those users
     * <p />
     * It fulfills its job by performing the following sequential steps:
     * <ol>
     *     <li>Query the {@code movie_streaming_release_subscriptions} table for subscription requests that were made
     *         no earlier than yesterday and before today
     *     </li>
     *     <li>Group subscriptions by their TMDB movie ID</li>
     *     <li>Get the corresponding requested platforms for each subscription</li>
     *     <li>For each streaming platforms associated to each subscription, validate whether the streaming platform
     *         has been released by calling TMDB API to fetch live data
     *     </li>
     *     <li>For each platform that has released the movie append it to the set of platforms to be included in the
     *         email notification for that subscription
     *     </li>
     *     <li>For each subscription that has at least one of the platforms requested released, notify the recipient</li>
     * </ol>
     *
     * @return void
     */
    public Mono<Void> checkStreamingAvailability() {
        final LocalDate now = LocalDate.now();

        return this.movieSubscriptionRepository
                .findAllCreatedToday(now.minusDays(1L).atStartOfDay(), now.plusDays(1L).atStartOfDay().minusNanos(1L))
                .groupBy(MovieReleaseSubscription::getTmdbMovieId)
                .flatMap(groupedSubscriptions -> {
                    LOGGER.info("Group with key: {}", groupedSubscriptions.key());

                    final String tmdbId = String.valueOf(groupedSubscriptions.key());

                    // 1. Collect subscriptions for this movieId
                    return groupedSubscriptions.collectList()
                            .flatMapMany(subscriptionsForSameMovie -> {
                                final List<Long> subscriptionIds = subscriptionsForSameMovie.stream()
                                        .map(MovieReleaseSubscription::getSubscriptionId)
                                        .toList();

                                // 2. Consolidate all the platforms that are associated to any of the subscriptions for
                                //    the same movie
                                return this.platformRepository.findAllBySubscriptionIdIn(subscriptionIds)
                                        .collectList()
                                        .flatMapMany(platforms -> {
                                            // 3. Map platforms by subscriptionId
                                            final Map<Long, Set<StreamingPlatform>> platformsBySubId = platforms.stream()
                                                    .collect(Collectors.groupingBy(
                                                            SubscriptionPlatform::getSubscriptionId,
                                                            Collectors.mapping(SubscriptionPlatform::getPlatform, Collectors.toCollection(HashSet::new))
                                                    ));

                                            // 4. Fetch streaming platforms from TMDB for this movieId then filter each
                                            //    subscription for the same movie of their respective requested platforms
                                            return this.tmdbRepository.getMovieWatchProviders(tmdbId)
                                                    .flatMapMany(tmdbResponse -> Flux.fromIterable(subscriptionsForSameMovie)
                                                            .flatMap(subscription -> {
                                                                final Set<StreamingPlatform> requestedPlatforms = platformsBySubId
                                                                        .getOrDefault(subscription.getSubscriptionId(), Collections.emptySet());

                                                                final Set<StreamingPlatform> availablePlatforms = extractMatchingPlatforms(tmdbResponse, requestedPlatforms);

                                                                final Set<StreamingPlatform> matchedPlatforms = new HashSet<>(requestedPlatforms);
                                                                matchedPlatforms.retainAll(availablePlatforms);

                                                                if (matchedPlatforms.isEmpty()) {
                                                                    LOGGER.info("No platform has released the requested movie");
                                                                    return Mono.empty();
                                                                }

                                                                return Mono.just(NotificationMapper.fromMovieStreamingReleaseSubDetails(subscription.getEmail(), subscription.getMovieName(), matchedPlatforms));
                                                            },
                                                            16)
                                                            .flatMap(this.emailNotificationService::notifyRecipient, 32));
                                        });
                            });
                },
                8)
                .then();
    }

    private static Set<StreamingPlatform> extractMatchingPlatforms(WatchProvidersResponse providersResponse, Set<StreamingPlatform> streamingPlatforms) {
        return Optional.ofNullable(providersResponse.getResults())
                .map(map -> map.get("US"))
                .map(WatchProviderRegion::getFlatrate)
                .orElse(Collections.emptyList())
                .stream()
                .map(WatchProvider::getProviderName)
                .map(StreamingPlatform::fromDisplayName)
                .filter(streamingPlatforms::contains)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Mono<List<StreamingPlatform>> checkCacheIfPresent(final String subscriptionId,
                                                              final Set<StreamingPlatform> streamingPlatforms) {
        return this.reactiveRedisTemplate.opsForValue()
                .get(CACHE_PREFIX_KEY.concat(subscriptionId))
                .map(cachedWatchProvidersResponse -> cachedWatchProvidersResponse.getResults().get("US").getFlatrate()
                        .stream()
                        .map(WatchProvider::getProviderName)
                        .map(StreamingPlatform::fromDisplayName)
                        .filter(streamingPlatforms::contains)
                        .toList())
                .filter(Predicate.not(List::isEmpty));
    }
}
