package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    @Value("${ml.service.url:http://localhost:5000}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RecommendationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // ── Recommend similar products for a given product ────────────────────────

    public List<Map<String, Object>> getRecommendations(Long productId, Long userId, int topN) {
        try {
            String url = mlServiceUrl + "/recommend";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", productId);
            if (userId != null) requestBody.put("userId", userId);
            requestBody.put("topN", topN);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode recommendations = root.get("recommendations");
                List<Map<String, Object>> result = new ArrayList<>();
                if (recommendations != null && recommendations.isArray()) {
                    for (JsonNode node : recommendations) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("productId", node.get("productId").asLong());
                        item.put("name",      node.get("name").asText());
                        item.put("category",  node.get("category").asText());
                        item.put("price",     node.get("price").asDouble());
                        item.put("score",     node.get("score").asDouble());
                        result.add(item);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.error("ML service call failed for productId={}: {}", productId, e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Personalised recommendations for a user (homepage) ───────────────────

    public List<Map<String, Object>> getRecommendationsForUser(Long userId, int topN) {
        try {
            String url = mlServiceUrl + "/recommend/user/" + userId + "?topN=" + topN;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode recommendations = root.get("recommendations");
                List<Map<String, Object>> result = new ArrayList<>();
                if (recommendations != null && recommendations.isArray()) {
                    for (JsonNode node : recommendations) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("productId", node.get("productId").asLong());
                        item.put("name",      node.get("name").asText());
                        item.put("category",  node.get("category").asText());
                        item.put("price",     node.get("price").asDouble());
                        item.put("score",     node.get("score").asDouble());
                        result.add(item);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.error("ML service call failed for userId={}: {}", userId, e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Popular products fallback ─────────────────────────────────────────────

    public List<Map<String, Object>> getPopularProducts(int topN) {
        try {
            String url = mlServiceUrl + "/popular?topN=" + topN;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode recommendations = root.get("recommendations");
                List<Map<String, Object>> result = new ArrayList<>();
                if (recommendations != null && recommendations.isArray()) {
                    for (JsonNode node : recommendations) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("productId", node.get("productId").asLong());
                        item.put("name",      node.get("name").asText());
                        item.put("price",     node.get("price").asDouble());
                        item.put("score",     node.get("score").asDouble());
                        result.add(item);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.error("ML popular products call failed: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Health check ──────────────────────────────────────────────────────────

    public boolean isMlServiceHealthy() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    mlServiceUrl + "/health", String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}