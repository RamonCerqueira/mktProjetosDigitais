package com.mktplace.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private static final int LIMIT = 60;
    private static final long WINDOW_SECONDS = 60;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String key = buildKey(request);
        Bucket bucket = buckets.compute(key, (k, current) -> current == null || current.windowStart().plusSeconds(WINDOW_SECONDS).isBefore(Instant.now()) ? new Bucket(Instant.now(), 1) : new Bucket(current.windowStart(), current.count() + 1));
        if (bucket.count() > LIMIT) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String buildKey(HttpServletRequest request) {
        String actor = request.getRemoteUser() != null && !request.getRemoteUser().isBlank() ? request.getRemoteUser() : request.getRemoteAddr();
        return actor + ":" + request.getRequestURI();
    }

    private record Bucket(Instant windowStart, int count) {}
}
