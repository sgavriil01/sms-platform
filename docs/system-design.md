# System Design

## Overview

The SMS Platform is designed as a Java microservice responsible for accepting SMS requests, validating them, simulating delivery, storing message records, and exposing message retrieval/search APIs.

The first version is implemented as a single Quarkus service backed by PostgreSQL. The design keeps the core application modular so that delivery simulation, persistence, validation, and API handling remain separated and easy to extend.

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
        |-- MessageDeliverySimulator
        |-- MessageMapper
        |-- MessageRepository
        |
        v
PostgreSQL
```

## Main Responsibilities

### REST API Layer

The REST layer exposes endpoints for sending messages, listing stored messages, retrieving a message by ID, and searching messages by filters.

This layer is responsible for HTTP request/response handling only. Business rules are delegated to the service layer.

### Service Layer

The service layer contains the main application logic. It receives validated requests, creates message records, triggers delivery simulation, updates the final message status, and returns response DTOs.

Keeping this logic in the service layer makes the behavior easier to test and avoids placing business logic inside controllers.

### Persistence Layer

PostgreSQL is used as the source of truth for message records. Each message stores the sender number, destination number, content, status, failure reason when applicable, creation time, and processing time.

The repository layer isolates database access from the rest of the application.

### Delivery Simulation

Message delivery is handled through a dedicated simulator component.

This keeps delivery logic separate from the service and makes it possible to replace the simulator later with a real SMS gateway integration without changing the API contract.

## Message Processing Flow

```text
1. Client sends POST /api/messages
2. Request body is validated synchronously
3. Service creates a message with PENDING status
4. Delivery simulator determines the final result
5. Message is updated to DELIVERED or FAILED
6. Message is persisted in PostgreSQL
7. API returns the final message result to the caller
```

The current processing model is synchronous. The caller receives the final simulated delivery result in the response.

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

Validation is performed before message processing starts.

Main validation rules:

```text
sourceNumber       required, valid phone number format
destinationNumber  required, valid phone number format
content            required, maximum 160 characters
```

Invalid requests return structured `400 Bad Request` responses instead of being processed.

## Error Handling

The service uses centralized error handling to keep API responses consistent.

Typical error response structure:

```json
{
  "timestamp": "2026-05-12T15:30:00",
  "status": 400,
  "error": "Validation failed",
  "message": "Destination number must be a valid phone number",
  "path": "/api/messages"
}
```

This avoids leaking internal exceptions and gives API consumers clear feedback.

## Planned API Endpoints

```http
POST /api/messages
```

Send a new message.

```http
GET /api/messages
```

List stored messages.

```http
GET /api/messages/{id}
```

Retrieve a specific message.

```http
GET /api/messages/search?sourceNumber=&destinationNumber=&status=
```

Search messages using optional filters.

## Scalability Considerations

The service is stateless at the application layer. Message state is stored in PostgreSQL, which allows multiple instances of the service to run behind a load balancer if traffic increases.

For higher message volume, the processing flow can evolve into an asynchronous model:

```text
1. API stores the message as PENDING
2. A processing event is published to Kafka or RabbitMQ
3. One or more workers consume the event
4. Workers process delivery and update the message status
5. Clients retrieve the final status using the message ID
```

This would decouple API request handling from message processing and make the system easier to scale horizontally.

Database scalability would be supported through pagination, indexes on common search fields, connection pooling, and migration-based schema management.

## Maintainability Decisions

The codebase is structured by responsibility:

```text
controller   REST endpoints
dto          API request and response objects
model        database entities and enums
repository   database access
service      business logic
mapper       entity-to-DTO conversion
exception    centralized error handling
```

This structure keeps classes small, reduces coupling, and makes the system easier to test and extend.

## Future Extension Points

Possible future improvements include:

```text
asynchronous processing with Kafka or RabbitMQ
real SMS provider integration
pagination and sorting
Flyway database migrations
rate limiting
authentication and authorization
metrics and health checks
containerized deployment
```

## Design Summary

The system is designed as a modular SMS messaging microservice with clear separation between API handling, business logic, delivery processing, and persistence.

The initial synchronous design keeps the service simple and reliable, while the stateless structure and separated delivery component allow the system to evolve toward horizontal scaling and asynchronous processing when needed.