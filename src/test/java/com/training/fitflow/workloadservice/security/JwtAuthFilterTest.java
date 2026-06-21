package com.training.fitflow.workloadservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.fitflow.workloadservice.dto.exception.response.ErrorResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthFilter Tests")
class JwtAuthFilterTest {
    private static final String SECRET = "mySuperSecretKeyForJwtValidation12345678901234567890";
    private JwtAuthFilter filter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(SECRET);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("doFilterInternal -> missing Authorization header -> 401")
    void doFilterInternal_missingAuthorizationHeader_returnsUnauthorized() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());

        String body = response.getContentAsString();

        ErrorResponse error = new ObjectMapper().readValue(body, ErrorResponse.class);

        assertEquals("Missing or invalid Authorization header", error.message());
        verifyNoInteractions(filterChain);
    }

    @Test
    @DisplayName("doFilterInternal -> invalid token -> 401")
    void doFilterInternal_invalidToken_returnsUnauthorized() throws Exception {
        request.addHeader("Authorization", "Bearer invalid-token");
        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());

        ErrorResponse error = new ObjectMapper().readValue(response.getContentAsString(), ErrorResponse.class);

        assertEquals("Invalid or expired token", error.message());
        verifyNoInteractions(filterChain);
    }

    @Test
    @DisplayName("doFilterInternal -> valid token -> filter chain continues")
    void doFilterInternal_validToken_callsFilterChain() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject("john.trainer")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);

        assertEquals(200, response.getStatus());
    }
}