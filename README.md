# SMS Platform Microservice

A Java/Quarkus microservice that simulates an SMS messaging platform.

The service exposes REST APIs for sending SMS messages, listing stored messages, retrieving messages by ID, and searching message history. Each message is validated, stored in PostgreSQL with `PENDING` status, published to Kafka-compatible messaging through Redpanda, processed asynchronously, and later updated to `DELIVERED` or `FAILED`.

## Tech Stack

- Java 21
- Quarkus
- Maven
- PostgreSQL
- Hibernate ORM with Panache
- Kafka-compatible messaging with Redpanda
- Jakarta Bean Validation
- Swagger/OpenAPI
- JUnit 5 / Mockito
- Docker Compose

## System Design

The high-level system design is documented here:

[System Design](docs/system-design.md)

## Features

- Send SMS messages through a REST API
- Validate source number, destination number, and message content
- Store messages in PostgreSQL
- Publish message-processing events to Kafka/Redpanda
- Process messages asynchronously
- Simulate final delivery result
- Retrieve a message by ID
- List all stored messages
- Search messages by source number, destination number, and status
- Structured validation and error responses
- Swagger/OpenAPI documentation
- Docker Compose setup for local execution
- Unit and controller tests

## Message Statuses

```text
PENDING
DELIVERED
FAILED
```

A new message is first stored as `PENDING`. The Kafka consumer then processes it and updates the status to `DELIVERED` or `FAILED`.

The delivery simulator uses a deterministic rule:

```text
Destination numbers ending with 0000 return FAILED.
All other valid destination numbers return DELIVERED.
```

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/messages` | Accept a new SMS message for asynchronous processing |
| GET | `/api/messages/{id}` | Retrieve one message by ID |
| GET | `/api/messages` | List all stored messages |
| GET | `/api/messages/search` | Search messages using optional filters |

Search filters:

```text
sourceNumber
destinationNumber
status
```

Example:

```http
GET /api/messages/search?status=DELIVERED
```

## API Example

### Send Message

```http
POST /api/messages
Content-Type: application/json
```

```json
{
  "sourceNumber": "+35799123456",
  "destinationNumber": "+35799876543",
  "content": "Hello from SMS Platform"
}
```

Successful requests return `202 Accepted` with the persisted message in `PENDING` status:

```json
{
  "id": 1,
  "sourceNumber": "+35799123456",
  "destinationNumber": "+35799876543",
  "content": "Hello from SMS Platform",
  "status": "PENDING",
  "errorMessage": null,
  "createdAt": "2026-05-12T18:15:30.123",
  "processedAt": null
}
```

The caller can check completion by retrieving the message:

```http
GET /api/messages/1
```

After asynchronous processing, the message status becomes `DELIVERED` or `FAILED`:

```json
{
  "id": 1,
  "sourceNumber": "+35799123456",
  "destinationNumber": "+35799876543",
  "content": "Hello from SMS Platform",
  "status": "DELIVERED",
  "errorMessage": null,
  "createdAt": "2026-05-12T18:15:30.123",
  "processedAt": "2026-05-12T18:15:30.456"
}
```

## Validation

| Field | Rules |
|---|---|
| `sourceNumber` | Required, valid phone number format |
| `destinationNumber` | Required, valid phone number format |
| `content` | Required, max 160 characters |

Invalid requests return `400 Bad Request` with a structured error response.

## Run Locally with Maven

Start PostgreSQL and Redpanda:

```bash
docker compose up -d postgres redpanda
```

Run the application:

```bash
./mvnw quarkus:dev
```

The API will be available at:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/q/swagger-ui
```

## Run with Docker Compose

Package the application:

```bash
./mvnw package -DskipTests
```

Start the full stack:

```bash
docker compose up --build
```

This starts:

- `sms-platform-api`
- `sms-platform-postgres`
- `sms-platform-redpanda`

Swagger UI:

```text
http://localhost:8080/q/swagger-ui
```

## Local Service Configuration

PostgreSQL host connection:

```text
localhost:5433
```

PostgreSQL container-to-container connection:

```text
postgres:5432
```

Redpanda/Kafka host connection:

```text
localhost:19092
```

Redpanda/Kafka container-to-container connection:

```text
redpanda:9092
```

Default development credentials:

```text
Database: sms_platform
Username: sms_user
Password: sms_password
```

## Run Tests

Make sure PostgreSQL and Redpanda are running:

```bash
docker compose up -d postgres redpanda
```

Then run:

```bash
./mvnw test
```

## Project Structure

```text
src/main/java/com/smsplatform
├── controller
├── dto
├── exception
├── mapper
├── model
├── repository
└── service
```

## Main Design Choices

- REST API for simple client integration
- `202 Accepted` for asynchronous message submission
- PostgreSQL as the source of truth for message state
- Kafka/Redpanda for decoupled asynchronous processing
- DTOs separate the API contract from database entities
- Service layer contains business logic
- Repository layer handles persistence
- Mapper centralizes entity-to-response conversion
- Delivery simulation is isolated so it can be replaced by a real provider later
- Docker Compose provides reproducible local execution

## Build

```bash
./mvnw clean package
```

## Stop Services

```bash
docker compose down
```

To remove local database data as well:

```bash
docker compose down -v
```