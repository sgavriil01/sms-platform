# System Design

## Overview

The SMS Platform is designed as a Java microservice responsible for accepting SMS requests, validating them, storing message records, publishing processing events, simulating delivery, and exposing message retrieval/search APIs.

The application is implemented as a Quarkus service backed by PostgreSQL and Kafka-compatible messaging through Redpanda. The design keeps API handling, validation, persistence, delivery simulation, and asynchronous processing separated so the system remains easy to test and extend.

## High-Level Architecture

```text
Client / API Consumer
        |
        v
REST API - MessageController
        |
        v
MessageService
        |
        |-- MessageRepository
        |-- MessageMapper
        |-- MessageProcessingProducer
        |
        v
PostgreSQL + Kafka/Redpanda
        |
        v
MessageProcessingConsumer
        |
        v
MessageDeliverySimulator
        |
        v
PostgreSQL status update
```

## Main Responsibilities

### REST API Layer

The REST layer exposes endpoints for sending messages, listing stored messages, retrieving a message by ID, and searching messages by filters.
This layer is responsible for HTTP request/response handling only. Business rules are delegated to the service layer.

### Service Layer

The service layer contains the main application logic. It receives validated requests, creates message records, stores them as `PENDING`, publishes processing events, and exposes methods for retrieving and processing messages.

Keeping this logic in the service layer makes the behavior easier to test and avoids placing business logic inside controllers.

### Persistence Layer

PostgreSQL is used as the source of truth for message records. Each message stores the sender number, destination number, content, status, failure reason when applicable, creation time, and processing time.

The repository layer isolates database access from the rest of the application.

### Messaging Layer

Kafka-compatible messaging is used to decouple request handling from message processing.

The API stores the message first, then publishes a lightweight processing event containing the message ID. A consumer receives the event, loads the message from PostgreSQL, simulates delivery, and updates the message status.

### Delivery Simulation

Message delivery is handled through a dedicated simulator component.

This keeps delivery logic separate from the service and makes it possible to replace the simulator later with a real SMS gateway integration without changing the API contract.

## Message Processing Flow

```text
1. Client sends POST /api/messages
2. Request body is validated synchronously
3. Service creates a message with PENDING status
4. Message is persisted in PostgreSQL
5. Service publishes a processing event to Kafka/Redpanda
6. API returns 202 Accepted with the PENDING message
7. Kafka consumer receives the processing event
8. Consumer loads the message from PostgreSQL
9. Delivery simulator determines the final result
10. Message is updated to DELIVERED or FAILED
11. Client retrieves the final status with GET /api/messages/{id}
```

The processing model is asynchronous. The caller is notified that the request was accepted through `202 Accepted`, then checks the final result using the message ID.

## Data Model

A message record contains:

```text
id
sourceNumber
destinationNumber
content
status
errorMessage
createdAt
processedAt
```

Supported statuses:

```text
PENDING
DELIVERED
FAILED
```

## Validation Strategy

Validation is performed before a message is stored or published for processing.

Main validation rules:

```text
sourceNumber       required, valid phone number format
destinationNumber  required, valid phone number format
content            required, maximum 160 characters
```

Invalid requests return structured `400 Bad Request` responses and are not published to Kafka.

## Error Handling

The service uses centralized error handling to keep API responses consistent.

Typical validation error response:

```json
{
  "timestamp": "2026-05-12T15:30:00",
  "status": 400,
  "error": "Validation failed",
  "message": "The request contains invalid fields",
  "details": [
    "Destination number must be a valid phone number"
  ]
}
```

This avoids leaking internal exceptions and gives API consumers clear feedback.

## API Endpoints

```http
POST /api/messages
```

Accept a new message for asynchronous processing.

```http
GET /api/messages/{id}
```

Retrieve a specific message and its current processing status.

```http
GET /api/messages
```

List stored messages.

```http
GET /api/messages/search?sourceNumber=&destinationNumber=&status=
```

Search messages using optional filters.

## Scalability Considerations

The service is stateless at the application layer. Message state is stored in PostgreSQL, which allows multiple API instances to run behind a load balancer.

Kafka/Redpanda decouples message submission from message processing. Under higher load, API instances can continue accepting requests while consumers process queued events independently.

Consumers can be scaled horizontally using a shared consumer group. With multiple topic partitions, Kafka can distribute processing work across multiple service instances.

Database scalability would be supported through pagination, indexes on common search fields, connection pooling, and migration-based schema management.

## Concurrency and Idempotency

Message processing is guarded by the message status.

If a message is no longer `PENDING`, the consumer does not process it again. This protects against duplicate event delivery or repeated processing attempts.

```text
PENDING -> DELIVERED
PENDING -> FAILED
DELIVERED/FAILED -> ignored by processor
```

## Maintainability Decisions

The codebase is structured by responsibility:

```text
controller   REST endpoints
dto          API request and response objects
model        database entities and enums
repository   database access
service      business logic, delivery simulation, Kafka producer/consumer
mapper       entity-to-DTO conversion
exception    centralized error handling
```

This structure keeps classes small, reduces coupling, and makes the system easier to test and extend.

## Future Extension Points

Possible future improvements include:

```text
real SMS provider integration
dead-letter topic for failed processing events
retry policy for temporary delivery failures
pagination and sorting
Flyway database migrations
rate limiting
authentication and authorization
metrics and health checks
containerized deployment to Kubernetes
```

## Design Summary

The system is designed as a modular SMS messaging microservice with clear separation between API handling, business logic, asynchronous processing, delivery simulation, and persistence.

The asynchronous Kafka-based flow keeps request handling fast while allowing message processing to scale independently.