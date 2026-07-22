# HFF Data Studio

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 4.1.0" />
  <img src="https://img.shields.io/badge/PostgreSQL-Database-336791?logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Spring%20AI-DeepSeek-blue" alt="Spring AI DeepSeek" />
  <img src="https://img.shields.io/badge/License-Proprietary-red" alt="License" />
</p>

> **HFF Data Studio** is a comprehensive data management, exploration, cleaning, and analysis platform built for **Henri Fraise Fils & Cie**. It empowers users to upload raw datasets (CSV, Excel, SQL), profile them, apply cleaning rules, run predefined analytics, generate rich reports, and securely export the results — all from a single, well-governed backend.

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [Running the Application](#-running-the-application)
- [API Documentation](#-api-documentation)
- [Security](#-security)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)
- [Author](#-author)

---

## 🎯 Overview

HFF Data Studio is a production-ready data management platform that provides an end-to-end pipeline for handling business data with enterprise-grade security and compliance:

1. **Ingest** — Upload source files (CSV, Excel, SQL dumps) into isolated project workspaces with automatic validation.
2. **Explore** — Automatically profile datasets with statistical analysis, detect column types, and compute comprehensive quality scores.
3. **Clean** — Configure versioned, per-column cleaning rules (normalization, null handling, type conversion, deduplication) with full history tracking.
4. **Analyze** — Run predefined analyses powered by Python scripts and Spring AI (DeepSeek) with configurable parameters.
5. **Visualize & Export** — Generate interactive charts, professional PDF reports, and multi-format exports (CSV, Excel, JSON) with download tracking.
6. **Audit** — Every action is logged with user context, timestamps, and details for complete traceability and compliance.
7. **Secure** — JWT-based authentication with token blacklisting, role-based access control, and comprehensive permission system.

---

## ✨ Key Features

- 🔐 **Role-Based Access Control** — Users, categories, and fine-grained permissions with JWT authentication and token blacklisting.
- 📁 **Project Workspaces** — Organize source files, datasets, and analyses per project with archive/restore lifecycle and soft deletion.
- 🧪 **Automated Data Profiling** — Column type detection, null/unique statistics, distribution analysis, and comprehensive quality scoring.
- 🧹 **Configurable Cleaning Pipeline** — Ordered, versioned cleaning rules per column with full history, rollback capabilities, and preview.
- 🤖 **AI-Powered Analytics** — Predefined analyses running on Python with DeepSeek integration via Spring AI for intelligent insights.
- 📊 **Reports & Charts** — Generate professional PDF exploration reports and rich, exportable chart datasets.
- 📤 **Multi-Format Exports** — CSV, Excel, JSON export with download tracking and compression options.
- 📋 **Complete Audit Log** — Every mutation is recorded with user context, IP address, and timestamps for security and compliance reviews.
- ✅ **Rich Validation** — Custom Bean Validation constraints (email, password strength, file type, URL, phone, SQL identifier, etc.) with detailed error messages.
- 🌐 **CORS-Ready REST API** — Fully documented REST API ready for integration with SPA frontends (React, Vue, Angular).
- 🔄 **Token Management** — Automatic token refresh, blacklist management, and secure logout with token revocation.
- 📦 **File Storage** — Configurable local or cloud storage for uploaded files with automatic organization and cleanup.

---

## 🛠 Tech Stack

| Layer            | Technology                                                                    |
|------------------|-------------------------------------------------------------------------------|
| Language         | Java 21                                                                       |
| Framework        | Spring Boot 4.1.0                                                             |
| Persistence      | Spring Data JPA, Hibernate, PostgreSQL                                        |
| Security         | Spring Security, JWT, BCrypt                                                  |
| AI / ML          | Spring AI — DeepSeek starter                                                  |
| Validation       | Jakarta Validation (custom constraints)                                       |
| Web              | Spring Web MVC, Spring Data REST, Spring REST Client                          |
| Boilerplate      | Lombok                                                                        |
| Build            | Maven (with Maven Wrapper)                                                    |
| Code Style       | google-java-format                                                            |
| Testing          | Spring Boot Test, Spring Security Test, Data JPA Test                         |

---

## 🏗 Architecture

The application follows a classic **layered architecture** with strong separation of concerns:
```

┌────────────────────────────────────────────────────────────┐
│                        Controllers                         │  REST API (JSON)
├────────────────────────────────────────────────────────────┤
│                          Security                          │  JWT filter, RBAC
├────────────────────────────────────────────────────────────┤
│                          Services                          │  Business logic + audit
├────────────────────────────────────────────────────────────┤
│                    Mappers  ·  Validators                  │
├────────────────────────────────────────────────────────────┤
│                        Repositories                        │  Spring Data JPA
├────────────────────────────────────────────────────────────┤
│                          Entities                          │  Jakarta Persistence
├────────────────────────────────────────────────────────────┤
│                        PostgreSQL                          │
└────────────────────────────────────────────────────────────┘
```
---

## 📂 Project Structure
```

hff_data_studio/
├── src/main/java/com/henri_fraise/hff_data_studio
│   ├── config/           # Spring configuration
│   ├── controller/       # REST endpoints
│   ├── dto/              # Request/Response DTOs
│   ├── entity/           # JPA entities
│   ├── enums/            # Domain enums
│   ├── exception/        # Custom exceptions & handlers
│   ├── mapper/           # DTO ↔ Entity mappers
│   ├── repository/       # Spring Data JPA repositories (+ custom impls)
│   ├── security/         # Security config, JWT filter, handlers
│   ├── service/          # Business services (incl. auth)
│   ├── validation/       # Custom validation annotations & validators
│   └── HffDataStudioApplication.java
├── src/main/resources/   # Configuration, static assets, templates
├── src/test/             # Unit and integration tests
├── pom.xml
├── mvnw / mvnw.cmd       # Maven Wrapper
├── format.bat            # Formatting helper (google-java-format)
└── README.md
```
For deeper package documentation, see:

- [`REPOSITORY.md`](./REPOSITORY.md) — Repository layer reference
- [`SERVICE.md`](./SERVICE.md) — Service layer reference
- [`SECURITY.md`](./SECURITY.md) — Security & JWT reference
- [`OTHER.md`](./OTHER.md) — Validation reference

---

## 🚀 Getting Started

### Prerequisites

- **JDK 21+**
- **Maven 3.9+** (or use the bundled wrapper `./mvnw`)
- **PostgreSQL 14+**
- (Optional) **Docker** for running PostgreSQL locally
- A valid **DeepSeek API key** if you want to enable AI features

### Clone the repository

```bash
git clone https://github.com/<your-org>/hff_data_studio.git
cd hff_data_studio
```
```


---

## ⚙️ Configuration

Create an `application.yml` (or `application.properties`) file under `src/main/resources/` with your environment settings:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hff_data_studio
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000
  storage:
    upload-dir: ./uploads
```


> 💡 Prefer environment variables or an external secrets manager for credentials.

---

## ▶️ Running the Application

Using the Maven Wrapper:

```shell script
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```


Or build a runnable JAR:

```shell script
./mvnw clean package
java -jar target/hff_data_studio-0.0.1-SNAPSHOT.jar
```


The API will be available at:

```
http://localhost:8080/api/v1
```


---

## 📚 API Documentation

Once the app is running, interactive API documentation is available at:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

### Main Endpoint Groups

| Group        | Base Path                       | Description                       |
|--------------|---------------------------------|-----------------------------------|
| Auth         | `/api/v1/auth/**`               | Login, refresh, logout            |
| Users        | `/api/v1/admin/users/**`        | User management (admin)           |
| Categories   | `/api/v1/categories/**`         | User categories & permissions     |
| Projects     | `/api/v1/projects/**`           | Project CRUD & lifecycle          |
| Files        | `/api/v1/files/**`              | Source file upload & management   |
| Datasets     | `/api/v1/datasets/**`           | Dataset browsing & columns        |
| Exploration  | `/api/v1/exploration/**`        | Data profiling & reports          |
| Cleaning     | `/api/v1/cleaning/**`           | Cleaning rules & history          |
| Analyses     | `/api/v1/analyses/**`           | Predefined analysis catalog       |
| Executions   | `/api/v1/executions/**`         | Analysis execution & results      |
| Exports      | `/api/v1/exports/**`            | Export requests                   |
| Audit        | `/api/v1/audit/**`              | Audit log (admin)                 |

---

## 🔒 Security

- **Stateless JWT authentication** with `Authorization: Bearer <token>` header.
- **BCrypt** password hashing.
- **Role & permission** checks via `@PreAuthorize` and the custom `@HasPermission` annotation.
- **Token blacklist** service to invalidate revoked tokens.
- **CORS** pre-configured for known frontend origins.
- Comprehensive **audit logging** on every mutation.

See [`SECURITY.md`](./SECURITY.md) for the full security model.

---

## 🧪 Testing

Run the full test suite:

```shell script
./mvnw test
```


Run a specific test class:

```shell script
./mvnw test -Dtest=UserServiceTest
```


---

## 🎨 Code Formatting

The project uses **google-java-format**. To format the code:

```shell script
# Windows
format.bat

# or manually
java -jar google-java-format-1.23.0-all-deps.jar --replace $(find src -name "*.java")
```


---

## 🤝 Contributing

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/my-feature`.
3. Commit your changes following the [`.commity.yaml`](./.commity.yaml) conventions.
4. Push the branch and open a Pull Request.
5. Make sure all tests pass and code is formatted before submitting.

---

## 📄 License

This project is **proprietary** and belongs to **Henri Fraise Fils & Cie**. All rights reserved.
Unauthorized copying, distribution, or modification is strictly prohibited.

---

## 👤 Author

**Gaël RAMAHANDRISOA**

- 🌐 Website: [https://nassaigael.github.io](https://nassaigael.github.io)

---

<p align="center">
  Made with ❤️ by <a href="https://nassaigael.github.io"><strong>Gaël RAMAHANDRISOA</strong></a>
</p>
```
