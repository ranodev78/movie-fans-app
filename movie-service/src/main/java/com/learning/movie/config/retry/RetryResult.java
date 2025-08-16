package com.learning.movie.config.retry;

import org.springframework.http.HttpStatusCode;

import java.util.Objects;
import java.util.function.Function;

public class RetryResult<T> {
    private final ResultStatus status;
    private final HttpStatusCode errorCode;
    private final Throwable throwable;
    private final T result;

    private RetryResult(ResultStatus status, HttpStatusCode errorCode, Throwable throwable, T result) {
        this.status = status;
        this.errorCode = errorCode;
        this.throwable = throwable;
        this.result = result;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public HttpStatusCode getErrorCode() {
        return errorCode;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public T getResult() {
        return result;
    }

    public static <T> RetryResult<T> of(T result) {
        return new RetryResult<>(ResultStatus.SUCCESS, null, null, result);
    }

    public static <T> RetryResult<T> of(HttpStatusCode errorCode, Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable cannot be null");
        return new RetryResult<>(ResultStatus.FAILURE, errorCode, throwable, null);
    }

    public T resultElseThrow(Function<Throwable, RuntimeException> function) {
        if (ResultStatus.FAILURE == status) {
            throw function.apply(throwable);
        }

        return this.result;
    }
}
