package com.training.fitflow.workloadservice.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class TransactionLoggingFilter extends OncePerRequestFilter {
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String incoming = request.getHeader(TRANSACTION_ID_HEADER);
        String transactionId = (incoming != null && !incoming.isBlank()) ? incoming : UUID.randomUUID().toString().substring(0, 8);

        MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        long start = System.currentTimeMillis();
        try {
            log.info("Incoming request: method={} uri={}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Unhandled exception: method={} uri={} error={}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("Request completed: method={} uri={} status={} duration={}ms",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            MDC.clear();
        }
    }
}