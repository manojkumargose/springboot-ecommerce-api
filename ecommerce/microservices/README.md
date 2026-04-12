# E-Commerce Microservices

## Architecture
```
Client → API Gateway (8080) → Eureka (8761) → Auth Service (8081)
                                             → Core Service (8082)
                                             → AI Pricing Service (8083)
```

## RabbitMQ Event Flow
```
Core Service ──[DemandEventMessage]──► AI Pricing Service
                                              │ recalculates price
Core Service ◄──[PriceUpdateMessage]──────────┘
```

## Services

| Service              | Port | DB Port | Responsibility                          |
|----------------------|------|---------|------------------------------------------|
| Eureka Server        | 8761 | —       | Service discovery                        |
| API Gateway          | 8080 | —       | Single entry point, routing             |
| Auth Service         | 8081 | 3307    | Login, register, JWT, user, address     |
| Core Service         | 8082 | 3308    | Products, orders, cart, reviews, etc.   |
| AI Pricing Service   | 8083 | 3309    | Dynamic pricing, demand analytics       |

## Quick Start

### 1. Build all services
```bash
cd auth-service && mvn clean package -DskipTests && cd ..
cd core-service && mvn clean package -DskipTests && cd ..
cd ai-pricing-service && mvn clean package -DskipTests && cd ..
```

### 2. Run with Docker Compose
```bash
docker-compose up --build
```

### 3. Access
- API Gateway:     http://localhost:8080
- Eureka Dashboard: http://localhost:8761
- RabbitMQ UI:     http://localhost:15672 (guest/guest)

## API Endpoints

### Auth Service
```
POST /api/auth/register     → Register user
POST /api/auth/login        → Login + get JWT
GET  /api/users/me          → Current user profile
POST /api/addresses         → Add address
GET  /api/addresses         → Get user addresses
```

### Core Service
```
GET    /api/products                → List all products
GET    /api/products/{id}           → Get product (fires VIEW event)
POST   /api/products                → Create product (admin)
POST   /api/cart/add?productId=&quantity= → Add to cart (fires CART_ADD event)
GET    /api/cart                    → Get cart
POST   /api/orders                  → Place order (fires PURCHASE events)
GET    /api/orders                  → My orders
POST   /api/payments                → Process payment
POST   /api/wishlist/{productId}    → Add to wishlist (fires WISHLIST_ADD event)
GET    /api/reviews/product/{id}    → Get product reviews
POST   /api/reviews                 → Add review
GET    /api/coupons/validate?code=&amount= → Validate coupon
```

### AI Pricing Service
```
POST /api/pricing/rules                 → Create pricing rule
GET  /api/pricing/rules                 → Get active rules
POST /api/pricing/recalculate/{id}      → Trigger repricing
GET  /api/recommendations/trending      → Trending products
GET  /api/dashboard/analytics           → Demand analytics
```

## Seed a Default Pricing Rule
```json
POST http://localhost:8083/api/pricing/rules
{
  "highDemandThreshold": 50,
  "mediumDemandThreshold": 20,
  "highDemandMultiplier": 1.3,
  "mediumDemandMultiplier": 1.1,
  "lowDemandMultiplier": 0.95,
  "maxPriceIncreasePercent": 50,
  "maxPriceDecreasePercent": 20,
  "demandWindowHours": 24
}
```
