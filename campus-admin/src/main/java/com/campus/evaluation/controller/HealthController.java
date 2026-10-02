package com.campus.evaluation.controller;

import com.campus.evaluation.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping
    public ResponseEntity<R<Map<String, Object>>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> database = new LinkedHashMap<>();
        Map<String, Object> redis = new LinkedHashMap<>();
        boolean databaseUp = checkDatabase(database);
        boolean redisUp = checkRedis(redis);
        boolean allUp = databaseUp && redisUp;

        Map<String, Object> components = new LinkedHashMap<>();
        components.put("database", database);
        components.put("redis", redis);

        data.put("status", allUp ? "UP" : "DOWN");
        data.put("app", "campus-evaluation-system");
        data.put("java", System.getProperty("java.version"));
        data.put("time", LocalDateTime.now().format(FORMATTER));
        data.put("components", components);

        if (allUp) {
            return ResponseEntity.ok(R.ok(data));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(R.fail(503, "Service dependencies unavailable", data, "SERVICE_UNAVAILABLE"));
    }

    @GetMapping("/db")
    public ResponseEntity<R<Map<String, Object>>> healthDb() {
        Map<String, Object> data = new LinkedHashMap<>();
        boolean up = checkDatabase(data);
        if (up) {
            return ResponseEntity.ok(R.ok(data));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(R.fail(503, "Database connection unavailable", data, "DB_UNAVAILABLE"));
    }

    @GetMapping("/redis")
    public ResponseEntity<R<Map<String, Object>>> healthRedis() {
        Map<String, Object> data = new LinkedHashMap<>();
        boolean up = checkRedis(data);
        if (up) {
            return ResponseEntity.ok(R.ok(data));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(R.fail(503, "Redis connection unavailable", data, "REDIS_UNAVAILABLE"));
    }

    private boolean checkDatabase(Map<String, Object> data) {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            data.put("status", "UP");
            data.put("type", "MySQL");
            data.put("queryResult", result);
            return true;
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            data.put("status", "DOWN");
            data.put("type", "MySQL");
            data.put("error", e.getMessage());
            return false;
        }
    }

    private boolean checkRedis(Map<String, Object> data) {
        try {
            String testKey = "health:check:" + System.currentTimeMillis();
            stringRedisTemplate.opsForValue().set(testKey, "ping", Duration.ofSeconds(10));
            String value = stringRedisTemplate.opsForValue().get(testKey);
            stringRedisTemplate.delete(testKey);
            boolean pingOk = "ping".equals(value);
            data.put("status", pingOk ? "UP" : "DOWN");
            data.put("type", "Redis");
            data.put("pingResult", pingOk ? "OK" : "FAIL");
            return pingOk;
        } catch (Exception e) {
            log.error("Redis health check failed: {}", e.getMessage());
            data.put("status", "DOWN");
            data.put("type", "Redis");
            data.put("error", e.getMessage());
            return false;
        }
    }
}
