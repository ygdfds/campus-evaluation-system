package com.campus.evaluation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园服务质量在线评测系统 - 后端启动类
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.campus.evaluation")
public class CampusEvaluationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusEvaluationApplication.class, args);
        log.info("====================================");
        log.info("  校园服务质量在线评测系统后端启动成功！");
        log.info("  接口文档: http://localhost:8080/api/doc.html");
        log.info("  健康检查: http://localhost:8080/api/health");
        log.info("====================================");
    }
}
