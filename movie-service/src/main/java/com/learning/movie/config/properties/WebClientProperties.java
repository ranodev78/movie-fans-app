package com.learning.movie.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "spring.web.reactive.webclient")
public class WebClientProperties {
    private String connectionProviderName;
    private int maxConnections = 50;
    private long maxIdleTime = 2;         // In minutes
    private long evictionThreshold = 10;  // In minutes
    private int responseTimeout = 5;      // In seconds
    private int connectionTimeout = 5000; // In milliseconds

    public String getConnectionProviderName() {
        return connectionProviderName;
    }

    public void setConnectionProviderName(String connectionProviderName) {
        this.connectionProviderName = connectionProviderName;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public long getEvictionThreshold() {
        return evictionThreshold;
    }

    public void setEvictionThreshold(long evictionThreshold) {
        this.evictionThreshold = evictionThreshold;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Duration getMaxIdleTimeDuration() {
        return Duration.ofMinutes(this.maxIdleTime);
    }

    public Duration getEvictionThresholdDuration() {
        return Duration.ofMinutes(this.evictionThreshold);
    }

    public Duration getResponseTimeoutDuration() {
        return Duration.ofSeconds(this.responseTimeout);
    }

    public Duration getConnectionTimeoutDuration() {
        return Duration.ofMillis(this.connectionTimeout);
    }
}
