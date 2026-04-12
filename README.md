\# ⚡ ShopAI — AI-Powered E-Commerce Platform



!\[CI/CD](https://github.com/manojkumargose/springboot-ecommerce-api/actions/workflows/ci.yml/badge.svg)

!\[Java](https://img.shields.io/badge/Java-21-orange)

!\[Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)

!\[React](https://img.shields.io/badge/React-18-blue)

!\[Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

!\[License](https://img.shields.io/badge/License-MIT-yellow)



A full-stack e-commerce platform with \*\*AI-powered dynamic pricing\*\*, \*\*microservices architecture\*\*, and \*\*12-container Docker deployment\*\*. Built with Spring Boot, React, and Spring Cloud.



\---



\## 🎯 Key Features



\### Customer Features

\- \*\*Product Browsing\*\* — Search, filter by category/price, pagination \& sorting

\- \*\*Shopping Cart\*\* — Add, update quantity, remove items with stock validation

\- \*\*Wishlist\*\* — Save products for later

\- \*\*Order Management\*\* — Place orders, track status, cancel orders with auto stock restore

\- \*\*Reviews \& Ratings\*\* — Rate products, view average ratings

\- \*\*AI Recommendations\*\* — "Customers Also Bought" based on purchase history

\- \*\*ShopAI Pay\*\* — Integrated payment gateway with invoice PDF generation

\- \*\*Coupon System\*\* — Apply discount codes at checkout

\- \*\*Email Notifications\*\* — Order confirmation \& welcome emails via Gmail SMTP



\### Admin Features

\- \*\*Admin Dashboard\*\* — Revenue, orders, users, and product analytics

\- \*\*Product Management\*\* — CRUD with Cloudinary image upload

\- \*\*Order Management\*\* — View and update order statuses

\- \*\*Coupon Management\*\* — Create, activate/deactivate discount codes

\- \*\*AI Analytics Dashboard\*\* — Demand events, trending products, pricing insights

\- \*\*Stock Monitoring\*\* — Low stock and out-of-stock alerts



\### AI \& Dynamic Pricing

\- \*\*Stock-Based Pricing Formula\*\*: `price = basePrice × (1 + 0.25 × percentSold)`

\- Prices increase up to 25% as stock depletes

\- Automatic price recalculation on every purchase and stock restore

\- Demand levels: HIGH (≥70% sold), MEDIUM (≥30%), LOW (<30%)

\- Real-time demand event tracking via RabbitMQ



\---



\## 🏗️ Architecture



```

┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐

│   React UI  │────▶│  API Gateway     │────▶│  Eureka Server  │

│  (Port 3001)│     │  (Port 8084)     │     │  (Port 8761)    │

└─────────────┘     └──────────────────┘     └─────────────────┘

&#x20;      │                     │                        │

&#x20;      │              ┌──────┴──────┐                 │

&#x20;      │              │             │                 │

&#x20;      ▼              ▼             ▼                 │

┌─────────────┐ ┌───────────┐ ┌──────────────┐       │

│  Monolith   │ │ AI Pricing│ │   Services   │───────┘

│  (Port 8080)│ │ (Port 8083│ │  registered   │

└──────┬──────┘ └─────┬─────┘ └──────────────┘

&#x20;      │              │

&#x20;      ▼              ▼

┌─────────────┐ ┌───────────┐

│    MySQL    │ │ Pricing DB│

│  (ecommerce)│ │(pricing\_db│

└─────────────┘ └───────────┘

&#x20;      │              │

&#x20;      └──────┬───────┘

&#x20;             ▼

&#x20;      ┌─────────────┐     ┌───────┐     ┌────────┐

&#x20;      │  RabbitMQ   │     │ Redis │     │ Zipkin │

&#x20;      │  (Events)   │     │(Cache)│     │(Trace) │

&#x20;      └─────────────┘     └───────┘     └────────┘

```



\---



\## 🛠️ Tech Stack



| Layer | Technology |

|-------|-----------|

| \*\*Backend\*\* | Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA |

| \*\*Frontend\*\* | React 18, Tailwind CSS, Plus Jakarta Sans |

| \*\*Microservices\*\* | Spring Cloud (Eureka, API Gateway, OpenFeign) |

| \*\*Messaging\*\* | RabbitMQ (demand events, price sync) |

| \*\*Database\*\* | MySQL 8.0, Redis (caching) |

| \*\*AI/Pricing\*\* | Custom stock-based dynamic pricing algorithm |

| \*\*Auth\*\* | JWT (JSON Web Tokens) with role-based access (USER/ADMIN) |

| \*\*File Storage\*\* | Cloudinary (product images) |

| \*\*Email\*\* | Gmail SMTP (order confirmations) |

| \*\*PDF\*\* | iTextPDF (invoice generation) |

| \*\*Monitoring\*\* | Prometheus, Grafana, Zipkin (distributed tracing) |

| \*\*CI/CD\*\* | GitHub Actions |

| \*\*Containerization\*\* | Docker, Docker Compose (12 containers) |

| \*\*Orchestration\*\* | Kubernetes manifests (ready for deployment) |

| \*\*Testing\*\* | JUnit 5, Mockito, AssertJ (42 tests) |



\---



\## 🐳 Docker Deployment (12 Containers)



| Container | Port | Purpose |

|-----------|------|---------|

| shopai-frontend | 3001 | React UI (nginx) |

| ecommerce-service | 8080 | Spring Boot monolith |

| ai-pricing-service | 8083 | AI dynamic pricing |

| eureka-server | 8761 | Service discovery |

| api-gateway | 8084 | API routing |

| ecommerce-db | 3307 | MySQL (ecommerce) |

| pricing-db | 3309 | MySQL (pricing\_db) |

| rabbitmq | 5672/15672 | Message broker |

| redis | 6379 | Cache |

| prometheus | 9090 | Metrics collection |

| grafana | 3030 | Monitoring dashboards |

| zipkin | 9411 | Distributed tracing |



\### Quick Start with Docker



```bash

\# Clone the repo

git clone https://github.com/manojkumargose/springboot-ecommerce-api.git

cd springboot-ecommerce-api/ecommerce/microservices



\# Start all 12 containers

docker-compose up -d



\# Verify

docker ps

```



Open http://localhost:3001 to access the app.



\---



\## 🧪 Testing



\*\*42 unit \& integration tests\*\* covering:



| Test Class | Tests | Coverage |

|-----------|-------|----------|

| ProductServiceTest | 20 | CRUD, stock pricing, demand levels, recommendations |

| CartServiceTest | 7 | Add, remove, update, stock validation |

| ProductTest | 5 | Entity fields, inStock, pricing |

| ProductControllerTest | 4 | API endpoints, response format |

| AuthControllerTest | 4 | Register, login, duplicate check |

| EventPublisherServiceTest | 2 | RabbitMQ event publishing |



```bash

\# Run all tests

cd ecommerce

./mvnw test

```



\---



\## 📁 Project Structure



```

springboot-ecommerce-api/

├── .github/workflows/

│   └── ci.yml                    # CI/CD pipeline

├── ecommerce/                    # Spring Boot monolith

│   ├── src/main/java/            # Controllers, Services, Entities

│   ├── src/test/java/            # 42 unit tests

│   ├── Dockerfile

│   └── pom.xml

├── ecommerce-frontend/           # React frontend

│   ├── src/pages/                # All page components

│   ├── src/components/           # Navbar, etc.

│   ├── Dockerfile

│   └── nginx.conf

└── ecommerce/microservices/      # Spring Cloud microservices

&#x20;   ├── ai-pricing-service/       # AI dynamic pricing

&#x20;   ├── eureka-server/            # Service discovery

&#x20;   ├── api-gateway/              # API routing

&#x20;   ├── k8s/                      # Kubernetes manifests

&#x20;   ├── docker-compose.yml        # 12-container orchestration

&#x20;   └── prometheus.yml            # Monitoring config

```



\---



\## 🔌 API Endpoints



\### Auth

| Method | Endpoint | Description |

|--------|----------|-------------|

| POST | `/api/v1/auth/register` | Register new user |

| POST | `/api/v1/auth/login` | Login \& get JWT token |



\### Products

| Method | Endpoint | Description |

|--------|----------|-------------|

| GET | `/api/v1/products` | List available products (paginated) |

| GET | `/api/v1/products/{id}` | Get product details |

| GET | `/api/v1/products/{id}/recommendations` | AI recommendations |

| POST | `/api/v1/products` | Add product (ADMIN) |

| PUT | `/api/v1/products/{id}` | Update product (ADMIN) |

| DELETE | `/api/v1/products/{id}` | Delete product (ADMIN) |



\### Cart \& Orders

| Method | Endpoint | Description |

|--------|----------|-------------|

| GET | `/api/v1/cart` | Get my cart |

| POST | `/api/v1/cart/add` | Add to cart |

| DELETE | `/api/v1/cart/remove/{productId}` | Remove from cart |

| POST | `/api/v1/orders` | Place order |

| GET | `/api/v1/orders` | My orders |

| POST | `/api/v1/orders/{id}/cancel` | Cancel order |



\### AI Pricing (via AI Service)

| Method | Endpoint | Description |

|--------|----------|-------------|

| GET | `/api/v1/ai/analytics` | Demand analytics |

| GET | `/api/v1/ai/trending` | Trending products |



\---



\## ⚙️ Environment Setup (Local Development)



\### Prerequisites

\- Java 21

\- Node.js 18+

\- MySQL 8.0

\- Docker Desktop

\- RabbitMQ \& Redis (via Docker)



\### Backend (Monolith)

```bash

cd ecommerce

\# Update application.properties with your MySQL credentials

./mvnw spring-boot:run

```



\### Frontend

```bash

cd ecommerce-frontend

npm install

npm start     # Runs on port 3001

```



\### Infrastructure (Docker)

```bash

\# Start MySQL, Redis, RabbitMQ

docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

docker run -d --name redis -p 6379:6379 redis:7-alpine

```



\---



\## 📊 AI Pricing Algorithm



```

price = basePrice × (1 + 0.25 × percentSold)



Where:

&#x20; percentSold = (initialStock - currentStock) / initialStock



Example:

&#x20; basePrice = ₹1000, initialStock = 50



&#x20; 10 sold → percentSold = 20% → price = ₹1,050

&#x20; 25 sold → percentSold = 50% → price = ₹1,125

&#x20; 40 sold → percentSold = 80% → price = ₹1,200 (HIGH demand)

&#x20; Admin restocks → price drops back to ₹1,000

```



\---



\## 👤 Demo Credentials



| Role | Username | Password |

|------|----------|----------|

| Admin | admin2 | admin123 |

| User | Register via `/auth/register` | — |



\---



\## 🚀 CI/CD Pipeline



Every push to `main` triggers:

1\. \*\*Test Monolith\*\* — 42 unit tests with JUnit 5

2\. \*\*Build AI Pricing Service\*\* — Maven package

3\. \*\*Build Frontend\*\* — npm install + React build

4\. \*\*Build Docker Images\*\* — All 3 services containerized



\---



\## 📄 License



This project is open source and available under the \[MIT License](LICENSE).



\---



\## 🙋‍♂️ Author



\*\*Manoj Kumar Gose\*\*

\- GitHub: \[@manojkumargose](https://github.com/manojkumargose)



\---



> Built as a full-stack portfolio project demonstrating Spring Boot microservices, AI-powered pricing, React frontend, Docker deployment, and CI/CD best practices.

