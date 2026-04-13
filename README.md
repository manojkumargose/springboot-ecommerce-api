\# ⚡ ShopAI — AI-Powered E-Commerce Platform



!\[CI/CD](https://github.com/manojkumargose/springboot-ecommerce-api/actions/workflows/ci.yml/badge.svg)

!\[Java](https://img.shields.io/badge/Java-21-orange)

!\[Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)

!\[React](https://img.shields.io/badge/React-18-blue)

!\[Docker](https://img.shields.io/badge/Docker-12%20Containers-2496ED)



Full-stack e-commerce with \*\*AI dynamic pricing\*\*, \*\*microservices\*\*, and \*\*12-container Docker deployment\*\*.



\---



\## 📸 Screenshots



\### Frontend — Product View

!\[Products](images/frontend-product-view.png)



\### Frontend — Sign In

!\[Sign In](images/frontend-sign-in-page.png)



\### Admin Panel

!\[Admin](images/admin-pannel.png)



\### AI Pricing Analytics

!\[AI Pricing](images/ai-pricing.png)



\### Grafana Monitoring Dashboard

!\[Grafana](images/grafana-dashboard.png)



\### Prometheus Targets (All UP ✅)

!\[Prometheus](images/prometheus-targets.png)



\### Eureka Service Discovery

!\[Eureka](images/eureka-services.png)



\### RabbitMQ Message Broker

!\[RabbitMQ](images/rabbitmq.png)



\### CI/CD Pipeline (GitHub Actions)

!\[CI/CD](images/github-actions.png)



\---



\## 🛠️ Tech Stack



\*\*Backend:\*\* Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA, JWT Auth

\*\*Frontend:\*\* React 18, Tailwind CSS

\*\*Microservices:\*\* Spring Cloud (Eureka, API Gateway), RabbitMQ, Redis

\*\*AI:\*\* Stock-based dynamic pricing — `price = basePrice × (1 + 0.25 × percentSold)`

\*\*Database:\*\* MySQL 8.0, Redis (cache)

\*\*Monitoring:\*\* Prometheus, Grafana, Zipkin

\*\*CI/CD:\*\* GitHub Actions (42 tests on every push)

\*\*Infra:\*\* Docker Compose (12 containers), Kubernetes manifests



\---



\## 🏗️ Architecture



```

React UI (3001) → API Gateway (8084) → Eureka (8761)

&#x20;                       ↓

&#x20;        ┌──────────────┴──────────────┐

&#x20;        ↓                             ↓

&#x20;  Monolith (8080)            AI Pricing (8083)

&#x20;  MySQL + Redis              MySQL (pricing\_db)

&#x20;        └──────────┬─────────────────┘

&#x20;                RabbitMQ

&#x20;                   ↓

&#x20;        Prometheus → Grafana → Zipkin

```



\---



\## 🐳 Quick Start



```bash

git clone https://github.com/manojkumargose/springboot-ecommerce-api.git

cd springboot-ecommerce-api/ecommerce/microservices

docker-compose up -d

```



Open http://localhost:3001 | Admin: `admin2` / `admin123`



\---



\## 🧪 Testing



\*\*42 unit tests\*\* — JUnit 5 + Mockito + AssertJ



```bash

cd ecommerce \&\& ./mvnw test

```



| Test Class | Tests | Covers |

|-----------|-------|--------|

| ProductServiceTest | 20 | CRUD, AI pricing, stock, recommendations |

| CartServiceTest | 7 | Add, remove, stock validation |

| ProductTest | 5 | Entity, inStock, demand levels |

| ProductControllerTest | 4 | API endpoints |

| AuthControllerTest | 4 | Register, login |

| EventPublisherServiceTest | 2 | RabbitMQ events |



\---



\## 📊 AI Pricing Formula



```

price = basePrice × (1 + 0.25 × percentSold)



Example: ₹1000 base, 50 stock

&#x20; 10 sold → ₹1,050  |  25 sold → ₹1,125  |  40 sold → ₹1,200

&#x20; Admin restocks → price drops back to ₹1,000

```



\---



\## 📁 Project Structure



```

├── .github/workflows/ci.yml     # CI/CD

├── ecommerce/                    # Spring Boot monolith + 42 tests

├── ecommerce-frontend/           # React + Tailwind

└── ecommerce/microservices/      # Eureka, Gateway, AI Pricing, Docker, K8s

```



\---



\## 👤 Author



\*\*Manoj Kumar Gose\*\* — \[@manojkumargose](https://github.com/manojkumargose)

