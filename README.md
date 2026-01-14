# Adarsh Autho-Forge 🛡️

**Adarsh Autho-Forge** is a centralized, production-ready Authentication System built with Spring Boot 4.0 and Java 21, designed for microservices architectures.

It provides a complete ecosystem for secure authentication:
1.  **Auth Server (`autho-forge-service`)**: A standalone authentication service that issues and validates RS256-signed JWT tokens.
2.  **Client Library (`autho-forge-starter`)**: A plug-and-play Spring Boot specific starter for zero-config integration in your microservices.

---

## 🏗️ Architecture

-   **JWT & JWKS**: Uses asymmetric encryption (RS256). The server signs tokens with a private key, and clients validate them using public keys fetched from the server's JWK endpoint.
-   **Security**: Built on Spring Security 6+ and Nimbus JOSE-JWT.
-   **Database**: Users and Refresh Tokens are stored securely (BCrypt hashing).
-   **Scalability**: Stateless access tokens with secure, rotatable refresh tokens.

---

## 🚀 Getting Started

### 1. Run the Auth Server
The `autho-forge-service` is the heart of the system.
1.  Navigate to `autho-forge-service`.
2.  Run the application: `mvn spring-boot:run`.
3.  The service will be available on port **8080**.

**Key Endpoints:**
-   `POST /api/v1/auth/register`: Create a new user.
-   `POST /api/v1/auth/login`: Login and receive Access/Refresh tokens.
-   `GET /.well-known/jwks.json`: Public keys for token validation.

### 2. Protect Your Microservices
Use the `autho-forge-starter` to secure your existing Spring Boot apps (like OMS) with a single dependency.

**Add the dependency (via JitPack):**
```xml
<dependency>
    <groupId>com.github.YOUR_USERNAME.adarsh-autho-forge</groupId>
    <artifactId>autho-forge-starter</artifactId>
    <version>TAG</version>
</dependency>
```

**Configure `application.properties`:**
```properties
autho.forge.enabled=true
autho.forge.jwk-set-uri=http://localhost:8080/.well-known/jwks.json
autho.forge.issuer=https://adarsh-autho-forge
```

See the [Starter Documentation](autho-forge-starter/README.md) for full details.

---

## 📦 Project Modules

| Module | Description |
| :--- | :--- |
| **[`autho-forge-service`](autho-forge-service)** | The standalone Authentication Server application. |
| **[`autho-forge-starter`](autho-forge-starter)** | The client library for other microservices. |

## 🛠️ Tech Stack
-   **Java**: 21
-   **Spring Boot**: 4.0.0-RC2
-   **Security**: Spring Security, OAuth2 Resource Server concepts
-   **Tokens**: Nimbus JOSE-JWT (RS256)
