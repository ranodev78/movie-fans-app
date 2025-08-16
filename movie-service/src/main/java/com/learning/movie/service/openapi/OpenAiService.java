package com.learning.movie.service.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.learning.movie.dto.tmdb.review.AggregatedTmdbReviews;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {
    private final WebClient webClient;
    private final String openAiUri;

    public OpenAiService(@Qualifier("openAiClient") final WebClient webClient,
                         @Value("${openai.uri}") final String openAiUri) {
        this.webClient = webClient;
        this.openAiUri = openAiUri;
    }

    private static List<List<String>> chunkReviews(List<String> reviews, int chunkSize) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < reviews.size(); i += chunkSize) {
            chunks.add(reviews.subList(i, Math.min(i + chunkSize, reviews.size())));
        }

        return chunks;
    }

    public Mono<String> summarizeMovieReviews(AggregatedTmdbReviews aggregatedTmdbReviews) {
        final int chunkSize = 1;
        List<List<String>> chunks = chunkReviews(aggregatedTmdbReviews.reviews(), chunkSize);

        return Flux.fromIterable(chunks)
                .concatMap(chunk -> this.summarizeReviewsChunk(aggregatedTmdbReviews.movieName(), chunk)
                        .delayElement(Duration.ofMillis(300)))
                .collectList()
                .flatMap(partialSummaries -> {
                    String combinedSummaries = String.join("\n\n", partialSummaries);

                    String finalPrompt = String.format("""
                        Given these intermediate summaries of reviews for "%s", create a final summary with two sections:
                        Positives: bullet-point list of strengths,
                        Negatives: bullet-point list of criticisms.
        
                        Summaries:
                        %s
                        """,
                            aggregatedTmdbReviews.movieName(), combinedSummaries);

                    final var body = Map.of(
                            "model", "gpt-3.5-turbo",
                            "messages", List.of(
                                    Map.of("role", "system",
                                           "content", "You are a helpful summarizer"),
                                    Map.of("role", "user",
                                           "content", finalPrompt)),
                            "temperature", 0.7,
                            "max_tokens", 300);

                    return this.webClient.post()
                            .uri(uriBuilder -> uriBuilder.path(this.openAiUri).build())
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(body)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .map(json -> json.path("choices").get(0).path("message").path("content").asText());
                });
    }

    private Mono<String> summarizeReviewsChunk(String movieName, List<String> reviewsChunk) {
        String joinedReviews = String.join("\n\n", reviewsChunk);

        final String prompt = String.format("""
                Summarize the following movie reviews for "%s" into two sections:
                Positives: bullet-point list of strengths,
                Negatives: bullet-point list of criticisms.
                
                using the reviews and keeping it very brief:
                %s
                """,
                movieName, joinedReviews);

        final var body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system",
                               "content", "You are a helpful summarizer"),
                        Map.of("role", "user",
                               "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 300);

        return this.webClient.post()
                .uri(uriBuilder -> uriBuilder.path(this.openAiUri).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.path("choices").get(0).path("message").path("content").asText())
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> {
                            if (throwable instanceof WebClientResponseException e) {
                                return HttpStatus.TOO_MANY_REQUESTS.isSameCodeAs(e.getStatusCode());
                            }

                            return false;
                        }));
    }
}
