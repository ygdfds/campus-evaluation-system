package com.campus.evaluation.ai.service.impl;

import com.campus.evaluation.ai.domain.AiAskRequest;
import com.campus.evaluation.ai.service.AiAssistantService;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final int MAX_DOCUMENTS = 8;
    private static final int MAX_CONTEXT_CHARS = 12000;

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    @Value("${campus.ai.ollama-base-url:http://127.0.0.1:11434}")
    private String ollamaBaseUrl;

    @Value("${campus.ai.chat-model:qwen2.5:7b}")
    private String chatModel;

    @Value("${campus.ai.embedding-model:nomic-embed-text}")
    private String embeddingModel;

    @Value("${campus.ai.request-timeout-seconds:120}")
    private long requestTimeoutSeconds;

    private final Map<String, float[]> embeddingCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("baseUrl", normalizedBaseUrl());
        result.put("chatModel", chatModel);
        result.put("embeddingModel", embeddingModel);
        try {
            JsonNode tags = getJson("/api/tags");
            List<String> models = new ArrayList<>();
            tags.path("models").forEach(model -> models.add(model.path("name").asText()));
            result.put("available", true);
            result.put("models", models);
            result.put("chatModelReady", models.stream().anyMatch(name -> sameModel(name, chatModel)));
            result.put("embeddingModelReady", models.stream().anyMatch(name -> sameModel(name, embeddingModel)));
            result.put("message", "Ollama 已连接");
        } catch (Exception ex) {
            result.put("available", false);
            result.put("models", Collections.emptyList());
            result.put("chatModelReady", false);
            result.put("embeddingModelReady", false);
            result.put("message", "无法连接 Ollama，请确认服务已启动");
        }
        return result;
    }

    @Override
    public Map<String, Object> ask(AiAskRequest request) {
        String question = request == null || request.getQuestion() == null
                ? ""
                : request.getQuestion().trim();
        if (question.isBlank()) {
            throw new BusinessException(400, "请输入问题");
        }
        if (question.length() > 1000) {
            throw new BusinessException(400, "问题不能超过1000个字符");
        }

        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null && !SecurityUtils.hasRole("system_admin")) {
            throw new BusinessException(403, "当前账号没有租户信息");
        }

        List<KnowledgeDocument> documents = loadDocuments(tenantId);
        List<KnowledgeDocument> relevant = retrieve(question, documents);
        String prompt = buildPrompt(question, relevant, request == null ? null : request.getConversation());
        String answer = generate(prompt);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answer", answer);
        result.put("model", chatModel);
        result.put("retrievedCount", relevant.size());
        result.put("sources", relevant.stream().map(document -> {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("type", document.type());
            source.put("title", document.title());
            source.put("link", document.link());
            return source;
        }).toList());
        return result;
    }

    private List<KnowledgeDocument> loadDocuments(Long tenantId) {
        Long schoolId = SecurityUtils.getSchoolId();
        List<KnowledgeDocument> documents = new ArrayList<>();

        String tenantCondition = tenantId == null ? "" : " AND tenant_id = ?";
        List<Object> tenantArgs = tenantId == null ? List.of() : List.of(tenantId);

        List<Map<String, Object>> faqs = jdbc.queryForList("""
                SELECT id, question, answer, category, keywords, target_roles, school_id
                FROM msg_help_faq
                WHERE enabled = 1 AND status = 'enabled' AND deleted = 0
                """ + tenantCondition, tenantArgs.toArray());
        for (Map<String, Object> row : faqs) {
            if (!sameSchool(row.get("school_id"), schoolId)
                    || !roleVisible(row.get("target_roles"))) {
                continue;
            }
            String question = text(row.get("question"));
            String answer = text(row.get("answer"));
            documents.add(new KnowledgeDocument(
                    "faq-" + row.get("id"),
                    "FAQ",
                    question,
                    "问题：" + question + "\n答案：" + answer + "\n分类：" + text(row.get("category"))
                            + "\n关键词：" + text(row.get("keywords")),
                    null
            ));
        }

        List<Map<String, Object>> guides = jdbc.queryForList("""
                SELECT id, title, module, summary, steps, related_link, target_roles, school_id
                FROM msg_help_guide
                WHERE enabled = 1 AND deleted = 0
                """ + tenantCondition, tenantArgs.toArray());
        for (Map<String, Object> row : guides) {
            if (!sameSchool(row.get("school_id"), schoolId)
                    || !roleVisible(row.get("target_roles"))) {
                continue;
            }
            String steps = jsonText(row.get("steps"));
            documents.add(new KnowledgeDocument(
                    "guide-" + row.get("id"),
                    "操作指引",
                    text(row.get("title")),
                    "标题：" + text(row.get("title")) + "\n模块：" + text(row.get("module"))
                            + "\n摘要：" + text(row.get("summary")) + "\n步骤：" + steps,
                    text(row.get("related_link"))
            ));
        }

        List<Map<String, Object>> announcements = jdbc.queryForList("""
                SELECT id, title, summary, content, tag, publish_time, target_roles, school_id
                FROM msg_announcement
                WHERE status = 'published' AND deleted = 0
                """ + tenantCondition + " ORDER BY publish_time DESC", tenantArgs.toArray());
        for (Map<String, Object> row : announcements) {
            if (!sameSchool(row.get("school_id"), schoolId)
                    || !roleVisible(row.get("target_roles"))) {
                continue;
            }
            documents.add(new KnowledgeDocument(
                    "announcement-" + row.get("id"),
                    "校园公告",
                    text(row.get("title")),
                    "标题：" + text(row.get("title")) + "\n摘要：" + text(row.get("summary"))
                            + "\n正文：" + text(row.get("content")) + "\n发布时间：" + text(row.get("publish_time")),
                    "/student/announcements/" + row.get("id")
            ));
        }

        List<Map<String, Object>> profiles = jdbc.queryForList("""
                SELECT name, address, website, intro
                FROM sch_school_profile
                WHERE deleted = 0
                """ + tenantCondition + " LIMIT 1", tenantArgs.toArray());
        for (Map<String, Object> row : profiles) {
            documents.add(new KnowledgeDocument(
                    "school-profile",
                    "学校资料",
                    firstNonBlank(text(row.get("name")), "学校资料"),
                    "学校名称：" + firstNonBlank(text(row.get("name")), "")
                            + "\n地址：" + text(row.get("address"))
                            + "\n官网：" + text(row.get("website"))
                            + "\n简介：" + text(row.get("intro")),
                    null
            ));
        }
        return documents;
    }

    private List<KnowledgeDocument> retrieve(String question, List<KnowledgeDocument> documents) {
        if (documents.isEmpty()) {
            return List.of();
        }

        Map<String, Double> scores = new HashMap<>();
        float[] queryEmbedding = null;
        try {
            queryEmbedding = embed(question);
        } catch (Exception ex) {
            log.warn("Ollama embedding unavailable, using keyword retrieval: {}", ex.getMessage());
        }

        SetTokens questionTokens = SetTokens.of(question);
        for (KnowledgeDocument document : documents) {
            double keywordScore = keywordScore(questionTokens, document.searchText());
            double score = keywordScore;
            if (queryEmbedding != null) {
                float[] documentEmbedding = embeddingCache.computeIfAbsent(
                        document.cacheKey() + ":" + embeddingModel,
                        ignored -> {
                            try {
                                return embed(document.content());
                            } catch (Exception ex) {
                                log.warn("Embedding failed for {}", document.id(), ex);
                                return new float[0];
                            }
                        }
                );
                if (documentEmbedding.length > 0) {
                    score += cosine(queryEmbedding, documentEmbedding);
                }
            }
            scores.put(document.id(), score);
        }

        return documents.stream()
                .sorted(Comparator.comparingDouble((KnowledgeDocument document) -> scores.getOrDefault(document.id(), 0D))
                        .reversed())
                .limit(MAX_DOCUMENTS)
                .toList();
    }

    private String buildPrompt(String question, List<KnowledgeDocument> relevant, List<String> conversation) {
        String context = relevant.stream()
                .map(document -> "[" + document.type() + "] " + document.title() + "\n" + document.content())
                .collect(Collectors.joining("\n\n"));
        if (context.length() > MAX_CONTEXT_CHARS) {
            context = context.substring(0, MAX_CONTEXT_CHARS);
        }
        String history = conversation == null ? "" : conversation.stream()
                .filter(Objects::nonNull)
                .limit(6)
                .collect(Collectors.joining("\n"));
        return """
                你是校园服务质量在线评测系统的智能问答助手。
                请使用下面的知识库内容回答用户问题。只能把知识库作为事实依据，不要编造系统不存在的功能、数据或政策。
                如果知识库没有足够信息，请明确说“当前知识库没有找到相关信息”，并建议用户联系学校管理员。
                回答使用简体中文，先给结论，再给操作步骤；不要泄露其他租户、其他学校或内部实现细节。

                当前用户问题：
                %s

                最近对话：
                %s

                知识库检索结果：
                %s
                """.formatted(question, history.isBlank() ? "无" : history, context.isBlank() ? "无" : context);
    }

    private String generate(String prompt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", chatModel);
        payload.put("prompt", prompt);
        payload.put("stream", false);
        payload.put("options", Map.of("temperature", 0.2));
        JsonNode response = postJson("/api/generate", payload);
        String answer = response.path("response").asText("").trim();
        if (answer.isBlank()) {
            throw new BusinessException(503, "Ollama 没有返回回答，请检查模型是否已下载");
        }
        return answer;
    }

    private float[] embed(String input) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", embeddingModel);
            payload.put("input", List.of(input));
            JsonNode response = postJson("/api/embed", payload);
            JsonNode embeddings = response.path("embeddings");
            if (embeddings.isArray() && embeddings.size() > 0) {
                return toVector(embeddings.get(0));
            }
        } catch (Exception ex) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", embeddingModel);
            payload.put("prompt", input);
            JsonNode response = postJson("/api/embeddings", payload);
            return toVector(response.path("embedding"));
        }
        return new float[0];
    }

    private JsonNode getJson(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(normalizedBaseUrl() + path))
                .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                .GET()
                .build();
        return send(request);
    }

    private JsonNode postJson(String path, Object payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizedBaseUrl() + path))
                    .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            return send(request);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(503, "Ollama 请求失败，请确认本机服务已启动");
        }
    }

    private JsonNode send(HttpRequest request) {
        try {
            HttpResponse<String> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String detail = response.body();
                throw new BusinessException(503, "Ollama 返回错误：" + (detail.length() > 180 ? detail.substring(0, 180) : detail));
            }
            return objectMapper.readTree(response.body());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(503, "无法连接 Ollama，请确认服务已启动");
        }
    }

    private boolean roleVisible(Object rawRoles) {
        List<String> roles = readStringList(rawRoles);
        return roles.isEmpty() || roles.stream().anyMatch(SecurityUtils.getRoles()::contains);
    }

    private boolean sameSchool(Object rowSchoolId, Long currentSchoolId) {
        if (rowSchoolId == null || currentSchoolId == null) {
            return true;
        }
        return String.valueOf(rowSchoolId).equals(String.valueOf(currentSchoolId));
    }

    private List<String> readStringList(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return List.of();
        }
        try {
            if (value instanceof String text) {
                return objectMapper.readValue(text, new TypeReference<>() {});
            }
            return objectMapper.convertValue(value, new TypeReference<>() {});
        } catch (Exception ignored) {
            return Arrays.stream(String.valueOf(value).split(","))
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
    }

    private float[] toVector(JsonNode node) {
        if (!node.isArray()) {
            return new float[0];
        }
        float[] vector = new float[node.size()];
        for (int i = 0; i < node.size(); i++) {
            vector[i] = (float) node.get(i).asDouble();
        }
        return vector;
    }

    private double cosine(float[] left, float[] right) {
        if (left.length == 0 || left.length != right.length) {
            return 0D;
        }
        double dot = 0D;
        double leftNorm = 0D;
        double rightNorm = 0D;
        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }
        return leftNorm == 0 || rightNorm == 0 ? 0D : dot / Math.sqrt(leftNorm * rightNorm);
    }

    private double keywordScore(SetTokens questionTokens, String content) {
        SetTokens documentTokens = SetTokens.of(content);
        if (questionTokens.values().isEmpty()) {
            return 0D;
        }
        long matches = questionTokens.values().stream().filter(documentTokens.values()::contains).count();
        return (double) matches / questionTokens.values().size();
    }

    private String jsonText(Object value) {
        if (value == null) {
            return "";
        }
        try {
            JsonNode node = value instanceof String text ? objectMapper.readTree(text) : objectMapper.valueToTree(value);
            return node.isArray()
                    ? String.join("；", objectMapper.convertValue(node, new TypeReference<List<String>>() {}))
                    : node.toString();
        } catch (Exception ignored) {
            return String.valueOf(value);
        }
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        return Arrays.stream(values).filter(value -> value != null && !value.isBlank()).findFirst().orElse("");
    }

    private boolean sameModel(String installed, String configured) {
        return installed.equals(configured) || installed.startsWith(configured + ":")
                || configured.startsWith(installed + ":");
    }

    private String normalizedBaseUrl() {
        return ollamaBaseUrl.endsWith("/") ? ollamaBaseUrl.substring(0, ollamaBaseUrl.length() - 1) : ollamaBaseUrl;
    }

    private record KnowledgeDocument(String id, String type, String title, String content, String link) {
        private String searchText() {
            return title + "\n" + content;
        }

        private String cacheKey() {
            return id + ":" + Integer.toHexString(content.hashCode());
        }
    }

    private record SetTokens(List<String> values) {
        private static SetTokens of(String value) {
            return new SetTokens(Arrays.stream(value.toLowerCase(Locale.ROOT)
                            .split("[^\\p{L}\\p{N}]+"))
                    .map(String::trim)
                    .filter(token -> token.length() > 1)
                    .distinct()
                    .toList());
        }
    }
}
