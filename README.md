# market-hub

An e-commerce backend with Kafka-based async order processing. When an order is placed, it gets published to a Kafka topic and two consumers pick it up — one updates inventory, one creates a notification. Built this to understand how event-driven architecture works in a real e-commerce flow.

Runs locally — needs MySQL, Kafka and Redis running.

---

## Stack

- Spring Boot, Java 17
- Apache Kafka (async order processing)
- MySQL + Spring Data JPA
- Redis (product caching)
- Spring Security
- Thymeleaf (frontend)
- Lombok

---

## How it works

```
POST /orders
      ↓
 OrderProducer → Kafka topic "order-events"
                        ↙              ↘
          InventoryConsumer    NotificationConsumer
          (updates stock)      (saves notification to DB)
```

Products are cached in Redis — first call hits DB, subsequent calls return from cache.

---

## Pages

- `/` — home, product listing
- `/products` — browse products
- `/orders` — place and view orders
- `/notifications` — view order notifications
- `/admin/dashboard` — admin panel
- `/admin/products` — manage products
- `/admin/users` — manage users
- `/register` — user registration
- `/login` — login page

---

## API

```
GET  /products         → list all products (Redis cached)
POST /products         → add product (admin)
GET  /products/{id}    → get single product
POST /orders           → place order (triggers Kafka event)
GET  /orders           → list all orders
```

---

## Run locally

Need MySQL, Kafka and Redis running.

```bash
git clone https://github.com/official-vishalk-1999/market-hub
cd market-hub
```

Create DB:
```sql
CREATE DATABASE markethub;
```

Set env vars:
```
DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
KAFKA_BOOTSTRAP_SERVERS
REDIS_HOST, REDIS_PORT
```

```bash
./mvnw spring-boot:run
```

Open http://localhost:8080

Default admin login: `admin / admin`

---

## Notes

- Kafka runs two consumer groups independently — inventory and notification processing don't block each other
- Product list is cached in Redis — cache key is "products", clears on restart
- DataLoader creates default admin user on first run if not exists
