package com.mlouis594.CommerceAPI.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JWTUtilTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long!!";

    @Test
    void roundTripsUsername() {
        JWTUtil jwt = new JWTUtil(SECRET, Duration.ofMinutes(5));
        assertThat(jwt.extractUsername(jwt.generateToken("alice"))).isEqualTo("alice");
    }

    @Test
    void rejectsExpiredToken() {
        JWTUtil jwt = new JWTUtil(SECRET, Duration.ofSeconds(-1));
        String token = jwt.generateToken("alice");
        assertThatThrownBy(() -> jwt.extractUsername(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String foreign = new JWTUtil("another-secret-that-is-also-32-bytes-long!!", Duration.ofMinutes(5))
                .generateToken("alice");
        JWTUtil jwt = new JWTUtil(SECRET, Duration.ofMinutes(5));
        assertThatThrownBy(() -> jwt.extractUsername(foreign)).isInstanceOf(SignatureException.class);
    }
}
