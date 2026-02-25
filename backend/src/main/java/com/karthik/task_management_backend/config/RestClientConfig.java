package com.karthik.task_management_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for HTTP clients used in the application
 * 
 * Provides:
 * - RestTemplate: For calling external APIs (Google OAuth, etc.)
 * - ObjectMapper: For JSON serialization/deserialization
 */
@Configuration
@Slf4j
public class RestClientConfig {

    /**
     * RestTemplate bean for making HTTP requests
     * 
     * Used for calling external APIs like Google OAuth token endpoint.
     * Configured with timeouts for robustness.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.debug("Creating RestTemplate bean");
        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * ObjectMapper bean for JSON processing
     * 
     * Used for parsing JSON responses from external APIs and
     * serializing/deserializing DTOs.
     */
    @Bean
    public ObjectMapper objectMapper() {
        log.debug("Creating ObjectMapper bean");
        return new ObjectMapper();
    }
}
