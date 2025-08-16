package com.learning.movie.service.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.movie.dto.tmdb.review.AggregatedTmdbReviews;
import com.learning.movie.dto.tmdb.review.TmdbReviewSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class OpenAiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiService.class);

    private static final TmdbReviewSummary EMPTY_REVIEW = new TmdbReviewSummary(Collections.emptyList(), Collections.emptyList());

    // Prompt templates
    private static final String INTERMEDIATE_PROMPT_TEMPLATE = """
        Summarize the following movie reviews for "%s" into a JSON object with exactly two properties:
        - positives: array of short bullet-style strengths
        - negatives: array of short bullet-style criticisms

        Return only valid JSON, no extra commentary or text.

        Reviews:
        %s
        """;

    private static final String FINAL_PROMPT_TEMPLATE = """
        Combine the following intermediate JSON summaries into a final JSON with exactly two fields:
        - positives: array of concise bullet-point strengths
        - negatives: array of concise bullet-point criticisms

        Return only valid JSON.

        Intermediate summaries:
        %s
        """;

    private final WebClient webClient;
    private final String openAiUri;
    private final ObjectMapper objectMapper;
    private final int chunkSize;

    public OpenAiService(@Qualifier("openAiClient") final WebClient webClient,
                         @Value("${openai.uri}") final String openAiUri,
                         final ObjectMapper objectMapper,
                         @Value("${openai.chunk.size:3}") final int chunkSize) {
        this.webClient = webClient;
        this.openAiUri = openAiUri;
        this.objectMapper = objectMapper;
        this.chunkSize = chunkSize;
    }

    private List<List<String>> chunkReviews(List<String> reviews) {
        final List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < reviews.size(); i += this.chunkSize) {
            chunks.add(reviews.subList(i, Math.min(i + this.chunkSize, reviews.size())));
        }

        return chunks;
    }

    public Mono<TmdbReviewSummary> summarizeMovieReviews(AggregatedTmdbReviews aggregatedTmdbReviews) {
        final List<List<String>> chunks = chunkReviews(aggregatedTmdbReviews.reviews());

        return Flux.fromIterable(chunks)
                .index()
                .concatMap(tuple2 -> {
                    final long idx = tuple2.getT1();
                    final List<String> chunk = tuple2.getT2();

                    return this.summarizeChunk(aggregatedTmdbReviews.movieName(), chunk)
                            .delayElement(Duration.ofSeconds(idx == 0 ? 0: 2));
                })
                .collectList()
                .map(this::mergeJsonChunks)
                .flatMap(mergedJson -> {
                    final String finalizedPrompt = String.format(FINAL_PROMPT_TEMPLATE, mergedJson);
                    return this.callOpenAi(finalizedPrompt)
                            .defaultIfEmpty(EMPTY_REVIEW);
                });
    }

    private Mono<TmdbReviewSummary> summarizeChunk(String movieName, List<String> reviewsChunk) {
        final String joinedReviews = String.join("\n\n", reviewsChunk);
        final String prompt = String.format(INTERMEDIATE_PROMPT_TEMPLATE, movieName, joinedReviews);

        return this.callOpenAi(prompt);
    }

    private Mono<TmdbReviewSummary> callOpenAi(String prompt) {
        final var body = Map.of(
                "model", "gpt-4o-mini",
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system",
                               "content", "You are a helpful summarizer"),
                        Map.of("role", "user",
                               "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 300
        );

        return this.webClient.post()
                .uri(uriBuilder -> uriBuilder.path(this.openAiUri).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.path("choices")
                        .get(0)
                        .path("message")
                        .path("content"))
                .filter(jsonNode -> !jsonNode.isMissingNode() && !jsonNode.isNull())
                .map(this::jsonNodeToRecord)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException e &&
                                                       HttpStatus.TOO_MANY_REQUESTS.isSameCodeAs(e.getStatusCode())));
    }

    private TmdbReviewSummary mergeJsonChunks(List<TmdbReviewSummary> chunks) {
        final List<String> positivesCompilation = new ArrayList<>();
        final List<String> negativesCompilation = new ArrayList<>();

        for (TmdbReviewSummary chunk : chunks) {
            chunk.positives().stream()
                    .map(String::trim)
                    .filter(Predicate.not(String::isBlank))
                    .forEach(positivesCompilation::add);

            chunk.negatives().stream()
                    .map(String::trim)
                    .filter(Predicate.not(String::isBlank))
                    .forEach(negativesCompilation::add);
        }

        return new TmdbReviewSummary(positivesCompilation, negativesCompilation);
    }

    private TmdbReviewSummary jsonNodeToRecord(JsonNode jsonNode) {
        try {
            if (jsonNode.isTextual()) {
                return this.objectMapper.readValue(jsonNode.asText(), TmdbReviewSummary.class);
            } else if (jsonNode.isObject()) {
                return this.objectMapper.treeToValue(jsonNode, TmdbReviewSummary.class);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to map JsonNode to TmdbReviewSummary", e);
        }

        return EMPTY_REVIEW;
    }
}
