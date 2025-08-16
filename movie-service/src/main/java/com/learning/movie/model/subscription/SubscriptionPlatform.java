package com.learning.movie.model.subscription;

import com.learning.movie.dto.subscription.StreamingPlatform;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.springframework.data.relational.core.mapping.Table;

@Table("subscription_platforms")
public class SubscriptionPlatform {
    private Long subscriptionId;

    @Enumerated(EnumType.STRING)
    private StreamingPlatform platform;

    public SubscriptionPlatform(Long subscriptionId, StreamingPlatform platform) {
        this.subscriptionId = subscriptionId;
        this.platform = platform;
    }

    public SubscriptionPlatform() {}

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public StreamingPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(StreamingPlatform platform) {
        this.platform = platform;
    }
}
