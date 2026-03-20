package com.example.ecommerce.service;

import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestTemplate restTemplate;

    @Value("${recommendation.service.url:http://localhost:8000}")
    private String recommendationServiceUrl;

    public RecommendationService(ProductRepository productRepository,
                                 OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.restTemplate = new RestTemplate();
    }

    public List<Product> getRecommendations(Long productId, int topN) {
        try {
            // Fetch all products
            List<Product> allProducts = productRepository.findAll();

            // Fetch all order items for collaborative filtering
            List<OrderItem> allOrderItems = orderItemRepository.findAll();

            // Build request payload
            Map<String, Object> request = buildRequest(productId, allProducts, allOrderItems, topN);

            // Call Python ML service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    recommendationServiceUrl + "/api/recommendations",
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Integer> recommendedIds = (List<Integer>) response.getBody().get("recommendedProductIds");
                if (recommendedIds != null) {
                    List<Long> longIds = recommendedIds.stream()
                            .map(Integer::longValue)
                            .collect(Collectors.toList());
                    return productRepository.findAllById(longIds);
                }
            }
        } catch (Exception e) {
            logger.error("Error calling recommendation service: {}", e.getMessage());
            // Fallback: return products from same category
            return getFallbackRecommendations(productId, topN);
        }

        return Collections.emptyList();
    }

    public List<Product> getSimilarProducts(Long productId, int topN) {
        try {
            List<Product> allProducts = productRepository.findAll();
            Map<String, Object> request = buildRequest(productId, allProducts, Collections.emptyList(), topN);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    recommendationServiceUrl + "/api/recommendations/similar",
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Integer> recommendedIds = (List<Integer>) response.getBody().get("recommendedProductIds");
                if (recommendedIds != null) {
                    List<Long> longIds = recommendedIds.stream()
                            .map(Integer::longValue)
                            .collect(Collectors.toList());
                    return productRepository.findAllById(longIds);
                }
            }
        } catch (Exception e) {
            logger.error("Error calling recommendation service: {}", e.getMessage());
            return getFallbackRecommendations(productId, topN);
        }

        return Collections.emptyList();
    }

    public List<Product> getBoughtTogether(Long productId, int topN) {
        try {
            List<Product> allProducts = productRepository.findAll();
            List<OrderItem> allOrderItems = orderItemRepository.findAll();
            Map<String, Object> request = buildRequest(productId, allProducts, allOrderItems, topN);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    recommendationServiceUrl + "/api/recommendations/bought-together",
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Integer> recommendedIds = (List<Integer>) response.getBody().get("recommendedProductIds");
                if (recommendedIds != null) {
                    List<Long> longIds = recommendedIds.stream()
                            .map(Integer::longValue)
                            .collect(Collectors.toList());
                    return productRepository.findAllById(longIds);
                }
            }
        } catch (Exception e) {
            logger.error("Error calling recommendation service: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    private Map<String, Object> buildRequest(Long productId, List<Product> products,
                                             List<OrderItem> orderItems, int topN) {
        List<Map<String, Object>> productList = products.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("description", p.getDescription() != null ? p.getDescription() : "");
            map.put("price", p.getPrice());
            map.put("categoryId", p.getCategory() != null ? p.getCategory().getId() : null);
            map.put("categoryName", p.getCategory() != null ? p.getCategory().getName() : "");
            return map;
        }).collect(Collectors.toList());

        List<Map<String, Object>> orderItemList = orderItems.stream().map(oi -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", oi.getProduct().getId());
            map.put("orderId", oi.getOrder().getId());
            map.put("userId", oi.getOrder().getUser().getId());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> request = new HashMap<>();
        request.put("productId", productId);
        request.put("products", productList);
        request.put("orderItems", orderItemList);
        request.put("topN", topN);

        return request;
    }

    private List<Product> getFallbackRecommendations(Long productId, int topN) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent() && productOpt.get().getCategory() != null) {
            Long categoryId = productOpt.get().getCategory().getId();
            return productRepository.findAll().stream()
                    .filter(p -> p.getCategory() != null
                            && p.getCategory().getId().equals(categoryId)
                            && !p.getId().equals(productId))
                    .limit(topN)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}