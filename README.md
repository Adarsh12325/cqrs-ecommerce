# Implementing Event-Driven CQRS with Apache Kafka, Kafka Streams, and Materialized Views

This project implements a simple e-commerce system using **CQRS** and an **event-driven** architecture with **Apache Kafka**, **Kafka Streams**, and **PostgreSQL**. It is split into two Spring Boot microservices:

- **Command Service** – handles write operations (create products, create orders, update order status), persists to Postgres, and publishes events to Kafka.
- **Query Service** – consumes Kafka events with Kafka Streams, builds materialized views (state stores), and exposes read-only analytics APIs.

All services are containerized with Docker and orchestrated using `docker-compose`.[web:149][web:127]

---

## Architecture overview

High-level design:

- **Command Service**
  - REST API to create products and orders, and update order status.
  - Persists data in PostgreSQL (write model).
  - Publishes domain events to Kafka topics:
    - `product-events`
    - `order-events`
- **Query Service**
  - Kafka Streams application with `exactly_once_v2` processing guarantee.
  - Consumes `product-events` and `order-events`.
  - Joins product and order data and aggregates into materialized state stores:
    - `product-sales-store` – total sales per product.
    - `category-revenue-store` – total revenue per product category.
    - `hourly-sales-store` – hourly sales per product.
  - Exposes REST endpoints that query these state stores using Kafka Streams interactive queries.[web:144][web:148]

Data flow (CQRS):

- **Commands** (writes) go to the Command Service → Postgres + Kafka events.
- **Queries** (reads) go to the Query Service → Kafka Streams state stores (read model).[web:130][web:143]

---

## Prerequisites

- Docker and Docker Compose installed.
- Java 17 and Maven installed (only needed if you want to build/run services locally without Docker).

---

## Configuration

Configuration is driven by environment variables. A `.env.example` file is provided at the project root; copy it and fill in values:

```
cp .env.example .env
```
```
# Database
DB_HOST=db
DB_PORT=5432
POSTGRES_DB=analytics_db
POSTGRES_USER=user
POSTGRES_PASSWORD=password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
KAFKA_STREAMS_APP_ID=ecommerce-analytics

# Command Service
SERVER_PORT=8080
PRODUCT_TOPIC=product-events
ORDER_TOPIC=order-events

# Query Service
QUERY_SERVER_PORT=8081
```

## Running the stack with Docker
From the project root (cqrs-ecommerce), run:
```
docker-compose up --build
```

This will start:

- PostgreSQL database.

-  Kafka broker.

- Command Service (Spring Boot) on http://localhost:8080.

- Query Service (Spring Boot + Kafka Streams) on http://localhost:8081.[web:155][web:148]

You can stop everything with:
```
docker-compose down -v
```
## API usage examples
Below are example curl commands that match the flows you have tested end-to-end.

1. Create a product (Command Service)
```
curl -X POST "http://localhost:8080/api/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "category": "Electronics",
    "price": 500.0
  }'
```
* Response example:
```
json
{
  "id": 1,
  "name": "Laptop",
  "category": "Electronics",
  "price": 500.0
}
```
* This stores the product in Postgres and publishes a ProductCreated event to the product-events topic.[file:118][web:148]

2. Create an order (Command Service)
Use the product id returned above (e.g. 1):

```
curl -X POST "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 500.0
      }
    ]
  }'
```
* Response example:
```
json
{
  "id": 1,
  "customerId": 1,
  "status": "CREATED",
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 500.0
    }
  ]
}
```
* This stores the order in Postgres and publishes an OrderCreated event to the order-events topic.[file:118][web:148]

3. Update order status (Command Service)
```
curl -X PUT "http://localhost:8080/api/orders/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "SHIPPED"}'
This updates the order status in Postgres and emits an OrderStatusUpdated event to Kafka (if implemented in your command service).[file:118][web:127]
```

4. Product sales analytics (Query Service)
Query total sales for a given product id (e.g. 1):

```
curl "http://localhost:8081/api/analytics/products/1/sales"
```
* Response example:
```
json
{
  "productId": 1,
  "totalSales": 1000.0
}
```
* Value is derived from the product-sales-store Kafka Streams state store.[file:118][web:144]

5. Category revenue analytics (Query Service)
Query total revenue for a category (e.g. Electronics):

```
curl "http://localhost:8081/api/analytics/categories/Electronics/revenue"
```
* Response example:
```
json
{
  "category": "Electronics",
  "totalRevenue": 1000.0
}
```
* Value is derived from the category-revenue-store Kafka Streams state store.[file:118][web:144]

6. Hourly sales analytics (Query Service)
Query hourly sales between a start and end timestamp (ISO-8601). Example:

```
curl "http://localhost:8081/api/analytics/hourly-sales?start=2026-02-27T09:00:00Z&end=2026-02-27T12:00:00Z"
```
* Response example:
```
json
[
  {
    "windowStart": "2026-02-27T09:00:00Z",
    "windowEnd": "2026-02-27T10:00:00Z",
    "productId": 1,
    "totalSales": 1000.0
  }
]
```
* Values are derived from the hourly-sales-store windowed Kafka Streams state store.[file:118][web:144]

## Running tests
- There is a root-level Maven parent project and a tests module to orchestrate tests.

Run all tests for both services from the project root:

```
mvn test
```

Or explicitly via the tests module:

```
mvn -pl tests -am test
```

Individual services:

```
cd command-service
mvn test
```
```
cd ../query-service
mvn test
```

* Command Service: includes tests such as OrderControllerTest for REST endpoints.

* Query Service: includes tests such as StreamsConfigTest to verify that the Kafka Streams topology builds successfully.[file:118][web:122]

## Project structure
```
cqrs-ecommerce/
  command-service/      # Spring Boot write service (commands, Postgres, Kafka producer)
  query-service/        # Spring Boot read service (Kafka Streams, state stores, analytics API)
  tests/                # Maven module to aggregate and run tests
  docker-compose.yml    # Orchestrates Postgres, Kafka, and both services
  .env.example          # Example environment variables
  pom.xml               # Maven parent (multi-module) POM
  README.md             # This file
```