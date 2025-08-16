package com.learning.user.authentication.repository;

import com.learning.user.authentication.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    @Query(value = """
            SELECT *
            FROM verification_tokens
            WHERE token = :token
            AND used = false
            AND expiry_date > CURRENT_TIMESTAMP
            """,
            nativeQuery = true)
    Optional<VerificationToken> findValidToken(String token);
}
