# UniMart Backend — Spring Boot Monolithic REST API

This directory contains the monolithic REST API backend for UniMart, built using Spring Boot, Java 21, and MySQL.

---

## 🛠️ Technology Stack

* **Language**: Java 21 LTS
* **Framework**: Spring Boot 3.x / 4.x (Web, Security, Data JPA, Actuator)
* **Build System**: Maven (with Maven Wrapper)
* **Database**: MySQL 8.x
* **Migrations**: Flyway Migration
* **Security & Tokens**: Spring Security configured with Nimbus JWT Decoder/Encoder for OAuth2 Resource Server Bearer tokens
* **Validation**: Hibernate Validator (Spring Boot Validation Starter)

---

## 📂 Package-by-Feature Folder Structure

Organized into cohesive business feature packages under `varna.mit.kln.unimart`:

```text
src/main/java/varna/mit/kln/unimart/
├── common/              --> Global exception filters, base responses, and utility mappings
├── config/              --> Security filter chain beans, Cors configurations
├── security/            --> JWT tokens services, authentication utilities
├── auth/                --> Login, registration (controller, dto, entity, repository, service)
├── listing/             --> Catalog management (controller, dto, entity, repository, service)
├── review/              --> Ratings & feedback (controller, dto, entity, repository, service)
├── order/               --> Purchases (controller, dto, entity, repository, service)
├── notification/        --> Alerts and messaging queues (controller, dto, entity, repository, service)
├── category/            --> Classifications (controller, dto, entity, repository, service)
├── payment/             --> Payment gateway integrations (controller, dto, entity, repository, service)
└── chat/                --> Chat messages (controller, dto, entity, repository, service)
```

---

## ⚙️ Local Development Settings

Credentials and database settings are loaded dynamically via classpath resources using Spring Boot's profile configurations. 

Create a file named `env.properties` inside the `src/main/resources/` folder to store your credentials:

```properties
# Database connection details
DB_URL=jdbc:mysql://localhost:3306/unimart?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=your-mysql-user
DB_PASSWORD=your-mysql-password

# App configurations
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=local
APP_ALLOWED_ORIGINS=http://localhost:5173

# JWT security configurations (random base64-encoded secret key of at least 32 bytes)
JWT_SECRET=your-base64-encoded-secret-key
JWT_ACCESS_MINUTES=15
```

*Note: `env.properties` is listed in `.gitignore` and must never be committed to source control. A template template can be found in `src/main/resources/env.properties.example`.*

---

## 🏃 Build and Run Commands

The backend utilizes the Maven Wrapper (`mvnw`) for standard compilation, test execution, and deployment:

```bash
# Clean classes and compile the project
./mvnw clean compile

# Run the test suite (compiles schema migrations using Flyway natively)
./mvnw clean test

# Run the Spring Boot application locally (by default, boots up on port 8080)
./mvnw spring-boot:run
```

---

## 🩺 System Verification

To check if the application has booted successfully:
* **Custom Health Check Endpoint**:
  ```text
  GET http://localhost:8080/api/v1/public/ping
  ```
* **Spring Actuator Health Indicators**:
  ```text
  GET http://localhost:8080/actuator/health
  ```
Both endpoints are publicly accessible without authentication headers.
