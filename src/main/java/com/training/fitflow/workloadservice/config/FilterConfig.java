package com.training.fitflow.workloadservice.config;

import com.training.fitflow.workloadservice.logging.TransactionLoggingFilter;
import com.training.fitflow.workloadservice.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Value("${service.jwt.secret}")
    private String jwtSecret;

    @Bean
    public FilterRegistrationBean<TransactionLoggingFilter> transactionLoggingFilter() {
        FilterRegistrationBean<TransactionLoggingFilter> registration =
                new FilterRegistrationBean<>(new TransactionLoggingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("transactionLoggingFilter");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilter() {
        FilterRegistrationBean<JwtAuthFilter> registration =
                new FilterRegistrationBean<>(new JwtAuthFilter(jwtSecret));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(2);
        registration.setName("jwtAuthFilter");
        return registration;
    }
}
