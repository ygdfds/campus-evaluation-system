package com.campus.evaluation.controller;

import com.campus.evaluation.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查接口
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 基础健康检查
     */
    @GetMapping
    public R<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("app", "campus-evaluation-system");
        data.put("java", System.getProperty("java.version"));
        data.put("time", LocalDateTime.now().format(FORMATTER));
        return R.ok(data);
    }

    /**
     * 数据库连通性检查
     */
    @GetMapping("/db")
    public R<Map<String, Object>> healthDb() {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            data.put("status", "UP");
            data.put("database", "MySQL");
            data.put("query_result", result);
            return R.ok(data);
        } catch (Exception e) {
            log.error("数据库连接检查失败: {}", e.getMessage());
            data.put("status", "DOWN");
            data.put("database", "MySQL");
            data.put("error", e.getMessage());
            return R.fail(503, "数据库连接异常");
        }
    }

    /**
     * Redis 连通性检查
     */
    @GetMapping("/redis")
    public R<Map<String, Object>> healthRedis() {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            String testKey = "health:check:" + System.currentTimeMillis();
            stringRedisTemplate.opsForValue().set(testKey, "ping", java.time.Duration.ofSeconds(10));
            String value = stringRedisTemplate.opsForValue().get(testKey);
            stringRedisTemplate.delete(testKey);
            data.put("status", "UP");
            data.put("redis", "connected");
            data.put("ping_result", "ping".equals(value) ? "OK" : "FAIL");
            return R.ok(data);
        } catch (Exception e) {
            log.error("Redis 连接检查失败: {}", e.getMessage());
            data.put("status", "DOWN");
            data.put("redis", "disconnected");
            data.put("error", e.getMessage());
            return R.fail(503, "Redis 连接异常");
        }
    }
}
