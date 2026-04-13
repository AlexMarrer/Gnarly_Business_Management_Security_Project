# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot business management web application being hardened for security as part of the M183 (Application Security) module at BBZ BL. The base application (product/order/user management) already exists; the ongoing work is implementing security features in 7 phases. Team: Alex Uscata & Furkan Güner.

## Build & Run Commands

```bash
./mvnw spring-boot:run          # Run the application (port 2330)
./mvnw clean package -DskipTests # Build JAR
./mvnw test                      # Run all tests
./mvnw test -Dtest=ClassName     # Run a single test class
./mvnw clean compile             # Compile only
```

The app runs at `http://localhost:2330`. Requires MySQL running locally with database `businessproject` (user: `root`, pass: `root` — development only).

## Architecture

**Stack:** Spring Boot 3.1.3 + Thymeleaf + Spring Data JPA + MySQL

**Layer structure:**
- `controllers/` — Web controllers mapping URLs to Thymeleaf templates (`HomeController`, `AdminController`, `UserController`, `ProductController`, `OrderController`)
- `services/` — Business logic (`UserServices`, `AdminServices`, `ProductServices`, `OrderServices`)
- `repositories/` — Spring Data JPA interfaces (auto-implemented)
- `entities/` — JPA entities: `User`, `Admin`, `Product`, `Orders`
- `loginCredentials/` — Login DTOs: `AdminLogin`, `UserLogin`
- `basiclogics/Logic.java` — Shared utilities
- `Exceptions.java` — Custom exception handling
- `templates/` — 16 Thymeleaf HTML templates
- `static/` — CSS, JS, images, video assets

**Database:** Hibernate `ddl-auto=update` — schema is auto-managed. No migrations needed during development.

## Security Implementation Roadmap

The phases directory contains detailed plans for each phase:

| Phase | Focus | Key files to create/modify |
|-------|-------|---------------------------|
| 0 | Setup (Java/Spring Boot upgrade, DB) | `pom.xml`, `application.properties` |
| 1 | Bean Validation | Entities with `@NotBlank`/`@Pattern`/`@Email`, controllers with `@Valid` |
| 2 | Spring Security + BCrypt+Pepper | Add `SecurityConfig.java`, `PepperPasswordEncoder.java`, migrate logins from GET→POST |
| 3 | Session Management | `SecurityConfig` session settings, cookie flags |
| 4 | Authorization / RBAC | `@PreAuthorize`, `sec:authorize` in templates |
| 5 | Injection & XSS prevention | Security headers, `th:text` enforcement |
| 6 | TryHackMe "Bank Rott" challenge | Documentation only |
| 7 | Documentation & submission | Evidence doc, DB script with sample data |

**Key dependencies to add (Phase 2):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

## Current Security State (Pre-Implementation)

- Passwords stored in **plaintext** — BCrypt+Pepper required in Phase 2
- Login endpoints use **GET** requests (credentials exposed in URL) — `AdminController` lines ~47, ~63
- **No Spring Security** — all endpoints publicly accessible
- No input validation annotations on entities yet
- JPA/Hibernate used throughout — SQL injection risk is low (parameterized queries)
- Thymeleaf `th:text` (default) escapes output — XSS risk is low, but `th:utext` must be avoided

## Key Reference Files

- `PLAN.md` — German-language implementation plan with phase breakdown
- `PROJECT.md` — Entity specs with validation rules (UUID IDs, password complexity rules)
- `SECURITY_REQUIREMENTS.md` — Grading criteria; Criterion 2 (Auth/Authorization) and Criterion 3 (Login) are double-weighted
- `TECHNICAL_REFERENCE.md` — Ready-to-use code patterns: `SecurityConfig`, `PepperPasswordEncoder`, entity validation annotations
- `phases/PHASE_*.md` — Step-by-step instructions for each implementation phase

## Code Style

- Keine Kommentare im Code, ausser wo die Logik nicht selbsterklärend ist
