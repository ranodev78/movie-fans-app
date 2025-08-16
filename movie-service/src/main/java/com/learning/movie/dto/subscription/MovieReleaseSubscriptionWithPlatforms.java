package com.learning.movie.dto.subscription;

import com.learning.movie.model.subscription.MovieReleaseSubscription;

import java.util.Set;

public record MovieReleaseSubscriptionWithPlatforms(MovieReleaseSubscription subscription,
                                                    Set<StreamingPlatform> platforms) {
}
