package com.learning.movie.repository.subscription;

import com.learning.movie.model.subscription.MovieReleaseSubscription;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface MovieStreamingReleaseSubscriptionRepository extends ReactiveCrudRepository<MovieReleaseSubscription, Long> {

    @Query(value = """
            SELECT *
            FROM movie_streaming_release_subscriptions
            WHERE created_at >= :startOfDay AND created_at < :startOfNextDay""")
    Flux<MovieReleaseSubscription> findAllCreatedToday(LocalDateTime startOfDay, LocalDateTime startOfNextDay);
}
