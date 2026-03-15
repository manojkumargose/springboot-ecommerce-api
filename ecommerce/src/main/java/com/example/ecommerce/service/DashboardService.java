package com.example.ecommerce.service;

import com.example.ecommerce.dto.DashboardResponse;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final OrderService orderService;

    public DashboardService(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            ProductService productService,
                            OrderService orderService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
        this.orderService = orderService;
    }

    public DashboardResponse getDashboard() {
        DashboardResponse dashboard = new DashboardResponse();

        // ── Total counts ──────────────────────────────────────
        dashboard.setTotalOrders(orderRepository.count());
        dashboard.setTotalUsers(userRepository.count());
        dashboard.setTotalProducts(productRepository.count());

        // ── Total revenue ─────────────────────────────────────
        Double revenue = orderRepository.findAll()
                .stream()
                .filter(o -> !o.getStatus().equalsIgnoreCase("CANCELLED"))
                .mapToDouble(o -> o.getFinalAmount() != null ? o.getFinalAmount() : 0.0)
                .sum();
        dashboard.setTotalRevenue(Math.round(revenue * 100.0) / 100.0);

        // ── Orders by status ──────────────────────────────────
        Map<String, Long> ordersByStatus = new HashMap<>();
        ordersByStatus.put("PENDING", orderRepository.countByStatus("PENDING"));
        ordersByStatus.put("CONFIRMED", orderRepository.countByStatus("CONFIRMED"));
        ordersByStatus.put("SHIPPED", orderRepository.countByStatus("SHIPPED"));
        ordersByStatus.put("DELIVERED", orderRepository.countByStatus("DELIVERED"));
        ordersByStatus.put("CANCELLED", orderRepository.countByStatus("CANCELLED"));
        dashboard.setOrdersByStatus(ordersByStatus);

        // ── Recent 5 orders ───────────────────────────────────
        List<Order> recentOrders = orderRepository.findAll(
                        PageRequest.of(0, 5, Sort.by("createdAt").descending()))
                .getContent();
        dashboard.setRecentOrders(recentOrders.stream()
                .map(orderService::toResponsePublic)
                .collect(Collectors.toList()));

        // ── Low stock products ────────────────────────────────
        List<ProductResponse> lowStock = productService.getLowStockProducts(5);
        dashboard.setLowStockProducts(lowStock);

        // ── Best selling products ─────────────────────────────
        List<Object[]> rawBestSelling = orderRepository.findBestSellingProducts(
                PageRequest.of(0, 5));

        List<DashboardResponse.BestSellingProduct> bestSelling = new ArrayList<>();
        for (Object[] row : rawBestSelling) {
            DashboardResponse.BestSellingProduct bp =
                    new DashboardResponse.BestSellingProduct();
            bp.setProductId(((Number) row[0]).longValue());
            bp.setProductName((String) row[1]);
            bp.setTotalSold(((Number) row[2]).longValue());
            bp.setTotalRevenue(((Number) row[3]).doubleValue());
            bestSelling.add(bp);
        }
        dashboard.setBestSellingProducts(bestSelling);

        return dashboard;
    }
}