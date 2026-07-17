# market-hub

An event-driven e-commerce backend. When an order is placed it is published to a Kafka topic and two consumers pick it up independently — one decrements stock, one creates a notification record. The product catalog is cached in Redis.

Runs locally — needs MySQL, Kafka, and Redis running.

## Stack
- Spring Boot 3, Java 17
- Apache Kafka (event-driven order processing)
- MySQL + Spring Data JPA
- Redis (product catalog caching)
- Spring Security
- Thymeleaf (frontend)

## How it works
Placing an order saves it and publishes one Kafka event to "order-events". Two consumers, in separate consumer groups, each read that event independently: InventoryConsumer decrements the product stock, NotificationConsumer creates a notification record. The order transaction stays decoupled from both effects.

The product list is served through a @Cacheable method backed by Redis, so repeat reads skip the database.

## API / Pages
- GET  /         home page: product list, order buttons, notifications
- GET  /products list products (Redis cached)
- POST /orders   place an order (publishes the Kafka event)

## Run locally
Needs MySQL, Kafka, and Redis running. The app reads these env vars (falls back to localhost defaults):

MYSQLHOST, MYSQLPORT, MYSQLDATABASE, MYSQLUSER, MYSQLPASSWORD, KAFKA_SERVERS, REDISHOST, REDISPORT

Then run mvn spring-boot:run and open http://localhost:8080

## Notes
- Two Kafka consumer groups run independently, so inventory and notification processing don't block each other.
- Product list is cached in Redis (cache key "products").
- Three sample products are seeded on startup.