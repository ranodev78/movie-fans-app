package com.learning.movie.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StreamingReleaseSubscriptionRequest(@NotBlank Long movieId,
                                                  @NotBlank String movieName,
                                                  @NotNull StreamingPlatform streamingPlatform) {
}
