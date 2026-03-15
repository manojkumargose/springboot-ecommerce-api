package com.example.ecommerce.dto;

import java.util.List;
import java.util.Map;

public class DashboardResponse {

    private Long totalOrders;
    private Long totalUsers;
    private Long totalProducts;
    private Double totalRevenue;
    private Map<String, Long> ordersByStatus;
    private List<OrderResponse> recentOrders;
    private List<ProductResponse> lowStockProducts;
    private List<BestSellingProduct> bestSellingProducts;

    // ─── Getters & Setters ───────────────────────────────────

    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }

    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

    public Long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Long totalProducts) { this.totalProducts = totalProducts; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Map<String, Long> getOrdersByStatus() { return ordersByStatus; }
    public void setOrdersByStatus(Map<String, Long> ordersByStatus) { this.ordersByStatus = ordersByStatus; }

    public List<OrderResponse> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<OrderResponse> recentOrders) { this.recentOrders = recentOrders; }

    public List<ProductResponse> getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(List<ProductResponse> lowStockProducts) { this.lowStockProducts = lowStockProducts; }

    public List<BestSellingProduct> getBestSellingProducts() { return bestSellingProducts; }
    public void setBestSellingProducts(List<BestSellingProduct> bestSellingProducts) { this.bestSellingProducts = bestSellingProducts; }

    // ─── Inner class for best selling products ────────────────

    public static class BestSellingProduct {
        private Long productId;
        private String productName;
        private Long totalSold;
        private Double totalRevenue;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public Long getTotalSold() { return totalSold; }
        public void setTotalSold(Long totalSold) { this.totalSold = totalSold; }

        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
    }
}