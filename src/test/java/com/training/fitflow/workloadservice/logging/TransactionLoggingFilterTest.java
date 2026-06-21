package com.training.fitflow.workloadservice.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionLoggingFilter Tests")
class TransactionLoggingFilterTest {
    private TransactionLoggingFilter filter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new TransactionLoggingFilter();

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        request.setMethod("GET");
        request.setRequestURI("/api/workloads");

        MDC.clear();
    }

    @Test
    @DisplayName("doFilterInternal -> incoming transaction id -> propagated")
    void doFilterInternal_existingTransactionId_propagated() throws ServletException, IOException {
        request.addHeader("X-Transaction-Id", "tx123456");
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals("tx123456", response.getHeader("X-Transaction-Id"));

        assertNull(MDC.get("transactionId"));
    }

    @Test
    @DisplayName("doFilterInternal -> missing transaction id -> generated")
    void doFilterInternal_missingTransactionId_generated() throws ServletException, IOException {
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);

        String generated = response.getHeader("X-Transaction-Id");

        assertNotNull(generated);
        assertEquals(8, generated.length());

        assertNull(MDC.get("transactionId"));
    }

    @Test
    @DisplayName("doFilterInternal -> blank transaction id -> generated")
    void doFilterInternal_blankTransactionId_generated() throws ServletException, IOException {
        request.addHeader("X-Transaction-Id", "   ");
        filter.doFilter(request, response, filterChain);

        String generated = response.getHeader("X-Transaction-Id");

        assertNotNull(generated);
        assertEquals(8, generated.length());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal -> filter chain throws exception -> rethrows")
    void doFilterInternal_filterChainThrows_rethrowsException() throws ServletException, IOException {
        doThrow(new RuntimeException("boom")).when(filterChain).doFilter(request, response);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, filterChain));

        assertEquals("boom", ex.getMessage());
        assertNotNull(response.getHeader("X-Transaction-Id"));
        assertNull(MDC.get("transactionId"));
    }

    @Test
    @DisplayName("doFilterInternal -> response header always set")
    void doFilterInternal_responseHeaderAlwaysSet() throws ServletException, IOException {
        request.addHeader("X-Transaction-Id", "abc12345");
        filter.doFilter(request, response, filterChain);

        assertTrue(response.containsHeader("X-Transaction-Id"));
        assertEquals("abc12345", response.getHeader("X-Transaction-Id"));
    }
}