package com.learning.movie.config.retry;

import java.time.Duration;

public class RetryPolicy {
    protected int retryAttempts;
    protected Duration retryDelay;

    public RetryPolicy(int retryAttempts, Duration retryDelay) {
        this.retryAttempts = retryAttempts;
        this.retryDelay = retryDelay;
    }

    public RetryPolicy() {}

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(Duration retryDelay) {
        this.retryDelay = retryDelay;
    }
}
