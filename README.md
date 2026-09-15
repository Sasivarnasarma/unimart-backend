# 🛒 UniMart Backend — Monolithic RESTful API Platform

![Java 21](https://img.shields.io/badge/Java-21_LTS-orange.svg?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-brightgreen.svg?style=for-the-badge&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg?style=for-the-badge&logo=mysql)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-red.svg?style=for-the-badge&logo=flyway)
![Tests](https://img.shields.io/badge/Tests-99_Passing-success.svg?style=for-the-badge&logo=junit5)
![License](https://img.shields.io/badge/License-MIT-purple.svg?style=for-the-badge)

Welcome to the **UniMart Monolithic Backend Service**, a high-performance, secure, feature-complete RESTful API engine built with **Java 21 LTS**, **Spring Boot 4.1.0**, **Spring Data JPA**, **Spring Security (OAuth2 JWT Resource Server)**, and **MySQL 8.0+**.

UniMart is an exclusive campus marketplace connecting university students to buy, sell, negotiate, order, pay, review, and receive real-time notifications for campus items and services.

---

## 🚀 Key Features

The backend platform powers essential marketplace capabilities for campus trading:

### 1. 🔐 Authentication & User Profiles
* **Student Verification & Access**: Secure account registration and authentication reserved for university students.
* **Profile Management**: Personal account profile customization and settings management.

### 2. 🛍️ Campus Marketplace & Item Listings
* **Category Exploration**: Organized item categories with active status filtering for easy discovery.
* **Smart Search & Filters**: Multi-criteria keyword search, category filters, and capped page navigation.
* **Rich Photo Galleries**: Multiple photo attachments per item listing with customized display ordering.
* **Item Archival & Availability**: Automated status tracking (`available`, `sold`, `inactive`) with soft archival.

### 3. 💬 Student Messaging & Negotiation
* **Direct Buyer-Seller Chat**: Dedicated conversation rooms between buyers and sellers for item inquiries and price negotiations.
* **Private & Secure Communication**: Enforced privacy restricting conversation access strictly to room participants.
* **Read Receipts & Alerts**: Real-time read status updates and message history tracking.

### 4. 📦 Purchase Orders & Safe Checkout
* **Order Lifecycle Tracking**: End-to-end purchase workflow management (`pending` $\rightarrow$ `paid` $\rightarrow$ `completed` / `cancelled`).
* **Automated Item Inventory**: Instant item reservation upon order placement, auto-marking as `sold` on completion, and auto-restoring to `available` on cancellation.
* **Idempotent Payment Engine**: Secure transaction processing protected against duplicate charging on network retries.

### 5. ⭐ Buyer Reviews & Seller Reputation
* **Verified Purchase Reviews**: Post-transaction ratings (1–5 stars) and comments restricted exclusively to verified item buyers.
* **Seller Trust Score**: Dynamic calculation of seller average star rating and total review count.
* **Feedback Control**: Author permissions allowing reviewers to update or delete their submitted feedback.

### 6. 🔔 Real-Time Notifications & Alerts
* **Activity Alerts Feed**: Instant notifications for order status updates, incoming chat messages, and seller reviews.
* **Unread Counter Badge**: Real-time unread alert count for instant UI badge updates.
* **Alert Management**: Flexible controls to mark individual alerts or bulk mark all notifications as read.

---

## 🏛️ Project Architecture & Package Structure

Organized strictly by **package-by-feature** architectural conventions under `varna.mit.kln.unimart`:

```text
src/main/java/varna/mit/kln/unimart/
├── auth/                --> User registration, JWT login, profile management (/me)
├── category/            --> Category catalog & soft deactivation
├── chat/                --> Buyer-seller conversation rooms & message history
├── common/              --> Base exceptions, global RestControllerAdvice, ApiErrorResponse
├── config/              --> Security filter chain beans, CORS policies
├── listing/             --> Product catalog, JPA Specifications search, gallery images
├── notification/        --> User alert feed, unread counter, read state management
├── order/               --> Purchase orders state machine & listing status sync
├── payment/             --> Payment processing engine & idempotency key validator
├── review/              --> Buyer review engine, star ratings, seller rating summary
└── security/            --> JwtTokenProvider, OAuth2 Resource Server beans
```

---

## 🛠️ Technology Stack

| Domain | Technology / Library | Description |
|---|---|---|
| **Core Platform** | Java 21 LTS | OpenJDK 21 LTS Runtime |
| **Framework** | Spring Boot 4.1.0 | Core application container & MVC engine |
| **Security** | Spring Security 6.x | OAuth2 Resource Server JWT authentication |
| **Persistence** | Spring Data JPA / Hibernate 7 | Entity-relational mapping & repository abstractions |
| **Database** | MySQL 8.0+ | Persistent relational storage |
| **Migrations** | Flyway | Executable SQL schema version control (V1–V4) |
| **Testing** | JUnit 5 & Mockito & MockMvc | Automated unit and integration test suite |
| **Build Tool** | Maven Wrapper (`mvnw`) | Standard build automation tool |

---

## 📋 Comprehensive REST API Reference

All REST API endpoints are prefixed with `/api/v1/`.

### 1. System & Authentication
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/health` | Public | Application & MySQL database status probe |
| `GET` | `/api/v1/ping` | Public | Lightweight heartbeat check |
| `POST` | `/api/v1/auth/register` | Public | Register a new user account |
| `POST` | `/api/v1/auth/login` | Public | Authenticate credentials & receive JWT token |
| `GET` | `/api/v1/auth/me` | Authenticated | Get current authenticated user profile |
| `PUT` | `/api/v1/auth/me` | Authenticated | Update user profile details |

### 2. Categories & Listings
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/categories` | Public | List active product categories |
| `POST` | `/api/v1/categories` | Admin | Create a new category |
| `GET` | `/api/v1/listings` | Public | Search listings with query, category, pagination |
| `POST` | `/api/v1/listings` | Authenticated | Create a new item listing |
| `GET` | `/api/v1/listings/{id}` | Public | Get listing details by ID |
| `POST` | `/api/v1/listings/{id}/images` | Seller | Add gallery image URL to listing |
| `DELETE` | `/api/v1/listings/{id}` | Seller | Soft-archive listing (`status = inactive`) |

### 3. Chat & Messaging
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/chat/conversations` | Authenticated | Start or fetch conversation room for a listing |
| `GET` | `/api/v1/chat/conversations` | Authenticated | List user active conversation rooms |
| `GET` | `/api/v1/chat/conversations/{id}/messages` | Participant | Get paginated message history |
| `POST` | `/api/v1/chat/conversations/{id}/messages` | Participant | Send a message to conversation room |
| `PATCH` | `/api/v1/chat/conversations/{id}/read` | Participant | Mark incoming messages as read |

### 4. Orders & Payments
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/orders` | Authenticated | Create purchase order for an available listing |
| `GET` | `/api/v1/orders` | Authenticated | Fetch purchase orders for buyer or seller |
| `GET` | `/api/v1/orders/{id}` | Participant | Get order details by ID |
| `PATCH` | `/api/v1/orders/{id}/status` | Participant | Transition status (`pending`, `paid`, `completed`, `cancelled`) |
| `POST` | `/api/v1/payments` | Buyer | Process payment with unique `idempotencyKey` |
| `GET` | `/api/v1/payments/order/{orderId}` | Participant | Retrieve payment receipt for an order |

### 5. Reviews & Ratings
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/reviews` | Buyer | Submit rating/comment for completed order |
| `GET` | `/api/v1/reviews/seller/{sellerId}` | Public | Get paginated reviews received by seller |
| `GET` | `/api/v1/reviews/seller/{sellerId}/summary` | Public | Get average star rating & total review count |
| `PUT` | `/api/v1/reviews/{id}` | Author | Update review rating or comment |
| `DELETE` | `/api/v1/reviews/{id}` | Author | Delete submitted review |

### 6. Notifications
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/notifications` | Authenticated | Get paginated notifications feed |
| `GET` | `/api/v1/notifications/unread-count` | Authenticated | Get unread notification counter badge |
| `PATCH` | `/api/v1/notifications/{id}/read` | Authenticated | Mark single notification as read |
| `PATCH` | `/api/v1/notifications/read-all` | Authenticated | Mark all user notifications as read |

---

## ⚙️ Setup & Environment Configuration

### 1. Database Configuration
Ensure MySQL 8.x or 9.x is running locally on port `3306` with database schema `unimart`.

### 2. Environment Properties File (`env.properties`)
Create `env.properties` in `src/main/resources/` (using template `src/main/resources/env.properties.example`):

```properties
# MySQL Connection
DB_URL=jdbc:mysql://localhost:3306/unimart?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_password

# Server Settings
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=local
APP_ALLOWED_ORIGINS=http://localhost:5173

# JWT Key Config (Minimum 32 bytes secret)
JWT_SECRET=your_base64_encoded_32byte_secret_key
JWT_ACCESS_MINUTES=60
```

---

## 🧪 Build, Test & Run Commands

Execute commands from the `unimart-backend` directory:

```bash
# Clean and compile project source files
./mvnw clean compile

# Run the complete 99-test automated suite (validates Flyway migrations)
./mvnw clean test

# Run Spring Boot application locally on http://localhost:8080
./mvnw spring-boot:run
```

---

## 🧪 Postman API Collection

A fully pre-configured Postman API collection is included in the project root:
📁 **[`unimart_api_collection.json`](https://github.com/Sasivarnasarma/unimart-backend/blob/main/unimart_api_collection.json)**

Import this file into Postman to instantly test all 30+ endpoints across System, Auth, Categories, Listings, Chat, Orders, Payments, Reviews, and Notifications!

---

## 📄 License

This repository is licensed under the [MIT License](LICENSE).
