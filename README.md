# Transaction API

A RESTful banking transaction API built with Java and Spring Boot to demonstrate practical use of JPA, Hibernate, PostgreSQL, and transaction management.

This project was created as a hands-on backend project to strengthen and demonstrate modern Java persistence concepts, including entity relationships, Hibernate dirty checking, database transactions, optimistic locking, validation, and automated testing.

## Tech Stack

- Java
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- Docker / Docker Compose
- Maven
- JUnit 5
- Mockito

## Features

### Customer Management
- Create customers
- Retrieve customer information
- Model customer-to-account relationships using JPA

### Account Management
- Create and retrieve accounts
- Support account types such as SAVINGS, CHECKING, and CREDIT
- Maintain account balances and account status
- Associate accounts with customers

### Transaction Processing
- Deposit funds
- Withdraw funds
- Transfer funds between accounts
- Validate account status and available balance
- Record completed transactions

## JPA & Hibernate Concepts

The project demonstrates several important persistence concepts.

### Entity Relationships

Customer and Account are modeled using JPA relationships.

```text
Customer
   |
   | 1:N
   v
Account
   |
   | 1:N
   v
Transaction
```

DTOs are separated from persistence entities to avoid exposing the database model directly through the API.

### Transaction Management

Financial operations are executed within Spring-managed transactions using `@Transactional`.

For example, a transfer updates both the source and destination accounts as one transactional operation. If the operation fails, the transaction can be rolled back rather than leaving partially updated data.

### Hibernate Dirty Checking

Account balances are updated through managed JPA entities.

Hibernate detects changes to entities within the persistence context and generates the required SQL updates when the transaction is flushed or committed, without requiring an explicit repository `save()` for every modification.

### Optimistic Locking

Accounts use JPA's `@Version` mechanism for optimistic locking.

```java
@Version
private Long version;
```

The version value allows Hibernate to detect stale updates and helps protect against lost updates when multiple transactions attempt to modify the same account.

## Validation & Error Handling

API requests use Jakarta Bean Validation for input validation.

Examples include:

- Required fields
- Positive transaction amounts
- Valid email addresses
- Enum validation

Application exceptions are handled centrally through a global exception handler to provide consistent HTTP error responses.

## Testing

The project contains both unit and integration tests.

### Unit Tests

JUnit 5 and Mockito are used to test service-layer business logic in isolation, including:

- Successful deposits and withdrawals
- Insufficient balance
- Invalid transaction types
- Invalid account status
- Account-not-found scenarios
- Transfers between accounts

### Integration Tests

Integration tests use the Spring application context and PostgreSQL to verify persistence behavior that cannot be tested meaningfully with mocks.

Examples include:

- Persisting balance changes through Hibernate dirty checking
- Transaction failure and rollback behavior
- Repository and database integration

## Database

PostgreSQL can be started locally using Docker Compose.

```bash
docker compose up -d
```

The application models the following core entities:

```text
Customer
    |
    +---- Account
             |
             +---- Transaction
```

## Running the Application

### Prerequisites

- Java
- Maven
- Docker
- Docker Compose

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Run the application

```bash
mvn spring-boot:run
```

### 3. Run tests

```bash
mvn clean test
```

## Project Structure

```text
src/
├── main/
│   └── java/
│       └── com/saya/transaction/api/
│           ├── controller/
│           ├── dto/
│           ├── entity/
│           ├── exception/
│           ├── repository/
│           └── service/
│
└── test/
    └── java/
        └── com/saya/transaction/api/
            ├── service/
            └── integration/
```

## Key Learning Areas

This project focuses on practical backend engineering concepts including:

- Spring Boot application architecture
- REST API design
- DTO and entity separation
- JPA entity relationships
- Spring Data repositories
- Hibernate persistence context
- Dirty checking
- Transaction boundaries and rollback
- Optimistic locking with `@Version`
- PostgreSQL integration
- Unit and integration testing

## Future Improvements

Potential enhancements include:

- OpenAPI / Swagger documentation
- Testcontainers-based isolated PostgreSQL integration tests
- Database migrations with Flyway
- Additional concurrency and optimistic-locking tests
- Pagination and transaction history
- Authentication and authorization

## Disclaimer

This project is intended for learning and portfolio demonstration purposes. It is not a production banking or payment system.