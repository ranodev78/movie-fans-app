package com.learning.movie.repository.subscription;

import com.learning.movie.model.subscription.SubscriptionPlatform;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface SubscriptionPlatformRepository extends ReactiveCrudRepository<SubscriptionPlatform, Long> {

    Flux<SubscriptionPlatform> findAllBySubscriptionId(Long subscriptionId);

    Flux<SubscriptionPlatform> findAllBySubscriptionIdIn(List<Long> subscriptionIds);
}
