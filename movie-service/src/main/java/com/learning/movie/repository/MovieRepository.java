package com.learning.movie.repository;

import com.learning.movie.model.Movie;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface MovieRepository extends ReactiveCrudRepository<Movie, UUID> {

    Mono<Movie> findByTtId(String ttId);
}
