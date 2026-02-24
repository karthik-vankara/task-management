package com.karthik.task_management_backend.config;

import com.karthik.task_management_backend.interceptor.RateLimitingInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 * 
 * Registers custom interceptors and configures request handling.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitingInterceptor rateLimitingInterceptor;

    /**
     * Register custom interceptors
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.debug("Registering custom interceptors");
        
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/auth/**")
                .order(1);
    }
}
