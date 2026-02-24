package com.karthik.task_management_backend.interceptor;

import com.karthik.task_management_backend.exception.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting Interceptor
 * 
 * Implements basic rate limiting for authentication endpoints to prevent abuse.
 * 
 * Limits:
 * - /api/auth/login: 10 requests per minute per IP
 * - /api/auth/callback: 5 requests per minute per IP
 * - Other /api/auth/*: 20 requests per minute per IP
 * 
 * In production, use Redis-based rate limiting for distributed systems.
 */
@Component
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final long WINDOW_SIZE_MS = 60_000;  // 1 minute
    private static final int LOGIN_LIMIT = 10;
    private static final int CALLBACK_LIMIT = 5;
    private static final int DEFAULT_LIMIT = 20;

    // IP Address -> Request Times
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> requestTimestamps = 
            new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        String clientIp = getClientIp(request);
        String path = request.getRequestURI();

        // Apply rate limiting to auth endpoints
        if (path.startsWith("/api/auth/")) {
            int limit = getLimit(path);
            checkRateLimit(clientIp, path, limit);
        }

        return true;
    }

    /**
     * Determines the rate limit for a specific endpoint
     */
    private int getLimit(String path) {
        if (path.contains("/login")) {
            return LOGIN_LIMIT;
        } else if (path.contains("/callback")) {
            return CALLBACK_LIMIT;
        }
        return DEFAULT_LIMIT;
    }

    /**
     * Checks if the client has exceeded the rate limit
     */
    private void checkRateLimit(String clientIp, String path, int limit) {
        long now = System.currentTimeMillis();

        ConcurrentLinkedQueue<Long> timestamps = requestTimestamps
                .computeIfAbsent(clientIp, k -> new ConcurrentLinkedQueue<>());

        // Remove timestamps older than the window
        timestamps.removeIf(t -> now - t > WINDOW_SIZE_MS);

        // Check if limit exceeded
        if (timestamps.size() >= limit) {
            log.warn("Rate limit exceeded for {} - IP: {}, Limit: {}, Requests: {}", 
                    path, clientIp, limit, timestamps.size());
            throw new RateLimitExceededException(
                    String.format("Too many requests. Maximum %d requests per minute.", limit)
            );
        }

        // Record the current request
        timestamps.offer(now);
        log.debug("Rate limit check passed for {} - IP: {}, Requests in window: {}", 
                path, clientIp, timestamps.size());
    }

    /**
     * Extracts client IP address from request
     * Checks X-Forwarded-For header for proxied requests
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0];
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
