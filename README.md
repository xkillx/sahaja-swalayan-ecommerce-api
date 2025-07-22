# Sahaja Swalayan eCommerce API

## Project Overview

Sahaja Swalayan eCommerce API is a robust, production-ready backend for online retail applications, built with Java and Spring Boot. It provides a secure, scalable, and extensible platform for managing users, products, categories, carts, and orders, with modern best practices and professional API documentation.

---

## Features

- **User Registration & Email Confirmation**  
  Secure registration flow with email verification and confirmation tokens.

- **Product Catalog**  
  CRUD operations for products with detailed attributes and search functionality.

- **Category Management**  
  Category CRUD with dependency checks and search.

- **Cart & Order Management**  
  (Planned/Extendable) Shopping cart and order processing endpoints.

- **Comprehensive API Documentation**  
  Swagger/OpenAPI with detailed examples and standardized responses.

- **Role-Based Security**  
  JWT-ready, stateless security with configurable public/private endpoints.

- **Pluggable Email Service**  
  Provider-agnostic email sending (Mailtrap out-of-the-box).

- **Database Migrations**  
  Schema versioning and migrations via Flyway.

---

## Tech Stack

- **Java 17+**
- **Spring Boot 3.x**
- **PostgreSQL**
- **Flyway** (Database migrations)
- **Maven** (Build tool)
- **Spring Data JPA**
- **Spring Security**
- **Swagger/OpenAPI** (API docs)
- **JUnit & MockMvc** (Testing)
- **Mailtrap** (Email sandbox)

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/xkillx/sahaja-swalayan-ecommerce-api.git
cd sahaja-swalayan-ecommerce-api
```

### 2. Prerequisites

- **JDK:** Java 17 or higher  
- **Maven:** 3.8+  
- **PostgreSQL:** 13 or higher

### 3. Environment Setup

Configuration is managed via `application.yaml` and environment variables for security and flexibility.

#### Option 1: Using Environment Variables

Set the following variables (e.g., in your terminal or a `.env` file):

```env
DB_URL=jdbc:postgresql://localhost:5432/sahaja_swalayan_db
DB_USERNAME=postgres
DB_PASSWORD=root
EMAIL_PROVIDER=mailtrap
EMAIL_FROM_ADDRESS=noreply@sahajaswalayan.com
EMAIL_FROM_NAME=Sahaja Swalayan
MAILTRAP_API_TOKEN=your-mailtrap-token
MAILTRAP_SANDBOX_ENABLED=true
```

#### Option 2: Edit `src/main/resources/application.yaml`

Defaults are provided; override as needed.

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/sahaja_swalayan_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:root}
email:
  provider: ${EMAIL_PROVIDER:mailtrap}
  from:
    address: ${EMAIL_FROM_ADDRESS:noreply@sahajaswalayan.com}
    name: ${EMAIL_FROM_NAME:Sahaja Swalayan}
```

### 4. Database Setup & Migration

#### **Option A: Automatic Migration (Recommended)**

Flyway will auto-create and migrate the database on app startup.

1. Ensure PostgreSQL is running and the user has privileges.
2. The database (`sahaja_swalayan_db`) will be created if it does not exist.

#### **Option B: Manual Database Creation**

If you prefer manual setup:

```sql
CREATE DATABASE sahaja_swalayan_db;
-- User/role creation if needed
```

Flyway will handle tables and schema.

### 5. Run the Application

```bash
mvn spring-boot:run
```

The API will be available at:  
`http://localhost:8080/api/v1/`

---

## API Documentation

Interactive Swagger UI is available at:  
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

- Comprehensive endpoint documentation
- Standardized responses and examples
- Try out endpoints directly from the browser

---

## Project Structure

```
src/main/java/com/sahaja/swalayan/ecommerce/
├── application/
│   ├── controller/      # REST controllers (API endpoints)
│   ├── dto/             # Data Transfer Objects (requests/responses)
│   └── service/         # Application services (business logic)
├── domain/
│   ├── model/           # JPA entities (Product, Category, User, etc.)
│   ├── repository/      # Domain repository interfaces
│   └── service/         # Domain service interfaces
├── infrastructure/
│   ├── repository/      # JPA repository implementations
│   ├── config/          # Configuration (security, OpenAPI, etc.)
│   └── swagger/         # Custom Swagger/OpenAPI annotations
└── common/              # Shared exceptions, utilities, etc.
```

---

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a new branch (`feature/my-feature`)
3. Commit your changes with clear messages
4. Open a pull request describing your changes

---

## License

This project is licensed under the [MIT License](LICENSE).

---

If you have any questions or need help getting started, feel free to open an issue or contact the maintainers. Happy coding!
