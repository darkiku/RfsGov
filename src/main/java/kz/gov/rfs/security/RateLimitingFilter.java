package kz.gov.rfs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // IP -> список timestamp запросов
    private final Map<String, ConcurrentHashMap<Long, Integer>> requestCounts = new ConcurrentHashMap<>();

    // Настройки rate limiting
    private static final int MAX_REQUESTS_PER_MINUTE = 100; // 100 запросов в минуту
    private static final int MAX_LOGIN_ATTEMPTS_PER_MINUTE = 5; // 5 попыток логина в минуту
    private static final long TIME_WINDOW_MS = 60_000; // 1 минута

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIP = getClientIP(request);
        String requestURI = request.getRequestURI();

        // Особо строгие ограничения для /api/auth/login
        int maxRequests = requestURI.equals("/api/auth/login")
                ? MAX_LOGIN_ATTEMPTS_PER_MINUTE
                : MAX_REQUESTS_PER_MINUTE;

        if (isRateLimited(clientIP, maxRequests)) {
            log.warn("🚨 SECURITY: Rate limit exceeded for IP: {} on {}", clientIP, requestURI);

            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> errorResponse = Map.of(
                    "status", 429,
                    "error", "Too Many Requests",
                    "message", "Rate limit exceeded. Please try again later.",
                    "timestamp", Instant.now().toString()
            );

            new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String clientIP, int maxRequests) {
        long currentMinute = System.currentTimeMillis() / TIME_WINDOW_MS;

        requestCounts.putIfAbsent(clientIP, new ConcurrentHashMap<>());
        ConcurrentHashMap<Long, Integer> ipRequests = requestCounts.get(clientIP);

        // Очистка старых записей (старше 2 минут)
        ipRequests.entrySet().removeIf(entry ->
                entry.getKey() < currentMinute - 2
        );

        // Увеличение счетчика для текущей минуты
        int count = ipRequests.merge(currentMinute, 1, Integer::sum);

        return count > maxRequests;
    }

    private String getClientIP(HttpServletRequest request) {
        // Проверка заголовков прокси
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Если X-Forwarded-For содержит несколько IP (через запятую), берем первый
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    // Метод для очистки кеша (можно вызывать по расписанию)
    public void clearOldEntries() {
        long currentMinute = System.currentTimeMillis() / TIME_WINDOW_MS;
        requestCounts.entrySet().removeIf(entry ->
                entry.getValue().keySet().stream().allMatch(minute -> minute < currentMinute - 5)
        );
        log.debug("Cleared old rate limiting entries. Current size: {}", requestCounts.size());
    }
}